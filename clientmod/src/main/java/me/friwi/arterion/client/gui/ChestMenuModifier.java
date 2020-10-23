package me.friwi.arterion.client.gui;

import me.friwi.arterion.client.data.ModValueEnum;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.inventory.ContainerChest;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ChestMenuModifier {

	@SubscribeEvent
	public void onGuiOpen(GuiOpenEvent event) {
		if(ModValueEnum.IS_ARTERION.getInt()!=1)return;
		if(event.gui instanceof GuiChest) { // Make sure GUI is container menu
			ContainerChest container = (ContainerChest) ((GuiChest)event.gui).inventorySlots;
			if(!container.getLowerChestInventory().getName().contains("\247"))return;
			event.setCanceled(true);
			Minecraft.getMinecraft().displayGuiScreen(new ChestMenuGui(container));
		}
	}
}