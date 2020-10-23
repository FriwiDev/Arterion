package me.friwi.arterion.plugin.ui.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.HelpCommand;
import co.aikar.commands.annotation.*;
import me.friwi.arterion.plugin.combat.Combat;
import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.player.ArterionPlayerUtil;
import me.friwi.arterion.plugin.util.language.api.LanguageAPI;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandAlias("pay|send")
public class PayCommand extends BaseCommand {
    private CommandManager commandManager;

    public PayCommand(CommandManager commandManager) {
        this.commandManager = commandManager;
    }

    @Default
    @CommandCompletion("@players")
    @Syntax("<player> <amount>")
    public void pay(CommandSender sender, String[] args) {
        if (args.length != 2) {
            this.help(sender);
            return;
        }

        ArterionPlayer ep = ArterionPlayerUtil.get((Player) sender);

        //Check for combat
        String combat = Combat.isPlayerInCombat(ep);
        if (combat != null) {
            Combat.sendInCombatMessage(ep, combat);
            return;
        }

        String player = args[0];
        int amount = 0;
        try {
            double i = Math.round(Double.parseDouble(args[1].replace(",", ".")) * 100);
            if (i > Integer.MAX_VALUE || i < Integer.MIN_VALUE) {
                ep.sendTranslation("command.pay.nonumber");
                return;
            }
            amount = (int) i; //Translate to cents
        } catch (NumberFormatException e) {
            ep.sendTranslation("command.pay.nonumber");
            return;
        }

        if (amount <= 0) {
            ep.sendTranslation("command.pay.negativenumber");
            return;
        }

        Player p = Bukkit.getPlayer(player);
        if (p != null && p.isOnline() && !ArterionPlayerUtil.get(p).isVanished()) {
            ArterionPlayer onlinePlayer = ArterionPlayerUtil.get(p);
            if (onlinePlayer.equals(ep)) {
                ep.sendTranslation("command.pay.self");
            } else {
                int finalAmount = amount;
                ep.getBagMoneyBearer().transferMoney(amount, onlinePlayer.getBagMoneyBearer(), success -> {
                    if (success) {
                        ep.sendTranslation("command.pay.sent", onlinePlayer, finalAmount / 100f);
                        onlinePlayer.sendTranslation("command.pay.received", ep, finalAmount / 100f);
                    } else {
                        ep.sendTranslation("command.pay.error");
                    }
                });
            }
        } else {
            sender.sendMessage(LanguageAPI.translate(sender, "command.pay.notonline"));
            return;
        }
    }

    @HelpCommand
    @Default
    public void help(CommandSender sender) {
        sender.sendMessage(LanguageAPI.translate(sender, "command.pay.help"));
    }
}
