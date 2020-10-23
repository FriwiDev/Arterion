package me.friwi.arterion.plugin.util.config.init.reflection;

import me.friwi.arterion.plugin.util.config.content.api.ContentWrapper;

import java.lang.reflect.Method;

public class IntsMethod extends ReflectedMethod<Integer[]> {

    public IntsMethod(Method m, String name, String def, boolean required) {
        super(m, name, def, required);
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
