package me.friwi.arterion.plugin;

import me.friwi.arterion.plugin.util.config.api.ConfigValue;
import me.friwi.arterion.plugin.util.config.api.Configureable;
import org.bukkit.Location;

@Configureable(location = "server.conf")
public class ArterionPluginConfig {
    @ConfigValue(fallback = "false")
    public Boolean maintenance;

    @ConfigValue(fallback = "null")
    public Location spawn;

    @ConfigValue(fallback = "world;0;20;0;0;0")
    public Location artefact;
    @ConfigValue(fallback = "world;0;20;0;0;0")
    public Location artefact_cristal_1;
    @ConfigValue(fallback = "world;0;20;0;0;0")
    public Location artefact_cristal_2;
    @ConfigValue(fallback = "world;0;20;0;0;0")
    public Location artefact_cristal_3;

    @ConfigValue(fallback = "world;0;20;0;0;0")
    public Location graveruin_center;
    @ConfigValue(fallback = "world;0;20;0;0;0")
    public Location graveruin_glass;
    @ConfigValue(fallback = "null")
    public String graveruin_owner;
    @ConfigValue(fallback = "-1")
    public Long graveruin_owner_until;

    @ConfigValue(fallback = "world;0;20;0;0;0")
    public Location deserttemple_center;
    @ConfigValue(fallback = "world;0;20;0;0;0")
    public Location deserttemple_glass;
    @ConfigValue(fallback = "null")
    public String deserttemple_owner;
    @ConfigValue(fallback = "-1")
    public Long deserttemple_owner_until;

    @ConfigValue(fallback = "world;0;20;0;0;0")
    public Location morgoth_portal;
    @ConfigValue(fallback = "world;0;20;0;0;0")
    public Location wilderness_portal;

    @ConfigValue(fallback = "none")
    public String replay_upload_secret;
    @ConfigValue(fallback = "https://arterion.de/upload")
    public String replay_upload_url;

    @ConfigValue(fallback = "0")
    public Long boostExpire;
    @ConfigValue(fallback = "none")
    public String currentBooster;
}