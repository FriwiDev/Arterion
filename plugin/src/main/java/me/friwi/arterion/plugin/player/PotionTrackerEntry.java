package me.friwi.arterion.plugin.player;

import org.bukkit.potion.PotionEffectType;

public class PotionTrackerEntry {
    private PotionEffectType effectType;
    private int amplifier;
    private long expires;

    public PotionTrackerEntry(PotionEffectType effectType, int amplifier, long expires) {
        this.effectType = effectType;
        this.amplifier = amplifier;
        this.expires = expires;
    }

    public PotionEffectType getEffectType() {
        return effectType;
    }

    public void setEffectType(PotionEffectType effectType) {
        this.effectType = effectType;
    }

    public int getAmplifier() {
        return amplifier;
    }

    public void setAmplifier(int amplifier) {
        this.amplifier = amplifier;
    }

    public long getExpires() {
        return expires;
    }

    public void setExpires(long expires) {
        this.expires = expires;
    }

    public boolean isActive() {
        return expires > 0;
    }

    public PotionTrackerEntry max(PotionTrackerEntry other) {
        if (other == null) return this;
        if (!other.getEffectType().equals(this.getEffectType())) return this;
        if (other.getAmplifier() < this.getAmplifier()) return this;
        if (other.getAmplifier() == this.getAmplifier() && other.getExpires() <= this.getExpires()) return this;
        return other;
    }

    @Override
    public String toString() {
        return "PotionTrackerEntry{" +
                "effectType=" + effectType +
                ", amplifier=" + amplifier +
                ", expires=" + expires +
                '}';
    }
}
