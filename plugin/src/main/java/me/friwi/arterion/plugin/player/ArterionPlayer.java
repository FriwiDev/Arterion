package me.friwi.arterion.plugin.player;

import me.friwi.arterion.plugin.ArterionPlugin;
import me.friwi.arterion.plugin.anticheat.BasicAntiCheat;
import me.friwi.arterion.plugin.chat.ChatChannel;
import me.friwi.arterion.plugin.combat.PlayerRelation;
import me.friwi.arterion.plugin.combat.classes.ClassEnum;
import me.friwi.arterion.plugin.combat.classes.StartWeapons;
import me.friwi.arterion.plugin.combat.friendlies.ArterionPlayerFriendlyPlayerList;
import me.friwi.arterion.plugin.combat.friendlies.FriendlyPlayerList;
import me.friwi.arterion.plugin.combat.friendlies.FriendlyPlayerListProvider;
import me.friwi.arterion.plugin.combat.gamemode.TemporaryWorld;
import me.friwi.arterion.plugin.combat.gamemode.arena.ArenaInitializer;
import me.friwi.arterion.plugin.combat.gamemode.artefact.Artefact;
import me.friwi.arterion.plugin.combat.gamemode.artefact.ArtefactCarrier;
import me.friwi.arterion.plugin.combat.gamemode.external.ExternalFight;
import me.friwi.arterion.plugin.combat.group.Group;
import me.friwi.arterion.plugin.combat.quest.Quest;
import me.friwi.arterion.plugin.combat.quest.QuestEnum;
import me.friwi.arterion.plugin.combat.skill.Skill;
import me.friwi.arterion.plugin.combat.skill.SkillEnum;
import me.friwi.arterion.plugin.combat.skill.SkillSlots;
import me.friwi.arterion.plugin.combat.team.Team;
import me.friwi.arterion.plugin.economy.BagMoneyBearer;
import me.friwi.arterion.plugin.economy.BankMoneyBearer;
import me.friwi.arterion.plugin.formula.ArterionFormula;
import me.friwi.arterion.plugin.guild.Guild;
import me.friwi.arterion.plugin.jobs.JobEnum;
import me.friwi.arterion.plugin.permissions.Rank;
import me.friwi.arterion.plugin.sanctions.SanctionType;
import me.friwi.arterion.plugin.stats.StatType;
import me.friwi.arterion.plugin.stats.list.GlobalStats;
import me.friwi.arterion.plugin.stats.object.StatObject;
import me.friwi.arterion.plugin.stats.object.StatObjectType;
import me.friwi.arterion.plugin.stats.tracker.StatObjectTracker;
import me.friwi.arterion.plugin.ui.gui.GUI;
import me.friwi.arterion.plugin.ui.hotbar.*;
import me.friwi.arterion.plugin.ui.mod.ModPacket;
import me.friwi.arterion.plugin.ui.mod.packet.Packet05FriendlyRemove;
import me.friwi.arterion.plugin.ui.mod.packet.Packet08TextGui;
import me.friwi.arterion.plugin.ui.mod.server.ModConnection;
import me.friwi.arterion.plugin.ui.mod.server.ModValueEnum;
import me.friwi.arterion.plugin.ui.scoreboard.PlayerScoreboard;
import me.friwi.arterion.plugin.util.database.Database;
import me.friwi.arterion.plugin.util.database.DatabaseObjectTask;
import me.friwi.arterion.plugin.util.database.DatabaseTask;
import me.friwi.arterion.plugin.util.database.entity.DatabasePlayer;
import me.friwi.arterion.plugin.util.database.entity.DatabaseSanction;
import me.friwi.arterion.plugin.util.language.api.Language;
import me.friwi.arterion.plugin.util.language.api.LanguageAPI;
import me.friwi.arterion.plugin.util.scheduler.InternalTask;
import me.friwi.arterion.plugin.world.chunk.ArterionChunkUtil;
import me.friwi.arterion.plugin.world.region.GuildRegion;
import me.friwi.arterion.plugin.world.region.Region;
import org.bukkit.*;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;
import java.util.function.Function;

public class ArterionPlayer implements FriendlyPlayerListProvider, StatObject {
    public int teleportIndex = 0;
    private DatabasePlayer persistenceHolder;
    private Language language;
    private Player bukkitPlayer;
    private Location cachedHomeLocation;
    private Region region;
    private Location lastKnownSafePosition;
    private PlayerScoreboard playerScoreboard;
    private ChatChannel chatChannel = ChatChannel.GLOBAL;
    private String hotbarMessage = "";
    private BagMoneyBearer bagMoneyBearer;
    private BankMoneyBearer bankMoneyBearer;
    private GUI openGui = null;
    private Group group;
    private Guild guild;
    private Team team;
    private boolean damageDebug = false;
    private int maxHealth = 0;
    private int regenHealth = 0;
    private SkillSlots skillSlots;
    private int mana = 0;
    private int maxMana = 0;
    private boolean previousNewbieProtection;
    private Queue<HotbarCard> hotbarCardQueue = new ConcurrentLinkedQueue<>();
    private HotbarCard currentHotbarCard = null;
    private FriendlyPlayerList friendlyPlayerList;
    private PlayerPotionTracker potionTracker;
    private ClericHealManager clericHealManager;
    private String lastWhisperer = "somewaytoolongplayernamethatwillnotexist";
    private Skill lastAffectedDamageSkill = null;
    private ArtefactCarrier artefactCarrier;
    private ArenaInitializer arenaInitializer;
    private Location temporaryWorldBackupLocation;
    private long lastPortalMessage;
    private ExternalFight respawnFight;
    private StatObjectTracker<ArterionPlayer> statObjectTracker;
    private int announcementIndex = -1;
    private boolean vanished = false;
    private long vanishTime = 0;
    private long afkTime = System.currentTimeMillis();
    private Quest quest;
    private BasicAntiCheat bac;
    private boolean anticheatDebug = false;

