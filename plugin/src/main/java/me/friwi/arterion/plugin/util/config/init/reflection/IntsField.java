package me.friwi.arterion.plugin.util.config.init.reflection;

import me.friwi.arterion.plugin.util.config.content.api.ContentWrapper;

import java.lang.reflect.Field;

public class IntsField extends ReflectedField<Integer[]> {

    public IntsField(Field f, String name, String def, boolean required) {
        super(f, name, def, required);
    }

    @Override
    protected Integer[] getValue(ContentWrapper wrapper) {
        return wrapper.getInts(name);
    }

    @Override
    protected void setValue(ContentWrapper wrapper, Integer[] value) {
        wrapper.setInts(name, value);
    }
}
