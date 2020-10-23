package me.friwi.arterion.plugin.permissions;

public enum TimeUnit {
    MINUTES(60 * 1000),
    HOURS(3600 * 1000),
    DAYS(24 * 3600 * 1000),
    YEARS(365 * 24 * 3600 * 1000);

    private long singleDuration;

    TimeUnit(long singleDuration) {
        this.singleDuration = singleDuration;
    }

    public long getSingleDuration() {
        return this.singleDuration;
    }
}
