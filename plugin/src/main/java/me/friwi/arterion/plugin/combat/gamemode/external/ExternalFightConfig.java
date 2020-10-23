package me.friwi.arterion.plugin.combat.gamemode.external;

import me.friwi.arterion.plugin.util.config.api.ConfigValue;
import me.friwi.arterion.plugin.util.config.api.Configureable;

@Configureable
public class ExternalFightConfig {
    @ConfigValue(fallback = "0")
    public Double replay_spawn_x;
    @ConfigValue(fallback = "0")
    public Double replay_spawn_y;
    @ConfigValue(fallback = "0")
    public Double replay_spawn_z;
    @ConfigValue(fallback = "0")
    public Float replay_spawn_yaw;
    @ConfigValue(fallback = "0")
    public Float replay_spawn_pitch;
}
