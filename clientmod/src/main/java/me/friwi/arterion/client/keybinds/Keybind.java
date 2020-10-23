package me.friwi.arterion.client.keybinds;

import org.lwjgl.input.Keyboard;
import me.friwi.arterion.client.gui.util.Mouse;

public abstract class Keybind {
	public int keycode = 0;
	boolean[] history = new boolean[]{false, false, false, false, false, false, false, false, false, false};
	public Keybind(int keycode){
		this.keycode = keycode;
	}
	public void onTick(){
		for(int i = 1; i<history.length;i++)history[i-1] = history[i];
		history[history.length-1] = isDown();
		if(history[history.length-1]&&!history[history.length-2]){
			boolean sthdown = false;
			for(int i = 0; i<history.length-2;i++){
				if(history[i])sthdown = true;
			}
			if(sthdown){
				//Appears to be a double click
				onDoubleClick();
			}else{
				//Last was not down
				onStartPressing();
			}
		}else if(!history[history.length-1]&&history[history.length-2]){
			//Last was down
			onStopPressing();
		}
	}
	
	public abstract void onStartPressing();
	public abstract void onStopPressing();
	public abstract void onDoubleClick();
	public boolean isDown(){
		if(keycode>0)return Keyboard.isKeyDown(keycode);
		else return Mouse.isButtonDown(keycode+100);
	}
	public abstract String toString();
}