    public ArterionPlayer(Player bukkitPlayer, DatabasePlayer databasePlayer) {
        this.persistenceHolder = databasePlayer;
        this.bukkitPlayer = bukkitPlayer;
        this.language = LanguageAPI.getLanguage(databasePlayer.getLocale());
        this.playerScoreboard = new PlayerScoreboard(this);
        this.bagMoneyBearer = new BagMoneyBearer(this, databasePlayer.getGold());
        this.bankMoneyBearer = new BankMoneyBearer(this, databasePlayer.getBank());
        this.cachedHomeLocation = databasePlayer.getClaimWorld() == null ? null : new Location(Bukkit.getWorld(databasePlayer.getClaimWorld()), databasePlayer.getHomeX(), databasePlayer.getHomeY(), databasePlayer.getHomeZ());
        this.skillSlots = new SkillSlots(this);
        this.previousNewbieProtection = hasNewbieProtection();
        this.friendlyPlayerList = new ArterionPlayerFriendlyPlayerList(this);
        this.potionTracker = new PlayerPotionTracker(this);
        this.clericHealManager = new ClericHealManager(this);
        this.statObjectTracker = new StatObjectTracker<>(this);
        this.bac = new BasicAntiCheat(this);
        if (this.getRank().isHigherTeam()) anticheatDebug = true;
    }

    public DatabasePlayer getPersistenceHolder() {
        return persistenceHolder;
    }

    public Language getLanguage() {
        return language;
    }

    public void setLanguage(Language language) {
        this.language = language;
    }

    public Player getBukkitPlayer() {
        return bukkitPlayer;
    }

    public void onJoin() {
        this.updateRegion();

        //Quest
        if (persistenceHolder.getQuestId() != -1) {
            quest = QuestEnum.getQuestById(persistenceHolder.getQuestId());
            quest.readFrom(persistenceHolder.getQuestData());
        }

        //When player got kicked from guild while offline, notify him and teleport him to safe destination
        //if he is still on guild grounds
        if (getKickedFromGuild() != null) {
            sendTranslation("line");
            sendTranslation("gui.guild.youkicked");
            sendTranslation("line");
            if (getRegion() instanceof GuildRegion) {
                ArterionPlugin.getInstance().getSchedulers().getMainScheduler().executeInSpigotCircle(new InternalTask() {
                    @Override
                    public void run() {
                        getBukkitPlayer().teleport(ArterionPlugin.getInstance().getArterionConfig().spawn);
                        ArterionPlayer.this.updateRegion(ArterionPlugin.getInstance().getArterionConfig().spawn);
                    }
                });
            }
            setKickedFromGuild(null, success -> {
            });
        }

        //Notify player when noob protection has expired during inactivity
        if (hasNewbieProtection() != hasNewbieProtection(persistenceHolder.getLastOnline())) {
            sendTranslation("player.newbie.protectionexpired");
            bukkitPlayer.playSound(bukkitPlayer.getLocation(), Sound.ANVIL_BREAK, 0.7f, 1);
        }

        //Notify player of remaining noob protection, if any
        if (hasNewbieProtection()) {
            long remaining = getRemainingNewbieProtection();
            long hours = remaining / (60 * 60 * 1000);
            long minutes = remaining % (60 * 60 * 1000) / (60 * 1000);
            sendTranslation("player.newbie.protectionactive", hours, minutes);
        }

        //Recalculate player hp
        this.recalculatePlayerMeta();

        //Update skills
        skillSlots.resetSkillSlots();
    }

    public void onQuit() {
        //Database actions here may not complete on shutdown
        updateInDB(p -> p.setLastOnline(System.currentTimeMillis()), success -> {
        });
        //Remove from all friendly player lists
        ModPacket packet = new Packet05FriendlyRemove(this.getBukkitPlayer().getUniqueId());
        for (Player p : ArterionPlugin.getOnlinePlayers()) {
            ArterionPlayer ap = ArterionPlayerUtil.get(p);
            if (ap.usesMod()) ModConnection.sendModPacket(ap, packet);
        }
        //Reset artefact
        if (isArtefactCarrier()) getArtefactCarrier().onDie();
        //Notify region
        if (region != null) region.onLeave(this);
        //Remove from arena initializer
        if (getArenaInitializer() != null) getArenaInitializer().remove(this, false);
        //Save stats
        ArterionPlugin.getInstance().getSchedulers().getDatabaseScheduler().executeInMyCircle(new InternalTask() {
            @Override
            public void run() {
                GlobalStats.getContext().getStatTracker().stopTracking(ArterionPlugin.getInstance().getExternalDatabase(), ArterionPlayer.this);
                GlobalStats.getTopContext().getStatTracker().stopTracking(ArterionPlugin.getInstance().getExternalDatabase(), ArterionPlayer.this);
                getStatTracker().stopAllTrackers(ArterionPlugin.getInstance().getExternalDatabase());
            }
        });
        //Teleport to safe position
        if (lastKnownSafePosition != null) bukkitPlayer.teleport(lastKnownSafePosition);
    }

    private void updateRegion() {
        this.lastKnownSafePosition = bukkitPlayer.getLocation().clone();
        Region n = ArterionChunkUtil.getNonNull(bukkitPlayer.getLocation().getChunk()).getRegion();
        if (this.region == null) {
            n.onEnter(this);
        } else if (!this.region.equals(n)) {
            this.region.onLeave(this);
            n.onEnter(this);
        }
        this.region = n;
    }

    public void updateRegion(Location loc) {
        this.lastKnownSafePosition = loc.clone();
        Region n = ArterionChunkUtil.getNonNull(loc.getChunk()).getRegion();
        if (this.region == null) {
            n.onEnter(this);
        } else if (!this.region.equals(n)) {
            this.region.onLeave(this);
            n.onEnter(this);
        }
        this.region = n;
        this.getPlayerScoreboard().updateAllPlayerRelations();
        this.getPlayerScoreboard().updateModValue(ModValueEnum.REGION);
    }

