package me.friwi.arterion.plugin.world.item.lock;

import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.world.item.CustomItemType;
import me.friwi.arterion.plugin.world.lock.GuildLock;
import me.friwi.arterion.plugin.world.lock.Lock;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class GuildLockItem extends LockItem {


    public GuildLockItem(ItemStack stack) {
        super(CustomItemType.GUILD_LOCK, stack);
    }

    public GuildLockItem() {
        super(CustomItemType.GUILD_LOCK);
    }

    @Override
    protected Material getLockMaterial() {
        return Material.IRON_INGOT;
    }

    @Override
    protected Lock generateLock(ArterionPlayer arterionPlayer) {
        if (arterionPlayer.getGuild() == null) {
            arterionPlayer.sendTranslation("lock.place.notinguild");
            return null;
        }
        return new GuildLock(arterionPlayer.getGuild());
    }
}
