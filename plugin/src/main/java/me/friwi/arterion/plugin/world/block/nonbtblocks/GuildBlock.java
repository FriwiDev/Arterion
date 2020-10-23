package me.friwi.arterion.plugin.world.block.nonbtblocks;

import me.friwi.arterion.plugin.ArterionPlugin;
import me.friwi.arterion.plugin.combat.gamemode.artefact.Artefact;
import me.friwi.arterion.plugin.economy.TransferDirection;
import me.friwi.arterion.plugin.guild.Guild;
import me.friwi.arterion.plugin.guild.GuildUpgradeEnum;
import me.friwi.arterion.plugin.guild.GuildUpgradeLevel;
import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.player.ArterionPlayerUtil;
import me.friwi.arterion.plugin.ui.gui.*;
import me.friwi.arterion.plugin.ui.hotbar.HotbarTitleMessageCard;
import me.friwi.arterion.plugin.ui.invite.InvitationHandler;
import me.friwi.arterion.plugin.util.database.Database;
import me.friwi.arterion.plugin.util.database.DatabaseObjectTask;
import me.friwi.arterion.plugin.util.database.DatabaseTask;
import me.friwi.arterion.plugin.util.database.entity.DatabaseGuild;
import me.friwi.arterion.plugin.util.database.entity.DatabasePlayer;
import me.friwi.arterion.plugin.util.scheduler.InternalTask;
import me.friwi.arterion.plugin.world.item.GuildblockItem;
import me.friwi.arterion.plugin.world.region.GuildRegion;
import me.friwi.arterion.plugin.world.region.Region;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class GuildBlock extends SpecialBlock {
    private Guild owner;
    private Map<ArterionPlayer, Integer> guiPage = new HashMap<>();
    private int invites = 0;
    private boolean noInteract = false;

    public GuildBlock(Location loc, Guild owner) {
        super(loc);
        this.owner = owner;
    }

    public Guild getOwner() {
        return owner;
    }

    @Override
    public boolean onInteract(ArterionPlayer player, Action action) {
        if (noInteract) {
            player.sendTranslation("block.guildblock.wait");
            return false;
        }
        if (player.isArtefactCarrier() && player.getGuild() != null && player.getGuild().equals(owner)) {
            player.setArtefactCarrier(null);
            owner.setHasArtefact(true, succ -> {
            });
            return false;
        }
        if (action == null || action == Action.RIGHT_CLICK_BLOCK) {
            player.closeGui();
            guiPage.put(player, 0);
            if (player.getPersistenceHolder().equals(this.owner.getLeader())) {
                onInteract(player, true, true);
            } else if (this.owner.getOfficers().contains(player.getPersistenceHolder())) {
                onInteract(player, false, true);
            } else if (this.owner.getMembers().contains(player.getPersistenceHolder())) {
                onInteract(player, false, false);
            } else {
                player.sendTranslation("block.guildblock.noperm");
            }
        } else {
            if (action == Action.LEFT_CLICK_BLOCK
                    && owner.isInLocalFight()
                    && owner.getLocalFight().getDefender().equals(owner)
                    && player.getGuild() != null
                    && owner.getLocalFight().getAttacker().equals(player.getGuild())) {
                owner.getLocalFight().attackBlock(player);
            }
        }
        return false;
    }

    private void onInteract(ArterionPlayer player, boolean leader, boolean officer) {
        boolean mod = player.usesMod();
        int stackCount = mod ? 18 : 18;
        int goldBoostInd = mod ? 0 : 0;
        int xpBoostInd = mod ? 2 : 1;
        int tagInd = mod ? 4 : 3;
        int upgradesInd = mod ? 6 : 4;
        int membersInd = mod ? 8 : 5;
        int goldInd = mod ? 9 : 7;
        int vaultInd = mod ? 11 : 8;
        int searchInd = mod ? 13 : 9;
        int breakInd = mod ? 15 : 13;
        int quitInd = mod ? 17 : 17;

        player.openGui(new ItemGUI(player, player.getTranslation("gui.guild.title"), () -> {
            ItemStack[] stacks = new ItemStack[stackCount];
            if (!owner.isInLocalFight()) {
                if (leader || officer) {
                    stacks[goldBoostInd] = NamedItemUtil.create(Material.INK_SACK, 1, owner.isGoldShare() ? (byte) 10 : (byte) 8, player.getTranslation(owner.isGoldShare() ? "gui.guild.disablegold.name" : "gui.guild.enablegold.name"),
                            player.getTranslation("gui.guild.goldboost", ArterionPlugin.getInstance().getFormulaManager().GUILD_GOLD_MULTIPLIER.evaluateFloat(owner)));
                    stacks[xpBoostInd] = NamedItemUtil.create(Material.INK_SACK, 1, owner.isXpShare() ? (byte) 10 : (byte) 8, player.getTranslation(owner.isXpShare() ? "gui.guild.disablexp.name" : "gui.guild.enablexp.name"),
                            player.getTranslation("gui.guild.xpboost", ArterionPlugin.getInstance().getFormulaManager().GUILD_XP_MULTIPLIER.evaluateFloat(owner)));
                } else {
                    stacks[goldBoostInd] = NamedItemUtil.create(Material.INK_SACK, 1, owner.isGoldShare() ? (byte) 10 : (byte) 8, player.getTranslation(!owner.isGoldShare() ? "gui.guild.disabledgold.name" : "gui.guild.enabledgold.name"),
                            player.getTranslation("gui.guild.goldboost", ArterionPlugin.getInstance().getFormulaManager().GUILD_GOLD_MULTIPLIER.evaluateFloat(owner)));
                    stacks[xpBoostInd] = NamedItemUtil.create(Material.INK_SACK, 1, owner.isXpShare() ? (byte) 10 : (byte) 8, player.getTranslation(!owner.isXpShare() ? "gui.guild.disabledxp.name" : "gui.guild.enabledxp.name"),
                            player.getTranslation("gui.guild.xpboost", ArterionPlugin.getInstance().getFormulaManager().GUILD_XP_MULTIPLIER.evaluateFloat(owner)));
                }
                if (leader)
                    stacks[tagInd] = NamedItemUtil.create(Material.NAME_TAG, player.getTranslation("gui.guild.tag"));
                stacks[membersInd] = NamedItemUtil.create(Material.BOOK_AND_QUILL, player.getTranslation(leader ? "gui.guild.membersandinv" : "gui.guild.members"));
            }
            stacks[goldInd] = NamedItemUtil.create(Material.GOLD_BLOCK, player.getTranslation("gui.guild.vault"));
            if (officer) {
                stacks[searchInd] = NamedItemUtil.create(Material.COMPASS, player.getTranslation("gui.guild.search"), player.getTranslation("gui.guild.search.price", ArterionPlugin.getInstance().getFormulaManager().GUILD_SEARCH_PRICE.evaluateInt(owner) / 100f));
            } else if (owner.isInLocalFight()) {
                stacks[searchInd] = NamedItemUtil.create(Material.BARRIER, player.getTranslation("gui.guild.infight"), player.getTranslation("gui.guild.infight.desc"));
            }

            if (!owner.isInLocalFight()) {
                if (leader) {
                    stacks[breakInd] = NamedItemUtil.create(Material.GOLD_PICKAXE, player.getTranslation("gui.guild.break"));
                    stacks[quitInd] = NamedItemUtil.create(Material.INK_SACK, 1, (byte) 1, player.getTranslation("gui.guild.disband"));
                } else {
                    stacks[quitInd] = NamedItemUtil.create(Material.WOOD_DOOR, 1, player.getTranslation("gui.guild.leave"));
                }
                stacks[upgradesInd] = NamedItemUtil.create(Material.REDSTONE, player.getTranslation("gui.guild.upgrades"));
            }
            stacks[vaultInd] = NamedItemUtil.create(Material.ENDER_CHEST, player.getTranslation("gui.guild.guildvault"));
            return stacks;
        }, (clickType, i) -> {
            if (i == vaultInd) {
                player.closeGui();
                getOwner().getVault().show(player.getBukkitPlayer());
            } else if (i == upgradesInd && !owner.isInLocalFight()) {
                showUpgradesDialog(player);
            } else if (i == goldBoostInd) {
                if (!player.getPersistenceHolder().equals(this.owner.getLeader()) && !this.owner.getOfficers().contains(player.getPersistenceHolder()))
                    return;
                owner.setGoldShare(!owner.isGoldShare(), suc -> {
                    owner.sendTranslation(owner.isGoldShare() ? "gui.guild.goldenabled" : "gui.guild.golddisabled");
                    ArterionPlugin.getInstance().getSchedulers().getMainScheduler().executeInSpigotCircle(new InternalTask() {
                        @Override
                        public void run() {
                            updateGui(0);
                        }
                    });
                });
            } else if (i == xpBoostInd) {
                if (!player.getPersistenceHolder().equals(this.owner.getLeader()) && !this.owner.getOfficers().contains(player.getPersistenceHolder()))
                    return;
                owner.setXpShare(!owner.isXpShare(), suc -> {
                    owner.sendTranslation(owner.isXpShare() ? "gui.guild.xpenabled" : "gui.guild.xpdisabled");
                    ArterionPlugin.getInstance().getSchedulers().getMainScheduler().executeInSpigotCircle(new InternalTask() {
                        @Override
                        public void run() {
                            updateGui(0);
                        }
                    });
                });
            } else if (i == tagInd) {
                if (!player.getPersistenceHolder().equals(this.owner.getLeader())) return;
                player.openGui(new TextGUI(player, player.getTranslation("gui.guildblockbuy.selecttag.title"), () -> {
                    return new String[]{player.getTranslation("gui.guildblockbuy.selecttag.desc")};
                }, tag -> {
                    if (!Guild.GUILD_TAG_PATTERN.matcher(tag).matches()) {
                        player.sendTranslation("gui.guildblockbuy.selecttag.invalidtag");
                        return;
                    }
                    if (tag.equals(owner.getTag())) {
                        owner.sendTranslation("gui.guild.tagchanged", tag);
                        return;
                    }
                    if (ArterionPlugin.getInstance().getGuildManager().getGuildByTag(tag) != null) {
                        player.sendTranslation("gui.guildblockbuy.selecttag.exists");
                        return;
                    }
                    owner.setTag(tag, succ -> {
                        ArterionPlugin.getInstance().getSchedulers().getMainScheduler().executeInSpigotCircle(new InternalTask() {
                            @Override
                            public void run() {
                                updateGui(0);
                            }
                        });
                        if (succ) owner.sendTranslation("gui.guild.tagchanged", tag);
                    });
                }));
            } else if (i == membersInd) {
                showMembersDialog(player);
            } else if (i == goldInd) {
                showVaultDialog(player);
            } else if (i == searchInd) {
                boolean officer1 = player.getPersistenceHolder().equals(this.owner.getLeader()) || owner.getOfficers().contains(player.getPersistenceHolder());
                if (!officer1) {
                    player.closeGui();
                    return;
                }
                player.openGui(new ConfirmItemGUI(player, () -> {
                    //Perform search
                    if (player.getPersistenceHolder().equals(this.owner.getLeader()) || this.owner.getOfficers().contains(player.getPersistenceHolder())) {
                        int fee = ArterionPlugin.getInstance().getFormulaManager().GUILD_SEARCH_PRICE.evaluateInt(owner);
                        owner.getMoneyBearer().addMoney(-fee, success -> {
                            if (success) {
                                owner.sendTranslation("line");
                                owner.sendTranslation("gui.guild.search.header");
                                boolean foundOne = false;
                                for (Player p : ArterionPlugin.getOnlinePlayers()) {
                                    if (p.getGameMode() == GameMode.SPECTATOR || p.getGameMode() == GameMode.CREATIVE)
                                        continue;
                                    ArterionPlayer ap = ArterionPlayerUtil.get(p);
                                    if (ap.isVanished()) continue;
                                    if (ap.getRegion() instanceof GuildRegion) {
                                        if (owner.getRegion() != null && owner.getRegion().equals(ap.getRegion())) {
                                            if (ap.getGuild() == null || !ap.getGuild().equals(owner)) {
                                                //Enemy found
                                                foundOne = true;
                                                Location loc = ap.getBukkitPlayer().getLocation();
                                                owner.sendTranslation("gui.guild.search.match", ap, loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
                                            }
                                        }
                                    }
                                }
                                if (!foundOne) owner.sendTranslation("gui.guild.search.nomatch");
                                owner.sendTranslation("line");
                            } else {
                                player.sendTranslation("gui.guild.search.fee", fee / 100f);
                            }
                        });
                    }
                }, () -> {
                    onInteract(player, null);
                    return;
                }));
            } else if (i == breakInd) {
                player.openGui(new ConfirmItemGUI(player, () -> {
                    //Pickup guild block
                    if (player.getPersistenceHolder().equals(this.owner.getLeader())) {
                        int cd = ArterionPlugin.getInstance().getFormulaManager().GUILD_CLAIM_COOLDOWN.evaluateInt(owner);
                        if (System.currentTimeMillis() - cd * 60 * 1000 < owner.getLastReclaim()) {
                            //Still on cooldown
                            player.sendTranslation("gui.guild.pickup.cooldown", (cd * 60000 - System.currentTimeMillis() + owner.getLastReclaim()) / 60000);
                            return;
                        }
                        final int[] space = {player.getBukkitPlayer().getInventory().firstEmpty()};
                        if (space[0] == -1) {
                            player.sendTranslation("gui.guild.pickup.nospace");
                            return;
                        }
                        player.closeGui();
                        //Delete region
                        Region found = null;
                        for (Region r : ArterionPlugin.getInstance().getRegionManager().all()) {
                            if (r instanceof GuildRegion) {
                                if (((GuildRegion) r).getGuild().equals(owner)) {
                                    found = r;
                                    break;
                                }
                            }
                        }
                        owner.setRegion(null);
                        //Delete guild block
                        ArterionPlugin.getInstance().getSchedulers().getMainScheduler().executeInSpigotCircle(new InternalTask() {
                            @Override
                            public void run() {
                                owner.getHomeLocation().getBlock().setType(Material.AIR);
                            }
                        });
                        ArterionPlugin.getInstance().getSpecialBlockManager().remove(owner.getHomeLocation());
                        //Artefact
                        if (owner.hasArtefact()) Artefact.reset();
                        //Set home location to null
                        owner.setHomeLocation(null, suc -> {
                        });
                        player.sendTranslation("gui.guild.pickup.inprogress");
                        if (found != null) {
                            ArterionPlugin.getInstance().getRegionManager().unRegisterRegionParallel(found, GuildRegion.REGISTER_BATCH_SIZE, () -> {
                                //Inform guild
                                owner.sendTranslation("gui.guild.pickup");
                                //Give guild block
                                space[0] = player.getBukkitPlayer().getInventory().firstEmpty();
                                if (space[0] == -1) {
                                    space[0] = 0;
                                    player.getBukkitPlayer().getWorld().dropItem(player.getBukkitPlayer().getLocation(), player.getBukkitPlayer().getInventory().getItem(space[0]));
                                    player.getBukkitPlayer().getInventory().setItem(space[0], new GuildblockItem(owner).toItemStack());
                                    player.sendTranslation("gui.guild.pickup.dropped");
                                } else {
                                    player.getBukkitPlayer().getInventory().setItem(space[0], new GuildblockItem(owner).toItemStack());
                                }
                            });
                        }
                        owner.forceAllGuisClose();
                    }
                    player.closeGui();
                }, () -> {
                    onInteract(player, null);
                    return;
                }));
            } else if (i == quitInd) {
                player.openGui(new ConfirmItemGUI(player, () -> {
                    //Disband or leave guild
                    if (player.getPersistenceHolder().equals(this.owner.getLeader())) {
                        player.closeGui();
                        if (owner.getMoneyBearer().getCachedMoney() < 0) {
                            player.sendTranslation("gui.guild.disband.negative");
                            return;
                        } else if (owner.getMoneyBearer().getCachedMoney() >= 0) {
                            owner.getMoneyBearer().transferMoney(owner.getMoneyBearer().getCachedMoney(), player.getBagMoneyBearer(), success -> {
                                if (success) {
                                    ArterionPlugin.getInstance().getSchedulers().getMainScheduler().executeInSpigotCircle(new InternalTask() {
                                        @Override
                                        public void run() {
                                            owner.forceAllGuisClose();
                                            //Remove players
                                            owner.deleteGuild(false);
                                        }
                                    });
                                } else {
                                    player.sendTranslation("gui.guild.disband.vaultnotcleared");
                                }
                            });
                        }
                    } else {
                        if (this.owner.getDeleted() != DatabaseGuild.NOT_DELETED) {
                            player.closeGui();
                            return;
                        }
                        player.closeGui();
                        owner.removeMember(player.getPersistenceHolder(), succ -> {
                            player.setGuild(null);
                            owner.onPlayerLeave(player, true);
                            player.sendTranslation("gui.guild.left");
                            owner.sendTranslation("gui.guild.otherleft", player);
                            ArterionPlugin.getInstance().getSchedulers().getMainScheduler().executeInSpigotCircle(new InternalTask() {
                                @Override
                                public void run() {
                                    player.getBukkitPlayer().teleport(ArterionPlugin.getInstance().getArterionConfig().spawn);
                                    player.updateRegion(ArterionPlugin.getInstance().getArterionConfig().spawn);
                                }
                            });
                        });
                    }
                    updateAllGuis();
                }, () -> {
                    onInteract(player, null);
                    return;
                }));
            }
        }, () -> guiPage.remove(player)));
    }

    private void showUpgradesDialog(ArterionPlayer player) {
        player.closeGui();
        if (owner.isInLocalFight()) return;
        guiPage.put(player, 3);
        boolean leader = player.getPersistenceHolder().equals(this.owner.getLeader());
        UUID[] requiredHeads = new UUID[1];
        requiredHeads[0] = HeadCacheUtil.ARROW_LEFT;
        if (HeadCacheUtil.countMissingHeads(requiredHeads) > 0) {
            player.scheduleHotbarCard(new HotbarTitleMessageCard(2000, player, "hotbar.wait"));
        }
        HeadCacheUtil.supplyCachedHeadSync(heads -> {
            player.openGui(new ItemGUI(player, player.getTranslation("gui.guild.upgrades.title"), () -> {
                ItemStack[] stacks = new ItemStack[18];
                stacks[2] = GuildUpgradeEnum.VAULT.getIcon(owner, player.getLanguage());
                stacks[4] = GuildUpgradeEnum.OFFICER.getIcon(owner, player.getLanguage());
                stacks[6] = GuildUpgradeEnum.REGION.getIcon(owner, player.getLanguage());
                stacks[9] = NamedItemUtil.modify(heads[0].getHead(), player.getTranslation("gui.guild.upgrades.back"));
                return stacks;
            }, (clickType, i) -> {
                if (i == 9) {
                    onInteract(player, null);
                    return;
                } else {
                    if (!player.getBukkitPlayer().getUniqueId().equals(owner.getLeader().getUuid())) {
                        return;
                    }
                    if (owner.isInLocalFight()) {
                        player.closeGui();
                        return;
                    }
                    GuildUpgradeEnum upgrade;
                    switch (i) {
                        case 2:
                            upgrade = GuildUpgradeEnum.VAULT;
                            break;
                        case 4:
                            upgrade = GuildUpgradeEnum.OFFICER;
                            break;
                        case 6:
                            upgrade = GuildUpgradeEnum.REGION;
                            break;
                        default:
                            return;
                    }
                    if (owner.getUpgradeLevel(upgrade).ordinal() >= upgrade.getMaxLevel().ordinal()) return;
                    player.closeGui();
                    player.openGui(new ConfirmItemGUI(player, () -> {
                        GuildUpgradeLevel newLevel = owner.getUpgradeLevel(upgrade).next();
                        int price = upgrade.getPrice(newLevel);
                        if (owner.getMoneyBearer().getCachedMoney() < price) {
                            player.sendTranslation("gui.guild.upgrades.nofunds");
                            return;
                        }
                        player.closeGui();
                        player.scheduleHotbarCard(new HotbarTitleMessageCard(2000, player, "hotbar.wait"));
                        owner.getMoneyBearer().transferMoney(price, null, succ -> {
                            if (!succ) {
                                player.sendTranslation("gui.guild.upgrades.nofunds");
                            } else {
                                owner.upgrade(upgrade, newLevel, s -> {
                                    if (!s) {
                                        player.sendTranslation("gui.guild.upgrades.error");
                                    } else {
                                        player.sendTranslation("gui.guild.upgrades.success");
                                        updateGui(3);
                                        showUpgradesDialog(player);
                                    }
                                });
                            }
                        });
                    }, () -> {
                        showUpgradesDialog(player);
                    }));
                }
            }, () -> guiPage.remove(player)));
        }, requiredHeads);
    }

    private void showMembersDialog(ArterionPlayer player) {
        player.closeGui();
        if (!player.getGuild().equals(owner)) return;
        if (owner.isInLocalFight()) return;
        guiPage.put(player, 1);
        boolean leader = player.getPersistenceHolder().equals(this.owner.getLeader());
        UUID[] requiredHeads = new UUID[2 + owner.getMemberCount()];
        requiredHeads[0] = HeadCacheUtil.ARROW_LEFT;
        requiredHeads[1] = HeadCacheUtil.QUESTION_MARK;
        requiredHeads[2] = owner.getLeader().getUuid();
        int j = 3;
        int os = owner.getOfficers().size();
        for (DatabasePlayer p : owner.getOfficers()) {
            requiredHeads[j] = p.getUuid();
            j++;
        }
        for (DatabasePlayer p : owner.getMembers()) {
            requiredHeads[j] = p.getUuid();
            j++;
        }
        if (HeadCacheUtil.countMissingHeads(requiredHeads) > 0) {
            player.scheduleHotbarCard(new HotbarTitleMessageCard(2000, player, "hotbar.wait"));
        }
        HeadCacheUtil.supplyCachedHeadSync(heads -> {
            int maxmembers = ArterionPlugin.getInstance().getFormulaManager().GUILD_MAXMEMBERS.evaluateInt();
            int playerRows = ((maxmembers + 8) / 9 + 1);
            int beginControls = playerRows * 9;
            player.openGui(new ItemGUI(player, player.getTranslation("gui.guild.members.title"), () -> {
                ItemStack[] stacks = new ItemStack[(playerRows + 1) * 9];
                stacks[0] = NamedItemUtil.modify(heads[2].getHead(), player.getTranslation("gui.guild.members.leader", heads[2].getName()), player.getTranslation("gui.guild.members.leadertag"));
                for (int i = 3; i < heads.length; i++) {
                    if (i < os + 3) {
                        stacks[i - 2] = NamedItemUtil.modify(heads[i].getHead(), player.getTranslation("gui.guild.members.officer", heads[i].getName()), player.getTranslation("gui.guild.members.officertag"));
                    } else {
                        stacks[i - 2] = NamedItemUtil.modify(heads[i].getHead(), player.getTranslation("gui.guild.members.normal", heads[i].getName()));
                    }
                }
                stacks[beginControls] = NamedItemUtil.modify(heads[0].getHead(), player.getTranslation("gui.guild.members.back"));
                if (player.getPersistenceHolder().equals(this.owner.getLeader()) || this.owner.getOfficers().contains(player.getPersistenceHolder()))
                    stacks[beginControls + 8] = NamedItemUtil.modify(heads[1].getHead(), player.getTranslation("gui.guild.members.invite"));
                return stacks;
            }, (clickType, i) -> {
                if (i == beginControls) {
                    onInteract(player, null);
                } else if (i == beginControls + 8) {
                    if (player.getPersistenceHolder().equals(this.owner.getLeader()) || this.owner.getOfficers().contains(player.getPersistenceHolder())) {
                        if (invites + owner.getMemberCount() >= maxmembers) {
                            player.sendTranslation("gui.guild.members.invite.toomany");
                            return;
                        } else {
                            player.openGui(new TextGUI(player, player.getTranslation("gui.guild.members.invite.subtitle"), () -> {
                                return new String[]{player.getTranslation("gui.guild.members.invite.entername")};
                            }, result -> {
                                Player p = Bukkit.getPlayer(result);
                                if (p != null && p.isOnline() && !ArterionPlayerUtil.get(p).isVanished()) {
                                    ArterionPlayer other = ArterionPlayerUtil.get(p);
                                    if (other.getGroup() == null) {
                                        if (other.getGuild() != null) {
                                            player.sendTranslation("guild.otheralreadyinguild");
                                            player.closeGui(true);
                                            return;
                                        }
                                        if (other.getHomeLocation() != null) {
                                            player.sendTranslation("gui.guild.stillhome");
                                            player.closeGui(true);
                                            return;
                                        }
                                        invites++;
                                        ArterionPlugin.getInstance().getInvitationSystem().invite(player, owner.getUuid(), other, () -> {
                                            other.sendTranslation("gui.guild.members.invite.from", owner);
                                        }, new InvitationHandler() {
                                            @Override
                                            public void onAccept(ArterionPlayer other) {
                                                if (other.getGuild() != null) {
                                                    player.sendTranslation("gui.guild.members.invite.declined", other);
                                                    other.sendTranslation("gui.guild.members.invite.youdeclined", player);
                                                    invites--;
                                                    return;
                                                }
                                                if (other.getHomeLocation() != null) {
                                                    player.sendTranslation("gui.guild.stillhome");
                                                    other.sendTranslation("gui.guild.youstillhome");
                                                    invites--;
                                                    return;
                                                }
                                                if (other.getGroup() == null) {
                                                    if (owner.getDeleted() != DatabaseGuild.NOT_DELETED) {
                                                        other.sendTranslation("gui.guild.members.invite.youtimedout", player);
                                                        return;
                                                    }
                                                    player.sendTranslation("gui.guild.members.invite.accepted", other);
                                                    other.sendTranslation("gui.guild.members.invite.youaccepted", player);
                                                    owner.addMember(other.getPersistenceHolder(), succ -> {
                                                    });
                                                    other.setGuild(owner);
                                                    owner.sendTranslation("gui.guild.newmember", other);
                                                    owner.onPlayerJoin(other, true);
                                                    updateAllGuis();
                                                    invites--;
                                                    return;
                                                } else {
                                                    player.sendTranslation("gui.guild.members.invite.declined", other);
                                                    other.sendTranslation("gui.guild.members.invite.youalreadygroup");
                                                }
                                                invites--;
                                            }

                                            @Override
                                            public void onTimeout(ArterionPlayer other) {
                                                player.sendTranslation("gui.guild.members.invite.timedout", other);
                                                other.sendTranslation("gui.guild.members.invite.youtimedout", player);
                                                invites--;
                                            }

                                            @Override
                                            public void onDeny(ArterionPlayer other) {
                                                player.sendTranslation("gui.guild.members.invite.declined", other);
                                                other.sendTranslation("gui.guild.members.invite.youdeclined", player);
                                                invites--;
                                            }
                                        });
                                    } else {
                                        player.sendTranslation("gui.guild.members.invite.alreadygroup");
                                    }
                                } else {
                                    player.sendTranslation("gui.guild.members.invite.notonline");
                                }
                                player.closeGui(true);
                            }));
                        }
                    }
                } else {
                    //No actions for non-leaders (and not for the leader on himself)
                    if (player.getPersistenceHolder().equals(owner.getLeader()) && i != 0) {
                        if (heads[i + 2] == null) return;
                        UUID o = heads[i + 2].getUuid();
                        new DatabaseTask() {

                            @Override
                            public boolean performTransaction(Database db) {
                                DatabasePlayer dbp = db.find(DatabasePlayer.class, o);
                                ArterionPlugin.getInstance().getSchedulers().getMainScheduler().executeInSpigotCircle(new InternalTask() {
                                    @Override
                                    public void run() {
                                        editMember(player, dbp);
                                    }
                                });
                                return false;
                            }

                            @Override
                            public void onTransactionCommitOrRollback(boolean committed) {

                            }

                            @Override
                            public void onTransactionError() {

                            }
                        }.execute();
                    } else if (owner.getOfficers().contains(player.getPersistenceHolder()) && i > 1 + owner.getOfficers().size()) {
                        if (heads[i + 2] == null) return;
                        UUID o = heads[i + 2].getUuid();
                        new DatabaseTask() {

                            @Override
                            public boolean performTransaction(Database db) {
                                DatabasePlayer dbp = db.find(DatabasePlayer.class, o);
                                ArterionPlugin.getInstance().getSchedulers().getMainScheduler().executeInSpigotCircle(new InternalTask() {
                                    @Override
                                    public void run() {
                                        editOfficerMember(player, dbp);
                                    }
                                });
                                return false;
                            }

                            @Override
                            public void onTransactionCommitOrRollback(boolean committed) {

                            }

                            @Override
                            public void onTransactionError() {

                            }
                        }.execute();
                    }
                }
            }, () -> guiPage.remove(player)));
        }, requiredHeads);
    }

    private void editMember(ArterionPlayer player, DatabasePlayer o) {
        player.closeGui();
        if (owner.isInLocalFight()) return;

        HeadCacheUtil.supplyCachedHeadSync(heads -> {
            player.openGui(new ItemGUI(player, player.getTranslation("gui.guild.member.title", o.getName()), () -> {
                ItemStack[] stacks = new ItemStack[18];
                stacks[0] = NamedItemUtil.create(Material.DIAMOND_CHESTPLATE, player.getTranslation("gui.guild.member.toleader"));
                if (!owner.getOfficers().contains(o)) {
                    stacks[2] = NamedItemUtil.create(Material.GOLD_CHESTPLATE, player.getTranslation("gui.guild.member.toofficer"));
                } else {
                    stacks[2] = NamedItemUtil.create(Material.LEATHER_CHESTPLATE, player.getTranslation("gui.guild.member.tonormal"));
                }
                stacks[8] = NamedItemUtil.create(Material.INK_SACK, 1, (byte) 1, player.getTranslation("gui.guild.member.kick"));
                stacks[9] = NamedItemUtil.modify(heads[0].getHead(), player.getTranslation("gui.guild.member.back"));
                return stacks;
            }, (clickType, i) -> {
                if (i == 9) {
                    showMembersDialog(player);
                    return;
                }
                player.openGui(new ConfirmItemGUI(player, () -> {
                    if (i == 0) {
                        owner.setLeader(o, succ -> {
                            if (succ) owner.sendTranslation("gui.guild.newleader", o.getName());
                            else player.sendTranslation("gui.guild.dberror");
                            ArterionPlugin.getInstance().getSchedulers().getMainScheduler().executeInSpigotCircle(new InternalTask() {
                                @Override
                                public void run() {
                                    updateAllGuis();
                                    showMembersDialog(player);
                                }
                            });
                        });
                    } else if (i == 2 && !owner.getOfficers().contains(o)) {
                        if (owner.getOfficers().size() >= owner.getMaxOfficers()) {
                            player.sendTranslation("gui.guild.maxofficers");
                            editMember(player, o);
                            return;
                        }
                        owner.promoteOfficer(o, succ -> {
                            if (succ) owner.sendTranslation("gui.guild.newofficers", o.getName());
                            else player.sendTranslation("gui.guild.dberror");
                            ArterionPlugin.getInstance().getSchedulers().getMainScheduler().executeInSpigotCircle(new InternalTask() {
                                @Override
                                public void run() {
                                    updateAllGuis();
                                    showMembersDialog(player);
                                }
                            });
                        });
                    } else if (i == 2) { //And owner.getOfficers().contains(o) - implicit from call above
                        owner.demoteOfficer(o, succ -> {
                            if (succ) owner.sendTranslation("gui.guild.delofficers", o.getName());
                            else player.sendTranslation("gui.guild.dberror");
                            ArterionPlugin.getInstance().getSchedulers().getMainScheduler().executeInSpigotCircle(new InternalTask() {
                                @Override
                                public void run() {
                                    updateAllGuis();
                                    showMembersDialog(player);
                                }
                            });
                        });
                    } else if (i == 8) {
                        owner.removeMember(o, succ -> {
                            if (succ) {
                                Player p = Bukkit.getPlayer(o.getUuid());
                                if (p != null && p.isOnline()) {
                                    ArterionPlayer ap = ArterionPlayerUtil.get(p);
                                    ap.setGuild(null);
                                    owner.onPlayerLeave(ap, true);
                                    ap.sendTranslation("gui.guild.youkicked");
                                    if (ap.getRegion().equals(owner.getRegion())) {
                                        ArterionPlugin.getInstance().getSchedulers().getMainScheduler().executeInSpigotCircle(new InternalTask() {
                                            @Override
                                            public void run() {
                                                p.teleport(ArterionPlugin.getInstance().getArterionConfig().spawn);
                                                ap.updateRegion(ArterionPlugin.getInstance().getArterionConfig().spawn);
                                            }
                                        });
                                    }
                                } else {
                                    new DatabaseObjectTask<DatabasePlayer>(DatabasePlayer.class, o.getUuid()) {

                                        @Override
                                        public void updateObject(DatabasePlayer databasePlayer) {
                                            databasePlayer.setKickedFromGuild(owner.getUuid());
                                        }

                                        @Override
                                        public void success() {

                                        }

                                        @Override
                                        public void fail() {

                                        }
                                    }.execute();
                                }
                                owner.sendTranslation("gui.guild.kicked", o.getName());
                            } else player.sendTranslation("gui.guild.dberror");
                            ArterionPlugin.getInstance().getSchedulers().getMainScheduler().executeInSpigotCircle(new InternalTask() {
                                @Override
                                public void run() {
                                    updateAllGuis();
                                    showMembersDialog(player);
                                }
                            });
                        });
                    }
                }, () -> {
                    //Return to member menu
                    this.editMember(player, o);
                }));
            }));
        }, HeadCacheUtil.ARROW_LEFT);
    }

    private void editOfficerMember(ArterionPlayer player, DatabasePlayer o) {
        player.closeGui();
        if (owner.isInLocalFight()) return;
        if (!owner.getOfficers().contains(player.getPersistenceHolder())) {
            showMembersDialog(player);
            return;
        }
        HeadCacheUtil.supplyCachedHeadSync(heads -> {
            player.openGui(new ItemGUI(player, player.getTranslation("gui.guild.member.title", o.getName()), () -> {
                ItemStack[] stacks = new ItemStack[18];
                stacks[4] = NamedItemUtil.create(Material.INK_SACK, 1, (byte) 1, player.getTranslation("gui.guild.member.kick"));
                stacks[9] = NamedItemUtil.modify(heads[0].getHead(), player.getTranslation("gui.guild.member.back"));
                return stacks;
            }, (clickType, i) -> {
                if (i == 9) {
                    showMembersDialog(player);
                    return;
                }
                player.openGui(new ConfirmItemGUI(player, () -> {
                    if (i == 4) {
                        if (!owner.getOfficers().contains(player.getPersistenceHolder())) {
                            showMembersDialog(player);
                            return;
                        }
                        owner.removeMember(o, succ -> {
                            if (succ) {
                                Player p = Bukkit.getPlayer(o.getUuid());
                                if (p != null && p.isOnline()) {
                                    ArterionPlayer ap = ArterionPlayerUtil.get(p);
                                    ap.setGuild(null);
                                    owner.onPlayerLeave(ap, true);
                                    ap.sendTranslation("gui.guild.youkicked");
                                    if (ap.getRegion().equals(owner.getRegion())) {
                                        ArterionPlugin.getInstance().getSchedulers().getMainScheduler().executeInSpigotCircle(new InternalTask() {
                                            @Override
                                            public void run() {
                                                p.teleport(ArterionPlugin.getInstance().getArterionConfig().spawn);
                                                ap.updateRegion(ArterionPlugin.getInstance().getArterionConfig().spawn);
                                            }
                                        });
                                    }
                                } else {
                                    new DatabaseObjectTask<DatabasePlayer>(DatabasePlayer.class, o.getUuid()) {

                                        @Override
                                        public void updateObject(DatabasePlayer databasePlayer) {
                                            databasePlayer.setKickedFromGuild(owner.getUuid());
                                        }

                                        @Override
                                        public void success() {

                                        }

                                        @Override
                                        public void fail() {

                                        }
                                    }.execute();
                                }
                                owner.sendTranslation("gui.guild.kicked", o.getName());
                            } else player.sendTranslation("gui.guild.dberror");
                            ArterionPlugin.getInstance().getSchedulers().getMainScheduler().executeInSpigotCircle(new InternalTask() {
                                @Override
                                public void run() {
                                    updateAllGuis();
                                    showMembersDialog(player);
                                }
                            });
                        });
                    }
                }, () -> {
                    //Return to member menu
                    this.editMember(player, o);
                }));
            }));
        }, HeadCacheUtil.ARROW_LEFT);
    }

    private void showVaultDialog(ArterionPlayer player) {
        player.closeGui();
        if (!player.getGuild().equals(owner)) return;
        guiPage.put(player, 2);
        AtomicBoolean leader = new AtomicBoolean(player.getPersistenceHolder().equals(this.owner.getLeader()));
        AtomicBoolean officer = new AtomicBoolean(leader.get() || this.owner.getOfficers().contains(player.getPersistenceHolder()));
        player.openGui(new ItemGUI(player, player.getTranslation("gui.vault.choose"), () -> {
            ItemStack[] stacks = new ItemStack[9];
            stacks[1] = NamedItemUtil.create(Material.INK_SACK, 1, (byte) 10, player.getTranslation("gui.vault.deposit"));
            String tax = owner.getObsidian() == DatabaseGuild.OBSIDIAN_NOT_CALCULATED
                    ? player.getTranslation("gui.vault.item.taxunavailable")
                    : player.getTranslation("gui.vault.item.tax", ArterionPlugin.getInstance().getFormulaManager().GUILD_TAX.evaluateInt(owner.getObsidian()) / 100f);
            stacks[4] = NamedItemUtil.create(Material.GOLD_INGOT, 1, player.getTranslation("gui.vault.item.name"), player.getTranslation("gui.vault.item.amount", owner.getMoneyBearer().getCachedMoney() / 100d), tax);
            if (officer.get() && !owner.isInLocalFight()) {
                stacks[7] = NamedItemUtil.create(Material.INK_SACK, 1, (byte) 1, player.getTranslation("gui.vault.withdraw"));
            }
            return stacks;
        }, ((clickType, in) -> {
            if (in == 4) return;
            TransferDirection dir = in == 1 ? TransferDirection.DEPOSIT : TransferDirection.WITHDRAW;
            player.closeGui();
            player.openGui(new TextGUI(player, player.getTranslation("gui.vault.asksubtitle"), () -> {
                return new String[]{player.getTranslation(dir == TransferDirection.DEPOSIT ? "gui.vault.askdeposit" : "gui.vault.askwithdraw")};
            }, result -> {
                leader.set(player.getPersistenceHolder().equals(this.owner.getLeader()));
                officer.set(leader.get() || this.owner.getOfficers().contains(player.getPersistenceHolder()));
                long amount = 0;
                if (result.equalsIgnoreCase("all")) {
                    if (dir == TransferDirection.DEPOSIT) {
                        amount = player.getBagMoneyBearer().getCachedMoney();
                    } else {
                        amount = owner.getMoneyBearer().getCachedMoney();
                    }
                    if (amount < 0) amount = 0; //Dont allow withdrawing negative guild fees
                } else {
                    try {
                        double i = Math.round(Double.parseDouble(result.replace(",", ".")) * 100);
                        if (i > Integer.MAX_VALUE || i < 1) {
                            player.sendTranslation("gui.vault.nonumber");
                            return;
                        }
                        amount = (int) i; //Translate to cents
                    } catch (NumberFormatException e) {
                        player.sendTranslation("gui.vault.nonumber");
                        return;
                    }
                }

                if (dir == TransferDirection.WITHDRAW && (!officer.get() || owner.isInLocalFight())) {
                    player.sendTranslation("gui.vault.nowithdraw");
                    return;
                }

                long finalAmount = amount;
                TransferDirection finalDir = dir;
                player.getBagMoneyBearer().transferMoney(dir == TransferDirection.WITHDRAW ? -amount : amount, owner.getMoneyBearer(), success -> {
                    if (success) {
                        if (finalDir == TransferDirection.WITHDRAW) {
                            owner.sendTranslation("gui.vault.received", player, finalAmount / 100f);
                        } else {
                            owner.sendTranslation("gui.vault.sent", player, finalAmount / 100f);
                        }
                        ArterionPlugin.getInstance().getSchedulers().getMainScheduler().executeInSpigotCircle(new InternalTask() {
                            @Override
                            public void run() {
                                updateGui(2);
                            }
                        });
                    } else {
                        player.sendTranslation("gui.vault.error");
                    }
                });
            }));
        }), () -> guiPage.remove(player)));
    }

    @Override
    public boolean onBreak(ArterionPlayer player) {
        player.sendTranslation("guildblock.nobreak");
        return false;
    }

    private void updateAllGuis() {
        updateGui(0);
        updateGui(1);
        updateGui(2);
        updateGui(3);
    }

    public void updateGui(int id) {
        List<ArterionPlayer> affected = new LinkedList<>();
        List<ArterionPlayer> drop = new LinkedList<>();
        for (Map.Entry<ArterionPlayer, Integer> entry : guiPage.entrySet()) {
            if (entry.getValue() == id) {
                if ((entry.getKey().getPersistenceHolder().equals(owner.getLeader())
                        || owner.getOfficers().contains(entry.getKey().getPersistenceHolder())
                        || owner.getMembers().contains(entry.getKey().getPersistenceHolder()))) {
                    affected.add(entry.getKey());
                } else {
                    drop.add(entry.getKey());
                }
            }
        }
        for (ArterionPlayer a : drop) {
            guiPage.remove(a);
            a.closeGui();
        }
        if (id == 0) {
            for (ArterionPlayer a : affected) onInteract(a, null);
        } else if (id == 1) {
            for (ArterionPlayer a : affected) showMembersDialog(a);
        } else if (id == 2) {
            for (ArterionPlayer a : affected) showVaultDialog(a);
        } else if (id == 3) {
            for (ArterionPlayer a : affected) showUpgradesDialog(a);
        }
    }

    public void setNoInteract(boolean b) {
        this.noInteract = b;
    }
}
