package me.friwi.arterion.plugin.ui.gui;

import me.friwi.arterion.plugin.player.ArterionPlayer;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;

public abstract class GUI {
    private ArterionPlayer player;

    public GUI(ArterionPlayer player) {
        this.player = player;
    }

    public ArterionPlayer getPlayer() {
        return player;
    }

    public abstract void open();

    public abstract void close();

    /**
     * Returns true when msg was handled and the event should not continue
     *
     * @param msg
     * @return
     */
    public abstract boolean onChat(String msg);

    public abstract boolean onPlayerClickItem(Inventory clickedInventory, ClickType click, int slot);

    public abstract void onClose();
}
