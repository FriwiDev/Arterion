package me.friwi.arterion.plugin.util.config.init.reflection;

import me.friwi.arterion.plugin.util.config.content.api.ContentWrapper;

import java.lang.reflect.Method;

public class DoublesMethod extends ReflectedMethod<Double[]> {

    public DoublesMethod(Method m, String name, String def, boolean required) {
        super(m, name, def, required);
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
