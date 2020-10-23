package me.friwi.arterion.plugin.util.scheduler;

public abstract class InternalTask implements Runnable {
    private InternalScheduler executor = null;
    private boolean executed = false;
    private boolean repeat = false;
    private int delay = 0;
    private long net = 0;
    private long ri = 1;

    public InternalScheduler getExecutor() {
        return executor;
    }

    public void setExecutor(InternalScheduler executor) {
        this.executor = executor;
    }

    public void setExecuted(boolean executed) {
        this.executed = executed;
    }

    public boolean wasExecuted() {
        return executed;
    }

    public boolean getRepeating() {
        return repeat;
    }

    public void setRepeating(boolean repeat) {
        this.repeat = repeat;
    }

    public void cancel() {
        setRepeating(false);
    }

    public void setNextExecuteTick(long l) {
        net = l;
    }

    public long nextExecuteTick() {
        return net;
    }

    public long getRepeatingInterval() {
        return ri;
    }

    public void setRepeatingInterval(long i) {
        ri = i;
    }
}
