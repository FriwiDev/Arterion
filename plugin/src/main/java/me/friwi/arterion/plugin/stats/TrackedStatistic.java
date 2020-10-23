package me.friwi.arterion.plugin.stats;

import me.friwi.arterion.plugin.stats.time.TimeSlotUnit;

public class TrackedStatistic {
    private StatType statType;
    private TimeSlotUnit timeSlotUnit;

    public TrackedStatistic(StatType statType, TimeSlotUnit timeSlotUnit) {
        this.statType = statType;
        this.timeSlotUnit = timeSlotUnit;
    }

    public StatType getStatType() {
        return statType;
    }

    public TimeSlotUnit getTimeSlotUnit() {
        return timeSlotUnit;
    }
}
