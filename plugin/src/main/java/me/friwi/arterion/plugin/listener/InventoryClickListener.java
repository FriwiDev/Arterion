package me.friwi.arterion.plugin.listener;

import com.google.common.collect.ImmutableList;
import me.friwi.arterion.plugin.ArterionPlugin;
import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.player.ArterionPlayerUtil;
import me.friwi.arterion.plugin.util.scheduler.InternalTask;
import me.friwi.arterion.plugin.world.item.CustomItem;
import me.friwi.arterion.plugin.world.item.CustomItemType;
import me.friwi.arterion.plugin.world.item.CustomItemUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.EnchantingInventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class InventoryClickListener implements Listener {
    public static final List<Material> BANNED_ENDERCHEST_MATERIALS = ImmutableList.of(
            Material.DIAMOND_BLOCK,
            Material.GOLD_BLOCK,
            Material.IRON_BLOCK,
            Material.EMERALD_BLOCK,
            Material.LAPIS_BLOCK
    );

    private ArterionPlugin plugin;

    public InventoryClickListener(ArterionPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent evt) {
        if (evt.getWhoClicked() instanceof Player) {
            ArterionPlayer ep = ArterionPlayerUtil.get((Player) evt.getWhoClicked());
            if (ep.getOpenGui() != null) {
                if (ep.getOpenGui().onPlayerClickItem(evt.getClickedInventory(), evt.getClick(), evt.getSlot())) {
                    evt.setCancelled(true);
                    return;
                }
            }
            if (evt.getCurrentItem() != null) {
                if (evt.getClick() == ClickType.NUMBER_KEY || evt.getClick() == ClickType.SHIFT_LEFT || evt.getClick() == ClickType.SHIFT_RIGHT || (evt.getClickedInventory() != null && !evt.getClickedInventory().equals(ep.getBukkitPlayer().getInventory()))) {
                    CustomItem item = CustomItemUtil.getCustomItem(evt.getCurrentItem());
                    if (!item.onSwitchInventory(ep)) {
                        evt.setCancelled(true);
                        ep.getBukkitPlayer().updateInventory();
                        return;
                    }
                    if (item.getType() == CustomItemType.NONE && evt.getWhoClicked().getOpenInventory().getType() == InventoryType.ENDER_CHEST && !evt.getWhoClicked().isOp()) {
                        if (BANNED_ENDERCHEST_MATERIALS.contains(evt.getCurrentItem().getType())) {
                            evt.setCancelled(true);
                            ep.getBukkitPlayer().updateInventory();
                            ep.sendTranslation("notforechest");
                            return;
                        }
                        if (evt.getClick() == ClickType.NUMBER_KEY) {
                            ItemStack stack = evt.getWhoClicked().getInventory().getItem(evt.getHotbarButton());
                            if (stack != null && BANNED_ENDERCHEST_MATERIALS.contains(stack.getType())) {
                                evt.setCancelled(true);
                                ep.getBukkitPlayer().updateInventory();
                                ep.sendTranslation("notforechest");
                                return;
                            }
                        }
                    }
                }
            }
            if (evt.getCursor() != null) {
                if (evt.getClickedInventory() != null && !evt.getClickedInventory().equals(ep.getBukkitPlayer().getInventory()) && !evt.getWhoClicked().isOp()) {
                    CustomItem item = CustomItemUtil.getCustomItem(evt.getCursor());
                    if (!item.onSwitchInventory(ep)) {
                        evt.setCancelled(true);
                        ep.getBukkitPlayer().updateInventory();
                        return;
                    }
                    if (item.getType() == CustomItemType.NONE && evt.getWhoClicked().getOpenInventory().getType() == InventoryType.ENDER_CHEST) {
                        if (BANNED_ENDERCHEST_MATERIALS.contains(evt.getCursor().getType())) {
                            evt.setCancelled(true);
                            ep.getBukkitPlayer().updateInventory();
                            ep.sendTranslation("notforechest");
                            return;
                        }
                    }
                }
            }
            //Deglitch anvil
            if (evt.getClickedInventory() instanceof AnvilInventory) {
                if (evt.getSlot() == 2 && (evt.getClick() == ClickType.LEFT || evt.getClick() == ClickType.DOUBLE_CLICK || evt.getClick() == ClickType.RIGHT)) {
                    ArterionPlugin.getInstance().getSchedulers().getMainScheduler().executeInSpigotCircleLater(new InternalTask() {
                        @Override
                        public void run() {
                            ItemStack itemStack = evt.getWhoClicked().getItemOnCursor();
                            evt.getWhoClicked().setItemOnCursor(null);
                            evt.getWhoClicked().setItemOnCursor(itemStack);
                        }
                    }, 1);
                }
            }
            //Deglitch enchantment table
            if (evt.getClickedInventory() instanceof EnchantingInventory) {
                if (evt.getSlot() == 0) {
                    //Prevent bugging of multiple items
                    if (evt.getAction() == InventoryAction.HOTBAR_SWAP) {
                        evt.setCancelled(true);
                    }
                }
            }
        }
    }
}
