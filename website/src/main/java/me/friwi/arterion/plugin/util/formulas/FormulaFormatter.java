package me.friwi.arterion.plugin.util.formulas;

public class FormulaFormatter {
    public static String toString(ArterionFormula formula) {
        if (formula == null) return "NULL";
        return "<i>" + formula.getTextRepresentation().replace("player.", "") + "</i>";
    }
}
