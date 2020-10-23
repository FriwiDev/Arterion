package me.friwi.arterion.client.data;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import net.minecraft.client.Minecraft;

public class FriendlyPlayerList {
	private static Map<UUID, FriendlyPlayerEntry> friendlyPlayers = new HashMap<>();
	private static List<FriendlyPlayerEntry> list = new ArrayList<>(32);

	public static void setEntry(UUID uuid, FriendlyPlayerEntry ent) {
		if (Minecraft.getMinecraft().getSession() != null 
				&& Minecraft.getMinecraft().getSession().getProfile() != null
				&& Minecraft.getMinecraft().getSession().getProfile().getId() != null
				&& Minecraft.getMinecraft().getSession().getProfile().getId().equals(uuid))
			return;
		friendlyPlayers.put(uuid, ent);
	}

	public static void removeEntry(UUID uuid) {
		friendlyPlayers.remove(uuid);
	}
	
	public static void clear() {
		friendlyPlayers.clear();
	}

	public static List<FriendlyPlayerEntry> getFriendlyPlayers() {
		list.clear();
		list.addAll(friendlyPlayers.values());
		//list.add(new FriendlyPlayerEntry(UUID.fromString("cdd2f737-5d4a-43d1-9c70-63f1931b53a0"), "Friwi", "MAGE", "Magier", 100, 100, 200));
		//list.add(new FriendlyPlayerEntry(UUID.fromString("62f907bd-ad5f-4b3c-9763-b9705fff2ce9"), "0123456789ABCDEF", "PALADIN", "Paladin", 100, 200, 200));
		list.sort(Comparator.comparing(FriendlyPlayerEntry::getSelectedClass));
		return list;
	}
}
