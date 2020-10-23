package me.friwi.arterion.plugin.listener;

import me.friwi.arterion.plugin.ArterionPlugin;
import me.friwi.arterion.plugin.sanctions.SanctionType;
import me.friwi.arterion.plugin.shop.ProductUnlocker;
import me.friwi.arterion.plugin.util.database.Database;
import me.friwi.arterion.plugin.util.database.DatabaseTask;
import me.friwi.arterion.plugin.util.database.entity.DatabasePlayer;
import me.friwi.arterion.plugin.util.database.entity.DatabaseSanction;
import me.friwi.arterion.plugin.util.language.api.LanguageAPI;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class AsyncPlayerPreLoginListener implements Listener {
    private ArterionPlugin plugin;

    public AsyncPlayerPreLoginListener(ArterionPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onAsyncPlayerPreLogin(AsyncPlayerPreLoginEvent evt) {
        Object lock = new Object();

        //Do not allow login on shutdown
        if (ArterionPlugin.getInstance().shutdownTime - 90000 < System.currentTimeMillis()) {
            evt.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER,
                    LanguageAPI.getLanguage(LanguageAPI.DEFAULT_LANGUAGE).getTranslation("command.stop.closed")
                            .translate(ArterionPlugin.getInstance().shutdownReason).getMessage());
            return;
        }

        //Process player
        UUID uuid = evt.getUniqueId();
        new DatabaseTask() {
            @Override
            public boolean performTransaction(Database db) {
                //Check for sanctions
                DatabaseSanction banSanction = plugin.getSanctionManager().getCurrentSanction(uuid, SanctionType.BAN);
                if (banSanction != null) {
                    LocalDateTime localTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(banSanction.getExpires()), ArterionPlugin.SERVER_TIME_ZONE);
                    String time = DateTimeFormatter.ISO_DATE_TIME.format(localTime.atZone(ArterionPlugin.SERVER_TIME_ZONE).withZoneSameInstant(ArterionPlugin.TIME_ZONE)).replaceFirst("T", " ");
                    evt.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, "\247cYou are banned until \2477" + time + "\247c!\n" +
                            "\247cReason: \2477" + banSanction.getReason());
                } else {
                    OfflinePlayer p = Bukkit.getOfflinePlayer(uuid);
                    boolean isOp = p == null ? false : p.isOp();
                    boolean full = ArterionPlugin.getOnlinePlayers().size() >= Bukkit.getMaxPlayers();
                    if ((full || plugin.isMaintenance()) && !isOp) {
                        //Check for permissions
                        DatabasePlayer databasePlayer = db.find(DatabasePlayer.class, uuid);
                        if (databasePlayer != null) {
                            if (plugin.isMaintenance() && !databasePlayer.getRank().isTeam()) {
                                evt.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, LanguageAPI.getLanguage(databasePlayer.getLocale()).getTranslation("prelogin.maintenance").translate().getMessage());
                            } else if (full && !databasePlayer.getRank().isPremium() && !ProductUnlocker.hasPremiumUnlock(uuid)) {
                                evt.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, LanguageAPI.getLanguage(databasePlayer.getLocale()).getTranslation("prelogin.full").translate().getMessage());
                            } else {
                                evt.allow();
                            }
                        } else {
                            if (plugin.isMaintenance()) {
                                evt.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, LanguageAPI.getLanguage(LanguageAPI.DEFAULT_LANGUAGE).getTranslation("prelogin.maintenance").translate().getMessage());
                            } else if (!ProductUnlocker.hasPremiumUnlock(uuid)) {
                                evt.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, LanguageAPI.getLanguage(LanguageAPI.DEFAULT_LANGUAGE).getTranslation("prelogin.full").translate().getMessage());
                            } else {
                                evt.allow();
                            }
                        }
                    } else {
                        evt.allow();
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
                evt.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, "Internal error, please try again");
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
}
