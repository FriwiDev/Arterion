package me.friwi.arterion.plugin.ui.title;

import me.friwi.arterion.plugin.ArterionPlugin;
import me.friwi.arterion.plugin.player.ArterionPlayer;
import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class TitleAPI {
    private static Class<?> craftPlayerClass, packetPlayOutTitleClass, packetClass, chatSerializerClass, iChatBaseComponentClass;
    private static Method m3, craftPlayerHandleMethod, sendPacketMethod;
    private static Field playerConnectionField;
    private static Constructor<?> titleConstructor, subTitleConstructor, timingTitleConstructor;
    private static Object titleObj, subtitleObj;

    static {
        try {
            craftPlayerClass = Class.forName("org.bukkit.craftbukkit." + ArterionPlugin.REFLECTION_VERSION + ".entity.CraftPlayer");
            packetPlayOutTitleClass = Class.forName("net.minecraft.server." + ArterionPlugin.REFLECTION_VERSION + ".PacketPlayOutTitle");
            packetClass = Class.forName("net.minecraft.server." + ArterionPlugin.REFLECTION_VERSION + ".Packet");
            chatSerializerClass = Class.forName("net.minecraft.server." + ArterionPlugin.REFLECTION_VERSION + ".IChatBaseComponent$ChatSerializer");
            iChatBaseComponentClass = Class.forName("net.minecraft.server." + ArterionPlugin.REFLECTION_VERSION + ".IChatBaseComponent");
            titleConstructor = packetPlayOutTitleClass.getConstructor(packetPlayOutTitleClass.getDeclaredClasses()[0], iChatBaseComponentClass,
                    int.class, int.class, int.class);
            m3 = chatSerializerClass.getDeclaredMethod("a", String.class);
            craftPlayerHandleMethod = craftPlayerClass.getDeclaredMethod("getHandle");
            titleObj = packetPlayOutTitleClass.getDeclaredClasses()[0].getField("TITLE").get(null);
            subtitleObj = packetPlayOutTitleClass.getDeclaredClasses()[0].getField("SUBTITLE").get(null);
            subTitleConstructor = packetPlayOutTitleClass.getConstructor(
                    packetPlayOutTitleClass.getDeclaredClasses()[0], iChatBaseComponentClass,
                    int.class, int.class, int.class);
            timingTitleConstructor = packetPlayOutTitleClass.getConstructor(int.class, int.class, int.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void send(ArterionPlayer player, String title, String subtitle, int fadeInTime, int showTime, int fadeOutTime) {
        send(player.getBukkitPlayer(), title, subtitle, fadeInTime, showTime, fadeOutTime);
    }

    public static void send(ArterionPlayer player, String title, String subtitle) {
        send(player.getBukkitPlayer(), title, subtitle);
    }

    public static void send(Player player, String title, String subtitle) {
        send(player, title, subtitle,
                ArterionPlugin.getInstance().getFormulaManager().UI_TITLE_FADEIN.evaluateInt(),
                ArterionPlugin.getInstance().getFormulaManager().UI_TITLE_SHOW.evaluateInt(),
                ArterionPlugin.getInstance().getFormulaManager().UI_TITLE_FADEOUT.evaluateInt());
    }

    public static void send(Player player, String title, String subtitle, int fadeInTime, int showTime, int fadeOutTime) {
        try {
            Object chatTitle = m3.invoke(null, "{\"text\": \"" + title + "\"}");
            Object chatsTitle = m3.invoke(null, "{\"text\": \"" + subtitle + "\"}");
            Object packet = titleConstructor.newInstance(
                    titleObj, chatTitle,
                    fadeInTime, showTime, fadeOutTime);
            Object subtitlePacket = subTitleConstructor.newInstance(
                    subtitleObj, chatsTitle,
                    fadeInTime, showTime, fadeOutTime);
            Object timingPacket = timingTitleConstructor.newInstance(fadeInTime, showTime, fadeOutTime);

            Object craftPlayer = craftPlayerClass.cast(player);
            Object craftPlayerHandle = craftPlayerHandleMethod.invoke(craftPlayer);
            if (playerConnectionField == null)
                playerConnectionField = craftPlayerHandle.getClass().getDeclaredField("playerConnection");
            Object playerConnection = playerConnectionField.get(craftPlayerHandle);
            if (sendPacketMethod == null)
                sendPacketMethod = playerConnection.getClass().getSuperclass().getDeclaredMethod("sendPacket", packetClass);

            sendPacketMethod.invoke(playerConnection, packet);
            sendPacketMethod.invoke(playerConnection, subtitlePacket);
            sendPacketMethod.invoke(playerConnection, timingPacket);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
