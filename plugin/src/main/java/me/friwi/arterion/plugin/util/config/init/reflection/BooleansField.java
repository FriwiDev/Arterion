package me.friwi.arterion.plugin.util.config.init.reflection;

import me.friwi.arterion.plugin.util.config.content.api.ContentWrapper;

import java.lang.reflect.Field;

public class BooleansField extends ReflectedField<Boolean[]> {

    public BooleansField(Field f, String name, String def, boolean required) {
        super(f, name, def, required);
    }

    @Override
    protected Boolean[] getValue(ContentWrapper wrapper) {
        return wrapper.getBooleans(name);
    }

    @Override
    protected void setValue(ContentWrapper wrapper, Boolean[] value) {
        wrapper.setBooleans(name, value);
    }
}
