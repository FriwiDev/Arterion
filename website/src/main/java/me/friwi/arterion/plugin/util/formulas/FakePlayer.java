package me.friwi.arterion.plugin.util.formulas;

public class FakePlayer {
    private int level;
    private int prestigelevel;

    public FakePlayer() {
        this(0, 0);
    }

    public FakePlayer(int level, int prestigelevel) {
        this.level = level;
        this.prestigelevel = prestigelevel;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getPrestigelevel() {
        return prestigelevel;
    }

    public void setPrestigelevel(int prestigelevel) {
        this.prestigelevel = prestigelevel;
    }

    public int getMaxHealth() {
        throw new RuntimeException("FakePlayer can not provide max health");
    }

    public int getHealth() {
        throw new RuntimeException("FakePlayer can not provide health");
    }

    public int getMaxMana() {
        throw new RuntimeException("FakePlayer can not provide max mana");
    }

    public int getMana() {
        throw new RuntimeException("FakePlayer can not provide mana");
    }
}
