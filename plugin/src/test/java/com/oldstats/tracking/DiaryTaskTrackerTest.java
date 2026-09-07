package com.oldstats.tracking;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.oldstats.api.OldStatsApiClient;
import com.oldstats.api.StatEvent;
import java.util.List;
import java.util.stream.Collectors;
import net.runelite.api.Client;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.callback.ClientThread;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

/**
 * DiaryTaskTracker reads the diary journal's "journal scroll" interface
 * (group InterfaceID.JOURNALSCROLL) rather than a varbit, so these tests
 * build fake title/text-layer widgets the same shape RuneLite's own
 * decompiled DiaryRequirementsPlugin reads (confirmed via cfr).
 */
public class DiaryTaskTrackerTest {
    private OldStatsApiClient apiClient;
    private Client client;
    private ClientThread clientThread;
    private DiaryTaskTracker tracker;

    @Before
    public void setUp() {
        apiClient = mock(OldStatsApiClient.class);
        client = mock(Client.class);
        clientThread = mock(ClientThread.class);
        tracker = new DiaryTaskTracker(apiClient, client, clientThread);

        // Run the deferred client-thread callback synchronously so tests can assert immediately.
        doAnswer(invocation -> {
            invocation.<Runnable>getArgument(0).run();
            return null;
        }).when(clientThread).invokeLater(any(Runnable.class));
    }

    private Widget widget(String text) {
        Widget w = mock(Widget.class);
        when(w.getText()).thenReturn(text);
        return w;
    }

    private void setPage(String heading, String... lines) {
        Widget titleWidget = widget("Achievement Diary");
        when(client.getWidget(InterfaceID.Journalscroll.TITLE)).thenReturn(titleWidget);

        Widget[] lineWidgets = new Widget[lines.length + 1];
        lineWidgets[0] = widget(heading);
        for (int i = 0; i < lines.length; i++) {
            lineWidgets[i + 1] = widget(lines[i]);
        }
        Widget textLayer = mock(Widget.class);
        when(textLayer.getStaticChildren()).thenReturn(lineWidgets);
        when(client.getWidget(InterfaceID.Journalscroll.TEXTLAYER)).thenReturn(textLayer);
    }

    private WidgetLoaded loadEvent() {
        return loadEvent(InterfaceID.JOURNALSCROLL);
    }

    private WidgetLoaded loadEvent(int groupId) {
        WidgetLoaded event = new WidgetLoaded();
        event.setGroupId(groupId);
        return event;
    }

    @Test
    public void emitsOneEventPerTaskLineWithTheCorrectTierAndCompletionState() {
        setPage(
            "Ardougne Area Tasks",
            "Easy",
            "Enter the Wilderness",
            "<str>Talk to the Ardougne baker</str>",
            "Medium",
            "Steal from the Ardougne market stalls"
        );

        tracker.onWidgetLoaded(loadEvent());

        ArgumentCaptor<StatEvent> captor = ArgumentCaptor.forClass(StatEvent.class);
        verify(apiClient, times(3)).enqueue(captor.capture());
        List<StatEvent.DiaryTaskProgress> events = captor.getAllValues().stream()
            .map(e -> (StatEvent.DiaryTaskProgress) e)
            .collect(Collectors.toList());

        assertEquals("ARDOUGNE", events.get(0).diaryArea);
        assertEquals("EASY", events.get(0).tier);
        assertEquals("Enter the Wilderness", events.get(0).taskName);
        assertEquals(false, events.get(0).completed);

        assertEquals("EASY", events.get(1).tier);
        assertEquals("Talk to the Ardougne baker", events.get(1).taskName);
        assertEquals(true, events.get(1).completed);

        assertEquals("MEDIUM", events.get(2).tier);
        assertEquals("Steal from the Ardougne market stalls", events.get(2).taskName);
        assertEquals(false, events.get(2).completed);
    }

    @Test
    public void ignoresWidgetLoadsFromInterfacesOtherThanTheJournalScroll() {
        setPage("Ardougne Area Tasks", "Easy", "Enter the Wilderness");
        tracker.onWidgetLoaded(loadEvent(999));
        verify(apiClient, never()).enqueue(any());
    }

    @Test
    public void ignoresJournalScrollPagesThatAreNotAnAchievementDiaryEgTheQuestJournal() {
        Widget titleWidget = widget("Quest Journal");
        when(client.getWidget(InterfaceID.Journalscroll.TITLE)).thenReturn(titleWidget);

        Widget[] lineWidgets = new Widget[] { widget("Dragon Slayer II"), widget("Some quest step") };
        Widget textLayer = mock(Widget.class);
        when(textLayer.getStaticChildren()).thenReturn(lineWidgets);
        when(client.getWidget(InterfaceID.Journalscroll.TEXTLAYER)).thenReturn(textLayer);

        tracker.onWidgetLoaded(loadEvent());

        verify(apiClient, never()).enqueue(any());
    }

    @Test
    public void resolvesCompoundAreaHeadingsToTheSameAreaTokenTheTierCompleteTrackerUses() {
        setPage("Kourend & Kebos Tasks", "Elite", "Do a hard Kourend thing");
        tracker.onWidgetLoaded(loadEvent());

        ArgumentCaptor<StatEvent> captor = ArgumentCaptor.forClass(StatEvent.class);
        verify(apiClient).enqueue(captor.capture());
        assertEquals("KOUREND", ((StatEvent.DiaryTaskProgress) captor.getValue()).diaryArea);
    }

    @Test
    public void anUnrecognizedAreaHeadingEmitsNothingRatherThanGuessing() {
        setPage("Some Unknown Page Heading", "Easy", "A task");
        tracker.onWidgetLoaded(loadEvent());
        verify(apiClient, never()).enqueue(any());
    }

    @Test
    public void taskLinesBeforeTheFirstTierHeaderAreSkippedNotMisattributed() {
        setPage("Varrock Tasks", "A stray line with no tier context yet", "Easy", "A real task");

        tracker.onWidgetLoaded(loadEvent());

        ArgumentCaptor<StatEvent> captor = ArgumentCaptor.forClass(StatEvent.class);
        verify(apiClient).enqueue(captor.capture());
        assertEquals("A real task", ((StatEvent.DiaryTaskProgress) captor.getValue()).taskName);
    }

    @Test
    public void blankLinesAreSkippedWithoutBreakingTierTracking() {
        setPage("Varrock Tasks", "Easy", "", "First task", "", "Second task");

        tracker.onWidgetLoaded(loadEvent());

        ArgumentCaptor<StatEvent> captor = ArgumentCaptor.forClass(StatEvent.class);
        verify(apiClient, times(2)).enqueue(captor.capture());
        List<StatEvent.DiaryTaskProgress> events = captor.getAllValues().stream()
            .map(e -> (StatEvent.DiaryTaskProgress) e)
            .collect(Collectors.toList());
        assertEquals("EASY", events.get(0).tier);
        assertEquals("EASY", events.get(1).tier);
    }
}
