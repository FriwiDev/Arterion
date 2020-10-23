package me.friwi.arterion.plugin.world.block.nonbtblocks;

import com.darkblade12.particleeffect.ParticleEffect;
import me.friwi.arterion.plugin.ArterionPlugin;
import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.player.ArterionPlayerUtil;
import me.friwi.arterion.plugin.ui.gui.HeadCacheUtil;
import me.friwi.arterion.plugin.ui.gui.ItemGUI;
import me.friwi.arterion.plugin.ui.gui.NamedItemUtil;
import me.friwi.arterion.plugin.ui.gui.TextGUI;
import me.friwi.arterion.plugin.ui.invite.InvitationHandler;
import me.friwi.arterion.plugin.util.database.DatabaseObjectTask;
import me.friwi.arterion.plugin.util.database.entity.DatabasePlayer;
import me.friwi.arterion.plugin.util.scheduler.InternalTask;
import me.friwi.arterion.plugin.world.item.HomeblockItem;
import me.friwi.arterion.plugin.world.region.PlayerClaimRegion;
import me.friwi.arterion.plugin.world.region.Region;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;
import java.util.function.Consumer;

public class HomeBlock extends SpecialBlock {
    private UUID owner;
    private boolean isInInvite = false;

    public HomeBlock(Location loc, UUID owner) {
        super(loc);
        this.owner = owner;
    }

    public static void resetPlayerHome(ArterionPlayer player, Consumer<Boolean> successCallback) {
        ArterionPlayer other = null;
        if (player.getRoomMate() != null) {
            Player p = Bukkit.getPlayer(player.getRoomMate());
            if (p != null && p.isOnline()) {
                other = ArterionPlayerUtil.get(p);
                if (player.isOwnsHomeBlock()) {
                    other.setHomeLocationAndRoommate(false, null, null, success -> {
                    });
                    other.sendTranslation("gui.homeblock.leave.remove", player);
                } else {
                    other.setHomeLocationAndRoommate(true, other.getHomeLocation(), null, success -> {
                    });
                    other.sendTranslation("gui.homeblock.leave.player", player);
                }
            } else {
                new DatabaseObjectTask<DatabasePlayer>(DatabasePlayer.class, player.getRoomMate()) {
                    @Override
                    public void updateObject(DatabasePlayer databasePlayer) {
                        if (player.isOwnsHomeBlock()) {
                            databasePlayer.setOwnsHomeBlock(false);
                            databasePlayer.setClaimWorld(null);
                            databasePlayer.setHomeX(0);
                            databasePlayer.setHomeY(0);
                            databasePlayer.setHomeZ(0);
                            databasePlayer.setRoomMate(null);
                        } else {
                            databasePlayer.setRoomMate(null);
                        }
                    }

                    @Override
                    public void success() {

                    }

                    @Override
                    public void fail() {

                    }
                }.execute();
            }
        }
        if (player.isOwnsHomeBlock()) {
            //Remove player claim
            Region found = null;
            for (Region r : ArterionPlugin.getInstance().getRegionManager().all()) {
                if (r instanceof PlayerClaimRegion) {
                    if (((PlayerClaimRegion) r).getPlayerUUID().equals(player.getBukkitPlayer().getUniqueId())) {
                        found = r;
                        break;
                    }
                }
            }
            if (found != null) ArterionPlugin.getInstance().getRegionManager().unRegisterRegion(found);
            //Remove specialblock
            ArterionPlugin.getInstance().getSpecialBlockManager().remove(player.getHomeLocation());
        }
        ArterionPlayer finalOther = other;
        player.setHomeLocationAndRoommate(false, null, null, success -> {
            if (finalOther != null) finalOther.getPlayerScoreboard().updateAllPlayerRelations();
            player.getPlayerScoreboard().updateAllPlayerRelations();
            successCallback.accept(success);
        });
    }

    public UUID getOwner() {
        return owner;
    }

