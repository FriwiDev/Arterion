package me.friwi.arterion.plugin.ui.mod.server;

import me.friwi.arterion.plugin.ArterionPlugin;
import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.player.PlayerLevelCalculator;
import me.friwi.arterion.plugin.player.PlayerPrestigeLevelCalculator;
import me.friwi.arterion.plugin.ui.mod.packet.Packet02IntValue;
import me.friwi.arterion.plugin.ui.mod.packet.Packet03StringValue;
import org.bukkit.Bukkit;

import java.util.function.Function;

public enum ModValueEnum {
    SELECTED_CLASS(player -> player.getSelectedClass().name()),
    SELECTED_CLASS_NAME(player -> player.getSelectedClass().getName(player.getLanguage())),
    LEVEL(player -> player.getLevel()),
    PRESTIGE_LEVEL(player -> player.getPrestigeLevel()),
    MANA(player -> player.getMana()),
    MAXMANA(player -> player.getMaxMana()),
    MAXHEALTH(player -> player.getMaxHealth()),
    XP_PER_MILLE(player -> {
        if (player.getLevel() >= PlayerLevelCalculator.getMaxLevel()) {
            float perc = 1f;
            int current = PlayerPrestigeLevelCalculator.getCurrentOverflowFromXP(player.getPrestigeXp());
            int max = PlayerPrestigeLevelCalculator.getXPNeededForLevel(player.getPrestigeLevel());
            if (player.getPrestigeLevel() < PlayerPrestigeLevelCalculator.getMaxLevel()) {
                perc = (current + 0f) / (max + 0f);
            }
            return (int) (perc * 1000);
        } else {
            int current = PlayerLevelCalculator.getCurrentOverflowFromXP(player.getClassXp());
            int max = PlayerLevelCalculator.getXPNeededForLevel(player.getLevel());
            float perc = (current + 0f) / (max + 0f);
            return (int) (perc * 1000);
        }
    }),
    REGION(player -> player.getRegion() == null ? "-" : player.getRegion().getName(player.getLanguage())),
    GOLD(player -> player.getBagMoneyBearer().getCachedMoney()),
    SERVER_STATUS(player -> player.getTranslation("mod.server_state.online", ArterionPlugin.getOnlinePlayers().size(), Bukkit.getMaxPlayers())),
    IS_ARTERION(player -> 1);

    private Function<ArterionPlayer, Object> func;

    ModValueEnum(Function<ArterionPlayer, Object> func) {
        this.func = func;
    }

    public void send(ArterionPlayer player) {
        Object value = func.apply(player);
        if (value instanceof Number) {
            ModConnection.sendModPacket(player, new Packet02IntValue(name(), ((Number) value).intValue()));
        } else {
            ModConnection.sendModPacket(player, new Packet03StringValue(name(), value.toString()));
        }
    }
}
