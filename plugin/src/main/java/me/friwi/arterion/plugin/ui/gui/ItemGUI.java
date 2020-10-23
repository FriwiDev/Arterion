package me.friwi.arterion.plugin.ui.gui;

import me.friwi.arterion.plugin.player.ArterionPlayer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class ItemGUI extends GUI implements InventoryHolder {
    private Inventory inventory;
    private BiConsumer<ClickType, Integer> action;
    private Runnable closeAction = null;

    public ItemGUI(ArterionPlayer player, String name, ItemStack[] stack, BiConsumer<ClickType, Integer> action) {
        super(player);
        this.inventory = Bukkit.createInventory(this, stack.length, name);
        this.inventory.setContents(stack);
        this.action = action;
    }

    public ItemGUI(ArterionPlayer player, String name, Supplier<ItemStack[]> supplier, BiConsumer<ClickType, Integer> action) {
        this(player, name, supplier.get(), action);
    }

    public ItemGUI(ArterionPlayer player, String name, Supplier<ItemStack[]> supplier, BiConsumer<ClickType, Integer> action, Runnable closeAction) {
        this(player, name, supplier.get(), action);
        this.closeAction = closeAction;
    }

    @Override
    public void open() {
        this.getPlayer().getBukkitPlayer().openInventory(this.inventory);
    }

    @Override
    public void close() {
        this.getPlayer().getBukkitPlayer().closeInventory();
        onClose();
    }

    @Override
    public boolean onChat(String msg) {
        return false;
    }

    @Override
    public boolean onPlayerClickItem(Inventory clickedInventory, ClickType click, int slot) {
        if (clickedInventory != null && clickedInventory.equals(this.inventory) && clickedInventory.getItem(slot) != null && clickedInventory.getItem(slot).getType() != Material.AIR) {
            this.action.accept(click, slot);
        }
        return (clickedInventory != null && clickedInventory.equals(this.inventory)) || click == ClickType.SHIFT_RIGHT || click == ClickType.SHIFT_LEFT; //Prevent all item moving
    }

    @Override
    public void onClose() {
        this.getPlayer().closeGui(true);
        if (closeAction != null) closeAction.run();
    }

    @Override
    public Inventory getInventory() {
        return this.inventory;
    }
}
