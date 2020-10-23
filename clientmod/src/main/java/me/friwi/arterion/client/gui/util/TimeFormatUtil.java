package me.friwi.arterion.client.gui.util;

public class TimeFormatUtil {
    public static String formatMillis(long millis) {
    	if(millis<10000) {
    		return ((millis/100)+0f)/10f+"";
    	}
    	long seconds = millis/1000;
        long hours = seconds / (60 * 60);
        String minutes = String.valueOf(seconds / 60 % 60);
        String sec = String.valueOf(seconds % 60);
        if (hours != 0 && minutes.length() <= 1) minutes = "0" + minutes;
        if (sec.length() <= 1) sec = "0" + sec;
        if (hours == 0) return minutes + ":" + sec;
        else return hours + ":" + minutes + ":" + sec;
    }
}
