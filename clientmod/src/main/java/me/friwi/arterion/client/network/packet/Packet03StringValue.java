package me.friwi.arterion.client.network.packet;

import java.nio.ByteBuffer;

import me.friwi.arterion.client.network.ModPacket;

public class Packet03StringValue extends ModPacket {
    private String name;
    private String value;

    public Packet03StringValue() {
    }

    public Packet03StringValue(String name, String value) {
        this.name = name;
        this.value = value;
    }

    @Override
    public byte getId() {
        return 3;
    }

    @Override
    public void writeData(ByteBuffer buffer) {
        writeString(buffer, name);
        writeString(buffer, value);
    }

    @Override
    public void readData(ByteBuffer buffer) {
        name = readString(buffer);
        value = readString(buffer);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "Packet03StringValue{" +
                "name='" + name + '\'' +
                ", value='" + value + '\'' +
                '}';
    }
}
