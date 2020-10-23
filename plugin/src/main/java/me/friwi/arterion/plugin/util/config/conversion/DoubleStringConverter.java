package me.friwi.arterion.plugin.util.config.conversion;

import me.friwi.arterion.plugin.util.config.conversion.api.TypeConverter;

public class DoubleStringConverter extends TypeConverter<Double, String> {

    @Override
    public Double convertOne(String value) {
        return Double.parseDouble(value);
    }

    @Override
    public String convertTwo(Double value) {
        if (value == null) return null;
        return value.toString();
    }

}
