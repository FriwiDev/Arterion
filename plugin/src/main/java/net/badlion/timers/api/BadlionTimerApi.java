package net.badlion.timers.api;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.concurrent.TimeUnit;

public abstract class BadlionTimerApi {
    static BadlionTimerApi instance;

    /**
     * Get the API instance.
     *
     * @return The API instance
     */
    public static BadlionTimerApi getInstance() {
        return BadlionTimerApi.instance;
    }

    /**
     * Create a new timer and register it into the API.
     * <p>
     * A timer will automatically handle synchronizing with its receivers,
     * and will repeat itself if it's mark as repeating. If not, it'll be
     * automatically removed from the API.
     *
     * @param item      Item to show in the client
     * @param repeating {@code true} if the timer is repeating, {@code false} otherwise
     * @param time      Countdown time, in ticks (20 per seconds)
     * @return The new timer instance
     */
    public abstract BadlionTimer createTickTimer(ItemStack item, boolean repeating, long time);

    /**
     * Create a new timer and register it into the API.
     * <p>
     * A timer will automatically handle synchronizing with its receivers,
     * and will repeat itself if it's mark as repeating. If not, it'll be
     * automatically removed from the API.
     *
     * @param name      Name to show in the client
     * @param item      Item to show in the client
     * @param repeating {@code true} if the timer is repeating, {@code false} otherwise
     * @param time      Countdown time, in ticks (20 per seconds)
     * @return The new timer instance
     */
    public abstract BadlionTimer createTickTimer(String name, ItemStack item, boolean repeating, long time);

    /**
     * Create a new timer and register it into the API.
     * <p>
     * A timer will automatically handle synchronizing with its receivers,
     * and will repeat itself if it's mark as repeating. If not, it'll be
     * automatically removed from the API.
     *
     * @param item      Item to show in the client
     * @param repeating {@code true} if the timer is repeating, {@code false} otherwise
     * @param time      Countdown time
     * @param timeUnit  Countdown time unit
     * @return The new timer instance
     */
    public abstract BadlionTimer createTimeTimer(ItemStack item, boolean repeating, long time, TimeUnit timeUnit);

    /**
     * Create a new timer and register it into the API.
     * <p>
     * A timer will automatically handle synchronizing with its receivers,
     * and will repeat itself if it's mark as repeating. If not, it'll be
     * automatically removed from the API.
     *
     * @param name      Name to show in the client
     * @param item      Item to show in the client
     * @param repeating {@code true} if the timer is repeating, {@code false} otherwise
     * @param time      Countdown time
     * @param timeUnit  Countdown time unit
     * @return The new timer instance
     */
    public abstract BadlionTimer createTimeTimer(String name, ItemStack item, boolean repeating, long time, TimeUnit timeUnit);

    /**
     * Remove a timer from the API, disabling all API features about it.
     *
     * @param timer The timer instance to remove
     */
    public abstract void removeTimer(BadlionTimer timer);

    /**
     * Clear all timers for a player.
     *
     * @param player The player instance to remove
     */
    public abstract void clearTimers(Player player);
}
