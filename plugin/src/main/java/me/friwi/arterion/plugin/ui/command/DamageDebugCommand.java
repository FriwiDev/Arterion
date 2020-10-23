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

@CommandAlias("damagedebug|dmgdebug|dmgdbg|dmg")
public class DamageDebugCommand extends BaseCommand {
    @Default
    @Syntax("")
    public void listPlugins(CommandSender sender, String args[]) {
        if (args.length >= 1) {
            help(sender);
            return;
        }
        if (sender instanceof Player) {
            ArterionPlayer p = ArterionPlayerUtil.get((Player) sender);
            p.setDamageDebug(!p.isDamageDebug());
            if (p.isDamageDebug()) {
                p.sendTranslation("damage.debug.enabled");
            } else {
                p.sendTranslation("damage.debug.disabled");
            }
        }
    }

    @HelpCommand
    @Default
    public void help(CommandSender sender) {
        sender.sendMessage(LanguageAPI.translate(sender, "damage.debug.help"));
    }
}
