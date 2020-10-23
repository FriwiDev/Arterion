package me.friwi.arterion.plugin.util.database.entity;

import me.friwi.arterion.plugin.util.database.DatabaseEntity;

import javax.persistence.*;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "tokens")
public class DatabaseLoginToken implements DatabaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID tokenId;
    private UUID player;
    private long expires;


    protected DatabaseLoginToken() {
    }

    public DatabaseLoginToken(UUID player, long expires) {
        this.player = player;
        this.expires = expires;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(tokenId);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) return true;
        if (other == null) return false;
        if (other.getClass() != this.getClass()) return false;
        return Objects.equals(this.tokenId, ((DatabaseLoginToken) other).tokenId);
    }

    public UUID getTokenId() {
        return tokenId;
    }

    public UUID getPlayer() {
        return player;
    }

    public long getExpires() {
        return expires;
    }

    @Override
    public String toString() {
        return "DatabaseLoginToken{" +
                "tokenId=" + tokenId +
                ", player=" + player +
                ", expires=" + expires +
                '}';
    }
}
