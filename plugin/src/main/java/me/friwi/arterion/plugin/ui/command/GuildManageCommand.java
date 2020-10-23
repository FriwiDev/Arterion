package me.friwi.arterion.plugin.ui.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.HelpCommand;
import co.aikar.commands.annotation.*;
import me.friwi.arterion.plugin.ArterionPlugin;
import me.friwi.arterion.plugin.guild.Guild;
import me.friwi.arterion.plugin.permissions.Permission;
import me.friwi.arterion.plugin.permissions.Rank;
import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.player.ArterionPlayerUtil;
import me.friwi.arterion.plugin.util.language.api.LanguageAPI;
import me.friwi.arterion.plugin.world.item.GuildblockItem;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandAlias("admguildmanage")
public class GuildManageCommand extends BaseCommand {
    private CommandManager commandManager;

    public GuildManageCommand(CommandManager commandManager) {
        this.commandManager = commandManager;
    }

    @Default
    @CommandCompletion("@nothing @guilds @players")
    @Syntax("<promote|disband|give> <guild> [player]")
    public void guildmanage(CommandSender sender, String[] args) {
        if (!Permission.getRank(sender).isHigherOrEqualThan(Rank.DEVELOPER)) {
            sender.sendMessage(LanguageAPI.translate(ArterionPlayerUtil.get((Player) sender), "command.notavailable"));
            return;
        }

        if (args.length < 2) {
            this.help(sender);
            return;
        }

        String action = args[0];
        String guild = args[1];
        String player = args.length >= 3 ? args[2] : null;

        ArterionPlayer ep = ArterionPlayerUtil.get((Player) sender);

        Player p = null;
        if (player != null) {
            p = Bukkit.getPlayer(player);
            if (!p.isOnline()) {
                p = null;
            }
        }

        Guild g = ArterionPlugin.getInstance().getGuildManager().getGuildByName(guild);
        if (g == null) {
            sender.sendMessage(LanguageAPI.translate(sender, "command.admguildmanage.fail"));
            return;
        }

        if (action.equalsIgnoreCase("promote")) {
            if (p == null) {
                sender.sendMessage(LanguageAPI.translate(sender, "command.admguildmanage.fail"));
                return;
            }
            g.setLeader(ArterionPlayerUtil.get(p).getPersistenceHolder(), succ -> {
                if (succ) {
                    sender.sendMessage(LanguageAPI.translate(sender, "command.admguildmanage.success"));
                } else {
                    sender.sendMessage(LanguageAPI.translate(sender, "command.admguildmanage.fail"));
                }
            });
        } else if (action.equalsIgnoreCase("disband")) {
            g.deleteGuild(false);
            sender.sendMessage(LanguageAPI.translate(sender, "command.admguildmanage.success"));
        } else if (action.equalsIgnoreCase("give")) {
            if (p == null) {
                sender.sendMessage(LanguageAPI.translate(sender, "command.admguildmanage.fail"));
                return;
            }
            GuildblockItem item = new GuildblockItem(g);
            p.getInventory().addItem(item.toItemStack());
            sender.sendMessage(LanguageAPI.translate(sender, "command.admguildmanage.success"));
        } else {
            sender.sendMessage(LanguageAPI.translate(sender, "command.admguildmanage.fail"));
        }
    }

    @HelpCommand
    @Default
    public void help(CommandSender sender) {
        sender.sendMessage(LanguageAPI.translate(sender, "command.admguildmanage.help"));
    }
}
