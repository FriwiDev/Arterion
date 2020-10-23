package me.friwi.arterion.plugin.util.config.init.reflection;

import me.friwi.arterion.plugin.util.config.content.api.ContentWrapper;

import java.lang.reflect.Field;

public class FloatsField extends ReflectedField<Float[]> {

    public FloatsField(Field f, String name, String def, boolean required) {
        super(f, name, def, required);
    }

    @Override
    protected Float[] getValue(ContentWrapper wrapper) {
        return wrapper.getFloats(name);
    }

    @Override
    protected void setValue(ContentWrapper wrapper, Float[] value) {
        wrapper.setFloats(name, value);
    }
}
