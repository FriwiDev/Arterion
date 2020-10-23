package me.friwi.arterion.plugin.ui.scoreboard;

import me.friwi.arterion.plugin.ArterionPlugin;
import me.friwi.arterion.plugin.combat.PlayerRelation;
import me.friwi.arterion.plugin.combat.classes.ClassEnum;
import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.player.ArterionPlayerUtil;
import me.friwi.arterion.plugin.ui.mod.ModPacket;
import me.friwi.arterion.plugin.ui.mod.packet.Packet01ModVersion;
import me.friwi.arterion.plugin.ui.mod.packet.Packet04FriendlyCreateOrUpdate;
import me.friwi.arterion.plugin.ui.mod.packet.Packet05FriendlyRemove;
import me.friwi.arterion.plugin.ui.mod.server.ModConnection;
import me.friwi.arterion.plugin.ui.mod.server.ModValueEnum;
import me.friwi.arterion.plugin.util.scheduler.InternalTask;
import me.friwi.arterion.plugin.world.region.Region;
import me.friwi.recordable.RecordingCreator;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class PlayerScoreboard {
    private ArterionPlayer arterionPlayer;

    private Scoreboard board;

    private Team friendlies;

    private Objective healthObjective;

    private String[] sidebarLines = new String[13]; //Change this number for fewer lines

    private Objective sidebar;
    private Objective tablist;

    //Performance
    private ClassEnum cacheClass = null;
    private int cacheLevel = -10;
    private int cachePLevel = -10;
    private Region cacheRegion = null;
    private long cacheMoney = -10;

    //Clientmod
    private Set<UUID> clientFriendlies = new HashSet<>();
    private boolean attemptedUse = false;

    public PlayerScoreboard(ArterionPlayer arterionPlayer) {
        this.arterionPlayer = arterionPlayer;
    }

    public void setup() {
        ScoreboardManager manager = Bukkit.getScoreboardManager();

        board = manager.getNewScoreboard();

        friendlies = board.registerNewTeam("friendlies§f");
        friendlies.setAllowFriendlyFire(false);
        friendlies.setCanSeeFriendlyInvisibles(true);
        friendlies.setPrefix("\247a");

        friendlies.addEntry(arterionPlayer.getName());

        healthObjective = board.registerNewObjective("test", "dummy");
        healthObjective.setDisplayName("\247c\u2764");
        healthObjective.setDisplaySlot(DisplaySlot.BELOW_NAME);

        sidebar = board.registerNewObjective("sidebar", "dummy");
        sidebar.setDisplayName(arterionPlayer.getLanguage().getTranslation("scoreboard.title").translate().getMessage());
        sidebar.setDisplaySlot(DisplaySlot.SIDEBAR);

        tablist = board.registerNewObjective("tablist", "dummy");
        tablist.setDisplaySlot(DisplaySlot.PLAYER_LIST);

        for (Player p : ArterionPlugin.getOnlinePlayers()) {
            ArterionPlayer other = ArterionPlayerUtil.get(p);
            if (other != null) {
                updatePlayerLevel(other);
                other.getPlayerScoreboard().updatePlayerLevel(arterionPlayer);
            }
        }

        arterionPlayer.getBukkitPlayer().setScoreboard(board);

        updateAllPlayerRelations();

        ArterionPlugin.getInstance().getSchedulers().getMainScheduler().executeInSpigotCircleLater(new InternalTask() {
            @Override
            public void run() {
                updateSidebar(true);
            }
        }, 5l);
    }

    public void updateSidebar() {
        updateSidebar(false);
    }

    public void updateSidebar(boolean force) {
        if (sidebar == null || sidebarLines == null) return;
        int i = 4;
        boolean flag = false;
        if (force || arterionPlayer.getSelectedClass() != cacheClass || arterionPlayer.getLevel() != cacheLevel || arterionPlayer.getPrestigeLevel() != cachePLevel) {
            cacheClass = arterionPlayer.getSelectedClass();
            cacheLevel = arterionPlayer.getLevel();
            cachePLevel = arterionPlayer.getPrestigeLevel();
            setLine(0, "\2470");
            if (arterionPlayer.getSelectedClass() == null || arterionPlayer.getSelectedClass() == ClassEnum.NONE) {
                setLine(1, arterionPlayer.getTranslation("scoreboard.class_choose1"));
                setLine(2, arterionPlayer.getTranslation("scoreboard.class_choose2"));
                setLine(3, "\2471");
            } else {
                setLine(1, arterionPlayer.getTranslation("scoreboard.class", arterionPlayer.getTranslation("class." + arterionPlayer.getSelectedClass().toString().toLowerCase())));
                setLine(2, arterionPlayer.getTranslation("scoreboard.classv", arterionPlayer.getTranslation("class." + arterionPlayer.getSelectedClass().toString().toLowerCase())));
                setLine(3, "\2471");
                if (arterionPlayer.getPrestigeLevel() > 0) {
                    setLine(4, "\247f" + arterionPlayer.getTranslation("scoreboard.plevel", arterionPlayer.getLevel(), arterionPlayer.getPrestigeLevel()));
                    setLine(5, "\247f" + arterionPlayer.getTranslation("scoreboard.plevelv", arterionPlayer.getLevel(), arterionPlayer.getPrestigeLevel()));
                } else {
                    setLine(4, "\247f" + arterionPlayer.getTranslation("scoreboard.level", arterionPlayer.getLevel()));
                    setLine(5, "\247f" + arterionPlayer.getTranslation("scoreboard.levelv", arterionPlayer.getLevel()));
                }
                setLine(6, "\2472");
            }
            flag = true;
        }
        if (arterionPlayer.getSelectedClass() != null && arterionPlayer.getSelectedClass() != ClassEnum.NONE) {
            i = 7;
        }
        if ((arterionPlayer.getRegion() != null && !arterionPlayer.getRegion().equals(cacheRegion)) || flag) {
            cacheRegion = arterionPlayer.getRegion();
            String region = arterionPlayer.getRegion() == null ? "undefined" : arterionPlayer.getRegion().getName(arterionPlayer.getLanguage());
            setLine(i + 0, arterionPlayer.getTranslation("scoreboard.region", region));
            setLine(i + 1, arterionPlayer.getTranslation("scoreboard.regionv", region));
            setLine(i + 2, "\2473");
        }
        if (arterionPlayer.getBagMoneyBearer().getCachedMoney() != cacheMoney || flag) {
            cacheMoney = arterionPlayer.getBagMoneyBearer().getCachedMoney();
            setLine(i + 3, arterionPlayer.getTranslation("scoreboard.gold", arterionPlayer.getBagMoneyBearer().getCachedMoney() / 100d));
            setLine(i + 4, arterionPlayer.getTranslation("scoreboard.goldv", arterionPlayer.getBagMoneyBearer().getCachedMoney() / 100d));
            setLine(i + 5, "\2474");
        }
    }

    public void setLine(int i, String s) {
        if (sidebar == null) return; //Will be updated later
        if (sidebarLines[i] != null) {
            if (sidebarLines[i].equals(s)) return;
            board.resetScores(sidebarLines[i]);
        }
        sidebarLines[i] = s;
        sidebar.getScore(s).setScore(sidebarLines.length - i);
    }

    public void updateAllPlayerRelations() {
        if (friendlies == null)
            return; //Will be updated by the next player, setup not completed yet
        String tag = "";
        if (arterionPlayer.getGuild() != null) tag = "\2478[\247e" + arterionPlayer.getGuild().getTag() + "\2478] ";
        friendlies.setPrefix(tag + "\247a");
        for (Player p : ArterionPlugin.getOnlinePlayers()) {
            ArterionPlayer other = ArterionPlayerUtil.get(p);
            if (other != null) {
                updatePlayerRelation(other);
                other.getPlayerScoreboard().updatePlayerRelation(arterionPlayer);
            }
        }
    }

    public void sendFriendly(ArterionPlayer other) {
        ModConnection.sendModPacket(arterionPlayer, new Packet04FriendlyCreateOrUpdate(other.getBukkitPlayer().getUniqueId(),
                other.getName(),
                other.getSelectedClass().name(),
                other.getSelectedClass().getName(arterionPlayer.getLanguage()),
                other.getLevel(),
                other.getHealth(),
                other.getMaxHealth()));
    }

    public void updatePlayerLevel(ArterionPlayer other) {
        updatePlayerLevel(other, other.getLevel());
    }

    public void updatePlayerLevel(ArterionPlayer other, int level) {
        if (tablist == null) return;
        Score score = tablist.getScore(other.getName());
        if (score != null) score.setScore(level);
        if (arterionPlayer.getPlayerRelation(other) == PlayerRelation.FRIENDLY) {
            sendFriendly(other);
        }
    }

    public void updatePlayerRelation(ArterionPlayer other) {
        if (friendlies == null)
            return; //Will be updated by the next player, setup not completed yet
        if (other.equals(this.arterionPlayer)) return; //Dont update self
        PlayerRelation relation = arterionPlayer.getPlayerRelation(other);
        if (relation == PlayerRelation.FRIENDLY) {
            if (!friendlies.hasEntry(other.getName())) {
                Team prev = board.getEntryTeam(other.getName());
                if (prev != null) prev.unregister();
                friendlies.addEntry(other.getName());
            }
            if (arterionPlayer.getSkillSlots().usesMod()) {
                sendFriendly(other);
            }
        } else {
            if (arterionPlayer.getSkillSlots().usesMod()) {
                ModConnection.sendModPacket(arterionPlayer, new Packet05FriendlyRemove(other.getBukkitPlayer().getUniqueId()));
            }
            if (friendlies.hasEntry(other.getName())) friendlies.removeEntry(other.getName());
            Team prev = board.getEntryTeam(other.getName());
            if (prev == null) {
                prev = board.registerNewTeam(other.getName());
                prev.addEntry(other.getName());
            }
            String arteColor = other.isArtefactCarrier() ? "\2475" : "";
            if (relation == PlayerRelation.NEUTRAL) {
                String tag = "";
                if (other.getGuild() != null) tag = "\2478[\247e" + other.getGuild().getTag() + "\2478] ";
                prev.setPrefix(tag + ChatColor.getLastColors(arterionPlayer.getTranslation(other.getRank().getRankTranslation())) + arteColor);
                prev.setSuffix(" \2478[\247e" + other.getLevel() + "\2478]");
            } else if (this.arterionPlayer.getFriendlyPlayerList().isPlayerMarked(other)) {
                //Mark player name RED
                String tag = "";
                if (other.getGuild() != null) tag = "\2478[\247c" + other.getGuild().getTag() + "\2478] ";
                prev.setPrefix(tag + "\2474" + arteColor);
                prev.setSuffix(" \2478[\247c" + other.getLevel() + "\2478]");
            } else {
                String tag = "";
                if (other.getGuild() != null) tag = "\2478[\247c" + other.getGuild().getTag() + "\2478] ";
                prev.setPrefix(tag + "\2477" + arteColor);
                prev.setSuffix(" \2478[\247c" + other.getLevel() + "\2478]");
            }
        }

        this.updatePlayerHealth(other);
    }

    public void updatePlayerHealth(ArterionPlayer other) {
        if (healthObjective != null) healthObjective.getScore(other.getName()).setScore(other.getHealth());
        if (arterionPlayer.getPlayerRelation(other) == PlayerRelation.FRIENDLY) {
            sendFriendly(other);
        }
    }

    public void updateHealth() {
        for (Player p : ArterionPlugin.getOnlinePlayers()) {
            ArterionPlayer other = ArterionPlayerUtil.get(p);
            if (other != null) {
                other.getPlayerScoreboard().updatePlayerHealth(arterionPlayer);
            }
        }
        //Update replays
        for (RecordingCreator c : ArterionPlugin.getInstance().getRecordingManager().getActiveRecordings()) {
            c.addOrUpdateScore(arterionPlayer.getName(), "hp", arterionPlayer.getHealth());
        }
    }

    //Mod Stuff

    public void handleModPacket(ModPacket packet) {
        if (packet instanceof Packet01ModVersion) {
            if (!attemptedUse) {
                if (((Packet01ModVersion) packet).getVersion() > ModConnection.PROTOCOL_VERSION) {
                    arterionPlayer.sendTranslation("mod.downgrade");
                    return;
                } else if (((Packet01ModVersion) packet).getVersion() < ModConnection.PROTOCOL_VERSION) {
                    arterionPlayer.sendTranslation("mod.upgrade");
                    return;
                }
            }
            attemptedUse = true;
            arterionPlayer.getSkillSlots().markAsModUser();
            if (sidebar != null) {
                sidebar.unregister();
                sidebar = null;
                sidebarLines = null;
            }
            syncModValues();
            updateAllPlayerRelations();
        }
    }

    public void updateModValue(ModValueEnum valueEnum) {
        if (!arterionPlayer.usesMod()) return;
        valueEnum.send(arterionPlayer);
    }

    public void syncModValues() {
        if (!arterionPlayer.usesMod()) return;
        for (ModValueEnum modValue : ModValueEnum.values()) {
            updateModValue(modValue);
        }
    }
}
