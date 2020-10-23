package me.friwi.arterion.plugin.util.scheduler;

import co.aikar.timings.lib.MCTiming;
import me.friwi.arterion.plugin.ArterionPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.LinkedList;
import java.util.concurrent.ConcurrentLinkedQueue;

public class InternalScheduler extends Thread {
    private long[] last_ticks = new long[]{1, 1, 1, 1, 1};
    private double tps = 20;
    private long tick_duration = 0;
    private ConcurrentLinkedQueue<InternalTask> list = new ConcurrentLinkedQueue<InternalTask>();
    private String name = null;
    private long tick = 0;
    private int prev_task_amount = 0;
    private long startt = 0;
    private boolean shutdown = false;

    public InternalScheduler(String name) {
        this.name = name;
    }

    @Override
    public void run() {
        this.startt = System.currentTimeMillis();
        this.setName(name);
        while (!shutdown) {
            long des = (System.currentTimeMillis() - startt) / 50;
            for (; tick < des; tick++) runTick(tick);
            tps = 1000D / ((System.currentTimeMillis() - last_ticks[0] + 0D) / 5D);
            tps = (Math.round(tps * 10) + 0D) / 10D;
            last_ticks[0] = last_ticks[1];
            last_ticks[1] = last_ticks[2];
            last_ticks[2] = last_ticks[3];
            last_ticks[3] = last_ticks[4];
            last_ticks[4] = System.currentTimeMillis();
            long still_wait = (long) ((50 - (System.currentTimeMillis() - last_ticks[3])) * 2.5);
            tick_duration = System.currentTimeMillis() - last_ticks[3];
            if (still_wait > 0) {
                try {
                    Thread.sleep(still_wait);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void runTick(long tick) {
        InternalTask curr = null;
        LinkedList<InternalTask> requeue = new LinkedList<InternalTask>();
        prev_task_amount = 0;
        while (!list.isEmpty()) {
            curr = list.poll();
            prev_task_amount++;
            if (curr.nextExecuteTick() == tick) {
                try {
                    curr.run();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                curr.setExecuted(true);
                if (curr.getRepeating()) {
                    requeue.add(curr);
                    curr.setNextExecuteTick(tick
                            + curr.getRepeatingInterval());
                }
            } else {
                if (curr.nextExecuteTick() > tick)
                    requeue.add(curr);
            }
        }
        for (InternalTask r : requeue)
            list.add(r);
    }

    public void executeInMyCircle(InternalTask task) {
        task.setExecutor(this);
        task.setNextExecuteTick(tick + 1);
        list.add(task);
    }

    public void executeInMyCircleLater(InternalTask task, long delay) {
        task.setExecutor(this);
        task.setNextExecuteTick(tick + (delay <= 0 ? 1 : delay));
        list.add(task);
    }

    public void executeInMyCircleTimer(InternalTask task, long delay,
                                       long interval) {
        task.setExecutor(this);
        task.setNextExecuteTick(tick + (delay <= 0 ? 1 : delay));
        task.setRepeating(true);
        task.setRepeatingInterval(interval);
        list.add(task);
    }

    public void executeInSpigotCircle(InternalTask task) {
        task.setExecutor(this);
        new BukkitRunnable() {
            @Override
            public void run() {
                MCTiming timing = ArterionPlugin.getInstance().timing(task.getClass().getName());
                timing.startTiming();
                task.run();
                task.setExecuted(true);
                timing.stopTiming();
            }
        }.runTask(ArterionPlugin.getInstance());
    }

    public void executeInSpigotCircleLater(InternalTask task, long delay) {
        task.setExecutor(this);
        InternalTask sched = new InternalTask() {
            public void run() {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        MCTiming timing = ArterionPlugin.getInstance().timing(task.getClass().getName());
                        timing.startTiming();
                        task.run();
                        task.setExecuted(true);
                        timing.stopTiming();
                    }
                }.runTask(ArterionPlugin.getInstance());
            }
        };
        executeInMyCircleLater(sched, delay);
    }

    public void executeInSpigotCircleTimer(InternalTask task, long delay,
                                           long interval) {
        task.setExecutor(this);
        task.setRepeating(true);
        MCTiming timing = ArterionPlugin.getInstance().timing(task.getClass().getName());
        InternalTask sched = new InternalTask() {
            public void run() {
                if (!task.getRepeating()) {
                    cancel();
                    return;
                }
                try {
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            timing.startTiming();
                            task.run();
                            task.setExecuted(true);
                            timing.stopTiming();
                            if (!task.getRepeating()) return;
                        }
                    }.runTask(ArterionPlugin.getInstance());
                } catch (Exception e) {
                    //Ignore old tasks for error message! Only error for new tasks!
                    if (task.wasExecuted()) {
                        task.setRepeating(false);
                        return;
                    }
                    System.out.println("Warning: Could not register repeating task on server shutdown! " + task.getClass());
                }
                if (!task.getRepeating()) {
                    cancel();
                    return;
                }
            }
        };
        executeInMyCircleTimer(sched, delay, interval);
    }

    public double getTPS() {
        return tps;
    }

    public double getTickDuration() {
        return tick_duration;
    }

    public InternalScheduler startup() {
        if (!this.isAlive())
            start();
        return this;
    }

    public int getPrevTaskAmount() {
        return prev_task_amount;
    }

    public void shutdown() {
        this.shutdown = true;
    }
}
