package me.friwi.arterion.client.network.packet;

import java.nio.ByteBuffer;

import me.friwi.arterion.client.network.ModPacket;

public class Packet06Objective extends ModPacket {
    private long delay;
    private String text;

    public Packet06Objective() {
    }

    public Packet06Objective(long delay, String text) {
        this.delay = delay;
        this.text = text;
    }

    @Override
    public byte getId() {
        return 6;
    }

    @Override
    public void writeData(ByteBuffer buffer) {
        buffer.putLong(delay);
        writeString(buffer, text);
    }

    @Override
    public void readData(ByteBuffer buffer) {
        delay = buffer.getLong();
        text = readString(buffer);
    }

    public long getDelay() {
        return delay;
    }

    public void setDelay(long delay) {
        this.delay = delay;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return "Packet06Objective{" +
                "delay=" + delay +
                ", text='" + text + '\'' +
                '}';
    }
}
