package me.friwi.arterion.plugin.util.config.init.reflection;

import me.friwi.arterion.plugin.util.config.content.api.ContentWrapper;

import java.lang.reflect.Method;

public class BooleansMethod extends ReflectedMethod<Boolean[]> {

    public BooleansMethod(Method m, String name, String def, boolean required) {
        super(m, name, def, required);
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
