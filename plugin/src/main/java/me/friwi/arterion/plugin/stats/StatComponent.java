package me.friwi.arterion.plugin.stats;

import me.friwi.arterion.plugin.stats.context.StatContext;
import me.friwi.arterion.plugin.stats.context.StatContextType;
import me.friwi.arterion.plugin.stats.object.StatObject;
import me.friwi.arterion.plugin.stats.object.StatObjectType;
import me.friwi.arterion.plugin.util.database.Database;
import me.friwi.arterion.plugin.util.database.entity.DatabaseStatComponent;

import java.util.UUID;

public class StatComponent<T extends StatContext, S extends StatObject> {
    private DatabaseStatComponent persistenceHolder;
    private long loadValue;

    public StatComponent(DatabaseStatComponent persistenceHolder) {
        this.persistenceHolder = persistenceHolder;
        this.loadValue = persistenceHolder.getValue();
    }

    public StatComponent(T statContext, S statObject, UUID statObjectParty, StatType statType, int statData, long timeSlot) {
        this.persistenceHolder = new DatabaseStatComponent(statContext.getType(), statContext.getUUID(), statObject.getType(), statObject.getUUID(), statObjectParty, statType, statData, timeSlot, statType.getDefaultValue());
        this.loadValue = getStatType().getDefaultValue();
    }

    public UUID getUuid() {
        return persistenceHolder.getUuid();
    }

    public StatContextType getComponentType() {
        return persistenceHolder.getContextType();
    }

    public UUID getTargetComponent() {
        return persistenceHolder.getTargetContext();
    }

    public StatObjectType getObjectType() {
        return persistenceHolder.getObjectType();
    }

    public UUID getTargetObject() {
        return persistenceHolder.getTargetObject();
    }

    public UUID getTargetObjectParty() {
        return persistenceHolder.getTargetObjectParty();
    }

    public StatType getStatType() {
        return persistenceHolder.getStatType();
    }

    public int getStatData() {
        return persistenceHolder.getStatData();
    }

    public long getTimeSlot() {
        return persistenceHolder.getTimeSlot();
    }

    public long getValue() {
        return persistenceHolder.getValue();
    }

    public void setValue(long value) {
        persistenceHolder.setValue(value);
    }

    public void save(Database db) {
        if (getValue() == getStatType().getDefaultValue()) {
            if (persistenceHolder.getUuid() != null) {
                //Default value (irrelevant) and already saved -> delete
                db.beginTransaction();
                db.delete(persistenceHolder);
                db.commit();
            }
        } else if (getValue() != loadValue) {
            db.beginTransaction();
            db.save(persistenceHolder);
            db.commit();
        }
    }
}
