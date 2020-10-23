package me.friwi.arterion.plugin.util.language;

import me.friwi.arterion.plugin.util.config.conversion.api.TypeConverter;
import me.friwi.arterion.plugin.util.config.conversion.api.TypeConverterAPI;
import me.friwi.arterion.plugin.util.language.api.Language;
import me.friwi.arterion.plugin.util.language.api.Translateable;

public class ObjectTranslationPart extends TranslationPart {
    TypeConverter<Object, Translateable> converter = null;
    int index;

    @Override
    public String getCaption(Language lang, Object arg, String outerColor, String innerColor) {
        return null;
    }

    @Override
    public Translateable getTranslateable(Language lang, Object arg) {
        if (converter == null) converter = TypeConverterAPI.getConverter(arg.getClass(), Translateable.class);
        return TypeConverterAPI.convertTo(arg, converter);
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
