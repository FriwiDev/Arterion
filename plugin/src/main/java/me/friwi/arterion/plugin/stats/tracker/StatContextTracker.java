package me.friwi.arterion.plugin.stats.tracker;

import me.friwi.arterion.plugin.stats.context.StatContext;
import me.friwi.arterion.plugin.stats.object.StatObject;
import me.friwi.arterion.plugin.util.database.Database;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public class StatContextTracker<T extends StatContext> {
    private T statContext;
    private List<StatObject> statObjects = new LinkedList<>();

    public StatContextTracker(T statContext) {
        this.statContext = statContext;
    }

    public T getStatContext() {
        return statContext;
    }

    public <S extends StatObject> ContextBasedStatObjectTracker<T, S> beginTracking(S statObject, UUID statObjectParty) {
        statObjects.add(statObject);
        return statObject.getStatTracker().beginTracking(statContext, statObjectParty, statContext.getTrackedStatistics(statObject.getType()));
    }

    public <S extends StatObject> ContextBasedStatObjectTracker<T, S> continueTracking(Database db, S statObject, UUID statObjectParty) {
        statObjects.add(statObject);
        return statObject.getStatTracker().continueTracking(db, statContext, statObjectParty, statContext.getTrackedStatistics(statObject.getType()));
    }

    public void stopTracking(Database db, StatObject statObject) {
        statObject.getStatTracker().stopTracking(db, statContext);
        statObjects.remove(statObject);
    }

    public void stopAllTrackers(Database db) {
        for (StatObject s : statObjects) {
            s.getStatTracker().stopTracking(db, statContext);
        }
        statObjects.clear();
    }
}
