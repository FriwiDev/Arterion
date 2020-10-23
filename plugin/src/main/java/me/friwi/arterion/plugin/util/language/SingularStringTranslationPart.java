package me.friwi.arterion.plugin.util.language;

import me.friwi.arterion.plugin.util.config.conversion.api.TypeConverterAPI;
import me.friwi.arterion.plugin.util.language.api.Language;
import me.friwi.arterion.plugin.util.language.api.Translateable;

public class SingularStringTranslationPart extends TranslationPart {
    String ret;
    boolean singular;
    int index;

    public SingularStringTranslationPart(String ret, boolean singular) {
        this.ret = ret;
        this.singular = singular;
    }

    @Override
    public String getCaption(Language lang, Object arg, String outerColor, String innerColor) {
        if (singular) return TypeConverterAPI.convertTo(arg, Translateable.class).isSingular(lang) ? ret : "";
        else return TypeConverterAPI.convertTo(arg, Translateable.class).isSingular(lang) ? "" : ret;
    }

    @Override
    public Translateable getTranslateable(Language lang, Object arg) {
        return null;
    }

    @Override
    public int getIndex() {
        return index;
    }

    @Override
    public void setIndex(int index) {
        this.index = index;
    }
}
