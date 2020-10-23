package me.friwi.arterion.plugin.util.config.init.reflection;

import me.friwi.arterion.plugin.util.config.content.api.ContentWrapper;

import java.lang.reflect.Field;

public class StringsField extends ReflectedField<String[]> {

    public StringsField(Field f, String name, String def, boolean required) {
        super(f, name, def, required);
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
