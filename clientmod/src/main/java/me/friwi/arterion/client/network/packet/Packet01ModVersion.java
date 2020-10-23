package me.friwi.arterion.client.network.packet;

import java.nio.ByteBuffer;

import me.friwi.arterion.client.network.ModPacket;

public class Packet01ModVersion extends ModPacket {
    private short version;

    public Packet01ModVersion() {
    }

    public Packet01ModVersion(short version) {
        this.version = version;
    }

    @Override
    public byte getId() {
        return 1;
    }

    @Override
    public void writeData(ByteBuffer buffer) {
        buffer.putShort(version);
    }

    @Override
    public void readData(ByteBuffer buffer) {
        version = buffer.getShort();
    }

    public short getVersion() {
        return version;
    }

    public void setVersion(short version) {
        this.version = version;
    }

    @Override
    public String toString() {
        return "Packet01ModVersion{" +
                "version=" + version +
                '}';
    }
}
