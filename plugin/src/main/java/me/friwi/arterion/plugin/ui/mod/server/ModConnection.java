package me.friwi.arterion.plugin.ui.mod.server;

import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.ui.mod.ModPacket;
import net.badlion.timers.impl.NmsManager;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

public class ModConnection implements PluginMessageListener {
    public static final String CHANNEL_NAME = "arterion:mod";
    public static final short PROTOCOL_VERSION = 3;
    private ModPacketListener listener;

    public ModConnection(ModPacketListener listener) {
        this.listener = listener;
    }

    /**
     * Only call sync!
     *
     * @param player
     * @param packet
     */
    public static void sendModPacket(ArterionPlayer player, ModPacket packet) {
        sendModPacket(player.getBukkitPlayer(), packet);
    }

    /**
     * Only call sync!
     *
     * @param player
     * @param packet
     */
    public static void sendModPacket(Player player, ModPacket packet) {
        NmsManager.sendPluginMessage(player, CHANNEL_NAME, ModPacketList.toBytes(packet));
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] data) {
        this.listener.handlePacket(player, ModPacketList.fromBytes(data));
    }
}