    public void updatePlayer() {
        //Check if noob protection expired
        if (hasNewbieProtection() != previousNewbieProtection) {
            previousNewbieProtection = hasNewbieProtection();
            sendTranslation("player.newbie.protectionexpired");
            bukkitPlayer.playSound(bukkitPlayer.getLocation(), Sound.ANVIL_BREAK, 0.7f, 1);
        }

        //Afk
        if (this.lastKnownSafePosition != null && !this.lastKnownSafePosition.equals(this.getBukkitPlayer().getLocation())) {
            resetAfkTime();
        }

        //Nether ceiling exploit fix
        if (bukkitPlayer.getGameMode() != GameMode.CREATIVE && bukkitPlayer.getGameMode() != GameMode.SPECTATOR && !isVanished()) {
            if (this.lastKnownSafePosition != null && this.getBukkitPlayer().getWorld().getEnvironment() == World.Environment.NETHER && this.getBukkitPlayer().getLocation().getY() > 127) {
                this.getBukkitPlayer().teleport(this.lastKnownSafePosition);
            }
        }

        //Check regions
        Region newRegion = ArterionChunkUtil.getNonNull(bukkitPlayer.getLocation().getChunk()).getRegion();
        if (newRegion != region) {
            if (newRegion == null || region == null) return; //We do not want a greet ´message on join or bug
            if (!newRegion.equals(region)) {
                if (newRegion.isNoEnter(this) && this.region.isPvp()) {
                    if (bukkitPlayer.getGameMode() == GameMode.CREATIVE || bukkitPlayer.getGameMode() == GameMode.SPECTATOR || isVanished()) {
                        this.updateRegion();
                        newRegion.greetMsg(this);
                        getPlayerScoreboard().updateAllPlayerRelations();
                        getPlayerScoreboard().updateModValue(ModValueEnum.REGION);
                        return;
                    }
                    bukkitPlayer.teleport(this.lastKnownSafePosition);
                    newRegion.denyMsg(this);
                    Region checkOld = ArterionChunkUtil.getNonNull(this.lastKnownSafePosition.getChunk()).getRegion();
                    //Player already glitched in somehow, lets unfreeze him and accept the new region
                    if (checkOld.equals(newRegion)) {
                        updateRegion();
                        getPlayerScoreboard().updateAllPlayerRelations();
                        getPlayerScoreboard().updateModValue(ModValueEnum.REGION);
                    }
                    return;
                } else {
                    //Send region greetings
                    newRegion.greetMsg(this);
                    this.updateRegion();
                    getPlayerScoreboard().updateAllPlayerRelations();
                    getPlayerScoreboard().updateModValue(ModValueEnum.REGION);
                    return;
                }
            }
        }
        this.updateRegion();
    }

    public Region getRegion() {
        return region;
    }

    public void sendMessage(String message) {
        this.bukkitPlayer.sendMessage(message);
    }

    public void sendTranslation(String key, Object... values) {
        this.sendMessage(getTranslation(key, values));
    }

    public String getTranslation(String key, Object... values) {
        return this.getLanguage().getTranslation(key).translate(values).getMessage();
    }

    public Rank getRank() {
        return persistenceHolder.getRank();
    }

    public String getName() {
        return bukkitPlayer.getName();
    }

    public PlayerScoreboard getPlayerScoreboard() {
        return playerScoreboard;
    }

    public PlayerRelation getPlayerRelation(ArterionPlayer other) {
        if (other == null) return PlayerRelation.ENEMY;
        if (this.equals(other)) return PlayerRelation.FRIENDLY;
        if (other.getBukkitPlayer().getGameMode() == GameMode.CREATIVE || other.getBukkitPlayer().getGameMode() == GameMode.SPECTATOR || other.isVanished())
            return PlayerRelation.NEUTRAL;
        if (getBukkitPlayer().getGameMode() == GameMode.CREATIVE || getBukkitPlayer().getGameMode() == GameMode.SPECTATOR || isVanished())
            return PlayerRelation.NEUTRAL;
        if (this.getTeam() != null && other.getTeam() != null) {
            if (this.getTeam().equals(other.getTeam())) {
                return PlayerRelation.FRIENDLY;
            } else {
                return PlayerRelation.ENEMY;
            }
        }
        if (other.getGuild() != null && getGuild() != null && other.getGuild() == getGuild())
            return PlayerRelation.FRIENDLY;
        if (other.getGroup() != null && getGroup() != null && other.getGroup() == getGroup())
            return PlayerRelation.FRIENDLY;
        if (this.getRoomMate() != null && this.getRoomMate().equals(other.getBukkitPlayer().getUniqueId()))
            return PlayerRelation.FRIENDLY;
        if (other.getRegion() != null && !other.getRegion().isPvp()) return PlayerRelation.NEUTRAL;
        if (getRegion() != null && !getRegion().isPvp()) return PlayerRelation.NEUTRAL;
        return PlayerRelation.ENEMY;
    }

    public int getHealth() {
        if (!bukkitPlayer.isOnline()) return 0;
        return (int) (bukkitPlayer.getHealth() / bukkitPlayer.getMaxHealth() * getMaxHealth());
    }

    public void setHealth(int health) {
        double ch = (health + 0d) / (getMaxHealth() + 0d) * bukkitPlayer.getMaxHealth();
        if (ch < 0) ch = 0;
        if (ch > bukkitPlayer.getMaxHealth()) ch = bukkitPlayer.getMaxHealth();
        bukkitPlayer.setHealth(ch);
    }

