package me.friwi.arterion.plugin.util.config.init.reflection;

import me.friwi.arterion.plugin.util.config.content.api.ContentWrapper;

import java.lang.reflect.Field;

public class DoublesField extends ReflectedField<Double[]> {

    public DoublesField(Field f, String name, String def, boolean required) {
        super(f, name, def, required);
    }

    @Override
    protected Double[] getValue(ContentWrapper wrapper) {
        return wrapper.getDoubles(name);
    }

    @Override
    protected void setValue(ContentWrapper wrapper, Double[] value) {
        wrapper.setDoubles(name, value);
    }
}
