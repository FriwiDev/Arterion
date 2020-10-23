package me.friwi.arterion.plugin.util.language;

import me.friwi.arterion.plugin.util.language.api.Language;
import me.friwi.arterion.plugin.util.language.api.Translation;

import java.util.LinkedList;

public class TranslationBuilder {
    public static Translation buildTranslation(Language lang, String regex) {
        LinkedList<TranslationPart> list = new LinkedList<>();
        String outerColor = "";
        String innerColor = "";
        String build = "";
        int state = 0;

        regex = regex.replace("&Auml;", "\u00C4")
                .replace("&Ouml;", "\u00D6")
                .replace("&Uuml;", "\u00DC")
                .replace("&auml;", "\u00E4")
                .replace("&ouml;", "\u00F6")
                .replace("&uuml;", "\u00FC")
                .replace("&suml;", "\u00DF")
                .replace("\\n", "\n")
                .replace("\\u00BB", "\u00BB");

        char[] r = regex.toCharArray();
        char v = 0;

        for (int i = 0; i < r.length; i++) {
            if (state == 0) {
                if (r[i] == '|') state++;
                else outerColor += r[i];
            } else if (state == 1) {
                if (r[i] == '|') state++;
                else innerColor += r[i];
            } else if (state == 2) {
                if (r[i] == ' ' || r[i] == '+') continue;
                if (r[i] == '%') {
                    v = r[i + 1];
                    TranslationPart part = new ObjectTranslationPart();
                    part.setIndex(v - 'a');
                    list.add(part);
                    i++;
                } else if (r[i] == '\'') {
                    build = outerColor;
                    state = 3;
                } else if (r[i] == '#') {
                    v = r[i + 1];
                    i++;
                    state = 4;
                } else if (r[i] == '!') {
                    v = r[i + 1];
                    i++;
                    state = 5;
                } else throw new IllegalArgumentException("Invalid input (unallowed char in regex): " + regex);
            } else if (state == 3) {
                if (r[i] == '\'') {
                    TranslationPart part = new StringTranslationPart(build);
                    list.add(part);
                    state = 2;
                } else if (r[i] == '\\') {
                    build += r[i + 1];
                    i++;
                } else {
                    build += r[i];
                }
            } else if (state == 4 || state == 5) {
                if (r[i] == '\'') {
                    build = outerColor;
                    state += 2;
                } else throw new IllegalArgumentException("Invalid input (misplaced function): " + regex);
            } else if (state == 6 || state == 7) {
                if (r[i] == '\'') {
                    TranslationPart part = new SingularStringTranslationPart(build, state == 6);
                    part.setIndex(v - 'a');
                    list.add(part);
                    state = 2;
                } else if (r[i] == '\\') {
                    build += r[i + 1];
                    i++;
                } else {
                    build += r[i];
                }
            }
        }
        if (state != 2) throw new IllegalArgumentException("Invalid translation input: " + regex);
        TranslationPart[] parts = new TranslationPart[list.size()];
        list.toArray(parts);
        return new Translation(lang, parts, outerColor, innerColor);
    }
}
