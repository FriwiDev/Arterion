package me.friwi.arterion.plugin.player;

import me.friwi.arterion.plugin.ArterionPlugin;
import me.friwi.arterion.plugin.formula.ArterionFormula;

public class PlayerLevelCalculator {
    public static int getLevelFromXP(int xp) {
        ArterionFormula f = ArterionPlugin.getInstance().getFormulaManager().PLAYER_LEVEL_CURVE;
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
        ArterionFormula f = ArterionPlugin.getInstance().getFormulaManager().PLAYER_LEVEL_CURVE;
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
        return ArterionPlugin.getInstance().getFormulaManager().PLAYER_LEVEL_CURVE.evaluateInt(level);
    }

    public static int getTotalXPNeededForLevel(int level) {
        int sum = 0;
        for (int i = 0; i < level; i++) {
            sum += ArterionPlugin.getInstance().getFormulaManager().PLAYER_LEVEL_CURVE.evaluateInt(i);
        }
        return sum;
    }

    public static int getMaxLevel() {
        return ArterionPlugin.getInstance().getFormulaManager().PLAYER_LEVEL_MAX.evaluateInt();
    }
}
