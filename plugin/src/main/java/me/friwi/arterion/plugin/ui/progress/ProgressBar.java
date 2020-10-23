package me.friwi.arterion.plugin.ui.progress;

public class ProgressBar {
    public static String generate(String color, float current, float max, int length) {
        return generate(color, current / max, length);
    }

    public static String generate(String color, float percentage, int length) {
        String build = "\2478[" + color;
        boolean ins = false;
        for (int i = 0; i < length; i++) {
            if (!ins && (i + 0f) / (length + 0f) >= percentage) build += "\2477";
            build += "|";
        }
        build += "\2478]";
        return build;
    }
}
