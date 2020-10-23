package net.badlion.timers.api;

import net.badlion.timers.BadlionTimers;
import net.badlion.timers.impl.BadlionTimerImpl;
import net.badlion.timers.impl.NmsManager;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class BadlionTimerApiImpl extends BadlionTimerApi {

    private final BadlionTimers plugin;
    private final AtomicInteger idGenerator;

    public BadlionTimerApiImpl(BadlionTimers plugin) {
        this.plugin = plugin;
        this.idGenerator = new AtomicInteger(1);

        BadlionTimerApi.instance = this;
    }

    @Override
    public BadlionTimer createTickTimer(ItemStack item, boolean repeating, long time) {
        return this.createTickTimer(null, item, repeating, time);
    }

    @Override
    public BadlionTimer createTickTimer(String name, ItemStack item, boolean repeating, long time) {
        BadlionTimerImpl timer = new BadlionTimerImpl(this.plugin, this.idGenerator.getAndIncrement(), name, item, repeating, time);

        return timer;
    }

    @Override
    public BadlionTimer createTimeTimer(ItemStack item, boolean repeating, long time, TimeUnit timeUnit) {
        return this.createTimeTimer(null, item, repeating, time, timeUnit);
    }

    @Override
    public BadlionTimer createTimeTimer(String name, ItemStack item, boolean repeating, long time, TimeUnit timeUnit) {
        BadlionTimerImpl timer = new BadlionTimerImpl(this.plugin, this.idGenerator.getAndIncrement(), name, item, repeating, time, timeUnit);

        return timer;
    }

    @Override
    public void removeTimer(BadlionTimer timer) {
        // Failsafe
        if (timer instanceof BadlionTimerImpl) {
            BadlionTimerImpl timerImpl = (BadlionTimerImpl) timer;
        }

        timer.clearReceivers();
    }

    @Override
    public void clearTimers(Player player) {
        NmsManager.sendPluginMessage(player, BadlionTimers.CHANNEL_NAME, "REMOVE_ALL_TIMERS|{}".getBytes(BadlionTimers.UTF_8_CHARSET));
    }
}
