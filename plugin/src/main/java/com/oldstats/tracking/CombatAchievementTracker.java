package com.oldstats.tracking;

import com.oldstats.api.OldStatsApiClient;
import com.oldstats.api.StatEvent;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import net.runelite.api.Client;
import net.runelite.api.gameval.VarbitID;

/**
 * There is no dedicated "combat achievement completed" event or a
 * {@code Quest}-like enum to enumerate tasks by. RuneLite's generated
 * {@link VarbitID} constants do expose one boolean varbit per task named
 * {@code CA_TASK_<...>_COMPLETED} (~400 of them) plus a single
 * {@link VarbitID#CA_POINTS} running total, so this reflects over those
 * field names once at startup and polls/diffs them on every VarbitChanged
 * tick — the same pattern as {@link QuestTracker}. The derived task name
 * (e.g. "Vorkath Killcount 1") comes from the varbit's own constant name,
 * not Jagex's actual task title, since that mapping isn't exposed anywhere
 * in the client API.
 */
public class CombatAchievementTracker {
    private static final class TaskVarbit {
        final String name;
        final int varbitId;

        TaskVarbit(String name, int varbitId) {
            this.name = name;
            this.varbitId = varbitId;
        }
    }

    private final OldStatsApiClient apiClient;
    private final Client client;

    private List<TaskVarbit> taskVarbits;
    private final ConcurrentHashMap<Integer, Integer> lastValues = new ConcurrentHashMap<>();

    public CombatAchievementTracker(OldStatsApiClient apiClient, Client client) {
        this.apiClient = apiClient;
        this.client = client;
    }

    private List<TaskVarbit> taskVarbits() {
        if (taskVarbits == null) {
            List<TaskVarbit> found = new ArrayList<>();
            for (Field field : VarbitID.class.getFields()) {
                if (field.getName().startsWith("CA_TASK_") && field.getName().endsWith("_COMPLETED")) {
                    String middle = field.getName().substring("CA_TASK_".length());
                    middle = middle.substring(0, middle.length() - "_COMPLETED".length());
                    try {
                        found.add(new TaskVarbit(titleCase(middle), field.getInt(null)));
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            taskVarbits = found;
        }
        return taskVarbits;
    }

    public void reset() {
        lastValues.clear();
    }

    public void checkTasks() {
        for (TaskVarbit task : taskVarbits()) {
            int value = client.getVarbitValue(task.varbitId);
            Integer previous = lastValues.put(task.varbitId, value);
            if (previous == null) continue; // baseline only
            if (previous == 0 && value != 0) {
                int totalPoints = client.getVarbitValue(VarbitID.CA_POINTS);
                apiClient.enqueue(
                    new StatEvent.CombatAchievement(task.name, totalPoints > 0 ? totalPoints : null)
                );
            }
        }
    }

    private String titleCase(String name) {
        StringBuilder result = new StringBuilder();
        for (String part : name.toLowerCase().split("_")) {
            if (result.length() > 0) result.append(' ');
            if (!part.isEmpty()) {
                result.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
            }
        }
        return result.toString();
    }
}
