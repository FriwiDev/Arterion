package me.friwi.arterion.plugin.util.language.translateables;

import me.friwi.arterion.plugin.util.config.conversion.api.TypeConverter;

public class NumberTranslateableConverter extends TypeConverter<Number, NumberTranslateable> {

    @Override
    public Number convertOne(NumberTranslateable value) {
        return value.getNumber();
    }

    @Override
    public NumberTranslateable convertTwo(Number value) {
        return new NumberTranslateable(value);
    }

}
