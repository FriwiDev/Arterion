package me.friwi.arterion.plugin.util.config.init.reflection;

import me.friwi.arterion.plugin.util.config.content.api.ContentWrapper;

import java.lang.reflect.Method;

public class LongMethod extends ReflectedMethod<Long> {

    public LongMethod(Method m, String name, String def, boolean required) {
        super(m, name, def, required);
    }

    @Override
    protected Long getValue(ContentWrapper wrapper) {
        return wrapper.getLong(name);
    }

    @Override
    protected void setValue(ContentWrapper wrapper, Long value) {
        wrapper.setLong(name, value);
    }
}