    public void heal(int amount) {
        this.setHealth(this.getHealth() + amount);
        this.getPlayerScoreboard().updateHealth();
        if (isDamageDebug()) {
            sendTranslation("damage.debug.heal", amount);
        }
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    public int getRegenHealth() {
        return regenHealth;
    }

    public int getMana() {
        return this.mana;
    }

    public void setMana(int mana) {
        this.mana = mana;
        if (this.mana > maxMana) this.mana = maxMana;
        if (this.mana < 0) this.mana = 0;
        getPlayerScoreboard().updateModValue(ModValueEnum.MANA);
    }

    public int getLevel() {
        return persistenceHolder.getLevel();
    }

    public int getMaxLevel() {
        return persistenceHolder.getMaxLevel();
    }

    public int getMaxMana() {
        return this.maxMana;
    }

    public ClassEnum getSelectedClass() {
        return persistenceHolder.getSelectedClass();
    }

    public void setLevel(int level, Consumer<Boolean> callback) {
        updateInDB(p -> {
            p.setLevel(level);
            if (p.getMaxLevel() < level) p.setMaxLevel(level);
        }, success -> {
            ArterionPlugin.getInstance().getSchedulers().getDatabaseScheduler().executeInSpigotCircle(new InternalTask() {
                @Override
                public void run() {
                    getPlayerScoreboard().updateModValue(ModValueEnum.XP_PER_MILLE);
                    getPlayerScoreboard().updateModValue(ModValueEnum.LEVEL);
                    ArterionPlayer.this.recalculatePlayerMeta();
                    skillSlots.updateSkillSlots(true);
                    for (Player p : ArterionPlugin.getOnlinePlayers())
                        ArterionPlayerUtil.get(p).getPlayerScoreboard().updatePlayerLevel(ArterionPlayer.this, level);
                    ArterionPlugin.getInstance().getSchedulers().getDatabaseScheduler().executeInSpigotCircleLater(new InternalTask() {
                        @Override
                        public void run() {
                            ArterionPlayer.this.getPlayerScoreboard().updateAllPlayerRelations();
                        }
                    }, 1);
                }
            });
            callback.accept(success);
        });
    }

    public void removeXP(int xp) {
        this.addXP(-xp);
    }

    public void setSelectedClass(ClassEnum selectedClass, Consumer<Boolean> callback) {
        ClassEnum previous = getSelectedClass();
        updateInDB(p -> {
            p.setSelectedClass(selectedClass);
            Integer xp = p.getClassXp().get(selectedClass);
            if (xp == null) xp = 0;
            int level = PlayerLevelCalculator.getLevelFromXP(xp);
            if (level != getLevel()) {
                p.setLevel(level);
            }
        }, success -> {
            ArterionPlugin.getInstance().getSchedulers().getDatabaseScheduler().executeInSpigotCircle(new InternalTask() {
                @Override
                public void run() {
                    ArterionPlayer.this.recalculatePlayerMeta();
                    ArterionPlayer.this.scheduleHotbarCard(new HotbarClassSelectedCard(ArterionPlayer.this, selectedClass));
                    skillSlots.updateSkillSlots(true);
                    if (previous == null || previous == ClassEnum.NONE) {
                        StartWeapons.giveStartWeapons(ArterionPlayer.this, selectedClass);
                        skillSlots.giveSkillDisks();
                        StartWeapons.giveAdditionalStartWeapons(ArterionPlayer.this, selectedClass);
                        ArterionPlayer.this.sendTranslation("command.skilldisc.give");
                    }
                    ArterionPlugin.getInstance().getSchedulers().getDatabaseScheduler().executeInSpigotCircleLater(new InternalTask() {
                        @Override
                        public void run() {
                            getPlayerScoreboard().updateModValue(ModValueEnum.SELECTED_CLASS);
                            getPlayerScoreboard().updateModValue(ModValueEnum.SELECTED_CLASS_NAME);
                            getPlayerScoreboard().updateModValue(ModValueEnum.LEVEL);
                            for (Player p : ArterionPlugin.getOnlinePlayers()) {
                                ArterionPlayerUtil.get(p).getPlayerScoreboard().updatePlayerLevel(ArterionPlayer.this, getLevel());
                            }
                        }
                    }, 1);
                }
            });
            callback.accept(success);
        });
    }

    public void addXP(int xpadd) {
        addXP(xpadd, 1);
    }

    public void addXP(int xpadd, double alreadyAppliedBoost) {
        if (getSelectedClass() == null || getSelectedClass() == ClassEnum.NONE) return;
        if (quest != null) {
            quest.onEarnXP(this, xpadd);
        }
        this.trackStatistic(StatType.XP, 0, v -> v + xpadd);
        if (getLevel() >= PlayerLevelCalculator.getMaxLevel() && xpadd > 0 && getPrestigeLevel() >= PlayerPrestigeLevelCalculator.getMaxLevel())
            return;
        updateInDB(p -> {
            if (getLevel() >= PlayerLevelCalculator.getMaxLevel() && xpadd > 0 && getPrestigeLevel() >= PlayerPrestigeLevelCalculator.getMaxLevel())
                return;
            if (getSelectedClass() == null || getSelectedClass() == ClassEnum.NONE) return;
            Integer xp = persistenceHolder.getClassXp().get(getSelectedClass());
            if (xp == null) xp = Integer.valueOf(0);
            int maxXp = PlayerLevelCalculator.getTotalXPNeededForLevel(PlayerLevelCalculator.getMaxLevel());
            int prestigeAdd = 0;
            int a = xp.intValue() + xpadd;
            if (a < 0) {
                prestigeAdd = a;
                a = 0;
            } else if (a > maxXp) {
                prestigeAdd = a - maxXp;
                a = maxXp;
            }
            int prestige = getPrestigeXp() + prestigeAdd;
            if (prestige < 0) prestige = 0;
            int finalXp = a;
            int finalPrestige = prestige;
            p.getClassXp().put(getSelectedClass(), finalXp);
            p.setPrestigeXp(finalPrestige);
        }, success -> {
            if (success) {
                scheduleHotbarCard(new HotbarXPCard(ArterionPlayer.this, xpadd, alreadyAppliedBoost));
                getPlayerScoreboard().updateModValue(ModValueEnum.XP_PER_MILLE);
                recalculateLevel();
            }
        });
    }

    public void recalculateLevel() {
        Integer xp = persistenceHolder.getClassXp().get(getSelectedClass());
        if (xp == null) xp = Integer.valueOf(0);
        int level = PlayerLevelCalculator.getLevelFromXP(xp.intValue());
        int prestigeLevel = PlayerPrestigeLevelCalculator.getLevelFromXP(getPrestigeXp());
        if (level > getLevel()) {
            setLevel(level, success -> {
            });
            this.scheduleHotbarCard(new HotbarLevelupCard(this, level));
            //Legendary player
            if (level >= PlayerLevelCalculator.getMaxLevel()) {
                int xpForMaxLevel = PlayerLevelCalculator.getTotalXPNeededForLevel(PlayerLevelCalculator.getMaxLevel());
                boolean allComplete = true;
                for (ClassEnum cl : ClassEnum.values()) {
                    if (cl == ClassEnum.NONE) continue;
                    if (!persistenceHolder.getClassXp().containsKey(cl) || persistenceHolder.getClassXp().get(cl) < xpForMaxLevel) {
                        allComplete = false;
                        break;
                    }
                }
                if (allComplete) {
                    if (getRank().isLowerOrEqualThan(Rank.LEGENDARY)) {
                        setRank(ArterionPlugin.getInstance(), Rank.LEGENDARY);
                    }
                    this.scheduleHotbarCard(new LegendaryUnlockCard(this));
                }
            }
        }
        if (prestigeLevel > getPrestigeLevel()) {
            setPrestigeLevel(prestigeLevel, success -> {
            });
            this.scheduleHotbarCard(new HotbarPrestigeLevelupCard(this, prestigeLevel));
        }
    }

    public void recalculatePlayerMeta() {
        //MaxHealth
        ArterionFormula f = ArterionPlugin.getInstance().getFormulaManager().DMG_PLAYER_MAXHP.get(String.valueOf(getSelectedClass()));
        if (f == null || !f.isDeclared()) {
            f = ArterionPlugin.getInstance().getFormulaManager().DMG_PLAYER_MAXHP.get("other");
        }
        //Handle invalid setup
        if (f == null || !f.isDeclared()) {
            this.maxHealth = (int) bukkitPlayer.getMaxHealth();
            this.regenHealth = (int) bukkitPlayer.getMaxHealth();
        } else {
            this.regenHealth = f.evaluateInt(this);
            this.maxHealth = (int) (regenHealth + ArterionPlugin.getInstance().getFormulaManager().PRESTIGE_HEALTH.evaluateDouble(getPointsHealth()));
        }

        //MaxMana
        f = ArterionPlugin.getInstance().getFormulaManager().SKILL_PLAYER_MAXMANA.get(String.valueOf(getSelectedClass()));
        if (f == null || !f.isDeclared()) {
            f = ArterionPlugin.getInstance().getFormulaManager().SKILL_PLAYER_MAXMANA.get("other");
        }
        //Handle invalid setup
        if (f == null || !f.isDeclared()) {
            this.maxMana = (int) 0;
        } else {
            this.maxMana = f.evaluateInt(this);
        }

        if (mana > maxMana) mana = maxMana;

        //Update scoreboard data
        getPlayerScoreboard().updateHealth();
        getPlayerScoreboard().updateModValue(ModValueEnum.MAXMANA);
        getPlayerScoreboard().updateModValue(ModValueEnum.MANA);
        getPlayerScoreboard().updateModValue(ModValueEnum.MAXHEALTH);
    }

    public void addMana(int mana) {
        this.mana += mana;
        if (this.mana > maxMana) this.mana = maxMana;
        getPlayerScoreboard().updateModValue(ModValueEnum.MANA);
    }

    public boolean useMana(int mana) {
        if (this.mana < mana) return false;
        this.mana -= mana;
        getPlayerScoreboard().updateModValue(ModValueEnum.MANA);
        return true;
    }

    public int getClassXp() {
        Integer xp = persistenceHolder.getClassXp().get(getSelectedClass());
        if (xp == null) xp = 0;
        return xp;
    }

    public ChatChannel getChatChannel() {
        return chatChannel;
    }

    public void setChatChannel(ChatChannel chatChannel) {
        this.sendTranslation("chat.channel." + chatChannel.name().toLowerCase() + ".join");
        this.chatChannel = chatChannel;
    }

    public String getHotbarMessage() {
        return hotbarMessage;
    }

    public void setHotbarMessage(ArterionPlugin plugin, String hotbarMessage) {
        //Display next card
        if (currentHotbarCard != null) {
            if (currentHotbarCard.isExpired()) {
                currentHotbarCard = null;
                if (!hotbarCardQueue.isEmpty()) {
                    currentHotbarCard = hotbarCardQueue.poll();
                    currentHotbarCard.start();
                }
            }
        }
        if (currentHotbarCard == null) {
            //No current card, accept message
            this.hotbarMessage = hotbarMessage;
        } else {
            //We have a current card, display its message
            this.hotbarMessage = currentHotbarCard.getMessage();
        }
        plugin.getHotbarMessageUI().updatePlayer(this);
    }

    public void scheduleHotbarCard(HotbarCard card) {
        if (usesMod() && card instanceof NotForModUser) return;
        if (currentHotbarCard == null) {
            currentHotbarCard = card;
            currentHotbarCard.start();
        } else {
            if (card instanceof PriorityHotbarCard) {
                currentHotbarCard = card;
                currentHotbarCard.start();
                return;
            }
            boolean merged = false;
            if (currentHotbarCard instanceof MergeableHotbarCard) {
                if (((MergeableHotbarCard) currentHotbarCard).canBeMerged(card)) {
                    merged = true;
                    ((MergeableHotbarCard) currentHotbarCard).mergeWithCard((MergeableHotbarCard) card);
                    currentHotbarCard.start(); //Restart the card to display it longer when it got updated
                }
            }
            if (!merged) for (HotbarCard c : hotbarCardQueue) {
                if (c instanceof MergeableHotbarCard) {
                    if (((MergeableHotbarCard) c).canBeMerged(card)) {
                        merged = true;
                        ((MergeableHotbarCard) c).mergeWithCard((MergeableHotbarCard) card);
                        break;
                    }
                }
            }
            if (!merged) hotbarCardQueue.add(card);
        }
    }

    public BagMoneyBearer getBagMoneyBearer() {
        return bagMoneyBearer;
    }

    public BankMoneyBearer getBankMoneyBearer() {
        return bankMoneyBearer;
    }

    public void openGui(GUI gui) {
        if (this.openGui != null) {
            this.closeGui();
        }
        this.openGui = gui;
        gui.open();
    }

    public void closeGui() {
        closeGui(false);
    }

    public void closeGui(boolean silent) {
        if (this.openGui != null) {
            if (!silent) this.openGui.close();
            this.openGui = null;
        }
        if (getSkillSlots().usesMod()) {
            ModConnection.sendModPacket(this, new Packet08TextGui("", ""));
        }
    }

    public GUI getOpenGui() {
        return this.openGui;
    }

    public Location getHomeLocation() {
        return cachedHomeLocation == null ? null : cachedHomeLocation.clone();
    }

    public UUID getRoomMate() {
        return persistenceHolder.getRoomMate();
    }

    public boolean isOwnsHomeBlock() {
        return persistenceHolder.isOwnsHomeBlock();
    }

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }

