package me.friwi.arterion.client.keybinds;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;

public class CommandKeybind extends Keybind{
	public String command = null;
	public CommandKeybind(int keycode, String command) {
		super(keycode);
		this.command = command;
	}

	public CommandKeybind(String str) {
		this(Integer.parseInt(str.split(";")[0]), str.split(";")[1]);
	}

	@Override
	public void onStartPressing() {
		if(command.startsWith("/"))Minecraft.getMinecraft().thePlayer.sendChatMessage(command);
		else{
			Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage(new ChatComponentText("\247cCommands only!"));
		}
	}

	@Override
	public void onStopPressing() {
		
	}

	@Override
	public void onDoubleClick() {
		
	}

	@Override
	public String toString() {
		return "0"+keycode+";"+command;
	}

}
