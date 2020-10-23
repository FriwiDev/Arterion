package me.friwi.arterion.plugin.combat.gamemode.artefact;

import me.friwi.arterion.plugin.ArterionPlugin;
import me.friwi.arterion.plugin.guild.Guild;
import me.friwi.arterion.plugin.listener.CreatureSpawnListener;
import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.player.ArterionPlayerUtil;
import me.friwi.arterion.plugin.ui.progress.ProgressBar;
import me.friwi.arterion.plugin.util.language.api.LanguageAPI;
import me.friwi.arterion.plugin.util.scheduler.InternalTask;
import me.friwi.arterion.plugin.util.time.TimeFormatUtil;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

import java.util.LinkedList;
import java.util.List;

public class ArtefactCristal implements Listener {
    public static final int PROGRESSBAR_LENGTH = 25;
    public static final int ALLY_RADIUS = 4;

    private Location location;
    private EnderCrystal enderCrystal;
    private ArmorStand hologram;
    private int health;
    private Guild claimedBy;
    private int claimedTicks = 0;
    private ArtefactFight artefactFight;
    private long lastDamage = 0;

    public ArtefactCristal(Location location) {
        this.location = location;
        this.health = getMaxHealth();
        ArterionPlugin.getInstance().getServer().getPluginManager().registerEvents(this, ArterionPlugin.getInstance());
    }

    private int getMaxHealth() {
        return ArterionPlugin.getInstance().getFormulaManager().ARTEFACT_CRISTAL_MAXHP.evaluateInt();
    }

    public void respawn() {
        this.claimedBy = null;
        this.health = getMaxHealth();
        removeAll();
        spawn(true);
    }

    private void spawn(boolean createHolo) {
        if (artefactFight == null || artefactFight.isEnded()) return;
        this.location.getChunk().load();
        this.enderCrystal = location.getWorld().spawn(location, EnderCrystal.class);
        if (createHolo) {
            CreatureSpawnListener.isSpawningWithCommand = true;
            this.hologram = (ArmorStand) location.getWorld().spawnEntity(location.clone().add(0, 2, 0), EntityType.ARMOR_STAND);
            CreatureSpawnListener.isSpawningWithCommand = false;
            this.hologram.setVisible(false);
            updateHoloText();
            this.hologram.setCustomNameVisible(true);
            this.hologram.setGravity(false);
            this.hologram.setCanPickupItems(false);
            this.hologram.setNoDamageTicks(30000);
            this.hologram.setArms(false);
            this.hologram.setMarker(true);
        }
    }

    private void remove() {
        if (enderCrystal != null) enderCrystal.remove();
        enderCrystal = null;
    }

    void removeAll() {
        remove();
        if (hologram != null) hologram.remove();
        hologram = null;
    }

    public void move(Location n) {
        this.location = n.clone();
        if (hologram != null && !hologram.isDead()) {
            removeAll();
            spawn(true);
        }
    }

    private void onDestroy(Guild destroyer) {
        if (enderCrystal == null || enderCrystal.isDead()) return;
        remove();
        location.getWorld().playEffect(location, Effect.EXPLOSION_HUGE, 0);
        location.getWorld().playSound(location, Sound.EXPLODE, 1f, 1f);
        this.health = 0;
        this.claimedBy = destroyer;
        this.claimedTicks = ArterionPlugin.getInstance().getFormulaManager().ARTEFACT_CRISTAL_RESPAWN_TICKS.evaluateInt();
        LanguageAPI.broadcastMessage("artefact.cristal.destroyed", destroyer.getName(), Artefact.getCristals().length - Artefact.countLivingCristals(), Artefact.getCristals().length);
        Artefact.appendTranslationToRecordingChat("artefact.replay.cristal.destroyed", destroyer.getName(), Artefact.getCristals().length - Artefact.countLivingCristals(), Artefact.getCristals().length);
        updateHoloText();
        ArtefactFight oldArtefight = artefactFight;
        ArterionPlugin.getInstance().getSchedulers().getMainScheduler().executeInSpigotCircleTimer(new InternalTask() {
            @Override
            public void run() {
                claimedTicks -= 20;
                if (artefactFight == null || artefactFight.isEnded() || artefactFight != oldArtefight || Artefact.getCarrier() != null) {
                    cancel();
                    return;
                }
                if (claimedTicks <= 0) {
                    cancel();
                    removeAll();
                    respawn();
                    if (artefactFight != null) artefactFight.onCristalRespawn();
                    Artefact.updateObjectives();
                    return;
                }
                updateHoloText();
            }
        }, 20, 20);
        Artefact.updateObjectives();
    }

