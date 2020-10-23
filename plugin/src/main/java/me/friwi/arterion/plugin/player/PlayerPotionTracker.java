package me.friwi.arterion.plugin.player;

import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

public class PlayerPotionTracker {
    public static final int TOLERANCE = 40;

    private Map<PotionEffectType, List<PotionTrackerEntry>> potionMap = new HashMap<>();
    private ArterionPlayer player;

    public PlayerPotionTracker(ArterionPlayer player) {
        this.player = player;
        this.clearEffects();
    }

    public void updateTracking() {
        for (PotionEffect effect : player.getBukkitPlayer().getActivePotionEffects()) {
            List<PotionTrackerEntry> entries = potionMap.get(effect.getType());
            for (PotionTrackerEntry entry : entries) {
                entry.setExpires(entry.getExpires() - 1);
            }
            if (effect.getDuration() >= TOLERANCE && !contains(effect, entries)) {
                entries.add(new PotionTrackerEntry(effect.getType(), effect.getAmplifier(), effect.getDuration()));
            }
        }
        syncPotions();
    }

    public PotionTrackerEntry addPotionEffect(PotionEffectType type, int amplifier, int duration) {
        PotionTrackerEntry ent = new PotionTrackerEntry(type, amplifier, duration);
        potionMap.get(type).add(ent);
        syncPotions();
        return ent;
    }

    public PotionTrackerEntry addPotionEffect(PotionEffect effect) {
        PotionTrackerEntry entry = new PotionTrackerEntry(effect.getType(), effect.getAmplifier(), effect.getDuration());
        potionMap.get(effect.getType()).add(entry);
        syncPotions();
        return entry;
    }

    public void addPotionEffects(Collection<PotionEffect> effects) {
        for (PotionEffect effect : effects) {
            potionMap.get(effect.getType()).add(new PotionTrackerEntry(effect.getType(), effect.getAmplifier(), effect.getDuration()));
        }
        syncPotions();
    }

    public void removeAllPotionEffects(PotionEffectType type, int amplifier) {
        boolean sync = false;
        Iterator<PotionTrackerEntry> it = potionMap.get(type).iterator();
        while (it.hasNext()) {
            PotionTrackerEntry entry = it.next();
            if (entry.getEffectType().equals(type) && entry.getAmplifier() == amplifier) {
                it.remove();
                sync = true;
            }
        }
        if (sync) syncPotions();
    }

    public void removeAllPotionEffects(PotionEffectType type) {
        boolean sync = false;
        Iterator<PotionTrackerEntry> it = potionMap.get(type).iterator();
        while (it.hasNext()) {
            PotionTrackerEntry entry = it.next();
            if (entry.getEffectType().equals(type)) {
                it.remove();
                sync = true;
            }
        }
        if (sync) syncPotions();
    }

    public void removeAllPotionEffects(PotionEffectType[] types) {
        boolean sync = false;
        for (PotionEffectType type : types) {
            if (type == null) continue;
            Iterator<PotionTrackerEntry> it = potionMap.get(type).iterator();
            while (it.hasNext()) {
                PotionTrackerEntry entry = it.next();
                if (entry.getEffectType().equals(type)) {
                    it.remove();
                    sync = true;
                }
            }
        }
        if (sync) syncPotions();
    }

    public void removePotionTrackerEntry(PotionTrackerEntry entry) {
        potionMap.get(entry.getEffectType()).remove(entry);
        syncPotions();
    }

    private void syncPotions() {
        for (Map.Entry<PotionEffectType, List<PotionTrackerEntry>> entries : potionMap.entrySet()) {
            PotionTrackerEntry max = null;
            Iterator<PotionTrackerEntry> it = entries.getValue().iterator();
            while (it.hasNext()) {
                PotionTrackerEntry now = it.next();
                if (!now.isActive()) {
                    it.remove();
                    continue;
                } else {
                    max = now.max(max);
                }
            }
            if (max == null) {
                if (player.getBukkitPlayer().hasPotionEffect(entries.getKey())) {
                    player.getBukkitPlayer().removePotionEffect(entries.getKey());
                }
            } else {
                PotionEffect eff = getCurrentPotionFromPlayer(entries.getKey());
                if (!corresponds(eff, max)) {
                    player.getBukkitPlayer().addPotionEffect(new PotionEffect(max.getEffectType(), (int) (max.getExpires()), max.getAmplifier()), true);
                }
            }
        }
    }

    public PotionEffect getCurrentPotionFromPlayer(PotionEffectType type) {
        for (PotionEffect eff : player.getBukkitPlayer().getActivePotionEffects()) {
            if (eff.getType().equals(type)) {
                return eff;
            }
        }
        return null;
    }

    public boolean contains(PotionEffect effect, List<PotionTrackerEntry> entries) {
        for (PotionTrackerEntry entry : entries) {
            if (corresponds(effect, entry)) return true;
        }
        return false;
    }

    public boolean corresponds(PotionEffect effect, PotionTrackerEntry entry) {
        return effect != null && effect.getType().equals(entry.getEffectType()) && effect.getAmplifier() == entry.getAmplifier()
                && Math.abs(effect.getDuration() - entry.getExpires()) <= TOLERANCE;
    }

    public void clearEffects() {
        potionMap.clear();
        for (PotionEffectType type : PotionEffectType.values()) {
            if (type == null) continue;
            potionMap.put(type, new LinkedList<PotionTrackerEntry>());
        }
        syncPotions();
    }
}
