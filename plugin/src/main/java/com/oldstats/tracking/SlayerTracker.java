package com.oldstats.tracking;

import com.oldstats.api.OldStatsApiClient;
import com.oldstats.api.StatEvent;
import java.time.Instant;
import java.util.List;
import net.runelite.api.Client;
import net.runelite.api.gameval.VarPlayerID;
import net.runelite.api.gameval.VarbitID;

/**
 * Tracks the current Slayer task the same way RuneLite's own built-in Slayer
 * plugin does — no chat parsing. The task's monster identity isn't stored as
 * plain text anywhere; it lives in Jagex's client-side DB table system,
 * looked up via {@link Client#getDBRowsByValue}/{@link Client#getDBTableField} keyed by
 * {@link VarPlayerID#SLAYER_TARGET} (DB table 113, field 10 = display name).
 * Wilderness/Krystilia tasks are a special case: {@code SLAYER_TARGET}
 * reads as the sentinel value 98, and the real creature id instead lives in
 * {@link VarbitID#SLAYER_TARGET_BOSSID}, which cross-references into DB table 116
 * (field 4) to get the row id table 113 actually uses.
 *
 * <p>{@link VarPlayerID#SLAYER_COUNT} is the remaining kill count; a task is
 * considered complete when it hits 0 before the next task is assigned (or,
 * failing that tick boundary, when the creature id changes while the
 * previous task's remaining count was already 0 — cancelling/skipping a task
 * before finishing it does not emit a completion).
 */
public class SlayerTracker {
    private static final int WILDERNESS_SENTINEL = 98;
    private static final int TASK_TABLE = 113;
    private static final int WILDERNESS_TASK_TABLE = 116;
    private static final int TASK_NAME_FIELD = 10;

    private final OldStatsApiClient apiClient;
    private final Client client;

    private boolean baselineEstablished = false;
    private Integer currentCreature = null;
    private Integer currentBossId = null;
    private String currentTaskName = null;
    private Integer currentAmountAssigned = null;
    private Integer lastAmountSeen = null;
    private String startedAt = null;
    private Integer lastKnownPoints = null;
    private boolean completionEmitted = false;

    public SlayerTracker(OldStatsApiClient apiClient, Client client) {
        this.apiClient = apiClient;
        this.client = client;
    }

    public void reset() {
        baselineEstablished = false;
        currentCreature = null;
        currentBossId = null;
        currentTaskName = null;
        currentAmountAssigned = null;
        lastAmountSeen = null;
        startedAt = null;
        lastKnownPoints = null;
        completionEmitted = false;
    }

    public void checkTask() {
        int creature = client.getVarpValue(VarPlayerID.SLAYER_TARGET);
        Integer bossId = creature == WILDERNESS_SENTINEL ? client.getVarbitValue(VarbitID.SLAYER_TARGET_BOSSID) : null;
        int amount = client.getVarpValue(VarPlayerID.SLAYER_COUNT);
        int points = client.getVarbitValue(VarbitID.SLAYER_POINTS);

        if (!baselineEstablished) {
            baselineEstablished = true;
            currentCreature = creature;
            currentBossId = bossId;
            lastAmountSeen = amount;
            lastKnownPoints = points;
            completionEmitted = false;
            if (creature != 0) {
                currentTaskName = resolveTaskName(creature, bossId);
                currentAmountAssigned = amount;
                startedAt = now();
            }
            return;
        }

        boolean taskChanged = !Integer.valueOf(creature).equals(currentCreature)
            || (creature == WILDERNESS_SENTINEL && !java.util.Objects.equals(bossId, currentBossId));

        if (taskChanged) {
            if (currentCreature != null && currentCreature != 0
                && lastAmountSeen != null && lastAmountSeen == 0 && !completionEmitted) {
                emitCompletion(points);
            }
            currentCreature = creature;
            currentBossId = bossId;
            completionEmitted = false;
            if (creature != 0) {
                currentTaskName = resolveTaskName(creature, bossId);
                currentAmountAssigned = amount;
                startedAt = now();
            } else {
                currentTaskName = null;
                currentAmountAssigned = null;
                startedAt = null;
            }
        } else if (creature != 0 && amount == 0 && (lastAmountSeen != null ? lastAmountSeen : 0) > 0 && !completionEmitted) {
            emitCompletion(points);
            completionEmitted = true;
        }

        lastAmountSeen = amount;
        lastKnownPoints = points;
    }

    private void emitCompletion(int currentPoints) {
        String name = currentTaskName;
        if (name == null) return;
        Integer pointsEarned = null;
        if (lastKnownPoints != null) {
            int delta = currentPoints - lastKnownPoints;
            if (delta > 0) pointsEarned = delta;
        }
        apiClient.enqueue(
            new StatEvent.SlayerTask(name, currentAmountAssigned, pointsEarned, null, startedAt, now())
        );
    }

    private String resolveTaskName(int creature, Integer bossId) {
        Integer rowId;
        if (creature == WILDERNESS_SENTINEL) {
            if (bossId == null) return null;
            List<Integer> crossRefRows = client.getDBRowsByValue(WILDERNESS_TASK_TABLE, 1, 0, bossId);
            if (crossRefRows.isEmpty()) return null;
            int crossRefRow = crossRefRows.get(0);
            Object[] field = client.getDBTableField(crossRefRow, 4, 0);
            if (field.length == 0 || !(field[0] instanceof Integer)) return null;
            rowId = (Integer) field[0];
        } else {
            List<Integer> rows = client.getDBRowsByValue(TASK_TABLE, 0, 0, creature);
            if (rows.isEmpty()) return null;
            rowId = rows.get(0);
        }
        Object[] nameField = client.getDBTableField(rowId, TASK_NAME_FIELD, 0);
        if (nameField.length == 0 || !(nameField[0] instanceof String)) return null;
        return (String) nameField[0];
    }

    private static String now() {
        return Instant.now().toString();
    }
}
