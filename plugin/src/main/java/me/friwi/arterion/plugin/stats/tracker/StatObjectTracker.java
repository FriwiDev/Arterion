package me.friwi.arterion.plugin.stats.tracker;

import me.friwi.arterion.plugin.stats.StatType;
import me.friwi.arterion.plugin.stats.TrackedStatistic;
import me.friwi.arterion.plugin.stats.context.StatContext;
import me.friwi.arterion.plugin.stats.object.StatObject;
import me.friwi.arterion.plugin.util.database.Database;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

public class StatObjectTracker<S extends StatObject> {
    private Map<StatContext, ContextBasedStatObjectTracker<? extends StatContext, S>> contextBasedTrackers = new HashMap<>();
    private S statObject;

    public StatObjectTracker(S statObject) {
        this.statObject = statObject;
    }

    public S getStatObject() {
        return statObject;
    }

    public <T extends StatContext> ContextBasedStatObjectTracker<T, S> beginTracking(T context, UUID statObjectParty, TrackedStatistic[] toTrack) {
        ContextBasedStatObjectTracker<T, S> tracker = new ContextBasedStatObjectTracker<>(context, statObject, statObjectParty);
        tracker.beginTracking(toTrack);
        contextBasedTrackers.put(context, tracker);
        return tracker;
    }

    public <T extends StatContext> ContextBasedStatObjectTracker<T, S> continueTracking(Database db, T context, UUID statObjectParty, TrackedStatistic[] toTrack) {
        ContextBasedStatObjectTracker<T, S> tracker = new ContextBasedStatObjectTracker<>(context, statObject, statObjectParty);
        tracker.continueTracking(db, toTrack);
        contextBasedTrackers.put(context, tracker);
        return tracker;
    }

    public void stopTracking(Database db, StatContext context) {
        ContextBasedStatObjectTracker<? extends StatContext, S> tracker = contextBasedTrackers.remove(context);
        if (tracker != null) {
            tracker.save(db);
        }
    }

    public void stopAllTrackers(Database db) {
        Iterator<Map.Entry<StatContext, ContextBasedStatObjectTracker<? extends StatContext, S>>> it = contextBasedTrackers.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<StatContext, ContextBasedStatObjectTracker<? extends StatContext, S>> ent = it.next();
            ent.getValue().save(db);
            it.remove();
        }
    }

    public void trackStatistic(Database db, StatType type, int statData, Function<Long, Long> valueMapper) {
        Iterator<Map.Entry<StatContext, ContextBasedStatObjectTracker<? extends StatContext, S>>> it = contextBasedTrackers.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<StatContext, ContextBasedStatObjectTracker<? extends StatContext, S>> ent = it.next();
            ent.getValue().trackStatistic(db, type, statData, valueMapper);
        }
    }
}
