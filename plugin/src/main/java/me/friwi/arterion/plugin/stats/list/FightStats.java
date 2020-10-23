package me.friwi.arterion.plugin.stats.list;

import me.friwi.arterion.plugin.stats.StatType;
import me.friwi.arterion.plugin.stats.TrackedStatistic;
import me.friwi.arterion.plugin.stats.object.StatObjectType;
import me.friwi.arterion.plugin.stats.time.TimeSlotUnit;

public class FightStats {
    private static final TrackedStatistic[] PLAYER_STATISTICS = new TrackedStatistic[]{
            new TrackedStatistic(StatType.KILLS, TimeSlotUnit.TEN_SECONDS),
            new TrackedStatistic(StatType.DEATHS, TimeSlotUnit.TEN_SECONDS),
            new TrackedStatistic(StatType.MOB_KILLS, TimeSlotUnit.TEN_SECONDS),
            new TrackedStatistic(StatType.HEAL, TimeSlotUnit.TEN_SECONDS),
            new TrackedStatistic(StatType.DAMAGE_DEALT_PLAYERS, TimeSlotUnit.TEN_SECONDS),
            new TrackedStatistic(StatType.DAMAGE_DEALT_MOBS, TimeSlotUnit.TEN_SECONDS),
            new TrackedStatistic(StatType.DAMAGE_RECEIVED, TimeSlotUnit.TEN_SECONDS),
            new TrackedStatistic(StatType.CLICKS, TimeSlotUnit.TEN_SECONDS),
            new TrackedStatistic(StatType.HITS, TimeSlotUnit.TEN_SECONDS)
    };
    private static final TrackedStatistic[] GUILD_STATISTICS = new TrackedStatistic[]{

    };

    public static TrackedStatistic[] getPlayerTrackableStats() {
        return PLAYER_STATISTICS;
    }

    public static TrackedStatistic[] getGuildTrackableStats() {
        return GUILD_STATISTICS;
    }

    public static TrackedStatistic[] getTrackedStatistics(StatObjectType objectType) {
        if (objectType == StatObjectType.GUILD) return getGuildTrackableStats();
        else return getPlayerTrackableStats();
    }
}
