package me.friwi.arterion.plugin.util.language.translateables;

import me.friwi.arterion.plugin.util.language.api.Language;
import me.friwi.arterion.plugin.util.language.api.Translateable;

public class StringTranslateable extends Translateable {
    String s;

    public StringTranslateable(String s) {
        this.s = s;
    }


    @Override
    public String getCaption(Language lang, String outerColor, String innerColor) {
        return innerColor + s;
    }

    @Override
    public boolean isSingular(Language lang) {
        return s.isEmpty();
    }

    public String getString() {
        return s;
    }
}
