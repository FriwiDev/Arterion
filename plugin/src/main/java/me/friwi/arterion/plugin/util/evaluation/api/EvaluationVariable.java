package me.friwi.arterion.plugin.util.evaluation.api;

public class EvaluationVariable extends EvaluationElement<Number> {
    String name;
    int index;

    public EvaluationVariable(String name) {
        this.name = name;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    @Override
    public Number calc(Number... nums) {
        return nums[index];
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public void initVariables(Evaluation evaluation) {
        setIndex(evaluation.createBinding(name));
    }
}
