package me.friwi.arterion.plugin.world.villager;

import me.friwi.arterion.plugin.ArterionPlugin;
import me.friwi.arterion.plugin.guild.Guild;
import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.stats.list.GlobalStats;
import me.friwi.arterion.plugin.ui.gui.ItemGUI;
import me.friwi.arterion.plugin.ui.gui.NamedItemUtil;
import me.friwi.arterion.plugin.ui.gui.TextGUI;
import me.friwi.arterion.plugin.util.database.Database;
import me.friwi.arterion.plugin.util.database.DatabaseTask;
import me.friwi.arterion.plugin.util.database.entity.DatabaseGuild;
import me.friwi.arterion.plugin.util.database.entity.DatabasePlayer;
import me.friwi.arterion.plugin.util.scheduler.InternalTask;
import me.friwi.arterion.plugin.world.item.GuildblockItem;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.HashSet;

public class GuildVillagerInteractHandler implements VillagerInteractHandler {
    @Override
    public void handleInteract(ArterionPlayer ep) {
        if (ep.getGuild() != null) {
            ep.sendTranslation("guild.alreadyinguild");
            return;
        }
        if (ep.getHomeLocation() != null) {
            ep.sendTranslation("gui.guildblockbuy.home.exists");
            return;
        }
        ep.openGui(new TextGUI(ep, ep.getTranslation("gui.guildblockbuy.selectname.title"), () -> {
            return new String[]{ep.getTranslation("gui.guildblockbuy.selectname.desc")};
        }, name -> {
            if (!Guild.GUILD_NAME_PATTERN.matcher(name).matches()) {
                ep.sendTranslation("gui.guildblockbuy.selectname.invalidname");
                return;
            }
            if (ArterionPlugin.getInstance().getGuildManager().getGuildByName(name) != null) {
                ep.sendTranslation("gui.guildblockbuy.selectname.exists");
                return;
            }
            ep.closeGui(true);
            ArterionPlugin.getInstance().getSchedulers().getMainScheduler().executeInMyCircleLater(new InternalTask() {
                @Override
                public void run() {
                    ep.openGui(new TextGUI(ep, ep.getTranslation("gui.guildblockbuy.selecttag.title"), () -> {
                        return new String[]{ep.getTranslation("gui.guildblockbuy.selecttag.desc")};
                    }, tag -> {
                        if (!Guild.GUILD_TAG_PATTERN.matcher(tag).matches()) {
                            ep.sendTranslation("gui.guildblockbuy.selecttag.invalidtag");
                            return;
                        }
                        if (ArterionPlugin.getInstance().getGuildManager().getGuildByTag(tag) != null) {
                            ep.sendTranslation("gui.guildblockbuy.selecttag.exists");
                            return;
                        }
                        ep.closeGui(true);
                        ArterionPlugin.getInstance().getSchedulers().getMainScheduler().executeInMyCircleLater(new InternalTask() {
                            @Override
                            public void run() {
                                ep.openGui(new ItemGUI(ep, ep.getTranslation("gui.guildblockbuy.choose"), () -> {
                                    ItemStack[] stacks = new ItemStack[9];
                                    stacks[4] = NamedItemUtil.create(Material.ENDER_PORTAL_FRAME, 1,
                                            ep.getTranslation("gui.guildblockbuy.item.name"),
                                            ep.getTranslation("gui.guildblockbuy.item.desc", ArterionPlugin.getInstance().getFormulaManager().GUILD_GUILDBLOCK_FEE.evaluateInt(ep) / 100F, name, tag).split("\n"));
                                    return stacks;
                                }, ((clickType, in) -> {
                                    ep.closeGui();
                                    final int[] space = {ep.getBukkitPlayer().getInventory().firstEmpty()};
                                    if (space[0] == -1) {
                                        ep.sendTranslation("gui.guildblockbuy.nospace");
                                        return;
                                    }
                                    if (ArterionPlugin.getInstance().getGuildManager().getGuildByName(name) != null) {
                                        ep.sendTranslation("gui.guildblockbuy.selectname.exists");
                                        return;
                                    }
                                    if (ArterionPlugin.getInstance().getGuildManager().getGuildByTag(tag) != null) {
                                        ep.sendTranslation("gui.guildblockbuy.selecttag.exists");
                                        return;
                                    }
                                    if (ep.getHomeLocation() != null) {
                                        ep.sendTranslation("gui.guildblockbuy.home.exists");
                                        return;
                                    }
                                    ep.getBagMoneyBearer().addMoney(-ArterionPlugin.getInstance().getFormulaManager().GUILD_GUILDBLOCK_FEE.evaluateInt(ep), success -> {
                                        if (!success) {
                                            ep.sendTranslation("gui.guildblockbuy.nomoney");
                                            return;
                                        }
                                        if (ArterionPlugin.getInstance().getGuildManager().getGuildByName(name) != null) {
                                            ep.sendTranslation("gui.guildblockbuy.selectname.exists");
                                            ep.getBagMoneyBearer().addMoney(ArterionPlugin.getInstance().getFormulaManager().GUILD_GUILDBLOCK_FEE.evaluateInt(ep), success1 -> {
                                            });
                                            return;
                                        }
                                        if (ArterionPlugin.getInstance().getGuildManager().getGuildByTag(tag) != null) {
                                            ep.sendTranslation("gui.guildblockbuy.selecttag.exists");
                                            ep.getBagMoneyBearer().addMoney(ArterionPlugin.getInstance().getFormulaManager().GUILD_GUILDBLOCK_FEE.evaluateInt(ep), success1 -> {
                                            });
                                            return;
                                        }
                                        //Create guild
                                        long prot = ArterionPlugin.getInstance().getFormulaManager().FIGHT_PROTECTION_INITIAL.evaluateInt();
                                        new DatabaseTask() {
                                            DatabaseGuild dbg;

                                            @Override
                                            public boolean performTransaction(Database db) {
                                                dbg = new DatabaseGuild(name, tag, System.currentTimeMillis(), DatabaseGuild.NOT_DELETED, DatabaseGuild.NOT_IN_MINUS_BALANCE, DatabaseGuild.OBSIDIAN_NOT_CALCULATED, 0, false, false, null, 0, 0, 0, 0, ep.getPersistenceHolder(), new HashSet<DatabasePlayer>(), new HashSet<DatabasePlayer>(), System.currentTimeMillis() + prot, false, new HashMap<>(), 0);
                                                db.save(dbg);
                                                return true;
                                            }

                                            @Override
                                            public void onTransactionCommitOrRollback(boolean committed) {
                                                if (committed) {
                                                    Guild guild = new Guild(dbg);
                                                    GlobalStats.getContext().getStatTracker().beginTracking(guild, null);
                                                    GlobalStats.getTopContext().getStatTracker().beginTracking(guild, null);
                                                    ArterionPlugin.getInstance().getGuildManager().onGuildCreate(guild, ep);
                                                    ArterionPlugin.getInstance().getSchedulers().getMainScheduler().executeInSpigotCircle(new InternalTask() {
                                                        @Override
                                                        public void run() {
                                                            ItemStack item = new GuildblockItem(guild).toItemStack();
                                                            if (ep.getBukkitPlayer().getInventory().firstEmpty() == -1) {
                                                                ep.getBukkitPlayer().getWorld().dropItem(ep.getBukkitPlayer().getLocation(), ep.getBukkitPlayer().getInventory().getItem(space[0]));
                                                            } else {
                                                                space[0] = ep.getBukkitPlayer().getInventory().firstEmpty();
                                                            }
                                                            ep.getBukkitPlayer().getInventory().setItem(space[0], item);
                                                            ep.sendTranslation("gui.guildblockbuy.success");
                                                        }
                                                    });
                                                } else {
                                                    ep.sendTranslation("gui.guildblockbuy.dberror");
                                                }
                                            }

                                            @Override
                                            public void onTransactionError() {
                                                ep.sendTranslation("gui.guildblockbuy.dberror");
                                            }
                                        }.execute();
                                    });
                                })));
                            }
                        }, 1l);
                    }));
                }
            }, 1l);
        }));
    }
}
