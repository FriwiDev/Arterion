package me.friwi.arterion.plugin.ui.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.HelpCommand;
import co.aikar.commands.annotation.Syntax;
import me.friwi.arterion.plugin.combat.classes.ClassEnum;
import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.player.ArterionPlayerUtil;
import me.friwi.arterion.plugin.util.language.api.LanguageAPI;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandAlias("unbind")
public class UnbindCommand extends BaseCommand {
    @Default
    @Syntax("")
    public void skill(CommandSender sender, String args[]) {
        if (args.length >= 1) {
            help(sender);
            return;
        }

        if (sender instanceof Player) {
            ArterionPlayer p = ArterionPlayerUtil.get((Player) sender);
            if (p.getSelectedClass() != ClassEnum.NONE && p.getSelectedClass() != null) {
                p.setRightClickSkill(p.getSelectedClass(), null, succ -> {
                    sender.sendMessage(LanguageAPI.translate(sender, "command.unbind.success"));
                });
            } else {
                sender.sendMessage(LanguageAPI.translate(sender, "command.unbind.noclass"));
            }
        }
    }

    @HelpCommand
    @Default
    public void help(CommandSender sender) {
        sender.sendMessage(LanguageAPI.translate(sender, "command.unbind.help"));
    }
}
