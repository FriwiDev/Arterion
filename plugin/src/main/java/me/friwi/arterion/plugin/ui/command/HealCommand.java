package me.friwi.arterion.plugin.ui.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.HelpCommand;
import co.aikar.commands.annotation.*;
import me.friwi.arterion.plugin.permissions.Permission;
import me.friwi.arterion.plugin.permissions.Rank;
import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.player.ArterionPlayerUtil;
import me.friwi.arterion.plugin.util.language.api.LanguageAPI;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandAlias("heal|heilen")
public class HealCommand extends BaseCommand {
    @Default
    @CommandCompletion("@players")
    @Syntax("<player>")
    public void heal(CommandSender sender, String args[]) {
        if (!Permission.getRank(sender).isHigherOrEqualThan(Rank.MODERATOR)) {
            sender.sendMessage(LanguageAPI.translate(ArterionPlayerUtil.get((Player) sender), "command.notavailable"));
            return;
        }

        if (args.length >= 2) {
            help(sender);
            return;
        }

        if (args.length == 0 && sender instanceof Player) {
            ArterionPlayer p = ArterionPlayerUtil.get((Player) sender);
            if (p.getBukkitPlayer().getHealth() > 0) p.getBukkitPlayer().setHealth(p.getBukkitPlayer().getMaxHealth());
            p.getBukkitPlayer().setFoodLevel(20);
            p.getBukkitPlayer().setSaturation(20);
            p.sendTranslation("command.heal.success");
            p.getPlayerScoreboard().updateHealth();
        } else {
            if (args.length != 1) {
                help(sender);
                return;
            }
            Player x = Bukkit.getPlayer(args[0]);
            if (x == null || !x.isOnline()) {
                sender.sendMessage(LanguageAPI.translate(sender, "command.heal.fail"));
                return;
            }
            ArterionPlayer p = ArterionPlayerUtil.get(x);
            if (p.getBukkitPlayer().getHealth() > 0) p.getBukkitPlayer().setHealth(p.getBukkitPlayer().getMaxHealth());
            p.getBukkitPlayer().setFoodLevel(20);
            p.getBukkitPlayer().setSaturation(20);
            p.sendTranslation("command.heal.success");
            sender.sendMessage(LanguageAPI.translate(sender, "command.heal.success_other", p));
            p.getPlayerScoreboard().updateHealth();
        }
    }

    @HelpCommand
    @Default
    public void help(CommandSender sender) {
        sender.sendMessage(LanguageAPI.translate(sender, "command.heal.help"));
    }
}
