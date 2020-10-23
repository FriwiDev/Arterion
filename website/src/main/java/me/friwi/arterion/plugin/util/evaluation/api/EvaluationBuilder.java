package me.friwi.arterion.plugin.util.evaluation.api;

import java.util.LinkedList;

public class EvaluationBuilder {
    public static final char[] num = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '.'};
    public static final char[] alp = new char[]{'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k',
            'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v',
            'w', 'x', 'y', 'z',
            'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K',
            'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V',
            'W', 'X', 'Y', 'Z'};
    public static final char[] op = new char[]{'+', '-', '*', '/', '%', '^'};

    public static Evaluation fromString(String str, ReflectionBinding... bindings) {
        char[] c = str.replace(" ", "").toCharArray();
        return new Evaluation(readEvaluation(c, 0, c.length), bindings);
    }

    private static EvaluationElement readEvaluation(char[] c, int start, int end) {
        if (start == end) throw new IllegalArgumentException("Empty expression!");
        LinkedList<EvaluationElement> evals = new LinkedList<EvaluationElement>();
        LinkedList<Integer> operations = new LinkedList<>();
        evals.add(readToken(c, start));
        int x = start;
        while (true) {
            x = getNextOperator(c, arrayContains(op, c[x]) ? x + 1 : x);
            if (x >= end - 1 || x == -1) {
                break;
            } else {
                operations.add((int) c[x]);
                evals.add(readToken(c, x + 1));
            }
        }
        if (evals.size() == 1) {
            return evals.get(0);
        } else if (evals.size() == 0) {
            throw new IllegalArgumentException("Empty expression!");
        } else {
            EvaluationElement[] ev = new EvaluationElement[evals.size()];
            char[] ops = new char[operations.size()];
            for (int i = 0; i < ev.length; i++) {
                ev[i] = evals.get(i);
            }
            for (int i = 0; i < ops.length; i++) {
                ops[i] = (char) operations.get(i).intValue();
            }
            return new EvaluationOperation(ev, ops);
        }
    }

    private static EvaluationElement readToken(char[] c, int start) {
        if (c[start] == '(') {
            int op_index = getNextEqualPos(c, start);
            if (op_index == -1) {
                throw new IllegalArgumentException("Invalid evaluation! Expected )");
            }
            return readEvaluation(c, start + 1, op_index);
        } else if (arrayContains(alp, c[start])) {
            //Variable
            String build = "";
            for (int i = start; i < c.length; i++) {
                if (arrayContains(num, c[i]) || arrayContains(alp, c[i])) {
                    build += c[i];
                } else {
                    break;
                }
            }
            return new EvaluationVariable(build);
        } else if (arrayContains(num, c[start])) {
            //Number
            String build = "";
            for (int i = start; i < c.length; i++) {
                if (arrayContains(num, c[i])) {
                    build += c[i];
                } else {
                    break;
                }
            }
            return new EvaluationNumber(Double.parseDouble(build));
        } else {
            throw new IllegalArgumentException("Invalid evaluation! Got " + c[start]);
        }
    }

    private static int getNextOperator(char[] c, int start) {
        start = getNextEqualPos(c, start);
        for (int i = start; i < c.length; i++) {
            if (arrayContains(op, c[i])) return i;
        }
        return -1;
    }

    private static int getNextEqualPos(char[] c, int start) {
        int kl = 0;
        for (int i = start; i < c.length; i++) {
            if (c[i] == '(') kl++;
            else if (c[i] == ')') kl--;
            if (kl == 0) return i;
        }
        return -1;
    }

    public static boolean arrayContains(char[] array, char symbol) {
        for (char c : array) if (c == symbol) return true;
        return false;
    }
}
