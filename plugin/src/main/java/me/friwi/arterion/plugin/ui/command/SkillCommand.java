package me.friwi.arterion.plugin.ui.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.HelpCommand;
import co.aikar.commands.annotation.*;
import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.player.ArterionPlayerUtil;
import me.friwi.arterion.plugin.util.language.api.LanguageAPI;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandAlias("skill")
public class SkillCommand extends BaseCommand {
    @Default
    @CommandCompletion("@skill")
    @Syntax("<skill>")
    public void skill(CommandSender sender, String args[]) {
        if (args.length < 1) {
            help(sender);
            return;
        }

        String build = "";
        for (String s : args) build += s + " ";

        if (sender instanceof Player) {
            ArterionPlayer p = ArterionPlayerUtil.get((Player) sender);
            p.getSkillSlots().castSkill(build);
        }
    }

    @HelpCommand
    @Default
    public void help(CommandSender sender) {
        sender.sendMessage(LanguageAPI.translate(sender, "command.skill.help"));
    }
}
