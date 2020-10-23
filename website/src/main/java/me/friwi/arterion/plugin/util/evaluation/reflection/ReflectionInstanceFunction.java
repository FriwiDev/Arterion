package me.friwi.arterion.plugin.util.evaluation.reflection;

import me.friwi.arterion.plugin.util.evaluation.api.ReflectionBinding;

import java.lang.reflect.InvocationTargetException;

public class ReflectionInstanceFunction extends ReflectionFunction {
    public ReflectionInstanceFunction(ReflectionBinding binding) {
        super(binding);
    }

    public Object invoke(Object obj) throws IllegalAccessException, InvocationTargetException {
        return obj;
    }
}
