package me.friwi.arterion.plugin.util.config.init.reflection;

import me.friwi.arterion.plugin.util.config.content.api.ContentWrapper;

import java.lang.reflect.Method;

public class StringMethod extends ReflectedMethod<String> {

    public StringMethod(Method m, String name, String def, boolean required) {
        super(m, name, def, required);
    }

    @Override
    protected String getValue(ContentWrapper wrapper) {
        return wrapper.getString(name);
    }

    @Override
    protected void setValue(ContentWrapper wrapper, String value) {
        wrapper.setString(name, value);
    }
}