    public Guild getGuild() {
        return guild;
    }

    public void setGuild(Guild guild) {
        this.guild = guild;
    }

    public void updateInDB(Consumer<DatabasePlayer> apply, Consumer<Boolean> successCallBack) {
        new DatabaseObjectTask<DatabasePlayer>(DatabasePlayer.class, persistenceHolder.getUuid()) {
            DatabasePlayer updatedHolder;

            @Override
            public void updateObject(DatabasePlayer databasePlayer) {
                updatedHolder = databasePlayer;
                apply.accept(databasePlayer);
            }

            @Override
            public void success() {
                ArterionPlayer.this.persistenceHolder = updatedHolder;
                if (ArterionPlayer.this.getGuild() != null) ArterionPlayer.this.getGuild().updateMember(updatedHolder);
                successCallBack.accept(true);
            }

            @Override
            public void fail() {
                successCallBack.accept(false);
            }
        }.execute();
    }

    public void setRank(ArterionPlugin plugin, Rank r) {
        this.setRank(plugin, null, r);
    }

    public void setRank(ArterionPlugin plugin, CommandSender sender, Rank r) {
        updateInDB(p -> p.setRank(r), success -> {
            if (success) {
                ArterionPlayer.this.sendTranslation("permissions.rank.updated", r.name());
                plugin.getSchedulers().getDatabaseScheduler().executeInSpigotCircle(new InternalTask() {
                    @Override
                    public void run() {
                        ArterionPlayer.this.getPlayerScoreboard().updateAllPlayerRelations();
                        plugin.getTablistManager().updatePlayerListName(ArterionPlayer.this);
                    }
                });
                if (sender != null) {
                    sender.sendMessage(LanguageAPI.translate(sender, "command.rank.updated", ArterionPlayer.this.getName()));
                }
            } else {
                if (sender != null) {
                    sender.sendMessage(LanguageAPI.translate(sender, "command.rank.error"));
                }
            }
        });
    }

