package me.friwi.arterion.plugin.combat.skill;

public class SkillContainerData {
    private long lastUsed;
    private long activeUntil;
    private long activeTime;

    public SkillContainerData() {
        this.lastUsed = System.currentTimeMillis();
        this.activeUntil = lastUsed;
    }

    public long getLastUsed() {
        return lastUsed;
    }

    public void setLastUsed(long lastUsed) {
        this.lastUsed = lastUsed;
    }

    public long getActiveUntil() {
        return activeUntil;
    }

    public void setActiveUntil(long activeUntil) {
        this.activeUntil = activeUntil;
        this.activeTime = activeUntil - System.currentTimeMillis();
        if (this.activeTime < 0) activeTime = 0;
    }

    public long getActiveTime() {
        return activeTime;
    }
}
