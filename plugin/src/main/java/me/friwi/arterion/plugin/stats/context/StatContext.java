package me.friwi.arterion.plugin.stats.context;

import me.friwi.arterion.plugin.stats.TrackedStatistic;
import me.friwi.arterion.plugin.stats.object.StatObjectType;
import me.friwi.arterion.plugin.stats.tracker.StatContextTracker;

import java.util.UUID;

public interface StatContext<T extends StatContext> {
    StatContextType getType();

    UUID getUUID();

    long getTime();

    StatContextTracker<T> getStatTracker();

    TrackedStatistic[] getTrackedStatistics(StatObjectType objectType);
}