    public void setHomeLocationAndRoommate(boolean ownsHomeBlock, Location homeLocation, UUID roomMate, Consumer<Boolean> callback) {
        updateInDB(p -> {
            p.setOwnsHomeBlock(ownsHomeBlock);
            p.setClaimWorld(homeLocation == null ? null : homeLocation.getWorld().getName());
            p.setHomeX(homeLocation == null ? 0 : homeLocation.getBlockX());
            p.setHomeY(homeLocation == null ? 0 : homeLocation.getBlockY());
            p.setHomeZ(homeLocation == null ? 0 : homeLocation.getBlockZ());
            p.setRoomMate(roomMate);
        }, success -> {
            if (success) cachedHomeLocation = homeLocation == null ? null : homeLocation.clone();
            callback.accept(success);
        });
    }

    public UUID getKickedFromGuild() {
        return persistenceHolder.getKickedFromGuild();
    }

    public void setKickedFromGuild(UUID kickedFromGuild, Consumer<Boolean> callback) {
        updateInDB(p -> p.setKickedFromGuild(kickedFromGuild), callback);
    }

    public boolean isDamageDebug() {
        return damageDebug;
    }

    public void setDamageDebug(boolean damageDebug) {
        this.damageDebug = damageDebug;
    }

    public SkillSlots getSkillSlots() {
        return skillSlots;
    }

    public void setSkillSlots(SkillSlots skillSlots) {
        this.skillSlots = skillSlots;
    }

    public boolean hasNewbieProtection() {
        return hasNewbieProtection(System.currentTimeMillis());
    }

    public boolean hasNewbieProtection(long time) {
        ArterionFormula f = ArterionPlugin.getInstance().getFormulaManager().PLAYER_NEWBIE_PROTECTION;
        if (f == null || !f.isDeclared()) return false;
        return persistenceHolder.getJoined() + f.evaluateInt() >= time;
    }

    public long getRemainingNewbieProtection() {
        ArterionFormula f = ArterionPlugin.getInstance().getFormulaManager().PLAYER_NEWBIE_PROTECTION;
        if (f == null || !f.isDeclared()) return 0;
        return persistenceHolder.getJoined() + f.evaluateInt() - System.currentTimeMillis();
    }

    @Override
    public FriendlyPlayerList getFriendlyPlayerList() {
        if (this.getGuild() != null && this.getTeam() == null) return this.getGuild().getFriendlyPlayerList();
        return friendlyPlayerList;
    }

    public SkillEnum getRightClickSkill(ClassEnum classEnum) {
        String s = persistenceHolder.getSkillRightClickBinding().get(classEnum);
        if (s == null) return null;
        try {
            return SkillEnum.valueOf(s);
        } catch (Exception e) {
            persistenceHolder.getSkillRightClickBinding().remove(classEnum);
            return null;
        }
    }

