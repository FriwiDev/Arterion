package me.friwi.arterion.plugin.ui.mod.laby;

import com.google.gson.JsonObject;
import org.bukkit.entity.Player;

import java.util.UUID;

public class LabyCurrentGamemode {
    private static UUID matchID = UUID.randomUUID();

    public static void updateGameInfo(Player player, boolean hasGame, String gamemode, long startTime, long endTime) {
        // Create game json object
        JsonObject obj = new JsonObject();
        obj.addProperty("hasGame", hasGame);

        if (hasGame) {
            obj.addProperty("game_mode", gamemode);
            obj.addProperty("game_startTime", startTime); // Set to 0 for countdown
            obj.addProperty("game_endTime", endTime); // // Set to 0 for timer
        }

        // Send to user
        LMCUtils.sendLMCMessage(player, "discord_rpc", obj);
    }
}
