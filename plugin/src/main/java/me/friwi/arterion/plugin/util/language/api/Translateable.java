package me.friwi.arterion.plugin.util.language.api;

import java.util.List;

public abstract class Translateable {
    String caption = null;

    public String getCaptionInternal(Language lang, String outerColor, String innerColor) {
        if (caption == null) caption = getCaption(lang, outerColor, innerColor);
        return caption;
    }

    public void addIndexes(List<Integer[]> index, int offs) {
        index.add(new Integer[]{offs, offs + caption.length()});
    }

    public void addTranslateables(List<Translateable> trans) {
        trans.add(this);
    }

    public abstract String getCaption(Language lang, String outerColor, String innerColor);

    public abstract boolean isSingular(Language lang);
}