    public void setRightClickSkill(ClassEnum classEnum, SkillEnum skill, Consumer<Boolean> callback) {
        updateInDB(p -> {
            if (skill == null) {
                p.getSkillRightClickBinding().remove(classEnum);
            } else {
                p.getSkillRightClickBinding().put(classEnum, skill.name());
            }
        }, callback);
    }

    public PlayerPotionTracker getPotionTracker() {
        return potionTracker;
    }

    public ClericHealManager getClericHealManager() {
        return clericHealManager;
    }

    public int calculateDroppedExperience() {
        int droppedLevels = getBukkitPlayer().getLevel();
        if (droppedLevels > 7) droppedLevels = 7;
        return droppedLevels * droppedLevels + 6 * droppedLevels;
    }

    public String getLastWhisperer() {
        return lastWhisperer;
    }

    public void setLastWhisperer(String lastWhisperer) {
        this.lastWhisperer = lastWhisperer;
    }

    public boolean usesMod() {
        return skillSlots.usesMod();
    }

    public Skill getLastAffectedDamageSkill() {
        return lastAffectedDamageSkill;
    }

    public void setLastAffectedDamageSkill(Skill lastAffectedDamageSkill) {
        this.lastAffectedDamageSkill = lastAffectedDamageSkill;
    }

    public int getJobXp(JobEnum job) {
        Integer i = persistenceHolder.getJobXp().get(job);
        return i == null ? 0 : i.intValue();
    }

    public void setJobXp(JobEnum job, int xp, Consumer<Boolean> callback) {
        this.updateInDB(dbp -> dbp.getJobXp().put(job, xp), callback);
    }

    public void addJobXp(JobEnum job, int xp) {
        int prevLevel = getJobLevel(job);
        if (prevLevel >= PlayerJobLevelCalculator.getMaxLevel()) return;
        int[] prev = new int[1];
        this.updateInDB(dbp -> {
            int level = getJobLevel(job);
            if (level >= PlayerJobLevelCalculator.getMaxLevel()) return;
            int goal = xp + getJobXp(job);
            dbp.getJobXp().put(job, goal);
            prev[0] = level;
        }, succ -> {
            if (succ) {
                scheduleHotbarCard(new HotbarJobXPCard(ArterionPlayer.this, prev[0], xp, job));
                int levelNew = getJobLevel(job);
                if (levelNew != prev[0]) {
                    scheduleHotbarCard(new HotbarJobLevelupCard(ArterionPlayer.this, job, levelNew));
                }
            }
        });
    }

    public int getJobLevel(JobEnum job) {
        return PlayerJobLevelCalculator.getLevelFromXP(getJobXp(job));
    }

    public int getPrestigeXp() {
        return persistenceHolder.getPrestigeXp();
    }

    public int getPrestigeLevel() {
        return persistenceHolder.getPrestigeLevel();
    }

    public void setPrestigeLevel(int lvl, Consumer<Boolean> callback) {
        this.updateInDB(dbp -> dbp.setPrestigeLevel(lvl), success -> {
            ArterionPlugin.getInstance().getSchedulers().getDatabaseScheduler().executeInSpigotCircle(new InternalTask() {
                @Override
                public void run() {
                    getPlayerScoreboard().updateModValue(ModValueEnum.XP_PER_MILLE);
                    getPlayerScoreboard().updateModValue(ModValueEnum.PRESTIGE_LEVEL);
                    ArterionPlayer.this.recalculatePlayerMeta();
                }
            });
            callback.accept(success);
        });
    }

    public ArtefactCarrier getArtefactCarrier() {
        return artefactCarrier;
    }

    public boolean isArtefactCarrier() {
        return artefactCarrier != null;
    }

    public void setArtefactCarrier(ArtefactCarrier artefactCarrier) {
        this.artefactCarrier = artefactCarrier;
        if (artefactCarrier != null) {
            Artefact.setCarrier(this);
            Artefact.setOwner(null);
            LanguageAPI.broadcastMessage("artefact.taken", this, this.getGuild().getName());
            Artefact.appendTranslationToRecordingChat("artefact.replay.taken", this, this.getGuild().getName());
            sendTranslation("artefact.youtaken");
        }
        getPlayerScoreboard().updateAllPlayerRelations();
    }

    public Team getTeam() {
        return team;
    }

    public void setTeam(Team team) {
        this.team = team;
    }

    public ArenaInitializer getArenaInitializer() {
        return arenaInitializer;
    }

    public void setArenaInitializer(ArenaInitializer arenaInitializer) {
        this.arenaInitializer = arenaInitializer;
    }

    public Location getTemporaryWorldBackupLocation() {
        return temporaryWorldBackupLocation;
    }

    public void setTemporaryWorldBackupLocation(Location temporaryWorldBackupLocation) {
        this.temporaryWorldBackupLocation = temporaryWorldBackupLocation;
    }

    public boolean isInTemporaryWorld() {
        for (TemporaryWorld w : ArterionPlugin.getInstance().getTemporaryWorldManager().all()) {
            if (w.getWorld().equals(this.getBukkitPlayer().getWorld())) return true;
        }
        return false;
    }

    public long getLastPortalMessage() {
        return lastPortalMessage;
    }

    public void setLastPortalMessage(long lastPortalMessage) {
        this.lastPortalMessage = lastPortalMessage;
    }

    public ExternalFight getRespawnFight() {
        return respawnFight;
    }

    public void setRespawnFight(ExternalFight respawnFight) {
        this.respawnFight = respawnFight;
    }

    public int getClanKills() {
        return persistenceHolder.getClanKills();
    }

    public void setClanKills(int clanKills, Consumer<Boolean> callback) {
        this.updateInDB(dbp -> dbp.setClanKills(clanKills), callback);
    }

    public void setVoteInfo(long lastVote, int newStreak, int offlineVotes, Consumer<Boolean> callback) {
        this.updateInDB(dbp -> {
            dbp.setLastVote(lastVote);
            dbp.setVoteDayStreak(newStreak);
            dbp.setOfflineVotes(offlineVotes);
        }, callback);
    }

