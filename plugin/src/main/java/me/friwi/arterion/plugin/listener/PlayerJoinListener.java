package me.friwi.arterion.plugin.listener;

import me.friwi.arterion.plugin.ArterionPlugin;
import me.friwi.arterion.plugin.combat.classes.ClassEnum;
import me.friwi.arterion.plugin.permissions.Rank;
import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.player.ArterionPlayerUtil;
import me.friwi.arterion.plugin.player.reward.RewardUtil;
import me.friwi.arterion.plugin.shop.ProductUnlocker;
import me.friwi.arterion.plugin.stats.list.GlobalStats;
import me.friwi.arterion.plugin.ui.chat.FormattedChat;
import me.friwi.arterion.plugin.ui.gui.HeadCacheUtil;
import me.friwi.arterion.plugin.ui.mod.laby.LabyAddonRecommendation;
import me.friwi.arterion.plugin.ui.mod.laby.LabyCurrentGamemode;
import me.friwi.arterion.plugin.ui.mod.laby.LabyPlayerMiddleClickActions;
import me.friwi.arterion.plugin.util.database.Database;
import me.friwi.arterion.plugin.util.database.DatabaseTask;
import me.friwi.arterion.plugin.util.database.entity.DatabasePlayer;
import me.friwi.arterion.plugin.util.language.api.LanguageAPI;
import me.friwi.arterion.plugin.util.scheduler.InternalTask;
import me.friwi.arterion.plugin.util.time.TimeFormatUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.HashSet;

public class PlayerJoinListener implements Listener {
    private ArterionPlugin plugin;

