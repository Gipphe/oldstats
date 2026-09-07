package com.oldstats.api;

import java.time.Instant;
import java.util.List;

/**
 * Mirrors the discriminated union accepted by the OldStats server's
 * POST /api/events endpoint (server/src/types/events.ts). Field names must
 * match exactly since Gson serializes by declared field name.
 */
public abstract class StatEvent {
    public final String type;

    private StatEvent(String type) {
        this.type = type;
    }

    static String now() {
        return Instant.now().toString();
    }

    public static final class XpGain extends StatEvent {
        public final String skill;
        public final long xp;
        public final long xpGained;
        public final int level;
        public final String ts;

        public XpGain(String skill, long xp, long xpGained, int level) {
            super("xp_gain");
            this.skill = skill;
            this.xp = xp;
            this.xpGained = xpGained;
            this.level = level;
            this.ts = now();
        }
    }

    public static final class LevelUp extends StatEvent {
        public final String skill;
        public final int level;
        public final String ts;

        public LevelUp(String skill, int level) {
            super("level_up");
            this.skill = skill;
            this.level = level;
            this.ts = now();
        }
    }

    public static final class Kill extends StatEvent {
        public final String npcName;
        public final Integer npcId;
        public final boolean isBoss;
        public final String ts;

        public Kill(String npcName, Integer npcId, boolean isBoss) {
            super("kill");
            this.npcName = npcName;
            this.npcId = npcId;
            this.isBoss = isBoss;
            this.ts = now();
        }
    }

    public static final class Drop extends StatEvent {
        public final String npcName;
        public final String itemName;
        public final Integer itemId;
        public final int quantity;
        public final long value;
        public final String ts;

        public Drop(String npcName, String itemName, Integer itemId, int quantity, long value) {
            super("drop");
            this.npcName = npcName;
            this.itemName = itemName;
            this.itemId = itemId;
            this.quantity = quantity;
            this.value = value;
            this.ts = now();
        }
    }

    public static final class Quest extends StatEvent {
        public final String questName;
        public final String state;
        public final Integer questPoints;
        public final String ts;

        public Quest(String questName, String state, Integer questPoints) {
            super("quest");
            this.questName = questName;
            this.state = state;
            this.questPoints = questPoints;
            this.ts = now();
        }
    }

    public static final class SlayerTask extends StatEvent {
        public final String taskName;
        public final Integer amountAssigned;
        public final Integer points;
        public final Integer streak;
        public final String startedAt;
        public final String completedAt;

        public SlayerTask(
            String taskName,
            Integer amountAssigned,
            Integer points,
            Integer streak,
            String startedAt,
            String completedAt
        ) {
            super("slayer_task");
            this.taskName = taskName;
            this.amountAssigned = amountAssigned;
            this.points = points;
            this.streak = streak;
            this.startedAt = startedAt;
            this.completedAt = completedAt;
        }
    }

    public static final class FarmingPatch extends StatEvent {
        public final String patchName;
        public final String crop;
        public final String state;
        public final String ts;

        public FarmingPatch(String patchName, String crop, String state) {
            super("farming_patch");
            this.patchName = patchName;
            this.crop = crop;
            this.state = state;
            this.ts = now();
        }
    }

    public static final class CollectionLogItem extends StatEvent {
        public final String itemName;
        public final Integer itemId;
        public final Integer totalUnlocked;
        public final Integer totalPossible;
        public final String ts;

        public CollectionLogItem(String itemName, Integer itemId, Integer totalUnlocked, Integer totalPossible) {
            super("collection_log_item");
            this.itemName = itemName;
            this.itemId = itemId;
            this.totalUnlocked = totalUnlocked;
            this.totalPossible = totalPossible;
            this.ts = now();
        }
    }

    public static final class CombatAchievement extends StatEvent {
        public final String taskName;
        public final Integer totalPoints;
        public final String ts;

        public CombatAchievement(String taskName, Integer totalPoints) {
            super("combat_achievement");
            this.taskName = taskName;
            this.totalPoints = totalPoints;
            this.ts = now();
        }
    }

    public static final class DiaryCompleted extends StatEvent {
        public final String diaryArea;
        public final String tier;
        public final String ts;

        public DiaryCompleted(String diaryArea, String tier) {
            super("diary_completed");
            this.diaryArea = diaryArea;
            this.tier = tier;
            this.ts = now();
        }
    }

    public static final class DiaryTaskProgress extends StatEvent {
        public final String diaryArea;
        public final String tier;
        public final String taskName;
        public final boolean completed;
        public final String ts;

        public DiaryTaskProgress(String diaryArea, String tier, String taskName, boolean completed) {
            super("diary_task_progress");
            this.diaryArea = diaryArea;
            this.tier = tier;
            this.taskName = taskName;
            this.completed = completed;
            this.ts = now();
        }
    }

    public static final class ClueCompleted extends StatEvent {
        public final String tier;
        public final Integer count;
        public final String ts;

        public ClueCompleted(String tier, Integer count) {
            super("clue_completed");
            this.tier = tier;
            this.count = count;
            this.ts = now();
        }
    }

    public static final class PetReceived extends StatEvent {
        public final String petName;
        public final String ts;

        public PetReceived(String petName) {
            super("pet_received");
            this.petName = petName;
            this.ts = now();
        }
    }

    public static final class PersonalBest extends StatEvent {
        public final String activityName;
        public final double durationSeconds;
        public final String ts;

        public PersonalBest(String activityName, double durationSeconds) {
            super("personal_best");
            this.activityName = activityName;
            this.durationSeconds = durationSeconds;
            this.ts = now();
        }
    }

    public static final class PlayerKill extends StatEvent {
        public final String opponentName;
        public final String ts;

        public PlayerKill(String opponentName) {
            super("player_kill");
            this.opponentName = opponentName;
            this.ts = now();
        }
    }

    public static final class PlayerDeath extends StatEvent {
        public final Long valueLost;
        public final String ts;

        public PlayerDeath(Long valueLost) {
            super("player_death");
            this.valueLost = valueLost;
            this.ts = now();
        }
    }

    public static final class WorldChange extends StatEvent {
        public final int world;
        public final List<String> worldTypes;
        public final String ts;

        public WorldChange(int world, List<String> worldTypes) {
            super("world_change");
            this.world = world;
            this.worldTypes = worldTypes;
            this.ts = now();
        }
    }

    public static final class NetWorthSnapshot extends StatEvent {
        public final long inventoryValue;
        public final long equipmentValue;
        public final long bankValue;
        public final long totalValue;
        public final String ts;

        public NetWorthSnapshot(long inventoryValue, long equipmentValue, long bankValue, long totalValue) {
            super("net_worth_snapshot");
            this.inventoryValue = inventoryValue;
            this.equipmentValue = equipmentValue;
            this.bankValue = bankValue;
            this.totalValue = totalValue;
            this.ts = now();
        }
    }

    public static final class BankSnapshot extends StatEvent {
        public final List<BankItemPayload> items;
        public final long totalValue;
        public final String ts;

        public BankSnapshot(List<BankItemPayload> items, long totalValue) {
            super("bank_snapshot");
            this.items = items;
            this.totalValue = totalValue;
            this.ts = now();
        }
    }
}
