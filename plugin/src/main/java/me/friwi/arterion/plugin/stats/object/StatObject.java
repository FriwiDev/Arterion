package me.friwi.arterion.plugin.stats.object;

import me.friwi.arterion.plugin.stats.tracker.StatObjectTracker;

import java.util.UUID;

public interface StatObject<T extends StatObject> {
    StatObjectType getType();

    UUID getUUID();

    StatObjectTracker<T> getStatTracker();
}
