package me.friwi.arterion.client.network.packet;

import java.nio.ByteBuffer;

import me.friwi.arterion.client.network.ModPacket;

public class Packet02IntValue extends ModPacket {
    private String name;
    private int value;

    public Packet02IntValue() {
    }

    public Packet02IntValue(String name, int value) {
        this.name = name;
        this.value = value;
    }

    @Override
    public byte getId() {
        return 2;
    }

    @Override
    public void writeData(ByteBuffer buffer) {
        writeString(buffer, name);
        buffer.putInt(value);
    }

    @Override
    public void readData(ByteBuffer buffer) {
        name = readString(buffer);
        value = buffer.getInt();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "Packet02IntValue{" +
                "name='" + name + '\'' +
                ", value=" + value +
                '}';
    }
}
