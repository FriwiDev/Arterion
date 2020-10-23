package me.friwi.arterion.plugin.stats.time;

import me.friwi.arterion.plugin.stats.context.StatContext;

public enum TimeSlotUnit {
    TEN_SECONDS(10 * 1000),
    ONE_MINUTE(60 * 1000),
    ONE_HOUR(60 * 60 * 1000),
    ONE_DAY(24 * 60 * 60 * 1000);

    private long unitDuration;

    TimeSlotUnit(long unitDuration) {
        this.unitDuration = unitDuration;
    }

    public long getUnitDuration() {
        return unitDuration;
    }

    public long getTimeSlot(long time) {
        return time / getUnitDuration();
    }

    public long getCurrentTimeSlot(StatContext context) {
        return getTimeSlot(context.getTime());
    }
}
