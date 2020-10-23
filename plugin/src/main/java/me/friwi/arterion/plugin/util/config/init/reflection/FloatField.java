package me.friwi.arterion.plugin.util.config.init.reflection;

import me.friwi.arterion.plugin.util.config.content.api.ContentWrapper;

import java.lang.reflect.Field;

public class FloatField extends ReflectedField<Float> {

    public FloatField(Field f, String name, String def, boolean required) {
        super(f, name, def, required);
    }

    @Override
    protected Float getValue(ContentWrapper wrapper) {
        return wrapper.getFloat(name);
    }

    @Override
    protected void setValue(ContentWrapper wrapper, Float value) {
        wrapper.setFloat(name, value);
    }
}
