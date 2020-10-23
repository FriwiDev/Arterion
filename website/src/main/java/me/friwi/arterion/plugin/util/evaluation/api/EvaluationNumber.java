package me.friwi.arterion.plugin.util.evaluation.api;

public class EvaluationNumber extends EvaluationElement<Double> {
    Double number;

    public EvaluationNumber(Double number) {
        this.number = number;
    }

    @Override
    public Double calc(Number... nums) {
        return number;
    }

    @Override
    public String toString() {
        return number.toString();
    }

    @Override
    public void initVariables(Evaluation evaluation) {

    }
}
