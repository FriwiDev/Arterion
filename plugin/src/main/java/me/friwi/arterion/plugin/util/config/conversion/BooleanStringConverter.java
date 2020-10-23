package me.friwi.arterion.plugin.util.config.conversion;

import me.friwi.arterion.plugin.util.config.conversion.api.TypeConverter;

public class BooleanStringConverter extends TypeConverter<Boolean, String> {

    @Override
    public Boolean convertOne(String value) {
        return Boolean.parseBoolean(value);
    }

    @Override
    public String convertTwo(Boolean value) {
        if (value == null) return null;
        return value.toString();
    }

}
