package me.friwi.arterion.plugin.util.evaluation.api;

import me.friwi.arterion.plugin.util.evaluation.reflection.ReflectionFunction;
import me.friwi.arterion.plugin.util.evaluation.reflection.ReflectionFunctionBuilder;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

public class Evaluation {
    EvaluationElement content;
    List<ReflectionFunction> functions = new ArrayList<ReflectionFunction>();
    List<String> function_names = new ArrayList<String>();
    ReflectionBinding[] bindings;

    protected Evaluation(EvaluationElement content, ReflectionBinding... bindings) {
        this.content = content;
        this.bindings = bindings;
        content.initVariables(this);
    }

    protected int createBinding(String name) {
        for (int i = 0; i < function_names.size(); i++) {
            if (function_names.get(i).equalsIgnoreCase(name)) return i;
        }
        ReflectionFunction func = ReflectionFunctionBuilder.buildFunction(name, bindings);
        functions.add(func);
        function_names.add(name.toLowerCase());
        return functions.size() - 1;
    }

    public synchronized Number evaluate(String[] keys, Object[] values) throws IllegalAccessException, InvocationTargetException {
        Number[] nums = new Number[functions.size()];
        outer:
        for (int i = 0; i < nums.length; i++) {
            ReflectionFunction f = functions.get(i);
            for (int j = 0; j < keys.length; j++) {
                if (keys[j].equalsIgnoreCase(f.getBinding().getName())) {
                    nums[i] = (Number) f.invoke(values[j]);
                    continue outer;
                }
            }
            throw new RuntimeException("The binding " + f.getBinding().getName() + " could not be linked!");
        }
        return content.calc(nums);
    }

    public String toString() {
        return content.toString();
    }
}
