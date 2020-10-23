package me.friwi.recordable.impl;

import me.friwi.recordable.AnvilOpener;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftInventoryView;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryView;

import java.util.function.Consumer;

public class AnvilOpenerImpl implements AnvilOpener {
    @Override
    public void open(Player player, Location loc, boolean force, boolean enchantAsWeapon, Consumer<Integer> priceAnnounce) {
        ((CraftPlayer) player).getHandle().openTileEntity(new net.minecraft.server.v1_8_R3.BlockAnvil.TileEntityContainerAnvil(
                ((CraftWorld)player.getWorld()).getHandle(),
                new BlockPosition(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ())));

        ContainerAnvil anvil = (ContainerAnvil) ((CraftPlayer) player).getHandle().activeContainer;
        if(anvil!=null) {
            anvil.setEnchantAsWeapon(enchantAsWeapon);
            anvil.setCallback(priceAnnounce);
        }
    }
}
