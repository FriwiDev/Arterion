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

@CommandAlias("quest|quests|auftrag|aufträge")
public class QuestsCommand extends BaseCommand {
    @Default
    @Syntax("")
    public void quests(CommandSender sender, String args[]) {
        if (args.length >= 1) {
            help(sender);
            return;
        }

        if (sender instanceof Player) {
            ArterionPlayer p = ArterionPlayerUtil.get((Player) sender);
            if (p.getQuest() == null) {
                p.sendTranslation("command.quests.noquest");
            } else {
                p.getQuest().print(p);
            }
        }
    }

    @HelpCommand
    @Default
    public void help(CommandSender sender) {
        sender.sendMessage(LanguageAPI.translate(sender, "command.quests.help"));
    }
}