    public void setLoginStreakInfo(long lastLogin, int loginStreak, Consumer<Boolean> callback) {
        this.updateInDB(dbp -> {
            dbp.setLastLogin(lastLogin);
            dbp.setLoginStreak(loginStreak);
        }, callback);
    }

    @Override
    public StatObjectType getType() {
        return StatObjectType.PLAYER;
    }

    @Override
    public UUID getUUID() {
        return persistenceHolder.getUuid();
    }

    @Override
    public StatObjectTracker getStatTracker() {
        return statObjectTracker;
    }

    public void trackStatistic(StatType type, int statData, Function<Long, Long> valueMapper) {
        ArterionPlugin.getInstance().getSchedulers().getDatabaseScheduler().executeInMyCircle(new InternalTask() {
            @Override
            public void run() {
                getStatTracker().trackStatistic(ArterionPlugin.getInstance().getExternalDatabase(), type, statData, valueMapper);
            }
        });
    }

    public int getAnnouncementIndex() {
        return announcementIndex;
    }

    public void setAnnouncementIndex(int announcementIndex) {
        this.announcementIndex = announcementIndex;
    }

    public void toggleIgnore(UUID uuid, Consumer<Boolean> newStateConsumer, Consumer<Boolean> successCallback) {
        this.updateInDB(dbp -> {
            if (dbp.getIgnoredPlayers().remove(uuid)) {
                newStateConsumer.accept(false);
            } else {
                dbp.getIgnoredPlayers().add(uuid);
                newStateConsumer.accept(true);
            }
        }, successCallback);
    }

    public boolean isVanished() {
        return vanished;
    }

    public void setVanished(boolean vanished) {
        this.vanished = vanished;
        for (Player p : ArterionPlugin.getOnlinePlayers()) {
            if (!p.equals(getBukkitPlayer())) {
                if (vanished) p.hidePlayer(getBukkitPlayer());
                else p.showPlayer(getBukkitPlayer());
            }
        }
        vanishTime = System.currentTimeMillis();
    }

    public long getVanishTime() {
        return vanishTime;
    }

    public void addKill() {
        this.updateInDB(dbp -> dbp.setKills(dbp.getKills() + 1), succ -> {
        });
    }

    public void addDeath() {
        this.updateInDB(dbp -> dbp.setDeaths(dbp.getDeaths() + 1), succ -> {
        });
    }

    public long getAfkTime() {
        return System.currentTimeMillis() - afkTime;
    }

    public void resetAfkTime() {
        this.afkTime = System.currentTimeMillis();
    }

    public Quest getQuest() {
        return quest;
    }

    public void setQuest(Quest quest, Consumer<Boolean> callback) {
        this.quest = quest;
        this.updateInDB(dbp -> {
            if (quest == null) {
                dbp.setQuestId(-1);
                dbp.setQuestData(null);
            } else {
                dbp.setQuestId(quest.getId());
                dbp.setQuestData(quest.write());
            }
        }, callback);
    }

    public void checkNotMuted(Runnable allowedCallback) {
        new DatabaseTask() {
            DatabaseSanction chatSanction = null;

            @Override
            public boolean performTransaction(Database db) {
                chatSanction = ArterionPlugin.getInstance().getSanctionManager().getCurrentSanction(getBukkitPlayer().getUniqueId(), SanctionType.MUTE);
                if (chatSanction != null) {
                    sendTranslation("chat.muted");
                    sendTranslation("chat.reason", chatSanction.getReason());
                }
                return true;
            }

            @Override
            public void onTransactionCommitOrRollback(boolean committed) {
                if (chatSanction == null) allowedCallback.run();
            }

            @Override
            public void onTransactionError() {
            }
        }.execute();
    }

    public BasicAntiCheat getBAC() {
        return bac;
    }

    public boolean isAnticheatDebug() {
        return anticheatDebug;
    }

    public void setAnticheatDebug(boolean anticheatDebug) {
        this.anticheatDebug = anticheatDebug;
    }

    public int getPrestigePoints() {
        return getPrestigeLevel() + (getRank() == Rank.LEGENDARY ? 15 : 0);
    }

    public int getPointsAttack() {
        return persistenceHolder.getPointsAttack();
    }

    public void setPointsAttack(int pointsAttack, Consumer<Boolean> callback) {
        this.updateInDB(dbp -> dbp.setPointsAttack(pointsAttack), callback);
    }

    public int getPointsDefense() {
        return persistenceHolder.getPointsDefense();
    }

    public void setPointsDefense(int pointsDefense, Consumer<Boolean> callback) {
        this.updateInDB(dbp -> dbp.setPointsDefense(pointsDefense), callback);
    }

    public int getPointsHealth() {
        return persistenceHolder.getPointsHealth();
    }

    public void setPointsHealth(int pointsHealth, Consumer<Boolean> callback) {
        this.updateInDB(dbp -> dbp.setPointsHealth(pointsHealth), succ -> {
            ArterionPlugin.getInstance().getSchedulers().getMainScheduler().executeInSpigotCircle(new InternalTask() {
                @Override
                public void run() {
                    recalculatePlayerMeta();
                }
            });
            callback.accept(succ);
        });
    }

    public int getPointsCooldown() {
        return persistenceHolder.getPointsCooldown();
    }

    public void setPointsCooldown(int pointsCooldown, Consumer<Boolean> callback) {
        this.updateInDB(dbp -> dbp.setPointsCooldown(pointsCooldown), succ -> {
            ArterionPlugin.getInstance().getSchedulers().getMainScheduler().executeInSpigotCircle(new InternalTask() {
                @Override
                public void run() {
                    getSkillSlots().updateSkillSlots();
                }
            });
            callback.accept(succ);
        });
    }

    public int getRemainingPrestigePoingts() {
        return getPrestigePoints() - getPointsAttack() - getPointsDefense() - getPointsHealth() - getPointsCooldown();
    }
}
