package me.friwi.arterion.plugin.util.evaluation.reflection;

import me.friwi.arterion.plugin.util.evaluation.api.ReflectionBinding;

public class ReflectionFunctionBuilder {
    public static ReflectionFunction buildFunction(String name, ReflectionBinding... bindings) {
        int doti = name.indexOf(".");
        String n = name;
        String r = null;
        if (doti != -1) {
            n = name.substring(0, doti);
            r = name.substring(doti + 1, name.length());
        }
        ReflectionBinding binding = null;
        for (ReflectionBinding b : bindings) {
            if (b.getName().equalsIgnoreCase(n)) {
                binding = b;
                break;
            }
        }
        if (binding == null) {
            throw new IllegalArgumentException("Invalid binding " + n);
        }
        if (doti == -1) {
            return new ReflectionInstanceFunction(binding);
        } else {
            return new ReflectionGetFunction(binding, binding.getType(), r);
        }
    }
}
