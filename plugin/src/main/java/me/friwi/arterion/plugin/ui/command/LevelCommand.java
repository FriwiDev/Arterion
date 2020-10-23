package me.friwi.arterion.plugin.ui.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.HelpCommand;
import co.aikar.commands.annotation.*;
import me.friwi.arterion.plugin.combat.classes.ClassEnum;
import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.player.ArterionPlayerUtil;
import me.friwi.arterion.plugin.player.PlayerLevelCalculator;
import me.friwi.arterion.plugin.player.PlayerPrestigeLevelCalculator;
import me.friwi.arterion.plugin.ui.progress.ProgressBar;
import me.friwi.arterion.plugin.util.language.api.LanguageAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandAlias("level|lvl|l")
public class LevelCommand extends BaseCommand {
    @Default
    @CommandCompletion("@players")
    @Syntax("[player]")
    public void level(CommandSender sender, String args[]) {
        if (args.length > 1) {
            help(sender);
            return;
        }
        if (sender instanceof Player) {
            ArterionPlayer ep = ArterionPlayerUtil.get((Player) sender);
            ArterionPlayer target = ep;
            if (args.length == 1) {
                Player t = Bukkit.getPlayer(args[0]);
                if (t == null || !t.isOnline() || ArterionPlayerUtil.get(t).isVanished()) {
                    ep.sendTranslation("command.lvl.notonline");
                    return;
                }
                target = ArterionPlayerUtil.get(t);
            }
            ep.sendTranslation("line");
            if (target.getSelectedClass() == ClassEnum.NONE || target.getSelectedClass() == null) {
                if (target.equals(ep)) {
                    ep.sendTranslation("command.lvl.younotchosen");
                } else {
                    ep.sendTranslation("command.lvl.notchosen");
                }
            } else {
                float perc = 1f;
                int current, max;
                if (target.getLevel() >= PlayerLevelCalculator.getMaxLevel()) {
                    current = PlayerPrestigeLevelCalculator.getCurrentOverflowFromXP(target.getPrestigeXp());
                    max = PlayerPrestigeLevelCalculator.getXPNeededForLevel(target.getPrestigeLevel());
                    if (target.getPrestigeLevel() < PlayerPrestigeLevelCalculator.getMaxLevel()) {
                        perc = (current + 0f) / (max + 0f);
                    }
                } else {
                    current = PlayerLevelCalculator.getCurrentOverflowFromXP(target.getClassXp());
                    max = PlayerLevelCalculator.getXPNeededForLevel(target.getLevel());
                    perc = (current + 0f) / (max + 0f);
                }
                ep.sendTranslation("command.lvl.output1", target.getName(), ep.getTranslation("class." + target.getSelectedClass().name().toLowerCase()));
                if (target.getLevel() < PlayerLevelCalculator.getMaxLevel() || target.getPrestigeLevel() < PlayerPrestigeLevelCalculator.getMaxLevel()) {
                    if (target.getPrestigeLevel() <= 0) {
                        String bar = ProgressBar.generate(ChatColor.DARK_GREEN.toString(), perc, 70);
                        ep.sendTranslation("command.lvl.output2a", target.getLevel(), current, max, bar);
                    } else {
                        String bar = ProgressBar.generate(ChatColor.DARK_GREEN.toString(), perc, 45);
                        ep.sendTranslation("command.lvl.output2b", target.getLevel(), current, max, bar, target.getPrestigeLevel());
                    }
                } else {
                    ep.sendTranslation("command.lvl.output2c", target.getLevel(), target.getPrestigeLevel());
                }
            }
            ep.sendTranslation("line");
        }
    }

    @HelpCommand
    public void help(CommandSender sender) {
        sender.sendMessage(LanguageAPI.translate(sender, "command.lvl.help"));
    }
}
