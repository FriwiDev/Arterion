package me.friwi.arterion.plugin.util.scheduler;

public class Schedulers {
    private InternalScheduler databaseScheduler = new InternalScheduler("Arterion-Database");
    private InternalScheduler mainScheduler = new InternalScheduler("Arterion-Main");
    private InternalScheduler networkScheduler = new InternalScheduler("Arterion-Network");
    private InternalScheduler[] schedulers = new InternalScheduler[]{networkScheduler, mainScheduler, databaseScheduler};

    public void start() {
        for (InternalScheduler s : schedulers) s.startup();
    }

    public void stop() {
        for (InternalScheduler s : schedulers) s.shutdown();
    }

    public InternalScheduler getDatabaseScheduler() {
        return databaseScheduler;
    }

    public InternalScheduler getMainScheduler() {
        return mainScheduler;
    }

    public InternalScheduler getNetworkScheduler() {
        return networkScheduler;
    }
}
