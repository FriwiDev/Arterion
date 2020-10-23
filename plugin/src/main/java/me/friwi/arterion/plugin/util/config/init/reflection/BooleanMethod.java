package me.friwi.arterion.plugin.util.config.init.reflection;

import me.friwi.arterion.plugin.util.config.content.api.ContentWrapper;

import java.lang.reflect.Method;

public class BooleanMethod extends ReflectedMethod<Boolean> {

    public BooleanMethod(Method m, String name, String def, boolean required) {
        super(m, name, def, required);
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
