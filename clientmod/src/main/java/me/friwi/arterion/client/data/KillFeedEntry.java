package me.friwi.arterion.client.data;

import java.util.UUID;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class KillFeedEntry {
	private int borderColor;
    private long expires;
    private UUID killerUUID;
    private String killerName;
    private ItemStack icon;
    private short imageId;
    private UUID killedUUID;
    private String killedName;


    public KillFeedEntry(int borderColor, int duration, UUID killerUUID, String killerName, byte type, short imageId, short imageSubId, UUID killedUUID, String killedName) {
        this.borderColor = borderColor;
        this.expires = duration + System.currentTimeMillis();
        this.killerUUID = killerUUID;
        this.killerName = killerName;
        if(type==0) {
        	icon = new ItemStack(Item.getItemById(imageId), 1, imageSubId);
        }else {
        	this.imageId = imageId;
        }
        this.killedUUID = killedUUID;
        this.killedName = killedName;
    }


	public int getBorderColor() {
		return borderColor;
	}


	public long getExpires() {
		return expires;
	}


	public UUID getKillerUUID() {
		return killerUUID;
	}


	public String getKillerName() {
		return killerName;
	}


	public ItemStack getIcon() {
		return icon;
	}


	public short getImageId() {
		return imageId;
	}


	public UUID getKilledUUID() {
		return killedUUID;
	}


	public String getKilledName() {
		return killedName;
	}
    
    public boolean isExpired() {
    	return expires<System.currentTimeMillis();
    }
    
    public ResourceLocation getKillfeedIcon() {
    	return KillFeedList.getKillfeedIcon(imageId);
    }


	@Override
	public String toString() {
		return "KillFeedEntry [borderColor=" + borderColor + ", expires=" + expires + ", killerUUID=" + killerUUID
				+ ", killerName=" + killerName + ", icon=" + icon + ", imageId=" + imageId + ", killedUUID="
				+ killedUUID + ", killedName=" + killedName + "]";
	}
    
    
}
