//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package net.minecraft.server.v1_8_R3;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftHumanEntity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;

public class InventoryEnderChest extends InventorySubcontainer {
    private TileEntityEnderChest a;
    public List<HumanEntity> transaction = new ArrayList();
    public Player player;
    private int maxStack = 64;

    public ItemStack[] getContents() {
        return this.items;
    }

    public void onOpen(CraftHumanEntity who) {
        this.transaction.add(who);
    }

    public void onClose(CraftHumanEntity who) {
        this.transaction.remove(who);
    }

    public List<HumanEntity> getViewers() {
        return this.transaction;
    }

    public InventoryHolder getOwner() {
        return this.player;
    }

    public void setMaxStackSize(int size) {
        this.maxStack = size;
    }

    public int getMaxStackSize() {
        return this.maxStack;
    }

    public InventoryEnderChest() {
        super("container.enderchest", false, 9); //TODO change from 27 to 9
    }

    public void a(TileEntityEnderChest tileentityenderchest) {
        this.a = tileentityenderchest;
    }

    public void a(NBTTagList nbttaglist) {
        int i;
        for(i = 0; i < this.getSize(); ++i) {
            this.setItem(i, (ItemStack)null);
        }

        for(i = 0; i < nbttaglist.size(); ++i) {
            NBTTagCompound nbttagcompound = nbttaglist.get(i);
            int j = nbttagcompound.getByte("Slot") & 255;
            if (j >= 0 && j < this.getSize()) {
                this.setItem(j, ItemStack.createStack(nbttagcompound));
            }
        }

    }

    public NBTTagList h() {
        NBTTagList nbttaglist = new NBTTagList();

        for(int i = 0; i < this.getSize(); ++i) {
            ItemStack itemstack = this.getItem(i);
            if (itemstack != null) {
                NBTTagCompound nbttagcompound = new NBTTagCompound();
                nbttagcompound.setByte("Slot", (byte)i);
                itemstack.save(nbttagcompound);
                nbttaglist.add(nbttagcompound);
            }
        }

        return nbttaglist;
    }

    public boolean a(EntityHuman entityhuman) {
        return this.a != null && !this.a.a(entityhuman) ? false : super.a(entityhuman);
    }

    public void startOpen(EntityHuman entityhuman) {
        if (this.a != null) {
            this.a.b();
        }

        super.startOpen(entityhuman);
    }

    public void closeContainer(EntityHuman entityhuman) {
        if (this.a != null) {
            this.a.d();
        }

        super.closeContainer(entityhuman);
        this.a = null;
    }
}