    @Override
    public boolean onInteract(ArterionPlayer player, Action action) {
        if (action != Action.RIGHT_CLICK_BLOCK) return false;
        if (player.getBukkitPlayer().getUniqueId().equals(this.owner)) {
            onOwnerInteract(player, action);
        } else if (player.getRoomMate() != null && player.getRoomMate().equals(this.owner)) {
            onRoomMateInteract(player, action);
        } else {
            player.sendTranslation("block.homeblock.noperm");
        }
        return false;
    }

    private void onOwnerInteract(ArterionPlayer player, Action action) {
        HeadCacheUtil.supplyCachedHeadSync(heads -> {
            player.openGui(new ItemGUI(player, player.getTranslation("gui.homeblock.title"), () -> {
                ItemStack[] stacks = new ItemStack[9];
                stacks[2] = NamedItemUtil.create(Material.MAP, player.getTranslation("gui.homeblock.showclaimitem.name"));
                stacks[4] = NamedItemUtil.modify(heads[0].getHead(),
                        player.getRoomMate() == null ? player.getTranslation("gui.homeblock.invite.name")
                                : player.getTranslation("gui.homeblock.kick.name", heads[0].getName()));
                stacks[6] = NamedItemUtil.create(Material.GOLD_PICKAXE, player.getTranslation("gui.homeblock.break.name"));
                return stacks;
            }, (clickType, i) -> {
                if (i == 2) {
                    player.closeGui();
                    showClaim(player);
                } else if (i == 4) {
                    //Invite/Kick player
                    if (player.getRoomMate() == null) {
                        //Invite player
                        if (isInInvite) {
                            player.sendTranslation("gui.homeblock.invite.alreadyinvited");
                            return;
                        } else {
                            player.openGui(new TextGUI(player, player.getTranslation("gui.homeblock.invite.subtitle"), () -> {
                                return new String[]{player.getTranslation("gui.homeblock.invite.entername")};
                            }, result -> {
                                Player p = Bukkit.getPlayer(result);
                                if (p != null && p.isOnline() && !ArterionPlayerUtil.get(p).isVanished()) {
                                    ArterionPlayer other = ArterionPlayerUtil.get(p);
                                    if (other.getGuild() != null) {
                                        player.sendTranslation("guild.otheralreadyinguild");
                                        return;
                                    }
                                    if (other.getHomeLocation() == null) {
                                        isInInvite = true;
                                        ArterionPlugin.getInstance().getInvitationSystem().invite(player, player.getBukkitPlayer().getUniqueId(), other, () -> {
                                            other.sendTranslation("gui.homeblock.invite.from", player);
                                        }, new InvitationHandler() {
                                            @Override
                                            public void onAccept(ArterionPlayer other) {
                                                if (other.getGuild() != null) {
                                                    other.sendTranslation("guild.alreadyinguild");
                                                    isInInvite = false;
                                                    return;
                                                }
                                                if (other.getHomeLocation() == null) {
                                                    player.sendTranslation("gui.homeblock.invite.accepted", other);
                                                    other.sendTranslation("gui.homeblock.invite.youaccepted", player);
                                                    player.setHomeLocationAndRoommate(true, player.getHomeLocation(), other.getBukkitPlayer().getUniqueId(), success -> {
                                                        other.setHomeLocationAndRoommate(false, player.getHomeLocation(), player.getBukkitPlayer().getUniqueId(), succ -> {
                                                            isInInvite = false;
                                                            other.getPlayerScoreboard().updateAllPlayerRelations();
                                                            player.getPlayerScoreboard().updateAllPlayerRelations();
                                                        });
                                                    });
                                                    return;
                                                } else {
                                                    player.sendTranslation("gui.homeblock.invite.declined", other);
                                                    other.sendTranslation("gui.homeblock.invite.youalreadyhome");
                                                }
                                                isInInvite = false;
                                            }

                                            @Override
                                            public void onTimeout(ArterionPlayer other) {
                                                player.sendTranslation("gui.homeblock.invite.timedout", other);
                                                other.sendTranslation("gui.homeblock.invite.youtimedout", player);
                                                isInInvite = false;
                                            }

                                            @Override
                                            public void onDeny(ArterionPlayer other) {
                                                player.sendTranslation("gui.homeblock.invite.declined", other);
                                                other.sendTranslation("gui.homeblock.invite.youdeclined", player);
                                                isInInvite = false;
                                            }
                                        });
                                    } else {
                                        player.sendTranslation("gui.homeblock.invite.alreadyhome");
                                    }
                                } else {
                                    player.sendTranslation("gui.homeblock.invite.notonline");
                                }
                                player.closeGui(true);
                            }));
                        }
                    } else {
                        //Kick player
                        boolean[] done = new boolean[]{false};
                        player.openGui(new ItemGUI(player, player.getTranslation("gui.homeblock.kick.confirm"), () -> {
                            ItemStack[] stacks = new ItemStack[9];
                            stacks[0] = NamedItemUtil.create(Material.INK_SACK, 1, (byte) 10, player.getTranslation("gui.homeblock.kick.yes"));
                            stacks[8] = NamedItemUtil.create(Material.INK_SACK, 1, (byte) 1, player.getTranslation("gui.homeblock.kick.no"));
                            return stacks;
                        }, (c, yesno) -> {
                            if (yesno == 0) {
                                if (done[0]) return;
                                done[0] = true;
                                Player otherp = Bukkit.getPlayer(player.getRoomMate());
                                if (otherp != null && otherp.isOnline()) {
                                    ArterionPlayer other = ArterionPlayerUtil.get(otherp);
                                    other.setHomeLocationAndRoommate(false, null, null, success -> {
                                        player.setHomeLocationAndRoommate(true, player.getHomeLocation(), null, succ -> {
                                            player.sendTranslation("gui.homeblock.kick.kicked", other);
                                            other.sendTranslation("gui.homeblock.kick.youkicked", player);
                                            HomeBlock.this.onInteract(player, action);
                                        });
                                    });
                                } else {
                                    UUID rm = player.getRoomMate();
                                    player.setHomeLocationAndRoommate(true, player.getHomeLocation(), null, success -> {
                                        new DatabaseObjectTask<DatabasePlayer>(DatabasePlayer.class, rm) {
                                            String name;

                                            @Override
                                            public void updateObject(DatabasePlayer dbp) {
                                                dbp.setClaimWorld(null);
                                                dbp.setOwnsHomeBlock(false);
                                                dbp.setHomeX(0);
                                                dbp.setHomeY(0);
                                                dbp.setHomeZ(0);
                                                dbp.setRoomMate(null);
                                                name = dbp.getName();
                                            }

                                            @Override
                                            public void success() {
                                                player.sendTranslation("gui.homeblock.kick.kickedoffline");
                                                HomeBlock.this.onInteract(player, action);
                                            }

                                            @Override
                                            public void fail() {

                                            }
                                        }.execute();
                                    });
                                }
                                player.closeGui();
                            } else {
                                this.onInteract(player, action);
                            }
                        }));
                    }
                } else {
                    //Destroy block
                    player.openGui(new ItemGUI(player, player.getTranslation("gui.homeblock.break.confirm"), () -> {
                        ItemStack[] stacks = new ItemStack[9];
                        stacks[0] = NamedItemUtil.create(Material.INK_SACK, 1, (byte) 10, player.getTranslation("gui.homeblock.break.yes"));
                        stacks[8] = NamedItemUtil.create(Material.INK_SACK, 1, (byte) 1, player.getTranslation("gui.homeblock.break.no"));
                        return stacks;
                    }, (c, yesno) -> {
                        if (yesno == 0) {
                            player.closeGui();
                            if (player.getRoomMate() != null) {
                                player.sendTranslation("gui.homeblock.break.other");
                                return;
                            }
                            ItemStack item = new HomeblockItem().toItemStack();
                            int space = player.getBukkitPlayer().getInventory().firstEmpty();
                            if (space == -1) {
                                //Inv full
                                player.sendTranslation("gui.homeblock.break.invfull");
                                return;
                            }
                            player.getBukkitPlayer().getInventory().setItem(space, item);
                            player.getHomeLocation().getBlock().setType(Material.AIR);
                            resetPlayerHome(player, success -> {
                                if (success) {
                                    player.sendTranslation("gui.homeblock.break.success");
                                } else {
                                    throw new RuntimeException("Error in complex homeblock removal routine! Manual fix may be required!");
                                }
                            });
                        } else {
                            //Return to homeblock main menu
                            this.onInteract(player, action);
                        }
                    }));
                }
            }));
        }, player.getRoomMate() == null ? HeadCacheUtil.QUESTION_MARK : player.getRoomMate());
    }

