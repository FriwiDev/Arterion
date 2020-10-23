package me.friwi.arterion.plugin.util.database.entity;

import me.friwi.arterion.plugin.util.database.DatabaseEntity;
import me.friwi.arterion.plugin.util.database.enums.GuildUpgradeEnum;
import me.friwi.arterion.plugin.util.database.enums.GuildUpgradeLevel;

import javax.persistence.*;
import java.util.*;

@Entity
@Table(name = "guilds")
public class DatabaseGuild implements DatabaseEntity {
    public static final long NOT_DELETED = -1;
    public static final long NOT_IN_MINUS_BALANCE = -1;
    public static final int OBSIDIAN_NOT_CALCULATED = -1;


    //General info
    @Id
    @GeneratedValue
    private UUID uuid;
    @Column(length = 16)
    private String name;
    @Column(length = 5)
    private String tag;
    private long created;
    private long deleted;
    private long minusBankBalance;
    private int obsidian;
    // Economy
    private long gold;
    private boolean xpShare;
    private boolean goldShare;
    // Guild claim
    @Column(length = 31)
    private String claimWorld;
    private int homeX;
    private int homeY;
    private int homeZ;
    private long lastReclaim;
    //Members
    @OneToOne
    @Basic(fetch = FetchType.EAGER)
    private DatabasePlayer leader;
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "guilds_officers",
            joinColumns = @JoinColumn(name = "guilds_uuid"),
            inverseJoinColumns = @JoinColumn(name = "players_uuid")
    )
    private Set<DatabasePlayer> officers = new HashSet<>();
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "guilds_members",
            joinColumns = @JoinColumn(name = "guilds_uuid"),
            inverseJoinColumns = @JoinColumn(name = "players_uuid")
    )
    private Set<DatabasePlayer> members = new HashSet<>();
    //Protection
    private long protection;
    //Artefact
    private boolean hasArtefact;
    //Upgrades
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "guild_upgrade_level", foreignKey = @ForeignKey(value = ConstraintMode.CONSTRAINT, name = "upgradeLevel"), joinColumns = @JoinColumn(name = "entryId"))
    @MapKeyColumn(name = "guild_upgrade", length = 20, nullable = false)
    @MapKeyEnumerated(EnumType.STRING)
    @Enumerated(EnumType.STRING)
    @Column(name = "guild_upgrade_lvl", nullable = false)
    private Map<GuildUpgradeEnum, GuildUpgradeLevel> guildUpgradeLevel = new HashMap<>();
    //Stats
    private int clanKills;

    public DatabaseGuild() {
    }

    public DatabaseGuild(String name, String tag, long created, long deleted, long minusBankBalance, int obsidian, long gold, boolean xpShare, boolean goldShare, String claimWorld, int homeX, int homeY, int homeZ, long lastReclaim, DatabasePlayer leader, Set<DatabasePlayer> officers, Set<DatabasePlayer> members, long protection, boolean hasArtefact, Map<GuildUpgradeEnum, GuildUpgradeLevel> guildUpgradeLevel, int clanKills) {
        this.name = name;
        this.tag = tag;
        this.created = created;
        this.deleted = deleted;
        this.minusBankBalance = minusBankBalance;
        this.obsidian = obsidian;
        this.gold = gold;
        this.xpShare = xpShare;
        this.goldShare = goldShare;
        this.claimWorld = claimWorld;
        this.homeX = homeX;
        this.homeY = homeY;
        this.homeZ = homeZ;
        this.lastReclaim = lastReclaim;
        this.leader = leader;
        this.officers = officers;
        this.members = members;
        this.protection = protection;
        this.hasArtefact = hasArtefact;
        this.guildUpgradeLevel = guildUpgradeLevel;
        this.clanKills = clanKills;
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
        return Objects.equals(this.uuid, ((DatabaseGuild) other).uuid);
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

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public long getCreated() {
        return created;
    }

    public void setCreated(long created) {
        this.created = created;
    }

    public long getDeleted() {
        return deleted;
    }

    public void setDeleted(long deleted) {
        this.deleted = deleted;
    }

    public long getMinusBankBalance() {
        return minusBankBalance;
    }

    public void setMinusBankBalance(long minusBankBalance) {
        this.minusBankBalance = minusBankBalance;
    }

    public int getObsidian() {
        return obsidian;
    }

    public void setObsidian(int obsidian) {
        this.obsidian = obsidian;
    }

    public long getGold() {
        return gold;
    }

    public void setGold(long gold) {
        this.gold = gold;
    }

    public boolean isXpShare() {
        return xpShare;
    }

    public void setXpShare(boolean xpShare) {
        this.xpShare = xpShare;
    }

    public boolean isGoldShare() {
        return goldShare;
    }

    public void setGoldShare(boolean goldShare) {
        this.goldShare = goldShare;
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

    public long getLastReclaim() {
        return lastReclaim;
    }

    public void setLastReclaim(long lastReclaim) {
        this.lastReclaim = lastReclaim;
    }

    public DatabasePlayer getLeader() {
        return leader;
    }

    public void setLeader(DatabasePlayer leader) {
        this.leader = leader;
    }

    public Set<DatabasePlayer> getOfficers() {
        return officers;
    }

    public void setOfficers(Set<DatabasePlayer> officers) {
        this.officers = officers;
    }

    public Set<DatabasePlayer> getMembers() {
        return members;
    }

    public void setMembers(Set<DatabasePlayer> members) {
        this.members = members;
    }

    public long getProtection() {
        return protection;
    }

    public void setProtection(long protection) {
        this.protection = protection;
    }

    public boolean isHasArtefact() {
        return hasArtefact;
    }

    public void setHasArtefact(boolean hasArtefact) {
        this.hasArtefact = hasArtefact;
    }

    public Map<GuildUpgradeEnum, GuildUpgradeLevel> getGuildUpgradeLevel() {
        return guildUpgradeLevel;
    }

    public void setGuildUpgradeLevel(Map<GuildUpgradeEnum, GuildUpgradeLevel> guildUpgradeLevel) {
        this.guildUpgradeLevel = guildUpgradeLevel;
    }

    public int getClanKills() {
        return clanKills;
    }

    public void setClanKills(int clanKills) {
        this.clanKills = clanKills;
    }

    @Override
    public String toString() {
        return "DatabaseGuild{" +
                "uuid=" + uuid +
                ", name='" + name + '\'' +
                ", tag='" + tag + '\'' +
                ", created=" + created +
                ", deleted=" + deleted +
                ", minusBankBalance=" + minusBankBalance +
                ", obsidian=" + obsidian +
                ", gold=" + gold +
                ", xpShare=" + xpShare +
                ", goldShare=" + goldShare +
                ", claimWorld='" + claimWorld + '\'' +
                ", homeX=" + homeX +
                ", homeY=" + homeY +
                ", homeZ=" + homeZ +
                ", lastReclaim=" + lastReclaim +
                ", leader=" + leader +
                ", officers=" + officers +
                ", members=" + members +
                ", protection=" + protection +
                ", hasArtefact=" + hasArtefact +
                ", guildUpgradeLevel=" + guildUpgradeLevel +
                ", clanKills=" + clanKills +
                '}';
    }
}
