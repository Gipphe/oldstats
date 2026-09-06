package com.oldstats.tracking

import com.oldstats.api.OldStatsApiClient
import com.oldstats.api.StatEvent
import net.runelite.api.Client
import net.runelite.api.events.WidgetLoaded
import net.runelite.api.gameval.InterfaceID
import net.runelite.api.widgets.Widget
import net.runelite.client.callback.ClientThread
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.whenever

/**
 * [DiaryTaskTracker] reads the diary journal's "journal scroll" interface
 * (group [InterfaceID.JOURNALSCROLL]) rather than a varbit, so these tests
 * build fake title/text-layer widgets the same shape RuneLite's own
 * decompiled `DiaryRequirementsPlugin` reads (confirmed via `cfr`).
 */
class DiaryTaskTrackerTest {
    private lateinit var apiClient: OldStatsApiClient
    private lateinit var client: Client
    private lateinit var clientThread: ClientThread
    private lateinit var tracker: DiaryTaskTracker

    @Before
    fun setUp() {
        apiClient = mock()
        client = mock()
        clientThread = mock()
        tracker = DiaryTaskTracker(apiClient, client, clientThread)

        // Run the deferred client-thread callback synchronously so tests can assert immediately.
        doAnswer { invocation -> (invocation.getArgument(0) as Runnable).run() }
            .whenever(clientThread)
            .invokeLater(any<Runnable>())
    }

    private fun widget(text: String): Widget {
        val w: Widget = mock()
        whenever(w.text).thenReturn(text)
        return w
    }

    private fun setPage(heading: String, vararg lines: String) {
        val titleWidget = widget("Achievement Diary")
        whenever(client.getWidget(InterfaceID.Journalscroll.TITLE)).thenReturn(titleWidget)

        val lineWidgets = (arrayOf(heading) + lines).map { widget(it) }.toTypedArray()
        val textLayer: Widget = mock()
        whenever(textLayer.staticChildren).thenReturn(lineWidgets)
        whenever(client.getWidget(InterfaceID.Journalscroll.TEXTLAYER)).thenReturn(textLayer)
    }

    private fun loadEvent(groupId: Int = InterfaceID.JOURNALSCROLL): WidgetLoaded {
        val event = WidgetLoaded()
        event.groupId = groupId
        return event
    }

    @Test
    fun `emits one event per task line with the correct tier and completion state`() {
        setPage(
            "Ardougne Area Tasks",
            "Easy",
            "Enter the Wilderness",
            "<str>Talk to the Ardougne baker</str>",
            "Medium",
            "Steal from the Ardougne market stalls",
        )

        tracker.onWidgetLoaded(loadEvent())

        val captor = argumentCaptor<StatEvent>()
        org.mockito.kotlin.verify(apiClient, org.mockito.kotlin.times(3)).enqueue(captor.capture())
        val events = captor.allValues.map { it as StatEvent.DiaryTaskProgress }

        assertEquals("ARDOUGNE", events[0].diaryArea)
        assertEquals("EASY", events[0].tier)
        assertEquals("Enter the Wilderness", events[0].taskName)
        assertEquals(false, events[0].completed)

        assertEquals("EASY", events[1].tier)
        assertEquals("Talk to the Ardougne baker", events[1].taskName)
        assertEquals(true, events[1].completed)

        assertEquals("MEDIUM", events[2].tier)
        assertEquals("Steal from the Ardougne market stalls", events[2].taskName)
        assertEquals(false, events[2].completed)
    }

    @Test
    fun `ignores widget loads from interfaces other than the journal scroll`() {
        setPage("Ardougne Area Tasks", "Easy", "Enter the Wilderness")
        tracker.onWidgetLoaded(loadEvent(groupId = 999))
        org.mockito.kotlin.verify(apiClient, never()).enqueue(any())
    }

    @Test
    fun `ignores journal scroll pages that are not an achievement diary (e g the quest journal)`() {
        val titleWidget = widget("Quest Journal")
        whenever(client.getWidget(InterfaceID.Journalscroll.TITLE)).thenReturn(titleWidget)

        val lineWidgets = arrayOf(widget("Dragon Slayer II"), widget("Some quest step"))
        val textLayer: Widget = mock()
        whenever(textLayer.staticChildren).thenReturn(lineWidgets)
        whenever(client.getWidget(InterfaceID.Journalscroll.TEXTLAYER)).thenReturn(textLayer)

        tracker.onWidgetLoaded(loadEvent())

        org.mockito.kotlin.verify(apiClient, never()).enqueue(any())
    }

    @Test
    fun `resolves compound area headings to the same area token the tier-complete tracker uses`() {
        setPage("Kourend & Kebos Tasks", "Elite", "Do a hard Kourend thing")
        tracker.onWidgetLoaded(loadEvent())

        val captor = argumentCaptor<StatEvent>()
        org.mockito.kotlin.verify(apiClient).enqueue(captor.capture())
        assertEquals("KOUREND", (captor.firstValue as StatEvent.DiaryTaskProgress).diaryArea)
    }

    @Test
    fun `an unrecognized area heading emits nothing rather than guessing`() {
        setPage("Some Unknown Page Heading", "Easy", "A task")
        tracker.onWidgetLoaded(loadEvent())
        org.mockito.kotlin.verify(apiClient, never()).enqueue(any())
    }

    @Test
    fun `task lines before the first tier header are skipped, not misattributed`() {
        setPage("Varrock Tasks", "A stray line with no tier context yet", "Easy", "A real task")

        tracker.onWidgetLoaded(loadEvent())

        val captor = argumentCaptor<StatEvent>()
        org.mockito.kotlin.verify(apiClient).enqueue(captor.capture())
        assertEquals("A real task", (captor.firstValue as StatEvent.DiaryTaskProgress).taskName)
    }

    @Test
    fun `blank lines are skipped without breaking tier tracking`() {
        setPage("Varrock Tasks", "Easy", "", "First task", "", "Second task")

        tracker.onWidgetLoaded(loadEvent())

        val captor = argumentCaptor<StatEvent>()
        org.mockito.kotlin.verify(apiClient, org.mockito.kotlin.times(2)).enqueue(captor.capture())
        val events = captor.allValues.map { it as StatEvent.DiaryTaskProgress }
        assertEquals("EASY", events[0].tier)
        assertEquals("EASY", events[1].tier)
    }
}
