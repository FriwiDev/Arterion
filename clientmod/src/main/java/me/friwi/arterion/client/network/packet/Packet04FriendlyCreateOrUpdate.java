package me.friwi.arterion.client.network.packet;

import java.nio.ByteBuffer;
import java.util.UUID;

import me.friwi.arterion.client.network.ModPacket;

public class Packet04FriendlyCreateOrUpdate extends ModPacket {
    private UUID player;
    private String name;
    private String selectedClass;
    private String selectedClassName;
    private int level;
    private int health;
    private int maxhealth;

    public Packet04FriendlyCreateOrUpdate() {
    }

    public Packet04FriendlyCreateOrUpdate(UUID player, String name, String selectedClass, String selectedClassName, int level, int health, int maxhealth) {
        this.player = player;
        this.name = name;
        this.selectedClass = selectedClass;
        this.selectedClassName = selectedClassName;
        this.level = level;
        this.health = health;
        this.maxhealth = maxhealth;
    }

    @Override
    public byte getId() {
        return 4;
    }

    @Override
    public void writeData(ByteBuffer buffer) {
        buffer.putLong(player.getMostSignificantBits());
        buffer.putLong(player.getLeastSignificantBits());
        writeString(buffer, name);
        writeString(buffer, selectedClass);
        writeString(buffer, selectedClassName);
        buffer.putInt(level);
        buffer.putInt(health);
        buffer.putInt(maxhealth);
    }

    @Override
    public void readData(ByteBuffer buffer) {
        player = new UUID(buffer.getLong(), buffer.getLong());
        name = readString(buffer);
        selectedClass = readString(buffer);
        selectedClassName = readString(buffer);
        level = buffer.getInt();
        health = buffer.getInt();
        maxhealth = buffer.getInt();
    }

    public UUID getPlayer() {
        return player;
    }

    public void setPlayer(UUID player) {
        this.player = player;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSelectedClass() {
        return selectedClass;
    }

    public void setSelectedClass(String selectedClass) {
        this.selectedClass = selectedClass;
    }

    public String getSelectedClassName() {
        return selectedClassName;
    }

    public void setSelectedClassName(String selectedClassName) {
        this.selectedClassName = selectedClassName;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getHealth() {
        return health;
    }

    public void setHealth(int health) {
        this.health = health;
    }

    public int getMaxhealth() {
        return maxhealth;
    }

    public void setMaxhealth(int maxhealth) {
        this.maxhealth = maxhealth;
    }

    @Override
    public String toString() {
        return "Packet04FriendlyCreateOrUpdate{" +
                "player=" + player +
                ", name='" + name + '\'' +
                ", selectedClass='" + selectedClass + '\'' +
                ", selectedClassName='" + selectedClassName + '\'' +
                ", level=" + level +
                ", health=" + health +
                ", maxhealth=" + maxhealth +
                '}';
    }
}
