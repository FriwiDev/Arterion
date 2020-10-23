package me.friwi.arterion.plugin.util.formulas;

import me.friwi.arterion.website.WebApplication;

public class PlayerJobLevelCalculator {
    public static int getLevelFromXP(int xp) {
        ArterionFormula f = WebApplication.getFormulaManager().JOB_LEVEL_CURVE;
        int i = 0;
        for (; xp > 0; i++) {
            xp -= f.evaluateInt(i);
        }
        if (xp != 0) {
            i--;
        }
        return i > getMaxLevel() ? getMaxLevel() : i;
    }

    public static int getCurrentOverflowFromXP(int xp) {
        ArterionFormula f = WebApplication.getFormulaManager().JOB_LEVEL_CURVE;
        int i = 0;
        for (; xp > 0; i++) {
            xp -= f.evaluateInt(i);
        }
        if (xp == 0) {
            xp -= f.evaluateInt(i);
        } else {
            i--;
        }
        if (i >= getMaxLevel()) return 0;
        return f.evaluateInt(i) + xp;
    }

    public static int getXPNeededForLevel(int level) {
        return WebApplication.getFormulaManager().JOB_LEVEL_CURVE.evaluateInt(level);
    }

    public static int getMaxLevel() {
        return WebApplication.getFormulaManager().JOB_LEVEL_MAX.evaluateInt();
    }
}
