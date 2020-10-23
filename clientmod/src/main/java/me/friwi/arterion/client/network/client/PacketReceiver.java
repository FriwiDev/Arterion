package me.friwi.arterion.client.network.client;

import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class PacketReceiver {
	public SimpleNetworkWrapper network;

	public void init(){
	    network = NetworkRegistry.INSTANCE.newSimpleChannel(ModConnection.CHANNEL_NAME);
	    network.registerMessage(GenericMessage.Handle.class, GenericMessage.class, 0, Side.CLIENT); 
	    network.registerMessage(IgnoredMessage.Handle.class, IgnoredMessage.class, 1, Side.CLIENT); //Do not crash in SP when receiving own packet
	}
}
