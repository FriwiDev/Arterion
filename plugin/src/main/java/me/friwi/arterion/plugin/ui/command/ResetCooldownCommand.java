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

@CommandAlias("resetcd")
public class ResetCooldownCommand extends BaseCommand {
    @Default
    @CommandCompletion("@players")
    @Syntax("<player>")
    public void heal(CommandSender sender, String args[]) {
        if (!Permission.getRank(sender).isHigherOrEqualThan(Rank.DEVELOPER)) {
            sender.sendMessage(LanguageAPI.translate(ArterionPlayerUtil.get((Player) sender), "command.notavailable"));
            return;
        }

        if (args.length >= 2) {
            help(sender);
            return;
        }

        if (args.length == 0 && sender instanceof Player) {
            ArterionPlayer p = ArterionPlayerUtil.get((Player) sender);
            p.getSkillSlots().setAllOffCooldown();
            p.setMana(p.getMaxMana());
            p.sendTranslation("command.resetcd.success");
            p.getPlayerScoreboard().updateHealth();
        } else {
            if (args.length != 1) {
                help(sender);
                return;
            }
            Player x = Bukkit.getPlayer(args[0]);
            if (x == null || !x.isOnline()) {
                sender.sendMessage(LanguageAPI.translate(sender, "command.resetcd.fail"));
                return;
            }
            ArterionPlayer p = ArterionPlayerUtil.get(x);
            p.getSkillSlots().setAllOffCooldown();
            p.setMana(p.getMaxMana());
            p.sendTranslation("command.resetcd.success");
            sender.sendMessage(LanguageAPI.translate(sender, "command.resetcd.success_other", p));
            p.getPlayerScoreboard().updateHealth();
        }
    }

    @HelpCommand
    @Default
    public void help(CommandSender sender) {
        sender.sendMessage(LanguageAPI.translate(sender, "command.resetcd.help"));
    }
}
