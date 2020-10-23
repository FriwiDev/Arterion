package me.friwi.arterion.plugin.util.config.init.reflection;

import me.friwi.arterion.plugin.util.config.content.api.ContentWrapper;

import java.lang.reflect.Field;

public class IntField extends ReflectedField<Integer> {

    public IntField(Field f, String name, String def, boolean required) {
        super(f, name, def, required);
    }

    @Override
    protected Integer getValue(ContentWrapper wrapper) {
        return wrapper.getInt(name);
    }

    @Override
    protected void setValue(ContentWrapper wrapper, Integer value) {
        wrapper.setInt(name, value);
    }
}
