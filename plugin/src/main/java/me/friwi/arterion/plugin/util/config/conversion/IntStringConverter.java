package me.friwi.arterion.plugin.util.config.conversion;

import me.friwi.arterion.plugin.util.config.conversion.api.TypeConverter;

public class IntStringConverter extends TypeConverter<Integer, String> {

    @Override
    public Integer convertOne(String value) {
        return Integer.parseInt(value);
    }

    @Override
    public String convertTwo(Integer value) {
        if (value == null) return null;
        return value.toString();
    }

}