    private void updateHoloText() {
        if (this.hologram == null || this.hologram.isDead()) return;
        if (this.claimedBy != null && this.claimedTicks > 0) {
            this.hologram.setCustomName("\247e" + claimedBy.getName() + " \2477" + TimeFormatUtil.formatSeconds(this.claimedTicks / 20));
        } else {
            float perc = (health + 0f) / (getMaxHealth() + 0f);
            this.hologram.setCustomName(ProgressBar.generate(perc > 0.5f ? "\247a" : (perc > 0.25f ? "\2476" : "\2474"), perc, PROGRESSBAR_LENGTH));
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEnderCrystalTakeDamage(EntityDamageEvent evt) {
        if ((enderCrystal != null && evt.getEntity().equals(enderCrystal)) || (hologram != null && evt.getEntity().equals(hologram))) {
            evt.setCancelled(true);
            if (health <= 0 || enderCrystal == null || lastDamage + 1000 > System.currentTimeMillis()) return;
            if (evt instanceof EntityDamageByEntityEvent) {
                if (((EntityDamageByEntityEvent) evt).getDamager() instanceof Player) {
                    Artefact.doReplayEvent();
                    ArterionPlayer damager = ArterionPlayerUtil.get((Player) ((EntityDamageByEntityEvent) evt).getDamager());
                    if (damager.getGuild() != null) {
                        lastDamage = System.currentTimeMillis();
                        int damage = 0;
                        //Calculate damage from nearby allies
                        List<ArterionPlayer> sendHealth = new LinkedList<>();
                        for (Entity ent : enderCrystal.getNearbyEntities(ALLY_RADIUS, ALLY_RADIUS, ALLY_RADIUS)) {
                            if (ent instanceof Player) {
                                ArterionPlayer nearby = ArterionPlayerUtil.get((Player) ent);
                                if (nearby.getGuild() != null && nearby.getGuild().equals(damager.getGuild())) {
                                    damage++;
                                    sendHealth.add(nearby);
                                }
                            }
                        }
                        if (damage >= health) {
                            onDestroy(damager.getGuild());
                            setHealth(0);
                        } else {
                            setHealth(health - damage);
                        }
                        if (health > 0) {
                            for (ArterionPlayer nearby : sendHealth) {
                                nearby.sendTranslation("artefact.cristal.health", health, getMaxHealth());
                            }
                        }
                    } else {
                        damager.sendTranslation("artefact.noguild");
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChunkUnload(ChunkUnloadEvent evt) {
        if ((enderCrystal != null && evt.getChunk().equals(enderCrystal.getLocation().getChunk())) || (hologram != null && evt.getChunk().equals(hologram.getLocation().getChunk()))) {
            evt.setCancelled(true);
        }
    }

    public Guild getClaimedBy() {
        return claimedBy;
    }

    public boolean isAlive() {
        return health > 0;
    }

    public void setArtefactFight(ArtefactFight artefactFight) {
        this.artefactFight = artefactFight;
        removeAll();
        if (artefactFight != null) {
            this.health = getMaxHealth();
            this.claimedBy = null;
            this.claimedTicks = 0;
            spawn(true);
        }
    }

    public int getHealth() {
        return health;
    }

    private void setHealth(int health) {
        this.health = health;
        if (health <= 0) {
            remove();
        }
        updateHoloText();
    }
}
