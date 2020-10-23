package me.friwi.arterion.client;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

public enum ClassEnum {
    NONE(null),
    PALADIN(new ItemStack(Items.iron_shovel)),
    BARBAR(new ItemStack(Items.iron_axe)),
    SHADOWRUNNER(new ItemStack(Items.iron_sword)),
    FORESTRUNNER(new ItemStack(Items.bow)),
    MAGE(new ItemStack(Items.iron_hoe)),
    CLERIC(new ItemStack(Items.stick));

    private ItemStack stack;

    private ClassEnum(ItemStack stack) {
        this.stack = stack;
    }

	public ItemStack getStack() {
		return stack;
	}    
}
