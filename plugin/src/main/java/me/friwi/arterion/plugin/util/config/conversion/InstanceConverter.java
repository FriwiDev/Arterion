package me.friwi.arterion.plugin.util.config.conversion;

import me.friwi.arterion.plugin.util.config.conversion.api.TypeConverter;

public class InstanceConverter<S, T> extends TypeConverter<S, T> {

    @Override
    public S convertOne(T value) {
        return (S) value;
    }

    @Override
    public T convertTwo(S value) {
        return (T) value;
    }

}
