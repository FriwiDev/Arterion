package me.friwi.arterion.plugin.combat.gamemode.dungeon.morgoth;

import me.friwi.arterion.plugin.util.config.api.ConfigValue;
import me.friwi.arterion.plugin.util.config.api.Configureable;
import org.bukkit.Location;

@Configureable
public class MorgothDungeonFightConfig {
    @ConfigValue(fallback = "world;0;0;0;0;0")
    Location respawn;
    @ConfigValue(fallback = "world;0;0;0;0;0")
    Location boss_spawn;
}
