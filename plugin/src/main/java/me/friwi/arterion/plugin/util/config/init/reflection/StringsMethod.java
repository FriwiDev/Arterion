package me.friwi.arterion.plugin.util.config.init.reflection;

import me.friwi.arterion.plugin.util.config.content.api.ContentWrapper;

import java.lang.reflect.Method;

public class StringsMethod extends ReflectedMethod<String[]> {

    public StringsMethod(Method m, String name, String def, boolean required) {
        super(m, name, def, required);
    }

    @Override
    protected String[] getValue(ContentWrapper wrapper) {
        return wrapper.getStrings(name);
    }

    @Override
    protected void setValue(ContentWrapper wrapper, String[] value) {
        wrapper.setStrings(name, value);
    }
}
