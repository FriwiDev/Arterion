package me.friwi.arterion.plugin.util.evaluation.reflection;

import me.friwi.arterion.plugin.util.evaluation.api.ReflectionBinding;

import java.lang.reflect.InvocationTargetException;

public abstract class ReflectionFunction {
    ReflectionBinding binding;

    public ReflectionFunction(ReflectionBinding binding) {
        this.binding = binding;
    }

    public abstract Object invoke(Object obj) throws IllegalAccessException, InvocationTargetException;

    public ReflectionBinding getBinding() {
        return binding;
    }
}
