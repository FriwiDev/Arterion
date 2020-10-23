//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package net.minecraft.server.v1_8_R3;

import net.minecraft.server.v1_8_R3.BlockDirt.EnumDirtVariant;
import net.minecraft.server.v1_8_R3.Item.EnumToolMaterial;

public class ItemHoe extends Item {
    protected EnumToolMaterial a;

    public ItemHoe(EnumToolMaterial var1) {
        this.a = var1;
        this.maxStackSize = 1;
        this.setMaxDurability(var1.a());
        this.a(CreativeModeTab.i);
    }

    public boolean interactWith(ItemStack var1, EntityHuman var2, World var3, BlockPosition var4, EnumDirection var5, float var6, float var7, float var8) {
        if (!var2.a(var4.shift(var5), var5, var1)) {
            return false;
        } else {
            IBlockData var9 = var3.getType(var4);
            Block var10 = var9.getBlock();
            if (var5 != EnumDirection.DOWN && var3.getType(var4.up()).getBlock().getMaterial() == Material.AIR) {
                if (var10 == Blocks.GRASS) {
                    return this.a(var1, var2, var3, var4, Blocks.FARMLAND.getBlockData());
                }

                if (var10 == Blocks.DIRT) {
                    switch((EnumDirtVariant)var9.get(BlockDirt.VARIANT)) {
                        case DIRT:
                            return this.a(var1, var2, var3, var4, Blocks.FARMLAND.getBlockData());
                        case COARSE_DIRT:
                            return this.a(var1, var2, var3, var4, Blocks.DIRT.getBlockData().set(BlockDirt.VARIANT, EnumDirtVariant.DIRT));
                    }
                }
            }

            return false;
        }
    }

    protected boolean a(ItemStack var1, EntityHuman var2, World var3, BlockPosition var4, IBlockData var5) {
        var3.makeSound((double)((float)var4.getX() + 0.5F), (double)((float)var4.getY() + 0.5F), (double)((float)var4.getZ() + 0.5F), var5.getBlock().stepSound.getStepSound(), (var5.getBlock().stepSound.getVolume1() + 1.0F) / 2.0F, var5.getBlock().stepSound.getVolume2() * 0.8F);
        if (var3.isClientSide) {
            return true;
        } else {
            var3.setTypeUpdate(var4, var5);
            var1.damage(1, var2);
            return true;
        }
    }

    public String g() {
        return this.a.toString();
    }

    //TODO Damage on hit
    public boolean a(ItemStack var1, EntityLiving var2, EntityLiving var3) {
        var1.damage(1, var3);
        return true;
    }
}
