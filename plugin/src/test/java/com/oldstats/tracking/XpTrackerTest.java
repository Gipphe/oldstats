package com.oldstats.tracking;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import com.oldstats.api.OldStatsApiClient;
import com.oldstats.api.StatEvent;
import net.runelite.api.Skill;
import net.runelite.api.events.StatChanged;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

public class XpTrackerTest {
    private OldStatsApiClient apiClient;
    private XpTracker tracker;

    @Before
    public void setUp() {
        apiClient = mock(OldStatsApiClient.class);
        tracker = new XpTracker(apiClient);
    }

    @Test
    public void firstSightingOfASkillEstablishesABaselineWithoutEmitting() {
        tracker.onStatChanged(new StatChanged(Skill.SLAYER, 1000, 50, 50));
        verifyNoMoreInteractions(apiClient);
    }

    @Test
    public void xpIncreaseAfterBaselineEmitsAnXpGainWithTheCorrectDelta() {
        tracker.onStatChanged(new StatChanged(Skill.SLAYER, 1000, 50, 50));
        tracker.onStatChanged(new StatChanged(Skill.SLAYER, 1500, 50, 50));

        ArgumentCaptor<StatEvent> captor = ArgumentCaptor.forClass(StatEvent.class);
        verify(apiClient).enqueue(captor.capture());
        StatEvent.XpGain event = (StatEvent.XpGain) captor.getValue();
        assertEquals("SLAYER", event.skill);
        assertEquals(1500L, event.xp);
        assertEquals(500L, event.xpGained);
        assertEquals(50, event.level);
    }

    @Test
    public void levelIncreaseEmitsALevelUpInAdditionToTheXpGain() {
        tracker.onStatChanged(new StatChanged(Skill.SLAYER, 1000, 50, 50));
        tracker.onStatChanged(new StatChanged(Skill.SLAYER, 1500, 51, 51));

        ArgumentCaptor<StatEvent> captor = ArgumentCaptor.forClass(StatEvent.class);
        verify(apiClient, times(2)).enqueue(captor.capture());
        assertTrue(captor.getAllValues().stream().anyMatch(e -> e instanceof StatEvent.XpGain));
        StatEvent.LevelUp levelUp = (StatEvent.LevelUp) captor.getAllValues().stream()
            .filter(e -> e instanceof StatEvent.LevelUp)
            .findFirst()
            .orElseThrow();
        assertEquals("SLAYER", levelUp.skill);
        assertEquals(51, levelUp.level);
    }

    @Test
    public void noChangeInXpOrLevelEmitsNothing() {
        tracker.onStatChanged(new StatChanged(Skill.SLAYER, 1000, 50, 50));
        tracker.onStatChanged(new StatChanged(Skill.SLAYER, 1000, 50, 50));
        verify(apiClient, never()).enqueue(any());
    }

    @Test
    public void resetClearsBaselinesSoTheNextSightingIsTreatedAsAFreshLogin() {
        tracker.onStatChanged(new StatChanged(Skill.SLAYER, 1000, 50, 50));
        tracker.reset();
        tracker.onStatChanged(new StatChanged(Skill.SLAYER, 1500, 50, 50));
        // Post-reset, this is a first sighting again — no emission expected.
        verifyNoMoreInteractions(apiClient);
    }

    @Test
    public void independentSkillsTrackSeparateBaselines() {
        tracker.onStatChanged(new StatChanged(Skill.SLAYER, 1000, 50, 50));
        tracker.onStatChanged(new StatChanged(Skill.MAGIC, 2000, 60, 60));
        tracker.onStatChanged(new StatChanged(Skill.SLAYER, 1100, 50, 50));

        ArgumentCaptor<StatEvent> captor = ArgumentCaptor.forClass(StatEvent.class);
        verify(apiClient).enqueue(captor.capture());
        StatEvent.XpGain event = (StatEvent.XpGain) captor.getValue();
        assertEquals("SLAYER", event.skill);
        assertEquals(100L, event.xpGained);
    }
}
