package me.friwi.arterion.client.gui;

import java.util.LinkedList;
import java.util.List;

import me.friwi.arterion.client.ArterionModConfig;
import me.friwi.arterion.client.data.ModValueEnum;
import me.friwi.arterion.client.data.SkillDataList;
import me.friwi.arterion.client.gui.util.TooltipPainter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.gui.GuiNewChat;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ChatModifier {

	@SubscribeEvent
	public void onGuiInit(GuiScreenEvent.DrawScreenEvent.Post event) {
		if (ModValueEnum.IS_ARTERION.getInt() != 1)
			return;
		
		if(Minecraft.getMinecraft().ingameGUI==null||Minecraft.getMinecraft().ingameGUI.getChatGUI()==null||!Minecraft.getMinecraft().ingameGUI.getChatGUI().getChatOpen()) {
			return;
		}

		int width = event.gui.width;
		int height = event.gui.height;
		int mouseX = event.mouseX;
		int mouseY = event.mouseY;

		int backupScale = Minecraft.getMinecraft().gameSettings.guiScale;
		Minecraft.getMinecraft().gameSettings.guiScale = getDesiredScale();

		ScaledResolution scaled = new ScaledResolution(Minecraft.getMinecraft());
		mouseX = mouseX * scaled.getScaledWidth() / width;
		mouseY = mouseY * scaled.getScaledHeight() / height;
		width = scaled.getScaledWidth();
		height = scaled.getScaledHeight();

		Minecraft.getMinecraft().entityRenderer.setupOverlayRendering();

		int slotCount = SkillDataList.getSkills().length;
		for (int i = 0; i < SkillDataList.getSkills().length; i++) {
			if (SkillDataList.getSkills()[i] != null && SkillDataList.getSkills()[i].getSkill() != -1) {
				int x = width / 2 - slotCount * OverlayGui.slotWidth / 2 + i * OverlayGui.slotWidth;
				int y = 25;
				if (x <= mouseX && y <= mouseY && x + OverlayGui.slotWidth > mouseX
						&& y + OverlayGui.slotHeight > mouseY) {
					//Draw skill description
					List<String> desc = formatWithLimit(SkillDataList.getSkills()[i].getSkillDescription(), "\2477", 40);
					desc.add(0, "\2478"+SkillDataList.getSkills()[i].getSkillName());
					TooltipPainter.drawHoveringText(desc, mouseX, mouseY, Minecraft.getMinecraft().fontRendererObj);
				}
			}
		}

		Minecraft.getMinecraft().gameSettings.guiScale = backupScale;

		Minecraft.getMinecraft().entityRenderer.setupOverlayRendering();
	}

	private int getDesiredScale() {
		int scale = ArterionModConfig.getGuiScale();
		if (scale == 1 || scale == 2) {
			return scale;
		} else {
			return 2;
		}
	}
	
	List<String> formatWithLimit(String d, String prefix, int limit) {
        List<String> ret = new LinkedList<>();
        for(String e : d.split("\n")) {
	        String[] desc = e.split(" ");
	        String s = "";
	        for (String x : desc) {
	            if (s.length() + x.length() < limit) s += x + " ";
	            else {
	                ret.add(prefix + s);
	                s = "";
	                s += x + " ";
	            }
	        }
	        if (s.length() > 0) ret.add(prefix + s);
        }
        return ret;
    }
}