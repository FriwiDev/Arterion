package me.friwi.arterion.plugin.stats.context;

import me.friwi.arterion.plugin.stats.TrackedStatistic;
import me.friwi.arterion.plugin.stats.list.GlobalStats;
import me.friwi.arterion.plugin.stats.object.StatObjectType;
import me.friwi.arterion.plugin.stats.tracker.StatContextTracker;

import java.util.UUID;

public class GlobalStatContext implements StatContext<GlobalStatContext> {
    private StatContextTracker<GlobalStatContext> statContextTracker = new StatContextTracker<>(this);

    @Override
    public StatContextType getType() {
        return StatContextType.GLOBAL;
    }

    @Override
    public UUID getUUID() {
        return null;
    }

    @Override
    public long getTime() {
        return System.currentTimeMillis();
    }

    @Override
    public StatContextTracker getStatTracker() {
        return statContextTracker;
    }

    @Override
    public TrackedStatistic[] getTrackedStatistics(StatObjectType objectType) {
        if (objectType == StatObjectType.GUILD) return GlobalStats.getGuildTrackableStats();
        else return GlobalStats.getPlayerTrackableStats();
    }
}
