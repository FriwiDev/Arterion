package me.friwi.arterion.plugin.util.language;

import me.friwi.arterion.plugin.util.language.api.Language;
import me.friwi.arterion.plugin.util.language.api.Translateable;

public class StringTranslationPart extends TranslationPart {
    String ret;

    public StringTranslationPart(String ret) {
        this.ret = ret;
    }

    @Override
    public String getCaption(Language lang, Object arg, String outerColor, String innerColor) {
        return ret;
    }

    @Override
    public Translateable getTranslateable(Language lang, Object arg) {
        return null;
    }

    @Override
    public int getIndex() {
        return -1;
    }

    @Override
    public void setIndex(int index) {

    }
}
