//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package net.minecraft.server.v1_8_R3;

public class EnchantmentArrowKnockback extends Enchantment {
    public EnchantmentArrowKnockback(int var1, MinecraftKey var2, int var3) {
        super(var1, var2, var3, EnchantmentSlotType.BOW);
        this.c("arrowKnockback");
    }

    public int a(int var1) {
        return 12 + (var1 - 1) * 20;
    }

    public int b(int var1) {
        return this.a(var1) + 25;
    }

    public int getMaxLevel() {
        return 1;
    } //TODO Limit ench level
}
