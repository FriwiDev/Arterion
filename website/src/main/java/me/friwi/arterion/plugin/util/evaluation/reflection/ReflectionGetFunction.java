package me.friwi.arterion.plugin.util.evaluation.reflection;

import me.friwi.arterion.plugin.util.evaluation.api.ReflectionBinding;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ReflectionGetFunction extends ReflectionFunction {
    Method m = null;
    Field f = null;
    ReflectionGetFunction func = null;

    public ReflectionGetFunction(ReflectionBinding binding, Class<?> type, String name) {
        super(binding);
        int doti = name.indexOf(".");
        String n = name;
        String r = null;
        if (doti != -1) {
            n = name.substring(0, doti);
            r = name.substring(doti + 1, name.length());
        }
        for (Method m : type.getDeclaredMethods()) {
            if (m.getName().equalsIgnoreCase("get" + n)) {
                m.setAccessible(true);
                this.m = m;
                break;
            }
        }
        if (m == null) {
            for (Field f : type.getDeclaredFields()) {
                if (f.getName().equalsIgnoreCase(n)) {
                    f.setAccessible(true);
                    this.f = f;
                    break;
                }
            }
        }
        if (f == null && m == null) {
            throw new IllegalArgumentException("Invalid mapping " + name + "!");
        }
        if (r != null) {
            if (m != null) {
                func = new ReflectionGetFunction(null, m.getReturnType(), r);
            } else {
                func = new ReflectionGetFunction(null, f.getType(), r);
            }
        }
    }

    public Object invoke(Object obj) throws IllegalAccessException, InvocationTargetException {
        Object ret = null;
        if (m != null) ret = m.invoke(obj);
        else ret = f.get(obj);
        if (func != null) return func.invoke(ret);
        else return ret;
    }
}
