package me.friwi.arterion.plugin.formula;

import me.friwi.arterion.plugin.util.evaluation.api.Evaluation;
import me.friwi.arterion.plugin.util.evaluation.api.EvaluationBuilder;
import me.friwi.arterion.plugin.util.evaluation.api.ReflectionBinding;

import java.lang.reflect.InvocationTargetException;

public class ArterionFormula {
    private String key;
    private ReflectionBinding[] bindings;
    private String[] keys;
    private Evaluation evaluation;
    private String textRepresentation;

    public ArterionFormula(String key, ReflectionBinding... bindings) {
        this.key = key;
        this.bindings = bindings;
        this.keys = new String[bindings.length];
        for (int i = 0; i < keys.length; i++) keys[i] = bindings[i].getName();
    }

    public void setFormula(String eval) {
        this.evaluation = EvaluationBuilder.fromString(eval, bindings);
        this.textRepresentation = eval;
    }

    public int evaluateInt(Object... values) throws IllegalArgumentException {
        Number num = evaluate(values);
        if (num == null)
            throw new RuntimeException("Formula evaluation for \"" + key + "\" returned null. Is it declared yet?");
        return num.intValue();
    }

    public double evaluateDouble(Object... values) throws IllegalArgumentException {
        Number num = evaluate(values);
        if (num == null) throw new RuntimeException("Formula evaluation returned null. Is it declared yet?");
        return num.doubleValue();
    }

    public float evaluateFloat(Object... values) throws IllegalArgumentException {
        Number num = evaluate(values);
        if (num == null) throw new RuntimeException("Formula evaluation returned null. Is it declared yet?");
        return num.floatValue();
    }

    public Number evaluate(Object... values) throws IllegalArgumentException {
        if (this.evaluation == null) return null;
        try {
            return this.evaluation.evaluate(this.keys, values);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new IllegalArgumentException("Error while evaluating formula. Did you specify correct parameters?", e);
        }
    }

    public String getKey() {
        return key;
    }

    public String getTranslateableKey() {
        return key;
    }

    public String getTextRepresentation() {
        return textRepresentation;
    }

    public ReflectionBinding[] getBindings() {
        return bindings;
    }

    public boolean isDeclared() {
        return evaluation != null;
    }
}
