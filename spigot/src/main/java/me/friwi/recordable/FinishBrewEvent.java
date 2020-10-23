package me.friwi.recordable;

import org.bukkit.block.Block;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.BlockEvent;
import org.bukkit.inventory.BrewerInventory;

public class FinishBrewEvent extends BlockEvent {
    private static final HandlerList handlers = new HandlerList();
    private BrewerInventory contents;

    public FinishBrewEvent(Block brewer, BrewerInventory contents) {
        super(brewer);
        this.contents = contents;
    }

    public BrewerInventory getContents() {
        return this.contents;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
