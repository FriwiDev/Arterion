package me.friwi.arterion.plugin.util.config.init.reflection;

import me.friwi.arterion.plugin.util.config.content.api.ContentWrapper;
import me.friwi.arterion.plugin.util.config.conversion.api.TypeConverterAPI;

import java.lang.reflect.Field;

public abstract class ReflectedField<T> {
    public static final String separator = ";";

    Field f;
    String name;
    T def;
    boolean required;
    Class<? extends T> cast;

    @SuppressWarnings("unchecked")
    public ReflectedField(Field f, String name, String def, boolean required) {
        this.f = f;
        this.name = name;
        cast = (Class<? extends T>) f.getType();
        this.def = convert(def);
        this.required = required;
        f.setAccessible(true);
    }

    public void apply(Object obj, ContentWrapper wrapper) throws IllegalAccessException {
        T value = getValue(wrapper);
        if (value == null) {
            if (def != null) {
                f.set(obj, def);
            } else {
                if (required)
                    throw new IllegalArgumentException("Wrapper did not provide a value for required field " + name);
            }
        } else {
            f.set(obj, value);
        }
    }

    public void extract(Object obj, ContentWrapper wrapper) throws IllegalAccessException {
        extract(obj, wrapper, null, false);
    }

    /**
     * Extracts a value from an object and adds it to a content wrapper
     *
     * @param obj          The object to be worked on
     * @param wrapper      The wrapper that stores the current information
     * @param diff         The wrapper the difference is built towards
     * @param changed_only Only apply changed values
     * @return Did a new value take effect?
     * @throws IllegalAccessException
     */
    @SuppressWarnings("unchecked")
    public boolean extract(Object obj, ContentWrapper wrapper, ContentWrapper diff, boolean changed_only) throws IllegalAccessException {
        T value = (T) f.get(obj);
        if (diff == null) {
            setValue(wrapper, value);
            return false;
        } else {
            T old_value = getValue(diff);
            if (value == null) {
                if (old_value != null) {
                    setValue(wrapper, value);
                    return true;
                } else {
                    if (!changed_only) setValue(wrapper, null);
                    return false;
                }
            } else {
                if (old_value == null) {
                    setValue(wrapper, value);
                    return true;
                } else if (old_value.equals(value)) {
                    if (!changed_only) setValue(wrapper, value);
                    return false;
                } else {
                    setValue(wrapper, value);
                    return true;
                }
            }
        }
    }

    protected abstract T getValue(ContentWrapper wrapper);

    protected abstract void setValue(ContentWrapper wrapper, T value);

    protected T convert(String s) {
        return TypeConverterAPI.convertTo(s, cast);
    }
}