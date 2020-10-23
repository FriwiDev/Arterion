package me.friwi.arterion.plugin.ui.mod.laby;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.bukkit.entity.Player;

public class LabyAddonRecommendation {
    public static void recommendAddons(Player player) {
        JsonObject object = new JsonObject();
        JsonArray addons = new JsonArray();

        JsonObject addon = new JsonObject();
        addon.addProperty("uuid", "8b6bff84-4266-4e12-89c2-204b142e74fa"); // Published uuid of the addon
        addon.addProperty("required", false); // Required for this server?
        addons.add(addon);

        object.add("addons", addons);

        // Send to LabyMod using the API
        //LMCUtils.sendLMCMessage(player, "addon_recommendation", object);
    }
}
