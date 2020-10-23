package me.friwi.arterion.plugin.listener;

import me.friwi.arterion.plugin.ArterionPlugin;
import me.friwi.arterion.plugin.util.database.Database;
import me.friwi.arterion.plugin.util.database.DatabaseTask;
import me.friwi.arterion.plugin.util.database.entity.DatabasePlayer;
import me.friwi.arterion.plugin.util.language.api.LanguageAPI;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerListPingEvent;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZonedDateTime;
import java.util.List;

public class ServerListPingListener implements Listener {
    private ArterionPlugin plugin;

    public ServerListPingListener(ArterionPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onServerListPing(ServerListPingEvent evt) {
        Object lock = new Object();

        //Process player
        String ip = evt.getAddress().toString();
        new DatabaseTask() {
            @Override
            public boolean performTransaction(Database db) {
                List<DatabasePlayer> dbp = db.findAllByColumn(DatabasePlayer.class, "lastIP", ip);
                if (dbp.size() == 0) {
                    if (plugin.isMaintenance())
                        evt.setMotd(LanguageAPI.getLanguage(LanguageAPI.DEFAULT_LANGUAGE).getTranslation(isPreRelease() ? "prelogin.ping.prerelease" : "prelogin.ping.maintenance").translate().getMessage().replace("\\n", "\n"));
                    else
                        evt.setMotd(LanguageAPI.getLanguage(LanguageAPI.DEFAULT_LANGUAGE).getTranslation("prelogin.ping.new").translate().getMessage().replace("\\n", "\n"));
                } else {
                    DatabasePlayer p = dbp.get(0);
                    if (plugin.isMaintenance())
                        evt.setMotd(LanguageAPI.getLanguage(p.getLocale()).getTranslation(isPreRelease() ? "prelogin.ping.prerelease" : "prelogin.ping.maintenance").translate().getMessage().replace("\\n", "\n"));
                    else {
                        String name = LanguageAPI.getLanguage(p.getLocale()).getTranslation(p.getRank().getRankTranslation()).translate().getMessage() + p.getName();
                        evt.setMotd(LanguageAPI.getLanguage(p.getLocale()).getTranslation("prelogin.ping.back").translate(name).getMessage().replace("\\n", "\n"));
                    }
                }
                return true;
            }

            @Override
            public void onTransactionCommitOrRollback(boolean committed) {
                synchronized (lock) {
                    lock.notifyAll();
                }
            }

            @Override
            public void onTransactionError() {
                evt.setMotd(LanguageAPI.getLanguage(LanguageAPI.DEFAULT_LANGUAGE).getTranslation("prelogin.ping.new").translate().getMessage().replace("\\n", "\n"));
                synchronized (lock) {
                    lock.notifyAll();
                }
            }
        }.execute();

        //Wait on our db operation to finish
        try {
            synchronized (lock) {
                lock.wait();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public boolean isPreRelease() {
        ZonedDateTime time = ZonedDateTime.of(LocalDateTime.of(2020, Month.AUGUST, 14, 18, 0), ArterionPlugin.TIME_ZONE);
        ZonedDateTime serverTime = time.withZoneSameInstant(ArterionPlugin.SERVER_TIME_ZONE);
        return (Instant.from(serverTime).getEpochSecond() * 1000) > System.currentTimeMillis();
    }
}
