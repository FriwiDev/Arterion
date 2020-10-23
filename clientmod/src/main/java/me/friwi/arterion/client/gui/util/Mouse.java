package me.friwi.arterion.client.gui.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;

public class Mouse {
	public static ScaledResolution res = null;
	
	public static int getX(){
		return (int) (org.lwjgl.input.Mouse.getX()*res.getScaledWidth()/Minecraft.getMinecraft().displayWidth);
	}
	public static int getY(){
		return (int) (org.lwjgl.input.Mouse.getY()*res.getScaledHeight()/Minecraft.getMinecraft().displayHeight);
	}
	public static boolean isButtonDown(int button){
		return org.lwjgl.input.Mouse.isButtonDown(button);
	}
	public static int getEventButton() {
		return org.lwjgl.input.Mouse.getEventButton();
	}
	public static int getDWheel() {
		return org.lwjgl.input.Mouse.getDWheel();
	}
	public static boolean isGrabbed() {
		return org.lwjgl.input.Mouse.isGrabbed();
	}
	public static void setCursorPosition(int new_x, int new_y) {
		org.lwjgl.input.Mouse.setCursorPosition(new_x, new_y);
	}
	public static void setGrabbed(boolean b) {
		org.lwjgl.input.Mouse.setGrabbed(b);
	}
}
