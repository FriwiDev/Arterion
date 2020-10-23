package me.friwi.arterion.plugin.ui.tablist;

import me.friwi.arterion.plugin.ArterionPlugin;
import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.player.ArterionPlayerUtil;
import me.friwi.arterion.plugin.ui.mod.server.ModValueEnum;
import me.friwi.arterion.plugin.util.language.api.LanguageAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class TablistManager {
    private static Class<?> TITLE_PACKET;
    private static Class<?> CHAT_SERIALIZER;
    private static Class<?> CRAFT_PLAYER;
    private static Constructor<?> TITLE_PACKET_CONSTRUCTOR;
    private static Field SUBTITLE_FIELD;
    private static Field PLAYER_CONNECTION;
    private static Method CHAT_SERIALIZE;
    private static Method GET_HANDLE;
    private static Method SEND_PACKET;

    static {
        try {
            TITLE_PACKET = Class.forName("net.minecraft.server." + ArterionPlugin.REFLECTION_VERSION + ".PacketPlayOutPlayerListHeaderFooter");
            CHAT_SERIALIZER = Class.forName("net.minecraft.server." + ArterionPlugin.REFLECTION_VERSION + ".IChatBaseComponent$ChatSerializer");
            CRAFT_PLAYER = Class.forName("org.bukkit.craftbukkit." + ArterionPlugin.REFLECTION_VERSION + ".entity.CraftPlayer");
            Class<?> ichatbasecomponent = Class.forName("net.minecraft.server." + ArterionPlugin.REFLECTION_VERSION + ".IChatBaseComponent");
            TITLE_PACKET_CONSTRUCTOR = TITLE_PACKET.getDeclaredConstructor(ichatbasecomponent);
            SUBTITLE_FIELD = TITLE_PACKET.getDeclaredField("b");
            SUBTITLE_FIELD.setAccessible(true);
            GET_HANDLE = CRAFT_PLAYER.getDeclaredMethod("getHandle");
            PLAYER_CONNECTION = GET_HANDLE.getReturnType().getDeclaredField("playerConnection");
            PLAYER_CONNECTION.setAccessible(true);
            CHAT_SERIALIZE = CHAT_SERIALIZER.getDeclaredMethod("a", String.class);
            Class<?> packet = Class.forName("net.minecraft.server." + ArterionPlugin.REFLECTION_VERSION + ".Packet");
            SEND_PACKET = PLAYER_CONNECTION.getType().getDeclaredMethod("sendPacket", packet);
        } catch (ClassNotFoundException | NoSuchFieldException | NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    public void updateTablistHeaderFooter() {
        int players = ArterionPlugin.getOnlinePlayers().size();
        for (Player p : ArterionPlugin.getOnlinePlayers()) {
            ArterionPlayer ep = ArterionPlayerUtil.get(p);
            if (ep != null) {
                String header = ep.getLanguage().getTranslation("tab.header").translate(players, Bukkit.getMaxPlayers()).getMessage();
                String footer = ep.getLanguage().getTranslation("tab.footer").translate().getMessage();
                sendTablist(p, header, footer);
                ep.getPlayerScoreboard().updateModValue(ModValueEnum.SERVER_STATUS);
            }
        }
    }

    public void updatePlayerListName(ArterionPlayer p) {
        String tag = "";
        if (p.getGuild() != null) tag = "\2478[\247e" + p.getGuild().getTag() + "\2478] ";
        String checkmark = "";
        if (p.usesMod()) checkmark = " \2472\u2714";
        p.getBukkitPlayer().setPlayerListName(tag + LanguageAPI.getLanguage(LanguageAPI.DEFAULT_LANGUAGE).getTranslation(p.getRank().getRankTranslation()).translate(p.getName()).getMessage() + p.getName() + checkmark);
    }

    public void onPlayerJoin(ArterionPlayer p) {
        this.updatePlayerListName(p);
    }

    private void sendTablist(Player p, String title, String subTitle) {
        if (title == null) title = "";
        if (subTitle == null) subTitle = "";

        try {

            Object tabTitle = CHAT_SERIALIZE.invoke(null, "{\"text\":\"" + title + "\"}");
            Object tabSubTitle = CHAT_SERIALIZE.invoke(null, "{\"text\":\"" + subTitle + "\"}");

            Object packet = TITLE_PACKET_CONSTRUCTOR.newInstance(tabTitle);
            SUBTITLE_FIELD.set(packet, tabSubTitle);

            SEND_PACKET.invoke(PLAYER_CONNECTION.get(GET_HANDLE.invoke(p)), packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
