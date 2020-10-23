package me.friwi.arterion.plugin.util.language.api;

import me.friwi.arterion.plugin.util.language.TranslationPart;

import java.util.LinkedList;

public class Translation {
    TranslationPart[] parts;
    Language lang;
    String outerColor;
    String innerColor;

    public Translation(Language lang, TranslationPart[] parts, String outerColor, String innerColor) {
        this.parts = parts;
        this.lang = lang;
        this.outerColor = outerColor;
        this.innerColor = innerColor;
    }

    public TranslatedMessage translate(Object... args) {
        StringBuilder build = new StringBuilder();
        LinkedList<Translateable> trans = new LinkedList<>();
        LinkedList<Integer[]> index = new LinkedList<>();
        int b = 0;
        for (TranslationPart part : parts) {
            Object s = part.getIndex() == -1 ? null : args[part.getIndex()];
            Translateable t = part.getTranslateable(lang, s);
            String caption = t == null ? part.getCaption(lang, s, outerColor, innerColor) : t.getCaptionInternal(lang, outerColor, innerColor);
            int c = caption.length();
            if (s != null && t != null) {
                t.addTranslateables(trans);
                t.addIndexes(index, b);
            }
            build.append(caption);
            b += c;
        }
        Translateable[] trans1 = new Translateable[trans.size()];
        trans.toArray(trans1);
        int[][] index1 = new int[index.size()][];
        trans.toArray(trans1);
        return new TranslatedMessage(build.toString().replace("Â", ""), index1, trans1);
    }
}
