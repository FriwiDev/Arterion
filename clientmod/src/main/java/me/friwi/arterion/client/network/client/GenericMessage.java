package me.friwi.arterion.client.network.client;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import scala.actors.threadpool.Arrays;

public class GenericMessage implements IMessage {
	public static class Handle implements IMessageHandler<GenericMessage, IMessage> {

		@Override
		public IMessage onMessage(GenericMessage message, MessageContext ctx) {
	        ModConnection.receivePacket(message.id, message.data);
			return null;
		}
	}

	private byte id;
	private byte[] data;

	public GenericMessage() {
		// Empty constructor for incoming messages.
	}

	@Override
	public void fromBytes(ByteBuf buf) {
	    id = buf.readByte();
	    short length = buf.readShort();
	    data = new byte[length];
	    buf.readBytes(data);
	}

	@Override
	public void toBytes(ByteBuf buf) {
		
	}
}
