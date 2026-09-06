package com.oldstats.tracking

import com.oldstats.api.OldStatsApiClient
import com.oldstats.api.StatEvent
import net.runelite.api.Client
import net.runelite.api.VarPlayer
import net.runelite.api.gameval.VarbitID
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

/**
 * [SlayerTracker] resolves task names via [Client.getDBRowsByValue] /
 * [Client.getDBTableField] (DB table 113, field 10), the same lookup path
 * RuneLite's own Slayer plugin uses instead of chat parsing. Wilderness
 * tasks cross-reference DB table 116 via [VarbitID.SLAYER_TARGET_BOSSID].
 */
class SlayerTrackerTest {
    private lateinit var apiClient: OldStatsApiClient
    private lateinit var client: Client
    private lateinit var tracker: SlayerTracker

    private val KRAKENS = 1234
    private val KRAKENS_ROW = 500
    private val SPECTRES = 5678
    private val SPECTRES_ROW = 600

    private val WILDY_BOSS_ID = 42
    private val WILDY_CROSSREF_ROW = 700
    private val WILDY_TASK_ROW = 800

    @Before
    fun setUp() {
        apiClient = mock()
        client = mock()
        tracker = SlayerTracker(apiClient, client)

        whenever(client.getDBRowsByValue(113, 0, 0, KRAKENS)).thenReturn(listOf(KRAKENS_ROW))
        whenever(client.getDBTableField(KRAKENS_ROW, 10, 0)).thenReturn(arrayOf<Any>("Cave krakens"))

        whenever(client.getDBRowsByValue(113, 0, 0, SPECTRES)).thenReturn(listOf(SPECTRES_ROW))
        whenever(client.getDBTableField(SPECTRES_ROW, 10, 0)).thenReturn(arrayOf<Any>("Aberrant spectres"))

        whenever(client.getDBRowsByValue(116, 1, 0, WILDY_BOSS_ID)).thenReturn(listOf(WILDY_CROSSREF_ROW))
        whenever(client.getDBTableField(WILDY_CROSSREF_ROW, 4, 0)).thenReturn(arrayOf<Any>(WILDY_TASK_ROW))
        whenever(client.getDBTableField(WILDY_TASK_ROW, 10, 0)).thenReturn(arrayOf<Any>("Revenants"))
    }

    private fun setTask(creature: Int, amount: Int, points: Int = 0, bossId: Int? = null) {
        whenever(client.getVarpValue(VarPlayer.SLAYER_TASK_CREATURE)).thenReturn(creature)
        whenever(client.getVarpValue(VarPlayer.SLAYER_TASK_SIZE)).thenReturn(amount)
        whenever(client.getVarbitValue(VarbitID.SLAYER_POINTS)).thenReturn(points)
        if (bossId != null) {
            whenever(client.getVarbitValue(VarbitID.SLAYER_TARGET_BOSSID)).thenReturn(bossId)
        }
    }

    @Test
    fun `first check establishes a baseline without emitting`() {
        setTask(KRAKENS, 130)
        tracker.checkTask()
        verify(apiClient, never()).enqueue(any())
    }

    @Test
    fun `emits a completion with the resolved task name and points earned when the remaining count hits zero`() {
        setTask(KRAKENS, 130, points = 100)
        tracker.checkTask() // baseline

        setTask(KRAKENS, 0, points = 112)
        tracker.checkTask()

        val captor = argumentCaptor<StatEvent>()
        verify(apiClient).enqueue(captor.capture())
        val event = captor.firstValue as StatEvent.SlayerTask
        assertEquals("Cave krakens", event.taskName)
        assertEquals(130, event.amountAssigned)
        assertEquals(12, event.points)
    }

    @Test
    fun `does not re-emit on subsequent ticks while the finished task is still assigned`() {
        setTask(KRAKENS, 130, points = 100)
        tracker.checkTask() // baseline

        setTask(KRAKENS, 0, points = 112)
        tracker.checkTask() // emits once

        tracker.checkTask() // still creature=KRAKENS, amount=0 -- must not double count
        tracker.checkTask()

        verify(apiClient, times(1)).enqueue(any())
    }

    @Test
    fun `does not double-emit when the next task is assigned after the count already hit zero`() {
        setTask(KRAKENS, 130, points = 100)
        tracker.checkTask() // baseline

        setTask(KRAKENS, 0, points = 112)
        tracker.checkTask() // emits once for krakens

        setTask(SPECTRES, 90, points = 112) // new task assigned
        tracker.checkTask() // must NOT re-emit the already-reported krakens completion

        verify(apiClient, times(1)).enqueue(any())
    }

    @Test
    fun `cancelling or skipping a task before its count reaches zero does not emit a completion`() {
        setTask(KRAKENS, 130, points = 100)
        tracker.checkTask() // baseline

        setTask(SPECTRES, 90, points = 100) // switched away with 130 kills still remaining
        tracker.checkTask()

        verify(apiClient, never()).enqueue(any())
    }

    @Test
    fun `emits via the task-changed fallback when the count was already zero at baseline`() {
        // Plugin starts up (or resets) right as the previous task was already finished,
        // so the >0 -to- 0 transition was never observed directly.
        setTask(KRAKENS, 0, points = 100)
        tracker.checkTask() // baseline, no emission even though remaining is already 0

        setTask(SPECTRES, 90, points = 112)
        tracker.checkTask()

        val captor = argumentCaptor<StatEvent>()
        verify(apiClient).enqueue(captor.capture())
        val event = captor.firstValue as StatEvent.SlayerTask
        assertEquals("Cave krakens", event.taskName)
    }

    @Test
    fun `resolves a wilderness task via the boss id cross-reference instead of the sentinel creature id`() {
        setTask(98, 40, points = 100, bossId = WILDY_BOSS_ID)
        tracker.checkTask() // baseline

        setTask(98, 0, points = 130, bossId = WILDY_BOSS_ID)
        tracker.checkTask()

        val captor = argumentCaptor<StatEvent>()
        verify(apiClient).enqueue(captor.capture())
        assertEquals("Revenants", (captor.firstValue as StatEvent.SlayerTask).taskName)
    }

    @Test
    fun `a wilderness boss id change while still under the sentinel counts as a task change`() {
        setTask(98, 5, points = 100, bossId = WILDY_BOSS_ID)
        tracker.checkTask() // baseline

        val otherBossId = 99
        whenever(client.getDBRowsByValue(116, 1, 0, otherBossId)).thenReturn(listOf(701))
        whenever(client.getDBTableField(701, 4, 0)).thenReturn(arrayOf<Any>(801))
        whenever(client.getDBTableField(801, 10, 0)).thenReturn(arrayOf<Any>("Vet'ion"))

        setTask(98, 3, points = 100, bossId = otherBossId) // switched wildy tasks with kills remaining
        tracker.checkTask()

        // Task changed with a nonzero remaining count on the old task: no completion.
        verify(apiClient, never()).enqueue(any())
    }

    @Test
    fun `no task assigned does not crash or emit`() {
        setTask(0, 0)
        tracker.checkTask() // baseline
        setTask(0, 0)
        tracker.checkTask()
        verify(apiClient, never()).enqueue(any())
    }

    @Test
    fun `reset re-establishes a fresh baseline`() {
        setTask(KRAKENS, 130, points = 100)
        tracker.checkTask()
        setTask(KRAKENS, 0, points = 112)
        tracker.checkTask() // emits once

        tracker.reset()

        setTask(KRAKENS, 0, points = 112)
        tracker.checkTask() // re-baselines at the already-finished state, no emission

        verify(apiClient, times(1)).enqueue(any())
    }
}
