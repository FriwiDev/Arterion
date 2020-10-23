package me.friwi.arterion.plugin.combat.gamemode;

import me.friwi.arterion.plugin.util.config.api.ConfigValue;
import me.friwi.arterion.plugin.util.config.api.Configureable;

@Configureable
public class TemporaryWorldConfig {
    @ConfigValue(fallback = "0")
    public Integer min_chunk_x;
    @ConfigValue(fallback = "0")
    public Integer max_chunk_x;
    @ConfigValue(fallback = "0")
    public Integer min_chunk_z;
    @ConfigValue(fallback = "0")
    public Integer max_chunk_z;
}