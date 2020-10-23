package me.friwi.arterion.plugin.world.villager;

import de.tr7zw.changeme.nbtapi.NBTCompound;
import de.tr7zw.changeme.nbtapi.NBTEntity;
import de.tr7zw.nbtinjector.NBTInjector;
import me.friwi.arterion.plugin.listener.CreatureSpawnListener;
import me.friwi.arterion.plugin.world.hologram.HologramCreator;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Villager;

public class CustomVillagerUtil {
    public static Villager spawnVillager(Location loc, VillagerType type) {
        CreatureSpawnListener.isSpawningWithCommand = true;
        Villager villager = (Villager) loc.getWorld().spawnEntity(loc, EntityType.VILLAGER);
        CreatureSpawnListener.isSpawningWithCommand = false;
        villager.setRemoveWhenFarAway(false);
        villager.setCanPickupItems(false);
        villager.setProfession(type.getProfession());
        //villager.setCustomName(type.getCustomName().split("\n")[0]);
        villager.setCustomNameVisible(false);
        HologramCreator.createHologram(loc.clone().add(0, 1.8, 0), type.getCustomName().split("\n"));
        setVillagerType(villager, type);
        return villager;
    }

    public static VillagerType getVillagerType(Entity villager) {
        NBTCompound comp = NBTInjector.getNbtData(villager);
        if (comp != null) {
            if (comp.hasKey("E_villager")) {
                return VillagerType.values()[comp.getInteger("E_villager")];
            }
        }
        return null;
    }

    public static void setVillagerType(Entity villager, VillagerType type) {
        villager = NBTInjector.patchEntity(villager); //This needs to be called at least once on the entity to enable custom nbt!
        NBTCompound comp = NBTInjector.getNbtData(villager);
        comp.setInteger("E_villager", type.ordinal());
        NBTEntity ent = new NBTEntity(villager);
        ent.setInteger("NoAI", 1);
        ent.setInteger("Silent", 1);
    }
}
