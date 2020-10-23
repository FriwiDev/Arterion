package me.friwi.arterion.plugin.util.time;

public class TimeFormatUtil {
    public static String formatSeconds(long seconds) {
        long hours = seconds / (60 * 60);
        String minutes = String.valueOf(seconds / 60 % 60);
        String sec = String.valueOf(seconds % 60);
        if (hours != 0 && minutes.length() <= 1) minutes = "0" + minutes;
        if (sec.length() <= 1) sec = "0" + sec;
        if (hours == 0) return minutes + ":" + sec;
        else return hours + ":" + minutes + ":" + sec;
    }
}
