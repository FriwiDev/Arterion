package me.friwi.arterion.plugin.util.database.entity;

import me.friwi.arterion.plugin.util.database.DatabaseEntity;

import javax.persistence.*;
import java.util.*;

@Entity
@Table(name = "artefact_fights")
public class DatabaseArtefactFight implements DatabaseEntity {
    public static final long FIGHT_UNFINISHED = -1;

    //General info
    @Id
    @GeneratedValue
    private UUID uuid;
    private long timeBegin;
    private long timeEnd;
    @ManyToMany(fetch = FetchType.LAZY)
    @CollectionTable(name = "guild_artefact_fights_participating")
    private Set<DatabaseGuild> participating;

    @ManyToOne
    @Basic(fetch = FetchType.EAGER)
    private DatabaseGuild winner;

    @ManyToOne
    @Basic(fetch = FetchType.EAGER)
    private DatabasePlayer capturer;

    @ManyToMany(fetch = FetchType.LAZY)
    @CollectionTable(name = "player_artefact_fights_participating")
    private Set<DatabasePlayer> playersParticipating;

    @ManyToMany(fetch = FetchType.LAZY)
    @CollectionTable(name = "player_artefact_fights_winning")
    private Set<DatabasePlayer> playersWinning;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "artefact_fights_guilds", foreignKey = @ForeignKey(value = ConstraintMode.CONSTRAINT, name = "guilds"), joinColumns = @JoinColumn(name = "entryId"))
    @MapKeyColumn(name = "guild", length = 20, nullable = false)
    @Column(name = "uuids", nullable = false)
    private final Map<DatabaseGuild, UUID[]> guilds = new HashMap<>();

    private String replayLocation;

    public DatabaseArtefactFight() {
    }

    public DatabaseArtefactFight(long timeBegin, long timeEnd, Set<DatabaseGuild> participating, DatabaseGuild winner, DatabasePlayer capturer, Set<DatabasePlayer> playersParticipating, Set<DatabasePlayer> playersWinning, String replayLocation) {
        this.timeBegin = timeBegin;
        this.timeEnd = timeEnd;
        this.participating = participating;
        this.winner = winner;
        this.capturer = capturer;
        this.playersParticipating = playersParticipating;
        this.playersWinning = playersWinning;
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
        return Objects.equals(this.uuid, ((DatabaseArtefactFight) other).uuid);
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

    public Set<DatabaseGuild> getParticipating() {
        return participating;
    }

    public void setParticipating(Set<DatabaseGuild> participating) {
        this.participating = participating;
    }

    public DatabaseGuild getWinner() {
        return winner;
    }

    public void setWinner(DatabaseGuild winner) {
        this.winner = winner;
    }

    public DatabasePlayer getCapturer() {
        return capturer;
    }

    public void setCapturer(DatabasePlayer capturer) {
        this.capturer = capturer;
    }

    public Set<DatabasePlayer> getPlayersParticipating() {
        return playersParticipating;
    }

    public void setPlayersParticipating(Set<DatabasePlayer> playersParticipating) {
        this.playersParticipating = playersParticipating;
    }

    public Set<DatabasePlayer> getPlayersWinning() {
        return playersWinning;
    }

    public void setPlayersWinning(Set<DatabasePlayer> playersWinning) {
        this.playersWinning = playersWinning;
    }

    public String getReplayLocation() {
        return replayLocation;
    }

    public void setReplayLocation(String replayLocation) {
        this.replayLocation = replayLocation;
    }

    public Map<DatabaseGuild, UUID[]> getGuilds() {
        return guilds;
    }

    @Override
    public String toString() {
        return "DatabaseArtefactFight{" +
                "uuid=" + uuid +
                ", timeBegin=" + timeBegin +
                ", timeEnd=" + timeEnd +
                ", participating=" + participating +
                ", winner=" + winner +
                ", capturer=" + capturer +
                ", playersParticipating=" + playersParticipating +
                ", playersWinning=" + playersWinning +
                ", guilds=" + guilds +
                ", replayLocation='" + replayLocation + '\'' +
                '}';
    }
}
