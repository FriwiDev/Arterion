package me.friwi.arterion.client.network.client;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import scala.actors.threadpool.Arrays;

public class IgnoredMessage implements IMessage {
	public static class Handle implements IMessageHandler<IgnoredMessage, IMessage> {

		@Override
		public IMessage onMessage(IgnoredMessage message, MessageContext ctx) {
			return null;
		}
	}

	public IgnoredMessage() {
		// Empty constructor for incoming messages.
	}

	@Override
	public void fromBytes(ByteBuf buf) {
	    
	}

	@Override
	public void toBytes(ByteBuf buf) {
		
	}
}
