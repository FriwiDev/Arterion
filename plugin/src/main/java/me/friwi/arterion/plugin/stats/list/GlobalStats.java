package me.friwi.arterion.plugin.stats.list;

import me.friwi.arterion.plugin.stats.StatType;
import me.friwi.arterion.plugin.stats.TrackedStatistic;
import me.friwi.arterion.plugin.stats.context.GlobalStatContext;
import me.friwi.arterion.plugin.stats.context.GlobalTopStatContext;
import me.friwi.arterion.plugin.stats.time.TimeSlotUnit;

public class GlobalStats {
    private static final GlobalStatContext CONTEXT = new GlobalStatContext();
    private static final GlobalTopStatContext CONTEXT_TOP = new GlobalTopStatContext();
    private static final TrackedStatistic[] PLAYER_STATISTICS = new TrackedStatistic[]{
            new TrackedStatistic(StatType.KILLS, TimeSlotUnit.ONE_HOUR),
            new TrackedStatistic(StatType.DEATHS, TimeSlotUnit.ONE_HOUR),
            new TrackedStatistic(StatType.XP, TimeSlotUnit.ONE_HOUR),
            new TrackedStatistic(StatType.MOB_KILLS, TimeSlotUnit.ONE_HOUR),
            new TrackedStatistic(StatType.DESTROYED_BLOCKS, TimeSlotUnit.ONE_HOUR),
            new TrackedStatistic(StatType.PLACED_BLOCKS, TimeSlotUnit.ONE_HOUR),
            new TrackedStatistic(StatType.HEAL, TimeSlotUnit.ONE_HOUR),
            new TrackedStatistic(StatType.DAMAGE_DEALT_PLAYERS, TimeSlotUnit.ONE_HOUR),
            new TrackedStatistic(StatType.DAMAGE_DEALT_MOBS, TimeSlotUnit.ONE_HOUR),
            new TrackedStatistic(StatType.DAMAGE_RECEIVED, TimeSlotUnit.ONE_HOUR),
            new TrackedStatistic(StatType.CLICKS, TimeSlotUnit.ONE_HOUR),
            new TrackedStatistic(StatType.HITS, TimeSlotUnit.ONE_HOUR),
            new TrackedStatistic(StatType.ONLINE_MINUTES, TimeSlotUnit.ONE_DAY)
    };
    private static final TrackedStatistic[] GUILD_STATISTICS = new TrackedStatistic[]{
            new TrackedStatistic(StatType.CLAN_KILLS, TimeSlotUnit.ONE_DAY),
            new TrackedStatistic(StatType.CLAN_DEATHS, TimeSlotUnit.ONE_DAY),
            new TrackedStatistic(StatType.GFIGHT_ATTACK_WINS, TimeSlotUnit.ONE_DAY),
            new TrackedStatistic(StatType.GFIGHT_ATTACK_LOSS, TimeSlotUnit.ONE_DAY),
            new TrackedStatistic(StatType.GFIGHT_DEFENSE_WINS, TimeSlotUnit.ONE_DAY),
            new TrackedStatistic(StatType.GFIGHT_DEFENSE_LOSS, TimeSlotUnit.ONE_DAY),
            new TrackedStatistic(StatType.CAPTURE_POINT_TAKEN, TimeSlotUnit.ONE_DAY),
            new TrackedStatistic(StatType.ARTEFACT_HOURS, TimeSlotUnit.ONE_DAY),
            new TrackedStatistic(StatType.GUILD_ONLINE_MINUTES, TimeSlotUnit.ONE_DAY)
    };

    public static TrackedStatistic[] getPlayerTrackableStats() {
        return PLAYER_STATISTICS;
    }

    public static TrackedStatistic[] getGuildTrackableStats() {
        return GUILD_STATISTICS;
    }

    public static GlobalStatContext getContext() {
        return CONTEXT;
    }

    public static GlobalTopStatContext getTopContext() {
        return CONTEXT_TOP;
    }
}
