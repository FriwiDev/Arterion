package me.friwi.arterion.plugin.combat.group;

import me.friwi.arterion.plugin.ArterionPlugin;
import me.friwi.arterion.plugin.chat.ChatChannel;
import me.friwi.arterion.plugin.combat.friendlies.FriendlyPlayerList;
import me.friwi.arterion.plugin.combat.friendlies.FriendlyPlayerListProvider;
import me.friwi.arterion.plugin.combat.friendlies.GroupFriendlyPlayerList;
import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.player.ArterionPlayerUtil;
import me.friwi.arterion.plugin.ui.gui.HeadCacheUtil;
import me.friwi.arterion.plugin.ui.gui.ItemGUI;
import me.friwi.arterion.plugin.ui.gui.NamedItemUtil;
import me.friwi.arterion.plugin.ui.gui.TextGUI;
import me.friwi.arterion.plugin.ui.hotbar.HotbarTitleMessageCard;
import me.friwi.arterion.plugin.ui.invite.InvitationHandler;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class Group implements FriendlyPlayerListProvider {
    private ArterionPlugin plugin;
    private ArterionPlayer leader;
    private List<ArterionPlayer> members = new CopyOnWriteArrayList<>();
    private int invites = 0;
    private boolean goldShare = false;
    private boolean xpShare = false;
    private Map<ArterionPlayer, Integer> guiPage = new HashMap<>();
    private boolean disbanded = false;
    private FriendlyPlayerList friendlyPlayerList;

    public Group(ArterionPlugin plugin, ArterionPlayer leader) {
        this.plugin = plugin;
        this.leader = leader;
        this.leader.setGroup(this);
        this.leader.sendTranslation("gui.group.created");
        this.friendlyPlayerList = new GroupFriendlyPlayerList(this);
        updatePlayerGroupAffiliation(leader);
    }

    public ArterionPlayer getLeader() {
        return leader;
    }

    public List<ArterionPlayer> getMembers() {
        return members;
    }

    public int getPlayerCount() {
        return members.size() + 1;
    }

    public boolean isMember(ArterionPlayer player) {
        return player.equals(leader) || members.contains(player);
    }

    public void removePlayer(ArterionPlayer player, boolean kick) {
        player.setGroup(null);
        if (player.getChatChannel() == ChatChannel.GROUP) player.setChatChannel(ChatChannel.GLOBAL);
        if (player.equals(leader)) {
            if (getPlayerCount() <= 1) {
                plugin.getGroupSystem().disbandGroup(this);
                return;
            } else {
                promoteLeader(members.get(0));
            }
        }
        members.remove(player);
        if (kick) {
            sendTranslation("gui.group.kick", player);
            player.sendTranslation("gui.group.youkick");
        } else {
            sendTranslation("gui.group.otherleave", player);
            player.sendTranslation("gui.group.youleave");
        }
        updateGui(0);
        updateGui(1);
        updatePlayerGroupAffiliation(player);
    }

    public void addPlayer(ArterionPlayer player) {
        if (player.getGuild() != null) {
            player.sendTranslation("guild.alreadyinguild");
            return;
        }
        sendTranslation("gui.group.join", player);
        player.sendTranslation("gui.group.youjoin");
        player.setGroup(this);
        members.add(player);
        members.sort(Comparator.comparing(ArterionPlayer::getName));
        updateGui(0);
        updateGui(1);
        updatePlayerGroupAffiliation(player);
    }

    public void promoteLeader(ArterionPlayer member) {
        sendTranslation("gui.group.newleader", member);
        members.add(leader);
        leader = member;
        members.remove(leader);
        members.sort(Comparator.comparing(ArterionPlayer::getName));
        updateGui(1);
    }

    public void onDisband() {
        sendTranslation("gui.group.disband");
        leader.setGroup(null);
        for (ArterionPlayer p : members) p.setGroup(null);
        disbanded = true;
        updatePlayerGroupAffiliation(leader);
    }

    public void sendTranslation(String key, Object... values) {
        leader.sendTranslation(key, values);
        for (ArterionPlayer member : members) member.sendTranslation(key, values);
    }

    public void sendMessage(String msg) {
        leader.sendMessage(msg);
        for (ArterionPlayer member : members) member.sendMessage(msg);
    }

    public void updateGui(int id) {
        List<ArterionPlayer> affected = new LinkedList<>();
        List<ArterionPlayer> drop = new LinkedList<>();
        for (Map.Entry<ArterionPlayer, Integer> entry : guiPage.entrySet()) {
            if (entry.getValue() == id) {
                if ((entry.getKey().equals(leader) || members.contains(entry.getKey()))) {
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
            for (ArterionPlayer a : affected) openGroupDialog(a);
        } else if (id == 1) {
            for (ArterionPlayer a : affected) showMembersDialog(a);
        }
    }

    public void openGroupDialog(ArterionPlayer player) {
        player.closeGui();
        guiPage.put(player, 0);
        if (player.equals(leader)) {
            HeadCacheUtil.supplyCachedHeadSync(heads -> {
                player.openGui(new ItemGUI(player, player.getTranslation("gui.group.title"), () -> {
                    ItemStack[] stacks = new ItemStack[9];
                    stacks[1] = NamedItemUtil.create(Material.INK_SACK, 1, goldShare ? (byte) 10 : (byte) 8, player.getTranslation(goldShare ? "gui.group.disablegold.name" : "gui.group.enablegold.name"),
                            player.getTranslation("gui.group.goldboost", plugin.getFormulaManager().GROUP_GOLD_MULTIPLIER.evaluateFloat(this)));
                    stacks[3] = NamedItemUtil.create(Material.INK_SACK, 1, xpShare ? (byte) 10 : (byte) 8, player.getTranslation(xpShare ? "gui.group.disablexp.name" : "gui.group.enablexp.name"),
                            player.getTranslation("gui.group.xpboost", plugin.getFormulaManager().GROUP_XP_MULTIPLIER.evaluateFloat(this)));
                    stacks[5] = NamedItemUtil.modify(heads[0].getHead(), player.getTranslation(player.equals(leader) ? "gui.group.membersandinv" : "gui.group.members"));
                    stacks[7] = NamedItemUtil.create(Material.INK_SACK, 1, (byte) 1, player.getTranslation("gui.group.leave"));
                    return stacks;
                }, (clickType, i) -> {
                    if (i == 1) {
                        goldShare = !goldShare;
                        sendTranslation(goldShare ? "gui.group.goldenabled" : "gui.group.golddisabled");
                        updateGui(0);
                    } else if (i == 3) {
                        xpShare = !xpShare;
                        sendTranslation(xpShare ? "gui.group.xpenabled" : "gui.group.xpdisabled");
                        updateGui(0);
                    } else if (i == 5) {
                        showMembersDialog(player);
                    } else {
                        removePlayer(player, false);
                        player.closeGui();
                    }
                }, () -> guiPage.remove(player)));
            }, HeadCacheUtil.QUESTION_MARK);
        } else {
            HeadCacheUtil.supplyCachedHeadSync(heads -> {
                player.openGui(new ItemGUI(player, player.getTranslation("gui.group.title"), () -> {
                    ItemStack[] stacks = new ItemStack[9];
                    stacks[1] = NamedItemUtil.create(Material.INK_SACK, 1, goldShare ? (byte) 10 : (byte) 8, player.getTranslation(!goldShare ? "gui.group.disabledgold.name" : "gui.group.enabledgold.name"),
                            player.getTranslation("gui.group.goldboost", plugin.getFormulaManager().GROUP_GOLD_MULTIPLIER.evaluateFloat(this)));
                    stacks[3] = NamedItemUtil.create(Material.INK_SACK, 1, xpShare ? (byte) 10 : (byte) 8, player.getTranslation(!xpShare ? "gui.group.disabledxp.name" : "gui.group.enabledxp.name"),
                            player.getTranslation("gui.group.xpboost", plugin.getFormulaManager().GROUP_XP_MULTIPLIER.evaluateFloat(this)));
                    stacks[5] = NamedItemUtil.modify(heads[0].getHead(), player.getTranslation("gui.group.members"));
                    stacks[7] = NamedItemUtil.create(Material.INK_SACK, 1, (byte) 1, player.getTranslation("gui.group.leave"));
                    return stacks;
                }, (clickType, i) -> {
                    if (i < 4) return;
                    if (i == 5) {
                        showMembersDialog(player);
                    } else {
                        player.openGui(new ItemGUI(player, player.getTranslation("gui.group.leave.confirm"), () -> {
                            ItemStack[] stacks = new ItemStack[9];
                            stacks[0] = NamedItemUtil.create(Material.INK_SACK, 1, (byte) 10, player.getTranslation("gui.group.leave.yes"));
                            stacks[8] = NamedItemUtil.create(Material.INK_SACK, 1, (byte) 1, player.getTranslation("gui.group.leave.no"));
                            return stacks;
                        }, (c, yesno) -> {
                            if (yesno == 0) {
                                player.closeGui();
                                removePlayer(player, false);
                            } else {
                                //Return to main menu
                                this.openGroupDialog(player);
                            }
                        }));
                    }
                }, () -> guiPage.remove(player)));
            }, HeadCacheUtil.QUESTION_MARK);
        }
    }

    private void showMembersDialog(ArterionPlayer player) {
        player.closeGui();
        guiPage.put(player, 1);
        UUID[] requiredHeads = new UUID[2 + getPlayerCount()];
        requiredHeads[0] = HeadCacheUtil.ARROW_LEFT;
        requiredHeads[1] = HeadCacheUtil.QUESTION_MARK;
        requiredHeads[2] = leader.getBukkitPlayer().getUniqueId();
        for (int i = 0; i < members.size(); i++) {
            requiredHeads[i + 3] = members.get(i).getBukkitPlayer().getUniqueId();
        }
        if (HeadCacheUtil.countMissingHeads(requiredHeads) > 0) {
            player.scheduleHotbarCard(new HotbarTitleMessageCard(2000, player, "hotbar.wait"));
        }
        HeadCacheUtil.supplyCachedHeadSync(heads -> {
            int maxmembers = plugin.getFormulaManager().GROUP_MAXMEMBERS.evaluateInt();
            int playerRows = ((maxmembers + 8) / 9 + 1);
            int beginControls = playerRows * 9;
            player.openGui(new ItemGUI(player, player.getTranslation("gui.group.members.title"), () -> {
                ItemStack[] stacks = new ItemStack[(playerRows + 1) * 9];
                stacks[0] = NamedItemUtil.modify(heads[2].getHead(), player.getTranslation("gui.group.members.leader", heads[2].getName()), player.getTranslation("gui.group.members.leadertag"));
                for (int i = 3; i < heads.length; i++) {
                    stacks[i - 2] = NamedItemUtil.modify(heads[i].getHead(), player.getTranslation("gui.group.members.normal", heads[i].getName()));
                }
                stacks[beginControls] = NamedItemUtil.modify(heads[0].getHead(), player.getTranslation("gui.group.members.back"));
                if (player.equals(leader))
                    stacks[beginControls + 8] = NamedItemUtil.modify(heads[1].getHead(), player.getTranslation("gui.group.members.invite"));
                return stacks;
            }, (clickType, i) -> {
                if (i == beginControls) {
                    openGroupDialog(player);
                } else if (i == beginControls + 8) {
                    if (player.equals(leader)) {
                        if (invites + getPlayerCount() >= maxmembers) {
                            player.sendTranslation("gui.group.members.invite.toomany");
                            return;
                        } else {
                            player.openGui(new TextGUI(player, player.getTranslation("gui.group.members.invite.subtitle"), () -> {
                                return new String[]{player.getTranslation("gui.group.members.invite.entername")};
                            }, result -> {
                                invite(player, result);
                                player.closeGui(true);
                            }));
                        }
                    }
                } else {
                    //No actions for non-leaders (and not for the leader on himself
                    if (player.equals(leader) && i != 0) {
                        UUID o = heads[i + 2].getUuid();
                        Player ot = Bukkit.getPlayer(o);
                        if (ot != null && ot.isOnline()) {
                            editMember(player, ArterionPlayerUtil.get(ot));
                        }
                    }
                }
            }, () -> guiPage.remove(player)));
        }, requiredHeads);
    }

    public void invite(ArterionPlayer player, String result) {
        if (invites + getPlayerCount() >= plugin.getFormulaManager().GROUP_MAXMEMBERS.evaluateInt()) {
            player.sendTranslation("gui.group.members.invite.toomany");
            return;
        }
        Player p = Bukkit.getPlayer(result);
        if (p != null && p.isOnline() && !ArterionPlayerUtil.get(p).isVanished()) {
            ArterionPlayer other = ArterionPlayerUtil.get(p);
            if (other.getGroup() == null) {
                if (other.getGuild() != null) {
                    player.sendTranslation("guild.otheralreadyinguild");
                    player.closeGui(true);
                    return;
                }
                invites++;
                ArterionPlugin.getInstance().getInvitationSystem().invite(player, player.getBukkitPlayer().getUniqueId(), other, () -> {
                    other.sendTranslation("gui.group.members.invite.from", player);
                }, new InvitationHandler() {
                    @Override
                    public void onAccept(ArterionPlayer other) {
                        if (other.getGuild() != null) {
                            player.sendTranslation("gui.group.members.invite.declined", other);
                            other.sendTranslation("gui.group.members.invite.youdeclined", player);
                            invites--;
                            return;
                        }
                        if (other.getGroup() == null) {
                            if (disbanded) {
                                other.sendTranslation("gui.group.members.invite.youtimedout", player);
                                invites--;
                                return;
                            }
                            player.sendTranslation("gui.group.members.invite.accepted", other);
                            other.sendTranslation("gui.group.members.invite.youaccepted", player);
                            addPlayer(other);
                            invites--;
                            return;
                        } else {
                            player.sendTranslation("gui.group.members.invite.declined", other);
                            other.sendTranslation("gui.group.members.invite.youalreadygroup");
                        }
                        invites--;
                    }

                    @Override
                    public void onTimeout(ArterionPlayer other) {
                        player.sendTranslation("gui.group.members.invite.timedout", other);
                        other.sendTranslation("gui.group.members.invite.youtimedout", player);
                        invites--;
                    }

                    @Override
                    public void onDeny(ArterionPlayer other) {
                        player.sendTranslation("gui.group.members.invite.declined", other);
                        other.sendTranslation("gui.group.members.invite.youdeclined", player);
                        invites--;
                    }
                });
            } else {
                player.sendTranslation("gui.group.members.invite.alreadygroup");
            }
        } else {
            player.sendTranslation("gui.group.members.invite.notonline");
        }
    }

    private void editMember(ArterionPlayer player, ArterionPlayer member) {
        player.closeGui();
        HeadCacheUtil.supplyCachedHeadSync(heads -> {
            player.openGui(new ItemGUI(player, player.getTranslation("gui.group.member.title", member), () -> {
                ItemStack[] stacks = new ItemStack[18];
                stacks[3] = NamedItemUtil.modify(heads[0].getHead(), player.getTranslation("gui.group.member.promote"));
                stacks[5] = NamedItemUtil.create(Material.INK_SACK, 1, (byte) 1, player.getTranslation("gui.group.member.kick"));
                stacks[9] = NamedItemUtil.modify(heads[1].getHead(), player.getTranslation("gui.group.member.back"));
                return stacks;
            }, (clickType, i) -> {
                if (i == 9) {
                    showMembersDialog(player);
                    return;
                }
                boolean promote = i == 3;
                player.openGui(new ItemGUI(player, player.getTranslation(promote ? "gui.group.member.promote.confirm" : "gui.group.member.kick.confirm"), () -> {
                    ItemStack[] stacks = new ItemStack[9];
                    stacks[0] = NamedItemUtil.create(Material.INK_SACK, 1, (byte) 10, player.getTranslation("gui.group.member.yes"));
                    stacks[8] = NamedItemUtil.create(Material.INK_SACK, 1, (byte) 1, player.getTranslation("gui.group.member.no"));
                    return stacks;
                }, (c, yesno) -> {
                    if (yesno == 0) {
                        if (promote) {
                            promoteLeader(member);
                        } else {
                            removePlayer(member, true);
                        }
                        showMembersDialog(player);
                    } else {
                        //Return to member menu
                        this.editMember(player, member);
                    }
                }));
            }));
        }, HeadCacheUtil.ARROW_UP, HeadCacheUtil.ARROW_LEFT);
    }

    public boolean isGoldShare() {
        return goldShare;
    }

    public boolean isXpShare() {
        return xpShare;
    }

    public void updatePlayerGroupAffiliation(ArterionPlayer p) {
        p.getPlayerScoreboard().updateAllPlayerRelations();
    }

    @Override
    public FriendlyPlayerList getFriendlyPlayerList() {
        return friendlyPlayerList;
    }
}
