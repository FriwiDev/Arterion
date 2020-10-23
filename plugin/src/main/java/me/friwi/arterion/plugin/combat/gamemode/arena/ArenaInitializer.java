package me.friwi.arterion.plugin.combat.gamemode.arena;

import com.google.common.collect.Lists;
import me.friwi.arterion.plugin.ArterionPlugin;
import me.friwi.arterion.plugin.permissions.TeleportPreconditions;
import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.player.ArterionPlayerUtil;
import me.friwi.arterion.plugin.ui.gui.*;
import me.friwi.arterion.plugin.ui.hotbar.HotbarTitleMessageCard;
import me.friwi.arterion.plugin.ui.invite.InvitationHandler;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class ArenaInitializer {
    public static final int MAX_PLAYERS = 8;

    private ArterionPlayer owner;
    private List<ArterionPlayer> team1, team2;
    private Set<ArterionPlayer> interfaceUsers;
    private ArterionPlayer editMember;
    private int[] invites = new int[2];
    private boolean disbanded = false;

    public ArenaInitializer(ArterionPlayer owner) {
        this.owner = owner;
        this.team1 = new LinkedList<>();
        this.team1.add(owner);
        owner.setArenaInitializer(this);
        this.team2 = new LinkedList<>();
        interfaceUsers = new HashSet<>();
    }

    public static String canJoinArenaInitializer(ArterionPlayer player) {
        if (player.getArtefactCarrier() != null) return "artefact";
        if (TeleportPreconditions.canTeleport(player, false) != null) return "combat";
        if (player.isInTemporaryWorld() || player.getArenaInitializer() != null) return "othermode";
        return null;
    }

    public static String canJoinArena(ArterionPlayer player) {
        if (player.getArtefactCarrier() != null) return "artefact";
        if (TeleportPreconditions.canTeleport(player, false) != null) return "combat";
        if (player.isInTemporaryWorld()) return "othermode";
        return null;
    }

    public void addToTeam(ArterionPlayer player, int team) {
        String join = canJoinArenaInitializer(player);
        if (join != null) {
            sendTranslation("arena.nojoin." + join, player);
            player.sendTranslation("arena.younojoin." + join);
            return;
        }
        sendTranslation("arena.join", player);
        player.sendTranslation("arena.youjoin", owner);
        if (team == 1) team1.add(player);
        else team2.add(player);
        player.setArenaInitializer(this);
        updateInterfaces();
    }

    public void remove(ArterionPlayer player, boolean kick) {
        if (player.equals(owner)) {
            sendTranslation("arena.disbanded");
            disband();
        } else if (team1.contains(player)) {
            team1.remove(player);
            player.setArenaInitializer(null);
            sendTranslation("arena.leave", player, 1);
            player.sendTranslation(kick ? "arena.youkick" : "arena.youleave");
            player.closeGui();
        } else {
            team2.remove(player);
            player.setArenaInitializer(null);
            sendTranslation("arena.leave", player, 2);
            player.sendTranslation(kick ? "arena.youkick" : "arena.youleave");
            player.closeGui();
        }
    }

    private void disband() {
        if (disbanded) return;
        disbanded = true;
        for (ArterionPlayer p : team1) {
            p.setArenaInitializer(null);
            p.closeGui();
        }
        for (ArterionPlayer p : team2) {
            p.setArenaInitializer(null);
            p.closeGui();
        }
    }

    private void updateInterfaces() {
        for (ArterionPlayer player : Lists.newLinkedList(interfaceUsers)) {
            player.closeGui();
            openInterface(player);
        }
        if (editMember != null) {
            owner.closeGui();
            editMember(owner, editMember, team1.contains(editMember) ? 1 : 2);
        }
    }

    public void openInterface(ArterionPlayer player) {
        player.closeGui();
        interfaceUsers.add(player);
        UUID[] req = new UUID[team1.size() + team2.size() + 2];
        req[0] = HeadCacheUtil.QUESTION_MARK;
        req[1] = HeadCacheUtil.ARROW_RIGHT;
        int i = 2;
        AtomicBoolean switchedToInvite = new AtomicBoolean(false);
        for (ArterionPlayer p : team1) {
            req[i] = p.getBukkitPlayer().getUniqueId();
            i++;
        }
        for (ArterionPlayer p : team2) {
            req[i] = p.getBukkitPlayer().getUniqueId();
            i++;
        }
        if (HeadCacheUtil.countMissingHeads(req) > 0) {
            player.scheduleHotbarCard(new HotbarTitleMessageCard(2000, player, "hotbar.wait"));
        }
        HeadCacheUtil.supplyCachedHeadSync(cachedHeads -> {
            player.openGui(new ItemGUI(player, player.getTranslation("arena.buildteams"),
                    () -> {
                        ItemStack[] stacks = new ItemStack[27];
                        stacks[0] = NamedItemUtil.create(Material.INK_SACK, 1, (byte) 9, player.getTranslation("arena.item.team1"));
                        if (team1.size() < MAX_PLAYERS && player.equals(owner))
                            stacks[8] = NamedItemUtil.modify(cachedHeads[0].getHead(), player.getTranslation("arena.item.team1.add"));
                        stacks[9] = NamedItemUtil.create(Material.INK_SACK, 1, (byte) 12, player.getTranslation("arena.item.team2"));
                        if (team2.size() < MAX_PLAYERS && player.equals(owner))
                            stacks[17] = NamedItemUtil.modify(cachedHeads[0].getHead(), player.getTranslation("arena.item.team2.add"));
                        int j = 2;
                        int l = 0;
                        for (ArterionPlayer p : team1) {
                            stacks[1 + l] = NamedItemUtil.modify(cachedHeads[j].getHead(), player.getLanguage().translateObject(p));
                            j++;
                            l++;
                        }
                        l = 0;
                        for (ArterionPlayer p : team2) {
                            stacks[10 + l] = NamedItemUtil.modify(cachedHeads[j].getHead(), player.getLanguage().translateObject(p));
                            j++;
                            l++;
                        }
                        stacks[22] = NamedItemUtil.create(Material.INK_SACK, 1, (byte) 1, player.getTranslation(owner.equals(player) ? "arena.item.disband" : "arena.item.leave"));
                        if (owner.equals(player) && team1.size() > 0 && team2.size() > 0)
                            stacks[26] = NamedItemUtil.modify(cachedHeads[1].getHead(), player.getTranslation("arena.item.begin"));
                        return stacks;
                    }, (clickType, k) -> {
                if (k == 8 && team1.size() < MAX_PLAYERS) {
                    switchedToInvite.set(true);
                    inviteGui(player, 1);
                } else if (k == 17 && team2.size() < MAX_PLAYERS) {
                    switchedToInvite.set(true);
                    inviteGui(player, 2);
                } else if (k == 22) {
                    player.openGui(new ConfirmItemGUI(player, () -> {
                        player.closeGui();
                        remove(player, false);
                        tryDisband();
                    }, () -> {
                        openInterface(player);
                    }));
                } else if (k == 26 && owner.equals(player) && team1.size() > 0 && team2.size() > 0) {
                    player.closeGui();
                    beginFight();
                } else if (k >= 1 && k <= 8 && player.equals(owner)) {
                    int l = k - 1;
                    if (team1.size() > l) {
                        editMember(player, team1.get(l), 1);
                    }
                } else if (k >= 10 && k <= 17 && player.equals(owner)) {
                    int l = k - 10;
                    if (team2.size() > l) {
                        editMember(player, team2.get(l), 2);
                    }
                }
            }, () -> {
                if (!switchedToInvite.get()) {
                    tryDisband();
                }
                interfaceUsers.remove(player);
            }));
        }, req);
    }

    private void editMember(ArterionPlayer player, ArterionPlayer other, int team) {
        editMember = other;
        UUID[] req = new UUID[2];
        req[0] = HeadCacheUtil.ARROW_LEFT;
        req[1] = team == 1 ? HeadCacheUtil.ARROW_DOWN : HeadCacheUtil.ARROW_UP;
        if (HeadCacheUtil.countMissingHeads(req) > 0) {
            player.scheduleHotbarCard(new HotbarTitleMessageCard(2000, player, "hotbar.wait"));
        }
        HeadCacheUtil.supplyCachedHeadSync(cachedHeads -> {
            player.openGui(new ItemGUI(player, player.getTranslation("arena.member.edit", other),
                    () -> {
                        ItemStack[] stacks = new ItemStack[18];
                        boolean otherteamfull = (team == 1 && team2.size() >= MAX_PLAYERS) || (team == 2 && team1.size() >= MAX_PLAYERS);
                        if (other.equals(owner) && otherteamfull) {
                            player.closeGui();
                            openInterface(player);
                        } else if (otherteamfull) {
                            stacks[4] = NamedItemUtil.create(Material.INK_SACK, 1, (byte) 1, player.getTranslation("arena.member.kick"));
                        } else if (other.equals(owner)) {
                            stacks[4] = NamedItemUtil.modify(cachedHeads[1].getHead(), player.getTranslation("arena.member.moveother" + team));
                        } else {
                            stacks[2] = NamedItemUtil.create(Material.INK_SACK, 1, (byte) 1, player.getTranslation("arena.member.kick"));
                            stacks[6] = NamedItemUtil.modify(cachedHeads[1].getHead(), player.getTranslation("arena.member.moveother" + team));
                        }
                        stacks[9] = NamedItemUtil.modify(cachedHeads[0].getHead(), player.getTranslation("arena.back"));
                        return stacks;
                    }, (clickType, k) -> {
                if (k == 9) {
                    player.closeGui();
                    openInterface(player);
                } else {
                    boolean otherteamfull = (team == 1 && team2.size() >= MAX_PLAYERS) || (team == 2 && team1.size() >= MAX_PLAYERS);
                    if (player.equals(owner) && otherteamfull) {
                        //No options here
                    } else if (otherteamfull) {
                        if (k == 4) {
                            player.openGui(new ConfirmItemGUI(player, () -> {
                                player.closeGui();
                                openInterface(player);
                                remove(other, true);
                            }, () -> {
                                player.closeGui();
                                editMember(player, other, team);
                            }));
                        }
                    } else if (other.equals(owner)) {
                        if (k == 4) {
                            moveTeam(player, other);
                        }
                    } else {
                        if (k == 2) {
                            player.openGui(new ConfirmItemGUI(player, () -> {
                                player.closeGui();
                                openInterface(player);
                                remove(other, true);
                            }, () -> {
                                player.closeGui();
                                editMember(player, other, team);
                            }));
                        } else if (k == 6) {
                            moveTeam(player, other);
                        }
                    }
                }
            }, () -> {
                editMember = null;
            }));
        }, req);
    }

    private void moveTeam(ArterionPlayer player, ArterionPlayer other) {
        if (team1.contains(other)) {
            team1.remove(other);
            team2.add(other);
        } else {
            team2.remove(other);
            team1.add(other);
        }
        player.closeGui();
        openInterface(player);
        updateInterfaces();
    }

    private void inviteGui(ArterionPlayer player, int team) {
        if ((team == 1 && team1.size() + invites[0] >= MAX_PLAYERS) || (team == 2 && team2.size() + invites[1] >= MAX_PLAYERS)) {
            player.sendTranslation("arena.invite.full");
            return;
        }
        player.closeGui();
        player.openGui(new TextGUI(player, player.getTranslation("arena.invite.title"), () -> {
            return new String[]{player.getTranslation("arena.invite.entername")};
        }, result -> {
            Player p = Bukkit.getPlayer(result);
            if (p != null && p.isOnline() && !ArterionPlayerUtil.get(p).isVanished()) {
                ArterionPlayer other = ArterionPlayerUtil.get(p);
                String join = canJoinArenaInitializer(other);
                if (join == null) {
                    invites[team - 1]++;
                    ArterionPlugin.getInstance().getInvitationSystem().invite(player, this, other, () -> {
                        other.sendTranslation("arena.invite.from", player);
                    }, new InvitationHandler() {
                        @Override
                        public void onAccept(ArterionPlayer other) {
                            String join2 = canJoinArenaInitializer(other);
                            if (join2 == null) {
                                if (disbanded) {
                                    other.sendTranslation("arena.invite.youtimedout", player);
                                    invites[team - 1]--;
                                    return;
                                }
                                player.sendTranslation("arena.invite.accepted", other);
                                other.sendTranslation("arena.invite.youaccepted", player);
                                addToTeam(other, team);
                                invites[team - 1]--;
                                return;
                            } else {
                                player.sendTranslation("arena.invite.declined", other);
                                other.sendTranslation("arena.younojoin." + join, player);
                            }
                            invites[team - 1]--;
                            tryDisband();
                        }

                        @Override
                        public void onTimeout(ArterionPlayer other) {
                            player.sendTranslation("arena.invite.timedout", other);
                            other.sendTranslation("arena.invite.youtimedout", player);
                            invites[team - 1]--;
                            tryDisband();
                        }

                        @Override
                        public void onDeny(ArterionPlayer other) {
                            player.sendTranslation("arena.invite.declined", other);
                            other.sendTranslation("arena.invite.youdeclined", player);
                            invites[team - 1]--;
                            tryDisband();
                        }
                    });
                } else {
                    player.sendTranslation("arena.nojoin." + join, other);
                }
            } else {
                player.sendTranslation("arena.invite.notonline");
            }
            player.closeGui(true);
            tryDisband();
        }, () -> {
            tryDisband();
        }));
    }

    private void tryDisband() {
        if (!disbanded && team1.size() + team2.size() + invites[0] + invites[1] <= 1) {
            disband();
        }
    }

    private void sendTranslation(String key, Object... values) {
        for (ArterionPlayer p : team1) {
            p.sendTranslation(key, values);
        }
        for (ArterionPlayer p : team2) {
            p.sendTranslation(key, values);
        }
    }

    private void beginFight() {
        owner.closeGui();
        UUID[] req = new UUID[1];
        req[0] = HeadCacheUtil.ARROW_LEFT;
        if (HeadCacheUtil.countMissingHeads(req) > 0) {
            owner.scheduleHotbarCard(new HotbarTitleMessageCard(2000, owner, "hotbar.wait"));
        }
        HeadCacheUtil.supplyCachedHeadSync(cachedHeads -> {
            AtomicInteger l = new AtomicInteger();
            owner.openGui(new ItemGUI(owner, owner.getTranslation("arena.selectmap"), () -> {
                ItemStack[] stacks = new ItemStack[(((ArenaMaps.values().length + 8) / 9) * 9) + 9];
                for (int i = 0; i < ArenaMaps.values().length; i++) {
                    if (ArenaMaps.values()[i].isPremiumOnly()) {
                        stacks[i] = NamedItemUtil.modify(ArenaMaps.values()[i].getPreviewStack(), "\2476" + ArenaMaps.values()[i].getName(owner.getLanguage()), ArenaMaps.values()[i].getBy(owner.getLanguage()), owner.getTranslation("arena.premium"));
                    } else {
                        stacks[i] = NamedItemUtil.modify(ArenaMaps.values()[i].getPreviewStack(), "\247a" + ArenaMaps.values()[i].getName(owner.getLanguage()), ArenaMaps.values()[i].getBy(owner.getLanguage()));
                    }
                }
                stacks[stacks.length - 9] = NamedItemUtil.modify(cachedHeads[0].getHead(), owner.getTranslation("arena.back"));
                l.set(stacks.length - 9);
                return stacks;
            }, (clickType, i) -> {
                if (team1.size() <= 0 || team2.size() <= 0) {
                    owner.closeGui();
                    openInterface(owner);
                    return;
                }
                if (i < ArenaMaps.values().length) {
                    ArenaMaps map = ArenaMaps.values()[i];
                    if (map.isPremiumOnly() && !owner.getRank().isPremium()) {
                        owner.sendTranslation("arena.premiumonly");
                    } else {
                        owner.closeGui();
                        beginFight(map);
                    }
                } else if (i == l.get()) {
                    owner.closeGui();
                    openInterface(owner);
                }
            }));
        }, HeadCacheUtil.ARROW_LEFT);
    }

    private void beginFight(ArenaMaps map) {
        for (ArterionPlayer p : team1) {
            String join = canJoinArena(p);
            if (join != null) {
                sendTranslation("arena.nobegin." + join, p);
                return;
            }
        }
        for (ArterionPlayer p : team2) {
            String join = canJoinArena(p);
            if (join != null) {
                sendTranslation("arena.nobegin." + join, p);
                return;
            }
        }
        new ArenaFight(team1, team2, map, succ -> {
            if (succ) {
                disband();
            }
        });
        sendTranslation("arena.soon");
    }
}
