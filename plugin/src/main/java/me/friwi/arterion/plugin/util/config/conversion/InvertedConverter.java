package me.friwi.arterion.plugin.util.config.conversion;

import me.friwi.arterion.plugin.util.config.conversion.api.TypeConverter;

public class InvertedConverter<S, T> extends TypeConverter<T, S> {
    TypeConverter parent;

    public InvertedConverter(TypeConverter parent) {
        super(false);
        this.parent = parent;
        this.one = parent.getTwo();
        this.two = parent.getOne();
    }


    @Override
    public T convertOne(S value) {
        return (T) parent.convertTwo(value);
    }

    @Override
    public S convertTwo(T value) {
        return (S) parent.convertOne(value);
    }

}
