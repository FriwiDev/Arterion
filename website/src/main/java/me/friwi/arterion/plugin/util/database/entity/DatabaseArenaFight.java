package me.friwi.arterion.plugin.util.database.entity;

import me.friwi.arterion.plugin.util.database.DatabaseEntity;

import javax.persistence.*;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "arena_fights")
public class DatabaseArenaFight implements DatabaseEntity {
    public static final long FIGHT_UNFINISHED = -1;

    //General info
    @Id
    @GeneratedValue
    private UUID uuid;
    private long timeBegin;
    private long timeEnd;
    @ManyToMany(fetch = FetchType.LAZY)
    @CollectionTable(name = "player_arena_fights_team_one")
    private Set<DatabasePlayer> playersTeamOne;
    @ManyToMany(fetch = FetchType.LAZY)
    @CollectionTable(name = "player_arena_fights_team_two")
    private Set<DatabasePlayer> playersTeamTwo;
    private int winner;
    private int remaining1;
    private int remaining2;

    private String replayLocation;

    public DatabaseArenaFight() {
    }

    public DatabaseArenaFight(long timeBegin, long timeEnd, Set<DatabasePlayer> playersTeamOne, Set<DatabasePlayer> playersTeamTwo, int winner, int remaining1, int remaining2, String replayLocation) {
        this.timeBegin = timeBegin;
        this.timeEnd = timeEnd;
        this.playersTeamOne = playersTeamOne;
        this.playersTeamTwo = playersTeamTwo;
        this.winner = winner;
        this.remaining1 = remaining1;
        this.remaining2 = remaining2;
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
        return Objects.equals(this.uuid, ((DatabaseArenaFight) other).uuid);
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

    public Set<DatabasePlayer> getPlayersTeamOne() {
        return playersTeamOne;
    }

    public void setPlayersTeamOne(Set<DatabasePlayer> playersTeamOne) {
        this.playersTeamOne = playersTeamOne;
    }

    public Set<DatabasePlayer> getPlayersTeamTwo() {
        return playersTeamTwo;
    }

    public void setPlayersTeamTwo(Set<DatabasePlayer> playersTeamTwo) {
        this.playersTeamTwo = playersTeamTwo;
    }

    public int getWinner() {
        return winner;
    }

    public void setWinner(int winner) {
        this.winner = winner;
    }

    public int getRemaining1() {
        return remaining1;
    }

    public void setRemaining1(int remaining1) {
        this.remaining1 = remaining1;
    }

    public int getRemaining2() {
        return remaining2;
    }

    public void setRemaining2(int remaining2) {
        this.remaining2 = remaining2;
    }

    public String getReplayLocation() {
        return replayLocation;
    }

    public void setReplayLocation(String replayLocation) {
        this.replayLocation = replayLocation;
    }

    @Override
    public String toString() {
        return "DatabaseArenaFight{" +
                "uuid=" + uuid +
                ", timeBegin=" + timeBegin +
                ", timeEnd=" + timeEnd +
                ", playersTeamOne=" + playersTeamOne +
                ", playersTeamTwo=" + playersTeamTwo +
                ", winner=" + winner +
                ", remaining1=" + remaining1 +
                ", remaining2=" + remaining2 +
                ", replayLocation='" + replayLocation + '\'' +
                '}';
    }
}
