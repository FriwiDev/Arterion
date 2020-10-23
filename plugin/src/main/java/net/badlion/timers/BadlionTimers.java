package net.badlion.timers;

import net.badlion.timers.api.BadlionTimerApiImpl;
import net.badlion.timers.impl.NmsManager;
import net.badlion.timers.listeners.BadlionTimerListener;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.nio.charset.Charset;

public class BadlionTimers {

    public static final String CHANNEL_NAME = "badlion:timers";
    public static final Charset UTF_8_CHARSET = Charset.forName("UTF-8"); // Do not use Guava because of 1.7

    private BadlionTimerApiImpl timerApi;


    public void onEnable(JavaPlugin plugin) {
        NmsManager.init(plugin);

        this.timerApi = new BadlionTimerApiImpl(this);

        Bukkit.getServer().getMessenger().registerOutgoingPluginChannel(plugin, BadlionTimers.CHANNEL_NAME);

        Bukkit.getServer().getPluginManager().registerEvents(new BadlionTimerListener(this), plugin);
    }

    public BadlionTimerApiImpl getTimerApi() {
        return this.timerApi;
    }
}
