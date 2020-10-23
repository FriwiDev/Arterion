package me.friwi.arterion.plugin.ui.gui;

import me.friwi.arterion.plugin.player.ArterionPlayer;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class ConfirmItemGUI extends ItemGUI {
    public ConfirmItemGUI(ArterionPlayer player, Runnable yes, Runnable no) {
        this(player, player.getTranslation("gui.confirm"), yes, no);
    }

    public ConfirmItemGUI(ArterionPlayer player, String name, Runnable yes, Runnable no) {
        super(player, name, () -> {
            ItemStack[] stacks = new ItemStack[9];
            stacks[2] = NamedItemUtil.create(Material.INK_SACK, 1, (byte) 10, player.getTranslation("gui.confirm.yes"));
            stacks[6] = NamedItemUtil.create(Material.INK_SACK, 1, (byte) 1, player.getTranslation("gui.confirm.no"));
            return stacks;
        }, ((clickType, i) -> {
            player.closeGui();
            if (i == 2) yes.run();
            else no.run();
        }));
    }
}
