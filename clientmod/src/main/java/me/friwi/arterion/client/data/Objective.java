package me.friwi.arterion.client.data;

import me.friwi.arterion.client.gui.util.TimeFormatUtil;

public class Objective {
	private static String text = null;
	private static long activeUntil = 0;

	public static String getText() {
		return text;
	}

	public static void setText(String text) {
		Objective.text = text;
	}

	public static long getActiveUntil() {
		return activeUntil;
	}

	public static void setActiveUntil(long activeUntil) {
		Objective.activeUntil = activeUntil;
	}
	
	public static boolean isActive() {
		return getActiveUntil()>System.currentTimeMillis() || getActiveUntil()==-1;
	}
	
	public static String buildMessage() {
		if(getActiveUntil()==-1) {
			return text;
		}else {
			return TimeFormatUtil.formatMillis(getActiveUntil()-System.currentTimeMillis())+"  "+text;
		}
	}
}
