package me.friwi.arterion.plugin.combat.gamemode.artefact;

import me.friwi.arterion.plugin.ArterionPlugin;
import me.friwi.arterion.plugin.listener.CreatureSpawnListener;
import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.ui.progress.ProgressBar;
import me.friwi.arterion.plugin.world.block.nonbtblocks.SpecialBlock;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.world.ChunkUnloadEvent;

public class ArtefactBlock extends SpecialBlock implements Listener {
    public static final int PROGRESSBAR_LENGTH = 25;

    boolean hasArtefact;
    int health;
    private ArmorStand hologram;
    private long lastDamage = 0;

    public ArtefactBlock(Location loc) {
        super(loc.getBlock().getLocation());
        this.health = getMaxHealth();
        ArterionPlugin.getInstance().getServer().getPluginManager().registerEvents(this, ArterionPlugin.getInstance());
    }

    @Override
    public boolean onInteract(ArterionPlayer player, Action action) {
        Artefact.doReplayEvent();
        if (action != Action.LEFT_CLICK_BLOCK || !this.hasArtefact || lastDamage + 1000 > System.currentTimeMillis()) {
            return false;
        }
        if (player.getGuild() == null) {
            player.sendTranslation("artefact.noguild");
            return false;
        }
        if (player.getBukkitPlayer().getItemInHand() == null || player.getBukkitPlayer().getItemInHand().getType() != Material.DIAMOND_PICKAXE) {
            player.sendTranslation("artefact.pickaxe");
            return false;
        }
        if (Artefact.areCristalsAlive()) {
            player.sendTranslation("artefact.cristalsalive");
            return false;
        }
        if (!Artefact.hasClaim(player.getGuild())) {
            player.sendTranslation("artefact.noclaim");
            return false;
        }
        if (player.getArenaInitializer() != null) {
            player.sendTranslation("artefact.inarena");
            return false;
        }
        lastDamage = System.currentTimeMillis();
        int damage = 1;
        if (damage >= health) {
            onDestroy(player);
            setHealth(0);
        } else {
            setHealth(health - damage);
        }
        if (health > 0) {
            player.sendTranslation("artefact.health", health, getMaxHealth());
        }
        return false;
    }

    @Override
    public boolean onBreak(ArterionPlayer player) {
        return false;
    }

    public int getMaxHealth() {
        return ArterionPlugin.getInstance().getFormulaManager().ARTEFACT_BLOCK_MAXHP.evaluateInt();
    }

    public boolean isHasArtefact() {
        return hasArtefact;
    }

    public void setHasArtefact(boolean hasArtefact) {
        this.hasArtefact = hasArtefact;
        getLocation().getBlock().setTypeIdAndData(Material.ENDER_PORTAL_FRAME.getId(), (byte) (hasArtefact ? 4 : 0), false);
        getLocation().getBlock().getRelative(BlockFace.DOWN).setTypeIdAndData((hasArtefact ? Material.STAINED_GLASS : Material.OBSIDIAN).getId(), (byte) (hasArtefact ? 2 : 0), true);
        remove();
        if (hasArtefact) {
            this.health = getMaxHealth();
            spawn();
        }
    }

    public int getHealth() {
        return health;
    }

    public void setHealth(int health) {
        this.health = health;
        if (health <= 0) {
            remove();
            return;
        }
        updateHoloText();
    }

    private void spawn() {
        if (Artefact.getFight() == null || Artefact.getFight().isEnded()) return;
        this.getLocation().getChunk().load();
        CreatureSpawnListener.isSpawningWithCommand = true;
        this.hologram = (ArmorStand) getLocation().getWorld().spawnEntity(getLocation().clone().add(0.5, 1, 0.5), EntityType.ARMOR_STAND);
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

    void remove() {
        if (this.hologram != null) hologram.remove();
        hologram = null;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChunkUnload(ChunkUnloadEvent evt) {
        if (hologram != null && evt.getChunk().equals(hologram.getLocation().getChunk())) {
            evt.setCancelled(true);
        }
    }

    private void updateHoloText() {
        if (this.hologram == null || this.hologram.isDead()) return;
        float perc = (health + 0f) / (getMaxHealth() + 0f);
        this.hologram.setCustomName(ProgressBar.generate(perc > 0.5f ? "\247a" : (perc > 0.25f ? "\2476" : "\2474"), perc, PROGRESSBAR_LENGTH));
    }

    private void onDestroy(ArterionPlayer destroyer) {
        getLocation().getWorld().playEffect(getLocation(), Effect.ENDER_SIGNAL, 0);
        for (Player p : ArterionPlugin.getOnlinePlayers()) {
            p.playSound(p.getLocation(), Sound.ENDERDRAGON_DEATH, 1f, 1f);
        }
        this.setHasArtefact(false);
        destroyer.setArtefactCarrier(new ArtefactCarrier(destroyer));
        for (ArtefactCristal cristal : Artefact.getCristals()) cristal.setArtefactFight(null);
        Artefact.updateObjectives();
    }
}
