package me.friwi.arterion.plugin.util.config.init.reflection;

import me.friwi.arterion.plugin.util.config.content.api.ContentWrapper;

import java.lang.reflect.Field;

public class BooleanField extends ReflectedField<Boolean> {

    public BooleanField(Field f, String name, String def, boolean required) {
        super(f, name, def, required);
    }

    @Override
    protected Boolean getValue(ContentWrapper wrapper) {
        return wrapper.getBoolean(name);
    }

    @Override
    protected void setValue(ContentWrapper wrapper, Boolean value) {
        wrapper.setBoolean(name, value);
    }
}
