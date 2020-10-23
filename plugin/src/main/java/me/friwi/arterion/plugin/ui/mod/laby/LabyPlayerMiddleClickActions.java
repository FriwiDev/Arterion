package me.friwi.arterion.plugin.ui.mod.laby;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.bukkit.entity.Player;

public class LabyPlayerMiddleClickActions {
    public static void playMiddleClickActions(Player player) {
        // List of all action menu entries
        JsonArray array = new JsonArray();

        // Add entries
        JsonObject entry = new JsonObject();
        entry.addProperty("displayName", "Player info");
        entry.addProperty("type", EnumActionType.RUN_COMMAND.name());
        entry.addProperty("value", "who {name}"); // {name} will be replaced with the players name
        array.add(entry);

        entry = new JsonObject();
        entry.addProperty("displayName", "Open statistics");
        entry.addProperty("type", EnumActionType.OPEN_BROWSER.name());
        entry.addProperty("value", "https://arterion.de/stats/player/{name}");
        array.add(entry);

        entry = new JsonObject();
        entry.addProperty("displayName", "Send Message");
        entry.addProperty("type", EnumActionType.SUGGEST_COMMAND.name());
        entry.addProperty("value", "msg {name} ");
        array.add(entry);

        // Send to LabyMod using the API
        LMCUtils.sendLMCMessage(player, "user_menu_actions", array);
    }

    enum EnumActionType {
        NONE,
        CLIPBOARD,
        RUN_COMMAND,
        SUGGEST_COMMAND,
        OPEN_BROWSER
    }
}
