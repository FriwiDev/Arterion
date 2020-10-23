package me.friwi.arterion.client.network.packet;

import java.nio.ByteBuffer;

import me.friwi.arterion.client.network.ModPacket;

public class Packet08TextGui extends ModPacket {
    private String title;
    private String description;

    public Packet08TextGui() {
    }

    public Packet08TextGui(String title, String description) {
        this.title = title;
        this.description = description;
    }

    @Override
    public byte getId() {
        return 8;
    }

    @Override
    public void writeData(ByteBuffer buffer) {
        writeString(buffer, title);
        writeString(buffer, description);
    }

    @Override
    public void readData(ByteBuffer buffer) {
        title = readString(buffer);
        description = readString(buffer);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "Packet08TextGui{" +
                "title='" + title + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
