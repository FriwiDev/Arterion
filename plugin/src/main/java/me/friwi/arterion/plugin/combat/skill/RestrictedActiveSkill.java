package me.friwi.arterion.plugin.combat.skill;

import me.friwi.arterion.plugin.combat.classes.ClassEnum;
import me.friwi.arterion.plugin.combat.skill.card.CooldownCard;
import me.friwi.arterion.plugin.player.ArterionPlayer;

public abstract class RestrictedActiveSkill<T extends SkillContainerData> extends ActiveSkill<T> {
    public RestrictedActiveSkill(ClassEnum boundClass, SkillSlotEnum skillSlot) {
        super(boundClass, skillSlot);
    }

    public abstract boolean cast(ArterionPlayer p);

    @Override
    public void attemptCast(ArterionPlayer p) {
        //Check for cooldowns
        int cd = this.getCooldown(p);
        if (cd > 0) {
            p.scheduleHotbarCard(new CooldownCard(p, this));
            return;
        }
        //Check for mana
        int mana = this.getMana(p) - p.getMana();
        if (mana > 0) {
            p.scheduleHotbarCard(new CooldownCard(p, this));
            return;
        }
        //Prerequisites met
        if (this.cast(p)) {
            this.onSucceedCast(p);
        }
    }

    @Override
    public int getCooldown(ArterionPlayer p) {
        long lastUsed = getSkillDataContainer(p).getLastUsed();
        long remain = lastUsed + getMaxCooldown(p) - System.currentTimeMillis();
        if (remain <= 0) return 0;
        remain += 999;
        remain /= 1000;
        return (int) remain;
    }

    public void onSucceedCast(ArterionPlayer p) {
        this.getSkillDataContainer(p).setLastUsed(System.currentTimeMillis());
        this.getSkillDataContainer(p).setActiveUntil(getActiveTime(p) + System.currentTimeMillis());
        p.useMana(getMana(p));
        if (p.usesMod()) this.updateToMod(p);
    }
}
