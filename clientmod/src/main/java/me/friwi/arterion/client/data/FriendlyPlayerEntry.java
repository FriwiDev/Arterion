package me.friwi.arterion.client.data;

import java.util.UUID;

import me.friwi.arterion.client.ClassEnum;

public class FriendlyPlayerEntry {
	private UUID player;
    private String name;
    private ClassEnum selectedClass;
    private String selectedClassName;
    private int level;
    private int health;
    private int maxhealth;
    
	public FriendlyPlayerEntry(UUID player, String name, String selectedClass, String selectedClassName, int level,
			int health, int maxhealth) {
		super();
		this.player = player;
		this.name = name;
		this.selectedClass = ClassEnum.valueOf(selectedClass);
		this.selectedClassName = selectedClassName;
		this.level = level;
		this.health = health;
		this.maxhealth = maxhealth;
	}

	public UUID getPlayer() {
		return player;
	}

	public String getName() {
		return name;
	}

	public ClassEnum getSelectedClass() {
		return selectedClass;
	}

	public String getSelectedClassName() {
		return selectedClassName;
	}

	public int getLevel() {
		return level;
	}

	public int getHealth() {
		return health;
	}

	public int getMaxhealth() {
		return maxhealth;
	}

	@Override
	public String toString() {
		return "FriendlyPlayerEntry [player=" + player + ", name=" + name + ", selectedClass=" + selectedClass
				+ ", selectedClassName=" + selectedClassName + ", level=" + level + ", health=" + health
				+ ", maxhealth=" + maxhealth + "]";
	}
    
    
}
