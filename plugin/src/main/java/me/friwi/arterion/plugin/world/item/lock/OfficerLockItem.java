package me.friwi.arterion.plugin.world.item.lock;

import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.world.item.CustomItemType;
import me.friwi.arterion.plugin.world.lock.Lock;
import me.friwi.arterion.plugin.world.lock.OfficerLock;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class OfficerLockItem extends LockItem {


    public OfficerLockItem(ItemStack stack) {
        super(CustomItemType.OFFICER_LOCK, stack);
    }

    public OfficerLockItem() {
        super(CustomItemType.OFFICER_LOCK);
    }

    @Override
    protected Material getLockMaterial() {
        return Material.GOLD_INGOT;
    }

    @Override
    protected Lock generateLock(ArterionPlayer arterionPlayer) {
        if (arterionPlayer.getGuild() == null) {
            arterionPlayer.sendTranslation("lock.place.notinguild");
            return null;
        }
        return new OfficerLock(arterionPlayer.getGuild());
    }
}
