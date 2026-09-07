package com.oldstats.tracking;

import com.oldstats.api.OldStatsApiClient;
import com.oldstats.api.StatEvent;
import java.util.concurrent.ConcurrentHashMap;
import net.runelite.api.Client;
import net.runelite.api.Quest;
import net.runelite.api.QuestState;
import net.runelite.api.gameval.VarPlayerID;
import net.runelite.client.callback.ClientThread;

/**
 * There is no dedicated "quest completed" event in the RuneLite API — quest
 * progress lives in varbits/varps with no single change notification, so
 * this re-checks every {@link Quest}'s state on each VarbitChanged tick and
 * reports newly-FINISHED quests. The first check after (re)login only
 * establishes a baseline so already-completed quests aren't reported again.
 *
 * <p>{@link Quest#getState} internally calls {@code client.runScript(ScriptID.QUEST_STATUS_GET, ...)}
 * — a real CS2 script invocation, not a cheap field read. Varbits are
 * frequently changed by scripts themselves, so VarbitChanged can fire while
 * one is still executing; calling another script synchronously from there
 * throws {@code AssertionError: scripts are not reentrant}. Deferring to the
 * next client tick via {@code clientThread} guarantees a clean call stack.
 */
public class QuestTracker {
    private final OldStatsApiClient apiClient;
    private final Client client;
    private final ClientThread clientThread;

    private final ConcurrentHashMap<Quest, QuestState> lastState = new ConcurrentHashMap<>();

    public QuestTracker(OldStatsApiClient apiClient, Client client, ClientThread clientThread) {
        this.apiClient = apiClient;
        this.client = client;
        this.clientThread = clientThread;
    }

    public void reset() {
        lastState.clear();
    }

    public void checkQuests() {
        clientThread.invokeLater(() -> {
            for (Quest quest : Quest.values()) {
                QuestState state = quest.getState(client);
                QuestState previous = lastState.put(quest, state);
                if (previous != null && previous != QuestState.FINISHED && state == QuestState.FINISHED) {
                    apiClient.enqueue(
                        new StatEvent.Quest(quest.name(), "COMPLETED", client.getVarpValue(VarPlayerID.QP))
                    );
                }
            }
        });
    }
}
