package me.friwi.arterion.plugin.util.database.entity;

import me.friwi.arterion.plugin.util.database.DatabaseEntity;

import javax.persistence.*;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "morgoth_dungeon_fights")
public class DatabaseMorgothDungeonFight implements DatabaseEntity {
    public static final long FIGHT_UNFINISHED = -1;

    //General info
    @Id
    @GeneratedValue
    private UUID uuid;
    private long timeBegin;
    private long timeEnd;
    @ManyToMany(fetch = FetchType.LAZY)
    @CollectionTable(name = "player_morgoth_dungeon_fights_participating")
    private Set<DatabasePlayer> playersParticipating;
    @ManyToMany(fetch = FetchType.LAZY)
    @CollectionTable(name = "player_morgoth_dungeon_fights_winning")
    private Set<DatabasePlayer> playersWinning;

    private String replayLocation;

    public DatabaseMorgothDungeonFight() {
    }

    public DatabaseMorgothDungeonFight(long timeBegin, long timeEnd, Set<DatabasePlayer> playersParticipating, Set<DatabasePlayer> playersWinning, String replayLocation) {
        this.timeBegin = timeBegin;
        this.timeEnd = timeEnd;
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
        return Objects.equals(this.uuid, ((DatabaseMorgothDungeonFight) other).uuid);
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

    @Override
    public String toString() {
        return "DatabaseMorgothDungeonFight{" +
                "uuid=" + uuid +
                ", timeBegin=" + timeBegin +
                ", timeEnd=" + timeEnd +
                ", playersParticipating=" + playersParticipating +
                ", playersWinning=" + playersWinning +
                ", replayLocation='" + replayLocation + '\'' +
                '}';
    }
}
