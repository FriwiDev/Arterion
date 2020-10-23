package me.friwi.arterion.plugin.util.language.translateables;

import me.friwi.arterion.plugin.util.config.conversion.api.TypeConverter;

public class StringTranslateableConverter extends TypeConverter<String, StringTranslateable> {

    @Override
    public String convertOne(StringTranslateable value) {
        return value.getString();
    }

    @Override
    public StringTranslateable convertTwo(String value) {
        return new StringTranslateable(value);
    }

}
