package me.friwi.arterion.client.keybinds;

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

public class KeybindManager {
	public static ArrayList<Keybind> list = new ArrayList<Keybind>();
	private static final File file = new File(Minecraft.getMinecraft().mcDataDir, "mods/arterion/keybinds.txt");
	public static void onTick(){
		if(Minecraft.getMinecraft().currentScreen!=null)return;
		try{
			for(Keybind k : list){
				k.onTick();
			}
		}catch(Exception e){
			
		}
	}
	public static void addKeybind(Keybind bind){
		try{
			list.add(bind);
		}catch(Exception e){
			addKeybind(bind);
		}
		save();
	}
	public static void delKeybind(Keybind bind){
		try{
			list.remove(bind);
		}catch(Exception e){
			delKeybind(bind);
		}
		save();
	}
	public static int getKeybindAmount(int keyCode){
    	int i = 0;
    	for(Keybind bind : list){
    		if(bind.keycode == keyCode)i++;
    	}
    	KeyBinding[] var3 = (KeyBinding[])ArrayUtils.clone(Minecraft.getMinecraft().gameSettings.keyBindings);
		for(KeyBinding b : var3){
			if(b!=null)if(b.getKeyCode()==keyCode)i++;
		}
    	return i;
    }
	
	public static void load(){
		list = new ArrayList<Keybind>();
		if(!file.exists()){
			loadDefaults();
			return;
		}
		try {
			BufferedReader read = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
			String str;
			while((str=read.readLine())!=null){
				addKeybind(parseBind(str));
			}
			read.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public static void loadDefaults() {
		addKeybind(new CommandKeybind(Keyboard.KEY_F8, "/spawn"));
		addKeybind(new CommandKeybind(Keyboard.KEY_F12, "/home"));
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
			for(Keybind k : list){
				write.println(k.toString());
			}
			write.flush();
			write.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public static Keybind parseBind(String str) {
		int type = Integer.parseInt(str.substring(0, 1));
		str = str.substring(1);
		switch(type){
		case 0:
			return new CommandKeybind(str);
		}
		return null;
	}
	public static void sortKeybinds(){
		for(int i = 0; i<list.size(); i++){
			for(int j = 0; j<list.size(); j++){
				if(i<j&&list.get(i).keycode>list.get(j).keycode||i>j&&list.get(i).keycode<list.get(j).keycode){
					Keybind backup = list.get(i);
					list.set(i, list.get(j));
					list.set(j, backup);
				}
			}
		}
	}
	public static String getKeyText(int key){
    	return getKeyText(key, 0);
    }
	public static String getKeyText(int key, int add){
    	if(key==0||key==-1)return "NONE";
    	if(key>0)return (KeybindManager.getKeybindAmount(key)+add>1?"\247c":"")+Keyboard.getKeyName(key);
    	else return (KeybindManager.getKeybindAmount(key)+add>1?"\247c":"")+"MOUSE "+(key+101);
    }
}
