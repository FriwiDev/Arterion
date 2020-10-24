package me.friwi.arterion.client.gui;

import org.lwjgl.opengl.GL11;

import me.friwi.arterion.client.data.ModValueEnum;
import me.friwi.arterion.client.gui.util.Mouse;
import me.friwi.arterion.client.keybinds.KeybindManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class RenderIngameGuiHandler
{
	
	OverlayGui topGui = new OverlayGui(Minecraft.getMinecraft());
	
    @SubscribeEvent
    public void onRenderGui(RenderGameOverlayEvent.Pre event)
    {
    	if (event.type != ElementType.EXPERIENCE) return;
    	if(ModValueEnum.IS_ARTERION.getInt()!=1)return;
    	Mouse.res = new ScaledResolution(Minecraft.getMinecraft());
    	KeybindManager.onTick();
    	if(topGui!=null)topGui.render();
    }
}