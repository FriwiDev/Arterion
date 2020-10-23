package me.friwi.arterion.plugin.ui.mod.server;

import me.friwi.arterion.plugin.ui.mod.ModPacket;
import org.bukkit.entity.Player;

public interface ModPacketListener {
    void handlePacket(Player player, ModPacket packet);
}
