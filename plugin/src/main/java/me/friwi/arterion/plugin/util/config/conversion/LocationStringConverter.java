package me.friwi.arterion.plugin.util.config.conversion;

import me.friwi.arterion.plugin.util.config.conversion.api.TypeConverter;
import org.bukkit.Bukkit;
import org.bukkit.Location;

public class LocationStringConverter extends TypeConverter<Location, String> {

    @Override
    public Location convertOne(String value) {
        String[] params = value.split(";");
        if (params.length != 6) return null;
        return new Location(Bukkit.getWorld(params[0]), Double.parseDouble(params[1]), Double.parseDouble(params[2]), Double.parseDouble(params[3]),
                Float.parseFloat(params[4]), Float.parseFloat(params[5]));
    }

    @Override
    public String convertTwo(Location value) {
        if (value == null) return null;
        return value.getWorld().getName() + ";" + value.getX() + ";" + value.getY() + ";" + value.getZ() + ";" + value.getYaw() + ";" + value.getPitch();
    }

}
