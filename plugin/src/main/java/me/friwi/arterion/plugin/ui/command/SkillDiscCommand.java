package me.friwi.arterion.plugin.ui.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.HelpCommand;
import co.aikar.commands.annotation.Syntax;
import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.player.ArterionPlayerUtil;
import me.friwi.arterion.plugin.util.language.api.LanguageAPI;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandAlias("skilldisc|skilldisk|sd")
public class SkillDiscCommand extends BaseCommand {
    @Default
    @Syntax("")
    public void listPlugins(CommandSender sender, String args[]) {
        if (args.length >= 1) {
            help(sender);
            return;
        }

        if (sender instanceof Player) {
            ArterionPlayer p = ArterionPlayerUtil.get((Player) sender);
            if (p.getSkillSlots().hasSkillDisks()) {
                p.getSkillSlots().removeSkillDisks();
                sender.sendMessage(LanguageAPI.translate(sender, "command.skilldisc.remove"));
            } else {
                p.getSkillSlots().giveSkillDisks();
                sender.sendMessage(LanguageAPI.translate(sender, "command.skilldisc.give"));
            }
        }
    }

    @HelpCommand
    @Default
    public void help(CommandSender sender) {
        sender.sendMessage(LanguageAPI.translate(sender, "command.skilldisc.help"));
    }
}
