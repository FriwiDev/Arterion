package me.friwi.arterion.plugin.stats.tracker;

import me.friwi.arterion.plugin.stats.StatComponent;
import me.friwi.arterion.plugin.stats.StatType;
import me.friwi.arterion.plugin.stats.TrackedStatistic;
import me.friwi.arterion.plugin.stats.context.StatContext;
import me.friwi.arterion.plugin.stats.object.StatObject;
import me.friwi.arterion.plugin.stats.time.TimeSlotUnit;
import me.friwi.arterion.plugin.util.database.Database;
import me.friwi.arterion.plugin.util.database.entity.DatabaseStatComponent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

public class ContextBasedStatObjectTracker<T extends StatContext, S extends StatObject> {
    private Map<Integer, StatComponent<T, S>>[] trackedStats = new Map[StatType.values().length];
    private T statContext;
    private S statObject;
    private UUID statObjectParty;
    private TimeSlotUnit[] timeSlotUnits = new TimeSlotUnit[StatType.values().length];

    public ContextBasedStatObjectTracker(T statContext, S statObject, UUID statObjectParty) {
        this.statContext = statContext;
        this.statObject = statObject;
        this.statObjectParty = statObjectParty;
    }

    public void beginTracking(TrackedStatistic[] toTrack) {
        for (TrackedStatistic stat : toTrack) {
            trackedStats[stat.getStatType().ordinal()] = new HashMap<>();
            timeSlotUnits[stat.getStatType().ordinal()] = stat.getTimeSlotUnit();
        }
    }

    public void continueTracking(Database db, TrackedStatistic[] toTrack) {
        for (TrackedStatistic stat : toTrack) {
            trackedStats[stat.getStatType().ordinal()] = new HashMap<>();
            timeSlotUnits[stat.getStatType().ordinal()] = stat.getTimeSlotUnit();
            db.beginTransaction();
            List<DatabaseStatComponent> existing = db.findAllByColumn(DatabaseStatComponent.class,
                    new String[]{
                            "contextType", "targetContext", "objectType", "targetObject", "targetObjectParty", "statType", "timeSlot"
                    }, new Object[]{
                            statContext.getType(), statContext.getUUID(),
                            statObject.getType(), statObject.getUUID(), statObjectParty,
                            stat.getStatType(), stat.getTimeSlotUnit().getCurrentTimeSlot(statContext)
                    });
            db.commit();
            for (DatabaseStatComponent component : existing) {
                trackedStats[stat.getStatType().ordinal()].put(component.getStatData(), new StatComponent<>(component));
            }
        }
    }

    public void trackStatistic(Database db, StatType type, int statData, Function<Long, Long> valueMapper) {
        if (timeSlotUnits[type.ordinal()] != null) {
            //Statistic is tracked by us!
            long timeSlot = timeSlotUnits[type.ordinal()].getCurrentTimeSlot(statContext);
            Map<Integer, StatComponent<T, S>> map = trackedStats[type.ordinal()];
            if (map != null) {
                StatComponent<T, S> tracked = map.get(statData);
                if (tracked != null && tracked.getTimeSlot() != timeSlot) {
                    tracked.save(db);
                    tracked = null;
                }
                if (tracked == null) {
                    tracked = new StatComponent<>(statContext, statObject, statObjectParty, type, statData, timeSlot);
                    map.put(statData, tracked);
                }
                tracked.setValue(valueMapper.apply(tracked.getValue()));
            }
        }
    }

    public void save(Database db) {
        for (Map<Integer, StatComponent<T, S>> map : trackedStats) {
            if (map != null) {
                for (Map.Entry<Integer, StatComponent<T, S>> ent : map.entrySet()) {
                    if (ent.getValue() != null) {
                        ent.getValue().save(db);
                    }
                }
            }
        }
    }
}
