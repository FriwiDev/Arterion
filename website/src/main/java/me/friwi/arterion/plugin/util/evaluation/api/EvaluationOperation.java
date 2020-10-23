package me.friwi.arterion.plugin.util.evaluation.api;

public class EvaluationOperation extends EvaluationElement<Double> {
    EvaluationElement[] eval;
    char[] operation;

    public EvaluationOperation(EvaluationElement[] eval, char[] operation) {
        this.eval = eval;
        this.operation = operation;
        sort();
    }

    @Override
    public Double calc(Number... nums) {
        Double ret = eval[0].calc(nums).doubleValue();
        for (int i = 0; i < operation.length; i++) {
            if (operation[i] == '+') ret += eval[i + 1].calc(nums).doubleValue();
            else if (operation[i] == '-') ret -= eval[i + 1].calc(nums).doubleValue();
            else if (operation[i] == '*') ret *= eval[i + 1].calc(nums).doubleValue();
            else if (operation[i] == '/') ret /= eval[i + 1].calc(nums).doubleValue();
            else if (operation[i] == '%') ret %= eval[i + 1].calc(nums).doubleValue();
            else if (operation[i] == '^') ret = Math.pow(ret, eval[i + 1].calc(nums).doubleValue());
            else throw new UnsupportedOperationException("Unknown operation " + operation[i]);
        }
        return ret;
    }

    private void sort() {
        boolean add = EvaluationBuilder.arrayContains(operation, '+') || EvaluationBuilder.arrayContains(operation, '-');
        boolean mul = EvaluationBuilder.arrayContains(operation, '*') || EvaluationBuilder.arrayContains(operation, '/') || EvaluationBuilder.arrayContains(operation, '%');
        boolean pot = EvaluationBuilder.arrayContains(operation, '^');
        int count = (add ? 1 : 0) + (mul ? 1 : 0) + (pot ? 1 : 0);
        if (count > 1) {
            if (pot) groupBy('^');
            else if (mul) groupBy('*', '/', '%');
            sort();
        }
    }

    private void groupBy(char... group) {
        boolean found = false;
        for (char c : group) {
            if (EvaluationBuilder.arrayContains(operation, c)) found = true;
        }
        if (!found) return;
        int start = -1;
        int end = -1;
        for (int index = 0; index < operation.length && end == -1; index++) {
            found = false;
            for (char c : group) {
                if (c == operation[index]) {
                    if (start == -1) start = index;
                    found = true;
                    break;
                }
            }
            if (!found && start != -1) {
                end = index - 1;
                break;
            }
        }
        if (end == -1) end = start;
        int between = end - start;
        EvaluationElement[] eval_inner = new EvaluationElement[between + 2];
        char[] operation_inner = new char[between + 1];
        EvaluationElement[] eval_new = new EvaluationElement[eval.length - between - 1];
        char[] operation_new = new char[operation.length - between - 1];
        for (int i = 0; i < start; i++) {
            eval_new[i] = eval[i];
            operation_new[i] = operation[i];
        }
        for (int i = start; i <= end + 1; i++) {
            eval_inner[i - start] = eval[i];
            if (i <= end) operation_inner[i - start] = operation[i];
        }
        for (int i = end + 1; i < eval.length - 1; i++) {
            eval_new[i - between] = eval[i + 1];
            operation_new[i - 1 - between] = operation[i];
        }
        eval_new[start] = new EvaluationOperation(eval_inner, operation_inner);
        this.eval = eval_new;
        this.operation = operation_new;
    }

    @Override
    public String toString() {
        String build = "(" + eval[0].toString();
        for (int i = 1; i < eval.length; i++) {
            build += operation[i - 1] + (eval[i] == null ? "null" : eval[i].toString());
        }
        return build += ")";
    }

    @Override
    public void initVariables(Evaluation evaluation) {
        for (EvaluationElement el : eval) el.initVariables(evaluation);
    }
}
