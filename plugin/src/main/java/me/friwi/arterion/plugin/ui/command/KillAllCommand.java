package me.friwi.arterion.plugin.ui.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.HelpCommand;
import co.aikar.commands.annotation.Syntax;
import me.friwi.arterion.plugin.permissions.Permission;
import me.friwi.arterion.plugin.permissions.Rank;
import me.friwi.arterion.plugin.player.ArterionPlayerUtil;
import me.friwi.arterion.plugin.util.language.api.LanguageAPI;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.*;

@CommandAlias("killall")
public class KillAllCommand extends BaseCommand {
    @Default
    @Syntax("")
    public void killall(CommandSender sender, String args[]) {
        if (!Permission.getRank(sender).isHigherOrEqualThan(Rank.DEVELOPER)) {
            sender.sendMessage(LanguageAPI.translate(ArterionPlayerUtil.get((Player) sender), "command.notavailable"));
            return;
        }

        if (args.length >= 1) {
            help(sender);
            return;
        }

        if (sender instanceof Player) {
            for (Entity e : ((Player) sender).getWorld().getEntities()) {
                if (e instanceof LivingEntity && !(e instanceof Player) && !(e instanceof Villager) && !(e instanceof ArmorStand)) {
                    if (e instanceof Tameable) {
                        if (((Tameable) e).isTamed()) continue;
                    }
                    e.remove();
                }
            }
            sender.sendMessage(LanguageAPI.translate(sender, "command.killall.success"));
        }
    }

    @HelpCommand
    @Default
    public void help(CommandSender sender) {
        sender.sendMessage(LanguageAPI.translate(sender, "command.killall.help"));
    }
}
