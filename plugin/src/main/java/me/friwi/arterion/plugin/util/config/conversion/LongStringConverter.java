package me.friwi.arterion.plugin.util.config.conversion;

import me.friwi.arterion.plugin.util.config.conversion.api.TypeConverter;

public class LongStringConverter extends TypeConverter<Long, String> {

    @Override
    public Long convertOne(String value) {
        return Long.parseLong(value);
    }

    @Override
    public String convertTwo(Long value) {
        if (value == null) return null;
        return value.toString();
    }

}
