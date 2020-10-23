package me.friwi.recordable.impl;

import me.friwi.recordable.EnchantmentTableOpener;
import net.minecraft.server.v1_8_R3.Container;
import net.minecraft.server.v1_8_R3.ContainerEnchantTable;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftInventoryView;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryView;

public class EnchantmentTableOpenerImpl implements EnchantmentTableOpener {
    @Override
    public void open(Player player, Location loc, boolean force, boolean enchantAsWeapon) {
        InventoryView view = player.openEnchanting(loc, force);
        Container container = ((CraftInventoryView)view).getHandle();
        if(container instanceof ContainerEnchantTable){
            ((ContainerEnchantTable) container).setEnchantAsWeapon(enchantAsWeapon);
        }
    }
}
