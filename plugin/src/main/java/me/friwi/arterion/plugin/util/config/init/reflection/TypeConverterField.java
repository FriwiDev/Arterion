package me.friwi.arterion.plugin.util.config.init.reflection;

import me.friwi.arterion.plugin.util.config.content.api.ContentWrapper;
import me.friwi.arterion.plugin.util.config.conversion.api.TypeConverter;
import me.friwi.arterion.plugin.util.config.conversion.api.TypeConverterAPI;

public class TypeConverterField<T> extends ReflectedField<T> {
    ReflectedField parent;
    TypeConverter to;
    TypeConverter back;

    public TypeConverterField(ReflectedField parent, TypeConverter to, TypeConverter back) {
        super(parent.f, parent.name, TypeConverterAPI.convertTo(parent.def, String.class), parent.required);
        this.parent = parent;
        this.to = to;
        this.back = back;
    }

    @Override
    protected T getValue(ContentWrapper wrapper) {
        return (T) back.convert(parent.getValue(wrapper));
    }

    @Override
    protected void setValue(ContentWrapper wrapper, T value) {
        parent.setValue(wrapper, to.convert(value));
    }
}
