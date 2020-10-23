package me.friwi.arterion.plugin.combat.skill;

import me.friwi.arterion.plugin.ArterionPlugin;
import me.friwi.arterion.plugin.combat.classes.ClassEnum;
import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.ui.mod.packet.Packet07SkillSlotData;
import me.friwi.arterion.plugin.ui.mod.server.ModConnection;

public abstract class ActiveSkill<T extends SkillContainerData> extends Skill<T> {
    private SkillSlotEnum skillSlot;

    public ActiveSkill(ClassEnum boundClass, SkillSlotEnum skillSlot) {
        super(boundClass);
        this.skillSlot = skillSlot;
    }

    public abstract void attemptCast(ArterionPlayer p);

    public abstract int getCooldown(ArterionPlayer p);

    public abstract boolean canBeCastInCurrentRegion(ArterionPlayer p);

    public abstract long getActiveTime(ArterionPlayer p);

    public int getUnlockLevel() {
        return ArterionPlugin.getInstance().getFormulaManager().SKILL_LEVEL.get(getSkillType().ordinal()).evaluateInt();
    }

    public int getMana(ArterionPlayer p) {
        return ArterionPlugin.getInstance().getFormulaManager().SKILL_MANA.get(getSkillType().ordinal()).evaluateInt(p);
    }

    public int getMaxCooldown(ArterionPlayer p) {
        //Apply prestige point
        double reduce = ArterionPlugin.getInstance().getFormulaManager().PRESTIGE_COOLDOWN.evaluateDouble(p.getPointsCooldown());
        return (int) (ArterionPlugin.getInstance().getFormulaManager().SKILL_COOLDOWN.get(getSkillType().ordinal()).evaluateInt(p) * reduce);
    }

    @Override
    public boolean isActive() {
        return true;
    }

    public SkillSlotEnum getSkillSlot() {
        return skillSlot;
    }

    @Override
    public T getSkillDataContainer(ArterionPlayer p) {
        return (T) p.getSkillSlots().getActiveSkillData()[skillSlot.ordinal()];
    }

    @Override
    public void setSkillDataContainer(ArterionPlayer p, T container) {
        p.getSkillSlots().getActiveSkillData()[skillSlot.ordinal()] = container;
    }

    public void updateToMod(ArterionPlayer p) {
        if (p.usesMod()) {
            T t = getSkillDataContainer(p);
            long totalDelay = t.getActiveTime();
            long delay = t.getActiveUntil() - System.currentTimeMillis();
            int color = skillSlot.getARGB();
            long maxCooldown = getMaxCooldown(p);
            long cooldown = t.getLastUsed() + maxCooldown - System.currentTimeMillis();
            if (delay < 0) delay = 0;
            if (cooldown < 0) cooldown = 0;
            ModConnection.sendModPacket(p, new Packet07SkillSlotData((byte) skillSlot.ordinal(), getSkillType().ordinal(), true, getSkillSlot().getColor() + getName(p), getDescriptionWithMana(p), delay, totalDelay, color, cooldown, maxCooldown, getMana(p)));
        }
    }

    public String getDescriptionWithMana(ArterionPlayer p) {
        String mana = p.getTranslation("skill.disc.mana", getMana(p)) + "\n";
        String cooldownStr = p.getTranslation("skill.disc.cooldown", ((int) getMaxCooldown(p)) / 1000d) + "\n";
        return mana + cooldownStr + getDescription(p);
    }
}
