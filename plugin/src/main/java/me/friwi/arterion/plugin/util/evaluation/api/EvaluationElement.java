package me.friwi.arterion.plugin.util.evaluation.api;

public abstract class EvaluationElement<T extends Number> {
    public abstract T calc(Number... numbers);

    public abstract void initVariables(Evaluation evaluation);
}
