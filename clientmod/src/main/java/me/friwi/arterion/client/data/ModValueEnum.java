package me.friwi.arterion.client.data;

import me.friwi.arterion.client.ClassEnum;

public enum ModValueEnum {
	SELECTED_CLASS,
    SELECTED_CLASS_NAME,
    LEVEL,
    PRESTIGE_LEVEL,
    MANA,
    MAXMANA,
    MAXHEALTH,
    XP_PER_MILLE,
    REGION,
    GOLD,
    SERVER_STATUS,
    IS_ARTERION;
	
	private Object value = 0;
	
	public void setValue(Object value) {
		this.value = value;
	}
	
	public int getInt() {
		return ((Number)value).intValue();
	}
	
	public String getString() {
		return value.toString();
	}
	
	public ClassEnum getClassEnum() {
		if(value instanceof String)value = ClassEnum.valueOf(value.toString());
		return (ClassEnum) value;
	}
}
