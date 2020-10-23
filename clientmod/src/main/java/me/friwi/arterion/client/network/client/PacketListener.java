package me.friwi.arterion.client.network.client;

import me.friwi.arterion.client.network.ModPacket;

public interface PacketListener {
	void handlePacket(ModPacket packet);
}
