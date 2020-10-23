package me.friwi.arterion.client;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;

import org.apache.commons.lang3.ArrayUtils;
import org.lwjgl.input.Keyboard;

public class ArterionModConfig {
	public static final String[] GUISCALES = new String[] {"Auto", "Small", "Normal", "Large"};
	
	private static int guiScale;
	private static final File file = new File(Minecraft.getMinecraft().mcDataDir, "mods/arterion/settings.txt");
	
	public static void setGuiScale(int guiScale) {
		ArterionModConfig.guiScale = guiScale;
		save();
	}
	
	public static void setGuiScaleWithoutSaving(int guiScale) {
		ArterionModConfig.guiScale = guiScale;
	}
	
	public static int getGuiScale() {
		return guiScale;
	}
	
	public static void load(){
		loadDefaults();
		if(!file.exists()){
			return;
		}
		try {
			BufferedReader read = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
			String str;
			while((str=read.readLine())!=null){
				try {
					guiScale = Integer.parseInt(str);
				}catch(Exception e) {
					//Hmm :D
				}
			}
			read.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public static void loadDefaults() {
		guiScale = 2;
	}
	public static void save(){
		if(!file.exists()){
			file.getParentFile().mkdirs();
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
				return;
			}
		}
		try {
			PrintWriter write = new PrintWriter(new FileOutputStream(file));
			write.println(guiScale);
			write.flush();
			write.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
