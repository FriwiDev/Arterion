package me.friwi.arterion.plugin.player;

import me.friwi.arterion.plugin.combat.skill.ActiveSkill;
import me.friwi.arterion.plugin.stats.StatType;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class ClericHealManager {
    private ArterionPlayer player;
    private List<KlerikerHeal> heals = new LinkedList<KlerikerHeal>();

    public ClericHealManager(ArterionPlayer player) {
        this.player = player;
    }

    public int heal(ActiveSkill skill, int heal, long cooldown, ArterionPlayer healer) {
        if (player.getHealth() <= 0) return 0;
        int divider = 1;
        Iterator<KlerikerHeal> it = heals.iterator();
        while (it.hasNext()) {
            KlerikerHeal n = it.next();
            if (n.isExpired()) it.remove();
            else if (n.getSkill().equals(skill)) {
                divider *= 2;
            }
        }
        heal /= divider;
        heals.add(new KlerikerHeal(skill, heal, System.currentTimeMillis() + cooldown));
        player.heal(heal);
        int finalHeal = heal;
        healer.trackStatistic(StatType.HEAL, 0, v -> v + finalHeal);
        return heal;
    }
}
