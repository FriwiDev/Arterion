package me.friwi.arterion.plugin.ui.gui;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class CachedHead {
    private UUID uuid;
    private String name;
    private String texture;
    private String signature;
    private ItemStack head;
    private Object gameProfile;

    public CachedHead(UUID uuid, String name, String texture, String signature, ItemStack head, Object gameProfile) {
        this.uuid = uuid;
        this.name = name;
        this.texture = texture;
        this.signature = signature;
        this.head = head;
        this.gameProfile = gameProfile;
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
        if (head == null) head = new ItemStack(Material.WOOD);
        return head.clone();
    }

    public Object getGameProfile() {
        return gameProfile;
    }
}
