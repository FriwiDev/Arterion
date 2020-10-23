package me.friwi.arterion.plugin.stats.context;

import me.friwi.arterion.plugin.stats.TrackedStatistic;
import me.friwi.arterion.plugin.stats.list.GlobalStats;
import me.friwi.arterion.plugin.stats.object.StatObjectType;
import me.friwi.arterion.plugin.stats.tracker.StatContextTracker;

import java.util.UUID;

public class GlobalTopStatContext implements StatContext<GlobalTopStatContext> {
    private StatContextTracker<GlobalTopStatContext> statContextTracker = new StatContextTracker<>(this);

    @Override
    public StatContextType getType() {
        return StatContextType.GLOBAL_TOP;
    }

    @Override
    public UUID getUUID() {
        return null;
    }

    @Override
    public long getTime() {
        return 0;
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
