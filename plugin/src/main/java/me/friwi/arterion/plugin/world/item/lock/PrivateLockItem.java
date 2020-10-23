package me.friwi.arterion.plugin.world.item.lock;

import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.world.item.CustomItemType;
import me.friwi.arterion.plugin.world.lock.Lock;
import me.friwi.arterion.plugin.world.lock.PrivateLock;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class PrivateLockItem extends LockItem {


    public PrivateLockItem(ItemStack stack) {
        super(CustomItemType.PRIVATE_LOCK, stack);
    }

    public PrivateLockItem() {
        super(CustomItemType.PRIVATE_LOCK);
    }

    @Override
    protected Material getLockMaterial() {
        return Material.DIAMOND;
    }

    @Override
    protected Lock generateLock(ArterionPlayer arterionPlayer) {
        return new PrivateLock(arterionPlayer.getBukkitPlayer().getUniqueId());
    }
}
