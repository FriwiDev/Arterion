package me.friwi.arterion.plugin.util.database.entity;

import me.friwi.arterion.plugin.util.database.DatabaseEntity;
import me.friwi.arterion.plugin.util.database.enums.StatContextType;
import me.friwi.arterion.plugin.util.database.enums.StatObjectType;
import me.friwi.arterion.plugin.util.database.enums.StatType;

import javax.persistence.*;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "stats")
public class DatabaseStatComponent implements DatabaseEntity {
    @Id
    @GeneratedValue
    private UUID uuid;
    @Enumerated(EnumType.ORDINAL)
    private StatContextType contextType;
    private UUID targetContext;
    @Enumerated(EnumType.ORDINAL)
    private StatObjectType objectType;
    private UUID targetObject;
    private UUID targetObjectParty;
    @Enumerated(EnumType.ORDINAL)
    private StatType statType;
    private int statData;
    private long timeSlot;
    private long value;


    public DatabaseStatComponent() {
    }

    public DatabaseStatComponent(StatContextType contextType, UUID targetContext, StatObjectType objectType, UUID targetObject, UUID targetObjectParty, StatType statType, int statData, long timeSlot, long value) {
        this.contextType = contextType;
        this.targetContext = targetContext;
        this.objectType = objectType;
        this.targetObject = targetObject;
        this.targetObjectParty = targetObjectParty;
        this.statType = statType;
        this.statData = statData;
        this.timeSlot = timeSlot;
        this.value = value;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(uuid);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) return true;
        if (other == null) return false;
        if (other.getClass() != this.getClass()) return false;
        return Objects.equals(this.uuid, ((DatabaseStatComponent) other).uuid);
    }

    public UUID getUuid() {
        return uuid;
    }

    public StatContextType getContextType() {
        return contextType;
    }

    public void setContextType(StatContextType contextType) {
        this.contextType = contextType;
    }

    public UUID getTargetContext() {
        return targetContext;
    }

    public void setTargetContext(UUID targetContext) {
        this.targetContext = targetContext;
    }

    public StatObjectType getObjectType() {
        return objectType;
    }

    public void setObjectType(StatObjectType objectType) {
        this.objectType = objectType;
    }

    public UUID getTargetObject() {
        return targetObject;
    }

    public void setTargetObject(UUID targetObject) {
        this.targetObject = targetObject;
    }

    public UUID getTargetObjectParty() {
        return targetObjectParty;
    }

    public void setTargetObjectParty(UUID targetObjectParty) {
        this.targetObjectParty = targetObjectParty;
    }

    public StatType getStatType() {
        return statType;
    }

    public void setStatType(StatType statType) {
        this.statType = statType;
    }

    public int getStatData() {
        return statData;
    }

    public void setStatData(int statData) {
        this.statData = statData;
    }

    public long getTimeSlot() {
        return timeSlot;
    }

    public void setTimeSlot(long timeSlot) {
        this.timeSlot = timeSlot;
    }

    public long getValue() {
        return value;
    }

    public void setValue(long value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "DatabaseStatComponent{" +
                "uuid=" + uuid +
                ", contextType=" + contextType +
                ", targetContext=" + targetContext +
                ", objectType=" + objectType +
                ", targetObject=" + targetObject +
                ", targetObjectParty=" + targetObjectParty +
                ", statType=" + statType +
                ", statData=" + statData +
                ", timeSlot=" + timeSlot +
                ", value=" + value +
                '}';
    }
}