    public PlayerJoinListener(ArterionPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent evt) {
        evt.setJoinMessage("");

        //Update local name caches
        HeadCacheUtil.updatePlayerName(evt.getPlayer().getUniqueId(), evt.getPlayer().getName());

        //Update player health bar to be 2 rows
        if (evt.getPlayer().getMaxHealth() != 40) {
            evt.getPlayer().setMaxHealth(40);
            if (evt.getPlayer().getHealth() > 0) evt.getPlayer().setHealth(40);
        } else {
            evt.getPlayer().setMaxHealth(40);
        }
        if (evt.getPlayer().getHealth() == Double.NaN || evt.getPlayer().getHealth() == Double.POSITIVE_INFINITY || evt.getPlayer().getHealth() == Double.NEGATIVE_INFINITY) {
            evt.getPlayer().setHealth(20);
        }

        //Vanish
        Player bukkitPlayer = evt.getPlayer();
        for (Player p : ArterionPlugin.getOnlinePlayers()) {
            if (!p.equals(bukkitPlayer)) {
                p.showPlayer(bukkitPlayer);
                ArterionPlayer otherAp = ArterionPlayerUtil.get(p);
                if (otherAp.isVanished()) bukkitPlayer.hidePlayer(p);
                else bukkitPlayer.showPlayer(p);
            }
        }

        //Process player
        new DatabaseTask() {
            DatabasePlayer databasePlayer;
            boolean firstJoin = false;

            @Override
            public boolean performTransaction(Database db) {
                databasePlayer = db.find(DatabasePlayer.class, bukkitPlayer.getUniqueId());
                String ip = evt.getPlayer().getAddress().getAddress().toString();
                if (databasePlayer == null) {
                    //Player was not online before, create him
                    firstJoin = true;
                    databasePlayer = new DatabasePlayer(bukkitPlayer.getUniqueId(), bukkitPlayer.getDisplayName(),
                            LanguageAPI.DEFAULT_LANGUAGE, Rank.NORMAL, ip, System.currentTimeMillis(), -1, plugin.getFormulaManager().PLAYER_BAG_INITIAL.evaluateInt(), 0, ClassEnum.NONE, 0, 0, 0, 0,
                            null, 0, 0, 0, null, false, null, 0, 0, 0, 0, 0, 0, new HashSet<>(), 0, 0);
                    db.save(databasePlayer);
                    databasePlayer = db.find(DatabasePlayer.class, bukkitPlayer.getUniqueId());
                } else if (!ip.equals(databasePlayer.getLastIP()) || !bukkitPlayer.getName().equals(databasePlayer.getName())) {
                    databasePlayer.setLastIP(ip);
                    databasePlayer.setName(bukkitPlayer.getName());
                    databasePlayer.setLastOnline(-1);
                    db.save(databasePlayer);
                }
                return true;
            }

            @Override
            public void onTransactionCommitOrRollback(boolean committed) {
                ArterionPlayer arterionPlayer = new ArterionPlayer(bukkitPlayer, databasePlayer);
                GlobalStats.getContext().getStatTracker().continueTracking(ArterionPlugin.getInstance().getExternalDatabase(), arterionPlayer, null);
                GlobalStats.getTopContext().getStatTracker().continueTracking(ArterionPlugin.getInstance().getExternalDatabase(), arterionPlayer, null);
                ArterionPlayerUtil.set(bukkitPlayer, arterionPlayer);
                ArterionPlugin.getOnlinePlayers().add(bukkitPlayer);
                if (firstJoin) {
                    LanguageAPI.broadcastMessage("server.join.first", bukkitPlayer.getName());
                }
                if (arterionPlayer.hasNewbieProtection()) {
                    FormattedChat.sendFormattedChat(arterionPlayer.getBukkitPlayer(), arterionPlayer.getTranslation("tutorial.suggest"));
                }
                plugin.getSchedulers().getMainScheduler().executeInSpigotCircle(new InternalTask() {
                    @Override
                    public void run() {
                        if (firstJoin) bukkitPlayer.teleport(plugin.getArterionConfig().spawn);
                        arterionPlayer.onJoin();

                        //Recommend laby addons
                        ArterionPlugin.getInstance().getSchedulers().getMainScheduler().executeInSpigotCircleLater(new InternalTask() {
                            @Override
                            public void run() {
                                LabyAddonRecommendation.recommendAddons(evt.getPlayer());
                                LabyCurrentGamemode.updateGameInfo(evt.getPlayer(), true, arterionPlayer.getTranslation("discord.gamemode"), System.currentTimeMillis(), 0);
                                LabyPlayerMiddleClickActions.playMiddleClickActions(evt.getPlayer());
                            }
                        }, 40l);

                        plugin.getGuildManager().onPlayerJoin(arterionPlayer);
                        plugin.getTablistManager().onPlayerJoin(arterionPlayer);
                        arterionPlayer.getPlayerScoreboard().setup();

                        //Update header and footer player count
                        plugin.getTablistManager().updateTablistHeaderFooter();

                        //Combat logging
                        plugin.getCombatLoggingHandler().handleLogin(arterionPlayer);

                        //Rewards
                        RewardUtil.onLogin(arterionPlayer);

                        //Product unlocks
                        ArterionPlugin.getInstance().getSchedulers().getDatabaseScheduler().executeInMyCircle(new InternalTask() {
                            @Override
                            public void run() {
                                ProductUnlocker.unlockAllProducts(arterionPlayer);
                            }
                        });

                        //Booster
                        if (ArterionPlugin.getInstance().getArterionConfig().boostExpire > System.currentTimeMillis()) {
                            String left = TimeFormatUtil.formatSeconds((ArterionPlugin.getInstance().getArterionConfig().boostExpire - System.currentTimeMillis()) / 1000);
                            arterionPlayer.sendTranslation("booster.used", ArterionPlugin.getInstance().getArterionConfig().currentBooster, left);
                        }
                    }
                });
            }

            @Override
            public void onTransactionError() {
                plugin.getSchedulers().getDatabaseScheduler().executeInSpigotCircle(new InternalTask() {
                    @Override
                    public void run() {
                        if (bukkitPlayer.isOnline())
                            bukkitPlayer.kickPlayer("Error while loading your database entry.");
                    }
                });
            }
        }.execute();
    }
}
