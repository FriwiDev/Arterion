package me.friwi.arterion.client.network.packet;

import java.nio.ByteBuffer;

import me.friwi.arterion.client.network.ModPacket;

public class Packet07SkillSlotData extends ModPacket {
    private byte slot;
    private int skill;
    private boolean enabled;
    private String skillName;
    private String skillDescription;
    private long delay;
    private long totalDelay;
    private int delayColor;
    private long cooldown;
    private long maxCooldown;
    private int mana;

    public Packet07SkillSlotData() {
    }

    public Packet07SkillSlotData(byte slot, int skill, boolean enabled, String skillName, String skillDescription, long delay, long totalDelay, int delayColor, long cooldown, long maxCooldown, int mana) {
        this.slot = slot;
        this.skill = skill;
        this.enabled = enabled;
        this.skillName = skillName;
        this.skillDescription = skillDescription;
        this.delay = delay;
        this.totalDelay = totalDelay;
        this.delayColor = delayColor;
        this.cooldown = cooldown;
        this.maxCooldown = maxCooldown;
        this.mana = mana;
    }

    @Override
    public byte getId() {
        return 7;
    }

    @Override
    public void writeData(ByteBuffer buffer) {
        buffer.put(slot);
        buffer.putInt(skill);
        buffer.put((byte) (enabled ? 1 : 0));
        writeString(buffer, skillName);
        writeString(buffer, skillDescription);
        buffer.putLong(delay);
        buffer.putLong(totalDelay);
        buffer.putInt(delayColor);
        buffer.putLong(cooldown);
        buffer.putLong(maxCooldown);
        buffer.putInt(mana);
    }

    @Override
    public void readData(ByteBuffer buffer) {
        slot = buffer.get();
        skill = buffer.getInt();
        enabled = buffer.get() == (byte) 1;
        skillName = readString(buffer);
        skillDescription = readString(buffer);
        delay = buffer.getLong();
        totalDelay = buffer.getLong();
        delayColor = buffer.getInt();
        cooldown = buffer.getLong();
        maxCooldown = buffer.getLong();
        mana = buffer.getInt();
    }

    public byte getSlot() {
        return slot;
    }

    public void setSlot(byte slot) {
        this.slot = slot;
    }

    public int getSkill() {
        return skill;
    }

    public void setSkill(int skill) {
        this.skill = skill;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getSkillName() {
        return skillName;
    }

    public void setSkillName(String skillName) {
        this.skillName = skillName;
    }

    public String getSkillDescription() {
        return skillDescription;
    }

    public void setSkillDescription(String skillDescription) {
        this.skillDescription = skillDescription;
    }

    public long getDelay() {
        return delay;
    }

    public void setDelay(long delay) {
        this.delay = delay;
    }

    public long getTotalDelay() {
        return totalDelay;
    }

    public void setTotalDelay(long totalDelay) {
        this.totalDelay = totalDelay;
    }

    public int getDelayColor() {
        return delayColor;
    }

    public void setDelayColor(int delayColor) {
        this.delayColor = delayColor;
    }

    public long getCooldown() {
        return cooldown;
    }

    public void setCooldown(long cooldown) {
        this.cooldown = cooldown;
    }

    public long getMaxCooldown() {
        return maxCooldown;
    }

    public void setMaxCooldown(long maxCooldown) {
        this.maxCooldown = maxCooldown;
    }

    public int getMana() {
        return mana;
    }

    public void setMana(int mana) {
        this.mana = mana;
    }

    @Override
    public String toString() {
        return "Packet07SkillSlotData{" +
                "slot=" + slot +
                ", skill=" + skill +
                ", enabled=" + enabled +
                ", skillName='" + skillName + '\'' +
                ", skillDescription='" + skillDescription + '\'' +
                ", delay=" + delay +
                ", totalDelay=" + totalDelay +
                ", delayColor=" + delayColor +
                ", cooldown=" + cooldown +
                ", maxCooldown=" + maxCooldown +
                ", mana=" + mana +
                '}';
    }
}
