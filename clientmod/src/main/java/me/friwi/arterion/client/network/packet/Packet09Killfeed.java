package me.friwi.arterion.client.network.packet;

import java.nio.ByteBuffer;
import java.util.UUID;

import me.friwi.arterion.client.network.ModPacket;

public class Packet09Killfeed extends ModPacket {
    private int borderColor;
    private int duration;
    private UUID killerUUID;
    private String killerName;
    //0 is item, 1 is icon
    private byte type;
    private short imageId;
    private short imageSubId;
    private UUID killedUUID;
    private String killedName;

    public Packet09Killfeed() {
    }

    public Packet09Killfeed(int borderColor, int duration, UUID killerUUID, String killerName, byte type, short imageId, short imageSubId, UUID killedUUID, String killedName) {
        this.borderColor = borderColor;
        this.duration = duration;
        this.killerUUID = killerUUID;
        this.killerName = killerName;
        this.type = type;
        this.imageId = imageId;
        this.imageSubId = imageSubId;
        this.killedUUID = killedUUID;
        this.killedName = killedName;
    }

    @Override
    public byte getId() {
        return 9;
    }

    @Override
    public void writeData(ByteBuffer buffer) {
        buffer.putInt(borderColor);
        buffer.putInt(duration);
        if (killerUUID == null) {
            buffer.putLong(0);
            buffer.putLong(0);
        } else {
            buffer.putLong(killerUUID.getMostSignificantBits());
            buffer.putLong(killerUUID.getLeastSignificantBits());
        }
        writeString(buffer, killerName);
        buffer.put(type);
        buffer.putShort(imageId);
        buffer.putShort(imageSubId);
        buffer.putLong(killedUUID.getMostSignificantBits());
        buffer.putLong(killedUUID.getLeastSignificantBits());
        writeString(buffer, killedName);
    }

    @Override
    public void readData(ByteBuffer buffer) {
        borderColor = buffer.getInt();
        duration = buffer.getInt();
        long msb = buffer.getLong();
        long lsb = buffer.getLong();
        if (msb == 0 && lsb == 0) {
            killerUUID = null;
        } else {
            killerUUID = new UUID(msb, lsb);
        }
        killerName = readString(buffer);
        type = buffer.get();
        imageId = buffer.getShort();
        imageSubId = buffer.getShort();
        killedUUID = new UUID(buffer.getLong(), buffer.getLong());
        killedName = readString(buffer);
    }

    public int getBorderColor() {
        return borderColor;
    }

    public void setBorderColor(int borderColor) {
        this.borderColor = borderColor;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public UUID getKillerUUID() {
        return killerUUID;
    }

    public void setKillerUUID(UUID killerUUID) {
        this.killerUUID = killerUUID;
    }

    public String getKillerName() {
        return killerName;
    }

    public void setKillerName(String killerName) {
        this.killerName = killerName;
    }

    public byte getType() {
        return type;
    }

    public void setType(byte type) {
        this.type = type;
    }

    public short getImageId() {
        return imageId;
    }

    public void setImageId(short imageId) {
        this.imageId = imageId;
    }

    public short getImageSubId() {
        return imageSubId;
    }

    public void setImageSubId(short imageSubId) {
        this.imageSubId = imageSubId;
    }

    public UUID getKilledUUID() {
        return killedUUID;
    }

    public void setKilledUUID(UUID killedUUID) {
        this.killedUUID = killedUUID;
    }

    public String getKilledName() {
        return killedName;
    }

    public void setKilledName(String killedName) {
        this.killedName = killedName;
    }

    @Override
    public String toString() {
        return "Packet09Killfeed{" +
                "borderColor=" + borderColor +
                ", duration=" + duration +
                ", killerUUID=" + killerUUID +
                ", killerName='" + killerName + '\'' +
                ", type=" + type +
                ", imageId=" + imageId +
                ", imageSubId=" + imageSubId +
                ", killedUUID=" + killedUUID +
                ", killedName='" + killedName + '\'' +
                '}';
    }
}