    private void onRoomMateInteract(ArterionPlayer player, Action action) {
        player.openGui(new ItemGUI(player, player.getTranslation("gui.homeblock.title"), () -> {
            ItemStack[] stacks = new ItemStack[9];
            stacks[3] = NamedItemUtil.create(Material.MAP, player.getTranslation("gui.homeblock.showclaimitem.name"));
            stacks[5] = NamedItemUtil.create(Material.INK_SACK, 1, (byte) 1, player.getTranslation("gui.homeblock.leave.name"));
            return stacks;
        }, (clickType, i) -> {
            if (i == 3) {
                player.closeGui();
                showClaim(player);
            } else {
                //Leave block
                player.openGui(new ItemGUI(player, player.getTranslation("gui.homeblock.leave.confirm"), () -> {
                    ItemStack[] stacks = new ItemStack[9];
                    stacks[0] = NamedItemUtil.create(Material.INK_SACK, 1, (byte) 10, player.getTranslation("gui.homeblock.leave.yes"));
                    stacks[8] = NamedItemUtil.create(Material.INK_SACK, 1, (byte) 1, player.getTranslation("gui.homeblock.leave.no"));
                    return stacks;
                }, (c, yesno) -> {
                    if (yesno == 0) {
                        player.closeGui();
                        resetPlayerHome(player, success -> {
                            if (success) {
                                player.sendTranslation("gui.homeblock.leave.success");
                            } else {
                                throw new RuntimeException("Error in complex homeblock removal routine! Manual fix may be required!");
                            }
                        });
                    } else {
                        //Return to homeblock main menu
                        this.onInteract(player, action);
                    }
                }));
            }
        }));
    }

