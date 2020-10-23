package me.friwi.arterion.plugin.tutorial;

import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.ui.chat.FormattedChat;
import org.bukkit.Bukkit;
import org.bukkit.Location;

public enum TutorialEnum {
    SPAWN(new Location(Bukkit.getWorlds().get(0), 0.5, 83, 0.5, 180, 0)),
    CLASS(new Location(Bukkit.getWorlds().get(0), -2.5, 81, -22.5, 90, 0)),
    GUILD(new Location(Bukkit.getWorlds().get(0), 21.5, 83, -49.5, 180, 0)),
    SIEGE(new Location(Bukkit.getWorlds().get(0), 24.5, 81, -20, 0, 0)),
    BANK(new Location(Bukkit.getWorlds().get(0), 51, 85, -26.5, -90, 0)),
    SMITH(new Location(Bukkit.getWorlds().get(0), 16, 81, -26, 142, 0)),
    TP(new Location(Bukkit.getWorlds().get(0), -1.5, 81, -57.5, 90, 0)),
    PVP(new Location(Bukkit.getWorlds().get(0), -121.5, 79, 6.5, 90, 0)),
    PLAYERS(new Location(Bukkit.getWorlds().get(0), 13.3, 81, -8.5, -90, 0)),
    HOME(new Location(Bukkit.getWorlds().get(0), 36.5, 83, -46.5, -90, 0)),
    END(new Location(Bukkit.getWorlds().get(0), -2.5, 81, -22.5, 90, 0));
    private Location loc;

    TutorialEnum(Location loc) {
        this.loc = loc;
    }

    public void sendToPlayer(ArterionPlayer player) {
        player.getBukkitPlayer().teleport(loc);
        player.sendTranslation("line");
        player.sendTranslation("tutorial." + name().toLowerCase() + ".title");
        player.sendTranslation("tutorial." + name().toLowerCase() + ".text");
        int prev = ordinal() == 0 ? (values().length - 1) : (ordinal() - 1);
        int next = (ordinal() + 1) % values().length;
        FormattedChat.sendFormattedChat(player.getBukkitPlayer(), player.getTranslation("tutorial.nav", prev, next));
        player.sendTranslation("line");
    }
}
