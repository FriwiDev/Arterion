package me.friwi.arterion.plugin.stats;

public enum StatType {
    //Player
    KILLS(0),
    DEATHS(0),
    XP(0),
    MOB_KILLS(0), //Mob type
    DESTROYED_BLOCKS(0), //Block id << 8 | data
    PLACED_BLOCKS(0), //Block id << 8 | data
    HEAL(0),
    DAMAGE_DEALT_PLAYERS(0),
    DAMAGE_DEALT_MOBS(0), //Mob type
    DAMAGE_RECEIVED(0), //Damage type
    CLICKS(0), //Item id (only weapons)
    HITS(0), //Item id (only weapons)
    ONLINE_MINUTES(0),

    //Guilds
    CLAN_KILLS(0),
    CLAN_DEATHS(0),
    GFIGHT_ATTACK_WINS(0),
    GFIGHT_ATTACK_LOSS(0),
    GFIGHT_DEFENSE_WINS(0),
    GFIGHT_DEFENSE_LOSS(0),
    CAPTURE_POINT_TAKEN(0), //Cap type
    ARTEFACT_HOURS(0),
    GUILD_ONLINE_MINUTES(0);

    private long defaultValue;

    StatType(long defaultValue) {
        this.defaultValue = defaultValue;
    }

    public long getDefaultValue() {
        return defaultValue;
    }
}