    private void showClaim(ArterionPlayer player) {
        int duration = ArterionPlugin.getInstance().getFormulaManager().PLAYER_HOMEBLOCK_SHOWCLAIM.evaluateInt();
        int spawnEvery = 5;
        player.sendTranslation("gui.homeblock.showclaim", duration / 20f);
        final double y = player.getHomeLocation().getY() + 0.5;
        Chunk c = player.getHomeLocation().getChunk();
        final double baseX = c.getX() * 16;
        final double baseZ = c.getZ() * 16;
        ArterionPlugin.getInstance().getSchedulers().getMainScheduler().executeInSpigotCircleTimer(new InternalTask() {
            int ticks = 0;
            float resolution = 32;

            @Override
            public void run() {
                Location loc = new Location(c.getWorld(), baseX, y, baseZ);
                //Spawn particles
                for (int i = 0; i < resolution; i++) {
                    ParticleEffect.FLAME.display(0, 0, 0, 0, 1, loc, 16);
                    loc.add(16f / resolution, 0, 0);
                }
                for (int i = 0; i < resolution; i++) {
                    ParticleEffect.FLAME.display(0, 0, 0, 0, 1, loc, 16);
                    loc.add(0, 0, 16f / resolution);
                }
                for (int i = 0; i < resolution; i++) {
                    ParticleEffect.FLAME.display(0, 0, 0, 0, 1, loc, 16);
                    loc.add(-16f / resolution, 0, 0);
                }
                for (int i = 0; i < resolution; i++) {
                    ParticleEffect.FLAME.display(0, 0, 0, 0, 1, loc, 16);
                    loc.add(0, 0, -16f / resolution);
                }
                //perform loop calculations
                ticks += spawnEvery;
                if (ticks > duration) cancel();
            }
        }, 0, spawnEvery);
    }

    @Override
    public boolean onBreak(ArterionPlayer player) {
        player.sendTranslation("block.homeblock.nobreak");
        return false;
    }
}
