//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package net.minecraft.server.v1_8_R3;

public class EnchantmentArrowDamage extends Enchantment {
    public EnchantmentArrowDamage(int var1, MinecraftKey var2, int var3) {
        super(var1, var2, var3, EnchantmentSlotType.BOW);
        this.c("arrowDamage");
    }

    public int a(int var1) {
        return 1 + (var1 - 1) * 10;
    }

    public int b(int var1) {
        return this.a(var1) + 15;
    }

    public int getMaxLevel() {
        return 4; //TODO
    }
}
