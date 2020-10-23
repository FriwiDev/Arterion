package me.friwi.arterion.client.labymod;

import java.util.Arrays;

import me.friwi.arterion.client.network.client.ModConnection;
import net.labymod.api.events.PluginMessageEvent;
import net.minecraft.network.PacketBuffer;

public class LabymodPluginMessageListener implements PluginMessageEvent {

	@Override
	public void receiveMessage(String channelName, PacketBuffer packetBuffer) {
		if ( channelName.equals( ModConnection.CHANNEL_NAME ) ) {
			packetBuffer.readByte();
            byte id = packetBuffer.readByte();
            packetBuffer.readShort(); //Skip length
            byte[] data = new byte[packetBuffer.readableBytes()];
            packetBuffer.readBytes(data);
            ModConnection.receivePacket(id, data);
        }
	}

}
