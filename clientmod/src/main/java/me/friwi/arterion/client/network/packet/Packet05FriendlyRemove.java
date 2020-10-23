package me.friwi.arterion.client.network.packet;

import java.nio.ByteBuffer;
import java.util.UUID;

import me.friwi.arterion.client.network.ModPacket;

public class Packet05FriendlyRemove extends ModPacket {
    private UUID player;

    public Packet05FriendlyRemove() {
    }

    public Packet05FriendlyRemove(UUID player) {
        this.player = player;
    }

    @Override
    public byte getId() {
        return 5;
    }

    @Override
    public void writeData(ByteBuffer buffer) {
        buffer.putLong(player.getMostSignificantBits());
        buffer.putLong(player.getLeastSignificantBits());
    }

    @Override
    public void readData(ByteBuffer buffer) {
        player = new UUID(buffer.getLong(), buffer.getLong());
    }

    public UUID getPlayer() {
        return player;
    }

    public void setPlayer(UUID player) {
        this.player = player;
    }

    @Override
    public String toString() {
        return "Packet05FriendlyRemove{" +
                "player=" + player +
                '}';
    }
}
