package me.friwi.arterion.client.gui.util;

import java.util.UUID;

import net.minecraft.item.ItemStack;

public class CachedHead {
    private UUID uuid;
    private String name;
    private String texture;
    private String signature;
    private ItemStack head;

    public CachedHead(UUID uuid, String name, String texture, String signature, ItemStack head) {
        this.uuid = uuid;
        this.name = name;
        this.texture = texture;
        this.signature = signature;
        this.head = head;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTexture() {
        return texture;
    }

    public String getSignature() {
        return signature;
    }

    public ItemStack getHead() {
        return head.copy();
    }
}
