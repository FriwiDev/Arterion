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

@CommandAlias("skills")
public class SkillsCommand extends BaseCommand {
    @Default
    @Syntax("")
    public void skills(CommandSender sender, String args[]) {
        if (args.length >= 1) {
            help(sender);
            return;
        }

        if (sender instanceof Player) {
            ArterionPlayer p = ArterionPlayerUtil.get((Player) sender);
            p.getSkillSlots().printSkills();
        }
    }

    @HelpCommand
    @Default
    public void help(CommandSender sender) {
        sender.sendMessage(LanguageAPI.translate(sender, "command.skills.help"));
    }
}
