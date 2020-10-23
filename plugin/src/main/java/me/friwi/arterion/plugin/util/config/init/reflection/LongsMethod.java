package me.friwi.arterion.plugin.util.config.init.reflection;

import me.friwi.arterion.plugin.util.config.content.api.ContentWrapper;

import java.lang.reflect.Method;

public class LongsMethod extends ReflectedMethod<Long[]> {

    public LongsMethod(Method m, String name, String def, boolean required) {
        super(m, name, def, required);
    }

    @Override
    protected Long[] getValue(ContentWrapper wrapper) {
        return wrapper.getLongs(name);
    }

    @Override
    protected void setValue(ContentWrapper wrapper, Long[] value) {
        wrapper.setLongs(name, value);
    }
}
