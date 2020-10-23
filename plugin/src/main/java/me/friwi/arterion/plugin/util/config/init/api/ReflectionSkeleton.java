package me.friwi.arterion.plugin.util.config.init.api;

import me.friwi.arterion.plugin.util.config.content.api.ContentWrapper;
import me.friwi.arterion.plugin.util.config.init.reflection.ReflectedField;
import me.friwi.arterion.plugin.util.config.init.reflection.ReflectedMethod;

import java.lang.reflect.InvocationTargetException;

public class ReflectionSkeleton {
    ReflectedField[] fields;
    ReflectedMethod[] getters;
    ReflectedMethod[] setters;

    protected ReflectionSkeleton(ReflectedField[] fields, ReflectedMethod[] getters, ReflectedMethod[] setters) {
        this.fields = fields;
        this.getters = getters;
        this.setters = setters;
    }

    /**
     * Fetches all values from content wrapper and pushes them into the object
     *
     * @param obj     The object to be worked on
     * @param wrapper The wrapper that stores the current information
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    public void apply(Object obj, ContentWrapper wrapper) throws IllegalAccessException, InvocationTargetException {
        for (ReflectedField f : fields) f.apply(obj, wrapper);
        for (ReflectedMethod m : setters) m.apply(obj, wrapper);
    }


    /**
     * Extracts all values from an object and adds them to a content wrapper
     *
     * @param obj     The object to be worked on
     * @param wrapper The wrapper that stores the current information
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    public void extract(Object obj, ContentWrapper wrapper) throws IllegalAccessException, InvocationTargetException {
        extract(obj, wrapper, null, false);
    }

    /**
     * Extracts all values from an object and adds them to a content wrapper
     *
     * @param obj          The object to be worked on
     * @param wrapper      The wrapper that stores the current information
     * @param diff         The wrapper the difference is built towards
     * @param changed_only Only apply changed values
     * @return Did a new value take effect?
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    public boolean extract(Object obj, ContentWrapper wrapper, ContentWrapper diff, boolean changed_only) throws IllegalAccessException, InvocationTargetException {
        boolean changed = false;
        for (ReflectedField f : fields) changed |= f.extract(obj, wrapper, diff, changed_only);
        for (ReflectedMethod m : getters) changed |= m.extract(obj, wrapper, diff, changed_only);
        return changed;
    }
}
