package me.friwi.arterion.client.gui.util;

import java.util.LinkedList;
import java.util.List;

import net.minecraft.util.EnumChatFormatting;

public class ParagraphUtil {
	public static final char COLOR_CHAR = '\247';
	
	public static List<String> getDescriptionWithLimit(String text, int limit) {
        List<String> ret = new LinkedList<>();
        String[] desc = text.split("\n");
        for(String s : desc)ret.addAll(getDescriptionSingleLineWithLimit(s, limit));
        return ret;
    }
	
	private static List<String> getDescriptionSingleLineWithLimit(String text, int limit) {
        List<String> ret = new LinkedList<>();
        String prefix = ChatColor.getLastColors(text);
        String[] desc = text.split(" ");
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
        return ret;
    }
	
}
