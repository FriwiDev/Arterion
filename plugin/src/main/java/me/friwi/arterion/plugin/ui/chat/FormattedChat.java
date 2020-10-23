package me.friwi.arterion.plugin.ui.chat;

import me.friwi.arterion.plugin.ArterionPlugin;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class FormattedChat {
    private static Class<?> craftPlayerClass, packetPlayOutChatClass, packetClass, chatSerializerClass, iChatBaseComponentClass;
    private static Method m3, craftPlayerHandleMethod, sendPacketMethod;
    private static Field playerConnectionField;

    static {
        try {
            craftPlayerClass = Class.forName("org.bukkit.craftbukkit." + ArterionPlugin.REFLECTION_VERSION + ".entity.CraftPlayer");
            packetPlayOutChatClass = Class.forName("net.minecraft.server." + ArterionPlugin.REFLECTION_VERSION + ".PacketPlayOutChat");
            packetClass = Class.forName("net.minecraft.server." + ArterionPlugin.REFLECTION_VERSION + ".Packet");
            chatSerializerClass = Class.forName("net.minecraft.server." + ArterionPlugin.REFLECTION_VERSION + ".IChatBaseComponent$ChatSerializer");
            iChatBaseComponentClass = Class.forName("net.minecraft.server." + ArterionPlugin.REFLECTION_VERSION + ".IChatBaseComponent");
            m3 = chatSerializerClass.getDeclaredMethod("a", String.class);
            craftPlayerHandleMethod = craftPlayerClass.getDeclaredMethod("getHandle");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void sendFormattedChat(Player player, String message) {
        sendFormattedChat(player, message, (byte) 0);
    }

    public static void sendActionBar(Player player, String message) {
        sendFormattedChat(player, "{\"text\": \"" + message + "\"}", (byte) 2);
    }

    private static void sendFormattedChat(Player player, String message, byte channel) {
        try {
            Object craftPlayer = craftPlayerClass.cast(player);
            Object cbc = iChatBaseComponentClass.cast(m3.invoke(null, message));
            Object packet = packetPlayOutChatClass.getConstructor(new Class<?>[]{iChatBaseComponentClass, byte.class}).newInstance(cbc, channel);
            Object craftPlayerHandle = craftPlayerHandleMethod.invoke(craftPlayer);
            if (playerConnectionField == null)
                playerConnectionField = craftPlayerHandle.getClass().getDeclaredField("playerConnection");
            Object playerConnection = playerConnectionField.get(craftPlayerHandle);
            if (sendPacketMethod == null)
                sendPacketMethod = playerConnection.getClass().getSuperclass().getDeclaredMethod("sendPacket", packetClass);
            sendPacketMethod.invoke(playerConnection, packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
