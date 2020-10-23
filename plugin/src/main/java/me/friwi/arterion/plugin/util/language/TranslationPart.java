package me.friwi.arterion.plugin.util.language;

import me.friwi.arterion.plugin.util.language.api.Language;
import me.friwi.arterion.plugin.util.language.api.Translateable;

public abstract class TranslationPart {
    public abstract int getIndex();

    public abstract void setIndex(int index);

    public abstract String getCaption(Language lang, Object arg, String outerColor, String innerColor);

    public abstract Translateable getTranslateable(Language lang, Object arg);
}
