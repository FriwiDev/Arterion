package me.friwi.arterion.plugin.combat.gamemode.arena;

import me.friwi.arterion.plugin.util.config.api.ConfigValue;
import me.friwi.arterion.plugin.util.config.api.Configureable;
import org.bukkit.Location;

@Configureable
public class ArenaFightConfig {
    @ConfigValue(fallback = "world;0;0;0;0;0")
    Location respawn,

    spawn_1_0,
            spawn_1_1,
            spawn_1_2,
            spawn_1_3,
            spawn_1_4,
            spawn_1_5,
            spawn_1_6,
            spawn_1_7,

    spawn_2_0,
            spawn_2_1,
            spawn_2_2,
            spawn_2_3,
            spawn_2_4,
            spawn_2_5,
            spawn_2_6,
            spawn_2_7;

    @ConfigValue(fallback = "10")
    Integer pregame_seconds;
    @ConfigValue(fallback = "600")
    Integer game_seconds;

    public Location[] getTeamSpawns(int team) {
        if (team == 1) {
            return new Location[]{spawn_1_0, spawn_1_1, spawn_1_2, spawn_1_3,
                    spawn_1_4, spawn_1_5, spawn_1_6, spawn_1_7};
        } else {
            return new Location[]{spawn_2_0, spawn_2_1, spawn_2_2, spawn_2_3,
                    spawn_2_4, spawn_2_5, spawn_2_6, spawn_2_7};
        }
    }
}
