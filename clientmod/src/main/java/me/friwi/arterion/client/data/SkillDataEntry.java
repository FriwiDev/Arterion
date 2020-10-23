package me.friwi.arterion.client.data;

public class SkillDataEntry {
	private byte slot;
    private int skill;
    private boolean enabled;
    private String skillName;
    private String skillDescription;
    private long delay;
    private long totalDelay;
    private int delayColor;
    private long cooldown;
    private long maxCooldown;
    private int mana;
    
    public SkillDataEntry(byte slot, int skill, boolean enabled, String skillName, String skillDescription, long delay, long totalDelay, int delayColor, long cooldown, long maxCooldown, int mana) {
        this.slot = slot;
        this.skill = skill;
        this.enabled = enabled;
        this.skillName = skillName;
        this.skillDescription = skillDescription;
        this.delay = delay;
        this.totalDelay = totalDelay;
        this.delayColor = delayColor;
        this.cooldown = cooldown;
        this.maxCooldown = maxCooldown;
        this.mana = mana;
    }

	public byte getSlot() {
		return slot;
	}

	public int getSkill() {
		return skill;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public String getSkillName() {
		return skillName;
	}

	public String getSkillDescription() {
		return skillDescription;
	}

	public long getDelay() {
		return delay-System.currentTimeMillis();
	}

	public long getTotalDelay() {
		return totalDelay;
	}

	public int getDelayColor() {
		return delayColor;
	}

	public long getCooldown() {
		return cooldown-System.currentTimeMillis();
	}

	public long getMaxCooldown() {
		return maxCooldown;
	}
	
	public boolean hasDelay() {
		return getDelay()>0;
	}
	
	public boolean hasCooldown() {
		return getCooldown()>0;
	}

	public int getMana() {
		return mana;
	}

	@Override
	public String toString() {
		return "SkillDataEntry [slot=" + slot + ", skill=" + skill + ", enabled=" + enabled + ", skillName=" + skillName
				+ ", skillDescription=" + skillDescription + ", delay=" + delay + ", totalDelay=" + totalDelay
				+ ", delayColor=" + delayColor + ", cooldown=" + cooldown + ", maxCooldown=" + maxCooldown + ", mana="
				+ mana + "]";
	}

	
}
