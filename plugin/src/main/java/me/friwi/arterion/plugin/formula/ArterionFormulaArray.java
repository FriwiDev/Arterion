package me.friwi.arterion.plugin.formula;

import me.friwi.arterion.plugin.util.evaluation.api.ReflectionBinding;

public class ArterionFormulaArray {
    private String key;
    private ArterionFormula[] formulas;
    private String[] names;

    public ArterionFormulaArray(String key, Object[] names, ReflectionBinding... bindings) {
        this.formulas = new ArterionFormula[names.length];
        this.names = new String[names.length];
        this.key = key;
        for (int i = 0; i < formulas.length; i++) {
            this.formulas[i] = new ArterionFormula(key + "." + names[i].toString().toLowerCase(), bindings) {
                @Override
                public String getTranslateableKey() {
                    return ArterionFormulaArray.this.key;
                }
            };
            this.names[i] = names[i].toString().toLowerCase();
        }
    }

    public ArterionFormula get(int i) {
        return formulas[i];
    }

    public ArterionFormula get(String name) {
        if (name == null) return null;
        ArterionFormula other = null;
        for (int i = 0; i < formulas.length; i++) {
            if (names[i].equalsIgnoreCase(name)) return formulas[i];
            if (names[i].equalsIgnoreCase("other")) other = formulas[i];
        }
        return other;
    }

    public ArterionFormula[] getFormulas() {
        return this.formulas;
    }
}
