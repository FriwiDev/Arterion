package me.friwi.arterion.plugin.util.database.entity;

import me.friwi.arterion.plugin.combat.classes.ClassEnum;
import me.friwi.arterion.plugin.jobs.JobEnum;
import me.friwi.arterion.plugin.permissions.Rank;
import me.friwi.arterion.plugin.util.database.DatabaseEntity;

import javax.persistence.*;
import java.util.*;

@Entity
@Table(name = "players")
public class DatabasePlayer implements DatabaseEntity {
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "player_class_xp", foreignKey = @ForeignKey(value = ConstraintMode.CONSTRAINT, name = "classXp"), joinColumns = @JoinColumn(name = "entryId"))
    @MapKeyColumn(name = "class", length = 20, nullable = false)
    @MapKeyEnumerated(EnumType.STRING)
    @Column(name = "xp", nullable = false)
    private final Map<ClassEnum, Integer> classXp = new HashMap<>();
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "skill_right_click_bind", foreignKey = @ForeignKey(value = ConstraintMode.CONSTRAINT, name = "skillRightClickBinding"), joinColumns = @JoinColumn(name = "entryId"))
    @MapKeyColumn(name = "class", length = 20, nullable = false)
    @MapKeyEnumerated(EnumType.STRING)
    @Column(name = "rightClickBind", nullable = false)
    private final Map<ClassEnum, String> skillRightClickBinding = new HashMap<>();
    //Jobs
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "player_job_xp", foreignKey = @ForeignKey(value = ConstraintMode.CONSTRAINT, name = "jobXp"), joinColumns = @JoinColumn(name = "entryId"))
    @MapKeyColumn(name = "job", length = 20, nullable = false)
    @MapKeyEnumerated(EnumType.STRING)
    @Column(name = "xp", nullable = false)
    private final Map<JobEnum, Integer> jobXp = new HashMap<>();
    //General info
    @Id
    private UUID uuid;
    @Column(length = 16)
    private String name;
    @Column(length = 5)
    private String locale;
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Rank rank;
    @Column(length = 31)
    private String lastIP;
    private long joined;
    private long lastOnline;
    // Economy
    private long gold;
    private long bank;
    // Classes
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private ClassEnum selectedClass;
    private int level;
    private int maxLevel;
    private int prestigeXp;
    private int prestigeLevel;
    // Private claim
    @Column(length = 31)
    private String claimWorld;
    private int homeX;
    private int homeY;
    private int homeZ;
    private UUID roomMate;
    private boolean ownsHomeBlock;
    //Guild
    private UUID kickedFromGuild;
    private int clanKills;
    //Votes
    private int voteDayStreak;
    private long lastVote;
    private int offlineVotes;
    //Login
    private int loginStreak;
    private long lastLogin;
    //Ignore
    @ElementCollection(fetch = FetchType.EAGER)
    private Set<UUID> ignoredPlayers = new HashSet<>();
    //Basic stats
    private int kills;
    private int deaths;
    //Quest
    private int questId = -1;
    private byte[] questData;
    //Booster
    private int remainingBoosters = 0;
    //Prestige points
    private int pointsAttack = 0;
    private int pointsDefense = 0;
    private int pointsHealth = 0;
    private int pointsCooldown = 0;

    public DatabasePlayer() {
    }

    public DatabasePlayer(UUID uuid) {
        this.uuid = uuid;
    }

    public DatabasePlayer(UUID uuid, String name, String locale, Rank rank, String lastIP, long joined, long lastOnline, long gold, long bank, ClassEnum selectedClass, int level, int maxLevel, int prestigeXp, int prestigeLevel, String claimWorld, int homeX, int homeY, int homeZ, UUID roomMate, boolean ownsHomeBlock, UUID kickedFromGuild, int clanKills, int voteDayStreak, long lastVote, int offlineVotes, int loginStreak, long lastLogin, Set<UUID> ignoredPlayers, int kills, int deaths) {
        this.uuid = uuid;
        this.name = name;
        this.locale = locale;
        this.rank = rank;
        this.lastIP = lastIP;
        this.joined = joined;
        this.lastOnline = lastOnline;
        this.gold = gold;
        this.bank = bank;
        this.selectedClass = selectedClass;
        this.level = level;
        this.maxLevel = maxLevel;
        this.prestigeXp = prestigeXp;
        this.prestigeLevel = prestigeLevel;
        this.claimWorld = claimWorld;
        this.homeX = homeX;
        this.homeY = homeY;
        this.homeZ = homeZ;
        this.roomMate = roomMate;
        this.ownsHomeBlock = ownsHomeBlock;
        this.kickedFromGuild = kickedFromGuild;
        this.clanKills = clanKills;
        this.voteDayStreak = voteDayStreak;
        this.lastVote = lastVote;
        this.offlineVotes = offlineVotes;
        this.loginStreak = loginStreak;
        this.lastLogin = lastLogin;
        this.ignoredPlayers = ignoredPlayers;
        this.kills = kills;
        this.deaths = deaths;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(uuid);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) return true;
        if (other == null) return false;
        if (other.getClass() != this.getClass()) return false;
        return Objects.equals(this.uuid, ((DatabasePlayer) other).uuid);
    }

    public Map<ClassEnum, Integer> getClassXp() {
        return classXp;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public Rank getRank() {
        return rank;
    }

    public void setRank(Rank rank) {
        this.rank = rank;
    }

    public String getLastIP() {
        return lastIP;
    }

    public void setLastIP(String lastIP) {
        this.lastIP = lastIP;
    }

    public long getJoined() {
        return joined;
    }

    public void setJoined(long joined) {
        this.joined = joined;
    }

    public long getLastOnline() {
        return lastOnline;
    }

    public void setLastOnline(long lastOnline) {
        this.lastOnline = lastOnline;
    }

    public long getGold() {
        return gold;
    }

    public void setGold(long gold) {
        this.gold = gold;
    }

    public long getBank() {
        return bank;
    }

    public void setBank(long bank) {
        this.bank = bank;
    }

    public ClassEnum getSelectedClass() {
        return selectedClass;
    }

    public void setSelectedClass(ClassEnum selectedClass) {
        this.selectedClass = selectedClass;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getMaxLevel() {
        return maxLevel;
    }

    public void setMaxLevel(int maxLevel) {
        this.maxLevel = maxLevel;
    }

    public int getPrestigeXp() {
        return prestigeXp;
    }

    public void setPrestigeXp(int prestigeXp) {
        this.prestigeXp = prestigeXp;
    }

    public int getPrestigeLevel() {
        return prestigeLevel;
    }

    public void setPrestigeLevel(int prestigeLevel) {
        this.prestigeLevel = prestigeLevel;
    }

    public String getClaimWorld() {
        return claimWorld;
    }

    public void setClaimWorld(String claimWorld) {
        this.claimWorld = claimWorld;
    }

    public int getHomeX() {
        return homeX;
    }

    public void setHomeX(int homeX) {
        this.homeX = homeX;
    }

    public int getHomeY() {
        return homeY;
    }

    public void setHomeY(int homeY) {
        this.homeY = homeY;
    }

    public int getHomeZ() {
        return homeZ;
    }

    public void setHomeZ(int homeZ) {
        this.homeZ = homeZ;
    }

    public UUID getRoomMate() {
        return roomMate;
    }

    public void setRoomMate(UUID roomMate) {
        this.roomMate = roomMate;
    }

    public boolean isOwnsHomeBlock() {
        return ownsHomeBlock;
    }

    public void setOwnsHomeBlock(boolean ownsHomeBlock) {
        this.ownsHomeBlock = ownsHomeBlock;
    }

    public UUID getKickedFromGuild() {
        return kickedFromGuild;
    }

    public void setKickedFromGuild(UUID kickedFromGuild) {
        this.kickedFromGuild = kickedFromGuild;
    }

    public Map<ClassEnum, String> getSkillRightClickBinding() {
        return skillRightClickBinding;
    }

    public Map<JobEnum, Integer> getJobXp() {
        return jobXp;
    }

    public int getClanKills() {
        return clanKills;
    }

    public void setClanKills(int clanKills) {
        this.clanKills = clanKills;
    }

    public int getVoteDayStreak() {
        return voteDayStreak;
    }

    public void setVoteDayStreak(int voteDayStreak) {
        this.voteDayStreak = voteDayStreak;
    }

    public long getLastVote() {
        return lastVote;
    }

    public void setLastVote(long lastVote) {
        this.lastVote = lastVote;
    }

    public int getOfflineVotes() {
        return offlineVotes;
    }

    public void setOfflineVotes(int offlineVotes) {
        this.offlineVotes = offlineVotes;
    }

    public int getLoginStreak() {
        return loginStreak;
    }

    public void setLoginStreak(int loginStreak) {
        this.loginStreak = loginStreak;
    }

    public long getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(long lastLogin) {
        this.lastLogin = lastLogin;
    }

    public Set<UUID> getIgnoredPlayers() {
        return ignoredPlayers;
    }

    public void setIgnoredPlayers(Set<UUID> ignoredPlayers) {
        this.ignoredPlayers = ignoredPlayers;
    }

    public int getKills() {
        return kills;
    }

    public void setKills(int kills) {
        this.kills = kills;
    }

    public int getDeaths() {
        return deaths;
    }

    public void setDeaths(int deaths) {
        this.deaths = deaths;
    }

    public int getQuestId() {
        return questId;
    }

    public void setQuestId(int questId) {
        this.questId = questId;
    }

    public byte[] getQuestData() {
        return questData;
    }

    public void setQuestData(byte[] questData) {
        this.questData = questData;
    }

    public int getRemainingBoosters() {
        return remainingBoosters;
    }

    public void setRemainingBoosters(int remainingBoosters) {
        this.remainingBoosters = remainingBoosters;
    }

    public int getPointsAttack() {
        return pointsAttack;
    }

    public void setPointsAttack(int pointsAttack) {
        this.pointsAttack = pointsAttack;
    }

    public int getPointsDefense() {
        return pointsDefense;
    }

    public void setPointsDefense(int pointsDefense) {
        this.pointsDefense = pointsDefense;
    }

    public int getPointsHealth() {
        return pointsHealth;
    }

    public void setPointsHealth(int pointsHealth) {
        this.pointsHealth = pointsHealth;
    }

    public int getPointsCooldown() {
        return pointsCooldown;
    }

    public void setPointsCooldown(int pointsCooldown) {
        this.pointsCooldown = pointsCooldown;
    }

    @Override
    public String toString() {
        return "DatabasePlayer{" +
                "classXp=" + classXp +
                ", skillRightClickBinding=" + skillRightClickBinding +
                ", jobXp=" + jobXp +
                ", uuid=" + uuid +
                ", name='" + name + '\'' +
                ", locale='" + locale + '\'' +
                ", rank=" + rank +
                ", lastIP='" + lastIP + '\'' +
                ", joined=" + joined +
                ", lastOnline=" + lastOnline +
                ", gold=" + gold +
                ", bank=" + bank +
                ", selectedClass=" + selectedClass +
                ", level=" + level +
                ", maxLevel=" + maxLevel +
                ", prestigeXp=" + prestigeXp +
                ", prestigeLevel=" + prestigeLevel +
                ", claimWorld='" + claimWorld + '\'' +
                ", homeX=" + homeX +
                ", homeY=" + homeY +
                ", homeZ=" + homeZ +
                ", roomMate=" + roomMate +
                ", ownsHomeBlock=" + ownsHomeBlock +
                ", kickedFromGuild=" + kickedFromGuild +
                ", clanKills=" + clanKills +
                ", voteDayStreak=" + voteDayStreak +
                ", lastVote=" + lastVote +
                ", offlineVotes=" + offlineVotes +
                ", loginStreak=" + loginStreak +
                ", lastLogin=" + lastLogin +
                ", ignoredPlayers=" + ignoredPlayers +
                ", kills=" + kills +
                ", deaths=" + deaths +
                ", questId=" + questId +
                ", questData=" + Arrays.toString(questData) +
                ", remainingBoosters=" + remainingBoosters +
                ", pointsAttack=" + pointsAttack +
                ", pointsDefense=" + pointsDefense +
                ", pointsHealth=" + pointsHealth +
                ", pointsCooldown=" + pointsCooldown +
                '}';
    }
}
