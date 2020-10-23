package me.friwi.arterion.plugin.combat.pvpchest;

import com.darkblade12.particleeffect.ParticleEffect;
import me.friwi.arterion.plugin.ArterionPlugin;
import me.friwi.arterion.plugin.combat.skill.Skill;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.security.SecureRandom;
import java.util.Random;

public class PvPChest {
    private static final Random RANDOM = new SecureRandom();

    private Block loc;
    private byte data;

    public PvPChest(Block loc, byte data) {
        this.loc = loc;
        this.data = data;
    }

    public void spawnChest() {
        loc.setTypeIdAndData(Material.CHEST.getId(), data, false);
        BlockState b = loc.getState();
        if (b instanceof Chest) {
            Chest c = (Chest) b;
            Inventory inv = c.getBlockInventory();
            int dropAmount = ArterionPlugin.getInstance().getFormulaManager().PVPCHEST_DROP_AMOUNT.evaluateInt(RANDOM.nextDouble());
            for (int i = 0; i < dropAmount; i++) {
                int slot = RANDOM.nextInt(inv.getSize());
                inv.setItem(slot, PvPChestDrops.INSTANCE.getRandomDrop());
            }
            c.update();
        }
        loc.getWorld().playSound(loc.getLocation(), Sound.ENDERMAN_TELEPORT, 0.8f, 1f);
        loc.getWorld().playEffect(loc.getLocation(), Effect.ENDER_SIGNAL, 0);
        System.out.println("Spawned pvp chest at " + loc.getX() + ", " + loc.getY() + ", " + loc.getZ());
    }

    public void removeChestWithoutDrops() {
        if (loc.getType() == Material.CHEST) {
            BlockState b = loc.getState();
            if (b instanceof InventoryHolder) {
                try {
                    ((InventoryHolder) b).getInventory().clear();
                    b.update();
                } catch (Exception e) {
                    //CraftBlock is not a chest, bla bla. Ignore it, only happens sometimes
                }
            }
        }
        removeChest();
    }

    public void removeChest() {
        loc.setType(Material.AIR);
        loc.getWorld().playSound(loc.getLocation(), Sound.ENDERMAN_TELEPORT, 0.8f, 1f);
        loc.getWorld().playEffect(loc.getLocation(), Effect.ENDER_SIGNAL, 0);
    }

    public Block getLoc() {
        return loc;
    }

    public void playEffect(boolean sound) {
        ParticleEffect.FLAME.display(0.8f, 0.8f, 0.8f, 0, 15, loc.getLocation().add(0.5, 0.5, 0.5), Skill.PARTICLE_RANGE);
        if (sound) loc.getWorld().playSound(loc.getLocation(), Sound.PORTAL, 1f, 1f);
    }
}
