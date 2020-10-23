package me.friwi.arterion.plugin.util.config.conversion;

import me.friwi.arterion.plugin.util.config.conversion.api.TypeConverter;

public class FloatStringConverter extends TypeConverter<Float, String> {

    @Override
    public Float convertOne(String value) {
        return Float.parseFloat(value);
    }

    @Override
    public String convertTwo(Float value) {
        if (value == null) return null;
        return value.toString();
    }

}
