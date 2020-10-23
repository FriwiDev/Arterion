package me.friwi.arterion.plugin.util.config.init.reflection;

import me.friwi.arterion.plugin.util.config.content.api.ContentWrapper;

import java.lang.reflect.Method;

public class DoubleMethod extends ReflectedMethod<Double> {

    public DoubleMethod(Method m, String name, String def, boolean required) {
        super(m, name, def, required);
    }

    @Override
    protected Double getValue(ContentWrapper wrapper) {
        return wrapper.getDouble(name);
    }

    @Override
    protected void setValue(ContentWrapper wrapper, Double value) {
        wrapper.setDouble(name, value);
    }
}
