package me.friwi.arterion.plugin.util.database.entity;

import me.friwi.arterion.plugin.util.database.DatabaseEntity;

import javax.persistence.*;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "guild_fights")
public class DatabaseGuildFight implements DatabaseEntity {
    public static final long FIGHT_UNFINISHED = -1;

    //General info
    @Id
    @GeneratedValue
    private UUID uuid;
    private long timeBegin;
    private long timeEnd;
    @ManyToOne
    @Basic(fetch = FetchType.EAGER)
    private DatabaseGuild attacker;
    @ManyToOne
    @Basic(fetch = FetchType.EAGER)
    private DatabaseGuild defender;
    @ManyToOne
    @Basic(fetch = FetchType.EAGER)
    private DatabaseGuild winner;
    @ManyToMany(fetch = FetchType.LAZY)
    @CollectionTable(name = "player_guild_fights_attacking")
    private Set<DatabasePlayer> playersAttacking;
    @ManyToMany(fetch = FetchType.LAZY)
    @CollectionTable(name = "player_guild_fights_defending")
    private Set<DatabasePlayer> playersDefending;
    private String replayLocation;

    public DatabaseGuildFight() {
    }

    public DatabaseGuildFight(long timeBegin, long timeEnd, DatabaseGuild attacker, DatabaseGuild defender, DatabaseGuild winner, Set<DatabasePlayer> playersAttacking, Set<DatabasePlayer> playersDefending, String replayLocation) {
        this.timeBegin = timeBegin;
        this.timeEnd = timeEnd;
        this.attacker = attacker;
        this.defender = defender;
        this.winner = winner;
        this.playersAttacking = playersAttacking;
        this.playersDefending = playersDefending;
        this.replayLocation = replayLocation;
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
        return Objects.equals(this.uuid, ((DatabaseGuildFight) other).uuid);
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public long getTimeBegin() {
        return timeBegin;
    }

    public void setTimeBegin(long timeBegin) {
        this.timeBegin = timeBegin;
    }

    public long getTimeEnd() {
        return timeEnd;
    }

    public void setTimeEnd(long timeEnd) {
        this.timeEnd = timeEnd;
    }

    public DatabaseGuild getAttacker() {
        return attacker;
    }

    public void setAttacker(DatabaseGuild attacker) {
        this.attacker = attacker;
    }

    public DatabaseGuild getDefender() {
        return defender;
    }

    public void setDefender(DatabaseGuild defender) {
        this.defender = defender;
    }

    public DatabaseGuild getWinner() {
        return winner;
    }

    public void setWinner(DatabaseGuild winner) {
        this.winner = winner;
    }

    public Set<DatabasePlayer> getPlayersAttacking() {
        return playersAttacking;
    }

    public void setPlayersAttacking(Set<DatabasePlayer> playersAttacking) {
        this.playersAttacking = playersAttacking;
    }

    public Set<DatabasePlayer> getPlayersDefending() {
        return playersDefending;
    }

    public void setPlayersDefending(Set<DatabasePlayer> playersDefending) {
        this.playersDefending = playersDefending;
    }

    public String getReplayLocation() {
        return replayLocation;
    }

    public void setReplayLocation(String replayLocation) {
        this.replayLocation = replayLocation;
    }

    @Override
    public String toString() {
        return "DatabaseGuildFight{" +
                "uuid=" + uuid +
                ", timeBegin=" + timeBegin +
                ", timeEnd=" + timeEnd +
                ", attacker=" + attacker +
                ", defender=" + defender +
                ", winner=" + winner +
                ", playersAttacking=" + playersAttacking +
                ", playersDefending=" + playersDefending +
                ", replayLocation='" + replayLocation + '\'' +
                '}';
    }
}
