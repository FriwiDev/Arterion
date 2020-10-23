package me.friwi.arterion.plugin.player;

import me.friwi.arterion.plugin.combat.skill.ActiveSkill;

public class KlerikerHeal {
    private ActiveSkill skill;
    private int heal;
    private long expires;

    public KlerikerHeal(ActiveSkill skill, int heal, long expires) {
        this.skill = skill;
        this.heal = heal;
        this.expires = expires;
    }

    public ActiveSkill getSkill() {
        return skill;
    }

    public void setSkill(ActiveSkill skill) {
        this.skill = skill;
    }

    public int getHeal() {
        return heal;
    }

    public void setHeal(int heal) {
        this.heal = heal;
    }

    public long getExpires() {
        return expires;
    }

    public void setExpires(long expires) {
        this.expires = expires;
    }

    public boolean isExpired() {
        return expires < System.currentTimeMillis();
    }

    @Override
    public String toString() {
        return "KlerikerHeal{" +
                "skill=" + skill +
                ", heal=" + heal +
                ", expires=" + expires +
                '}';
    }
}
