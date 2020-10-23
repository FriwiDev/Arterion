package me.friwi.arterion.plugin.util.config.init.reflection;

import me.friwi.arterion.plugin.util.config.content.api.ContentWrapper;

import java.lang.reflect.Field;

public class LongsField extends ReflectedField<Long[]> {

    public LongsField(Field f, String name, String def, boolean required) {
        super(f, name, def, required);
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
