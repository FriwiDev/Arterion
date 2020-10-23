package me.friwi.arterion.plugin.util.language.translateables;

import me.friwi.arterion.plugin.util.config.conversion.api.TypeConverter;

public class ArrayTranslateableConverter extends TypeConverter<Object[], ArrayTranslateable> {

    @Override
    public Object[] convertOne(ArrayTranslateable value) {
        return value.getArray();
    }

    @Override
    public ArrayTranslateable convertTwo(Object[] value) {
        return new ArrayTranslateable(value);
    }

}
