package me.friwi.arterion.client.network.client;

import me.friwi.arterion.client.data.FriendlyPlayerEntry;
import me.friwi.arterion.client.data.FriendlyPlayerList;
import me.friwi.arterion.client.data.KillFeedEntry;
import me.friwi.arterion.client.data.KillFeedList;
import me.friwi.arterion.client.data.ModValueEnum;
import me.friwi.arterion.client.data.Objective;
import me.friwi.arterion.client.data.SkillDataEntry;
import me.friwi.arterion.client.data.SkillDataList;
import me.friwi.arterion.client.gui.TextGui;
import me.friwi.arterion.client.network.ModPacket;
import me.friwi.arterion.client.network.packet.Packet02IntValue;
import me.friwi.arterion.client.network.packet.Packet03StringValue;
import me.friwi.arterion.client.network.packet.Packet04FriendlyCreateOrUpdate;
import me.friwi.arterion.client.network.packet.Packet05FriendlyRemove;
import me.friwi.arterion.client.network.packet.Packet06Objective;
import me.friwi.arterion.client.network.packet.Packet07SkillSlotData;
import me.friwi.arterion.client.network.packet.Packet08TextGui;
import me.friwi.arterion.client.network.packet.Packet09Killfeed;
import net.minecraft.client.Minecraft;

public class PacketListenerClient implements PacketListener {

	@Override
	public void handlePacket(ModPacket packet) {
		if (packet instanceof Packet02IntValue) {
			String key = ((Packet02IntValue) packet).getName();
			Object value = ((Packet02IntValue) packet).getValue();
			try {
				ModValueEnum.valueOf(key).setValue(value);
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			}
		} else if (packet instanceof Packet03StringValue) {
			String key = ((Packet03StringValue) packet).getName();
			Object value = ((Packet03StringValue) packet).getValue();
			try {
				ModValueEnum.valueOf(key).setValue(value);
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			}
		} else if (packet instanceof Packet04FriendlyCreateOrUpdate) {
			FriendlyPlayerList.setEntry(((Packet04FriendlyCreateOrUpdate) packet).getPlayer(),
					new FriendlyPlayerEntry(((Packet04FriendlyCreateOrUpdate) packet).getPlayer(),
							((Packet04FriendlyCreateOrUpdate) packet).getName(),
							((Packet04FriendlyCreateOrUpdate) packet).getSelectedClass(),
							((Packet04FriendlyCreateOrUpdate) packet).getSelectedClassName(),
							((Packet04FriendlyCreateOrUpdate) packet).getLevel(),
							((Packet04FriendlyCreateOrUpdate) packet).getHealth(),
							((Packet04FriendlyCreateOrUpdate) packet).getMaxhealth()));
		} else if (packet instanceof Packet05FriendlyRemove) {
			FriendlyPlayerList.removeEntry(((Packet05FriendlyRemove) packet).getPlayer());
		} else if (packet instanceof Packet06Objective) {
			if(((Packet06Objective) packet).getDelay()==-1000) {
				Objective.setActiveUntil(0);
				Objective.setText(null);
			}else {
				Objective.setText(((Packet06Objective) packet).getText());
				if(((Packet06Objective) packet).getDelay()==Long.MIN_VALUE) {
					Objective.setActiveUntil(-1);
				}else {
					Objective.setActiveUntil(System.currentTimeMillis()+((Packet06Objective) packet).getDelay());
				}
			}
		} else if (packet instanceof Packet07SkillSlotData) {
			SkillDataList.updateSkillData(new SkillDataEntry(
					((Packet07SkillSlotData) packet).getSlot(), 
					((Packet07SkillSlotData) packet).getSkill(), 
					((Packet07SkillSlotData) packet).isEnabled(),
					((Packet07SkillSlotData) packet).getSkillName(), 
					((Packet07SkillSlotData) packet).getSkillDescription(), 
					((Packet07SkillSlotData) packet).getDelay()+System.currentTimeMillis(), 
					((Packet07SkillSlotData) packet).getTotalDelay(), 
					((Packet07SkillSlotData) packet).getDelayColor(), 
					((Packet07SkillSlotData) packet).getCooldown()+System.currentTimeMillis(), 
					((Packet07SkillSlotData) packet).getMaxCooldown(),
					((Packet07SkillSlotData) packet).getMana()));
		} else if (packet instanceof Packet08TextGui) {
			Minecraft.getMinecraft().displayGuiScreen(new TextGui(
					((Packet08TextGui) packet).getTitle(),
					((Packet08TextGui) packet).getDescription()));
		} else if (packet instanceof Packet09Killfeed) {
			KillFeedList.addEntry(new KillFeedEntry(
					((Packet09Killfeed) packet).getBorderColor(),
					((Packet09Killfeed) packet).getDuration(),
					((Packet09Killfeed) packet).getKillerUUID(),
					((Packet09Killfeed) packet).getKillerName(),
					((Packet09Killfeed) packet).getType(),
					((Packet09Killfeed) packet).getImageId(),
					((Packet09Killfeed) packet).getImageSubId(),
					((Packet09Killfeed) packet).getKilledUUID(),
					((Packet09Killfeed) packet).getKilledName()));
		} else {
			System.out.println("Unhandled packet received: " + packet);
		}
	}

}
