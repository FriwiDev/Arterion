package me.friwi.arterion.plugin.ui.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.HelpCommand;
import co.aikar.commands.annotation.*;
import me.friwi.arterion.plugin.permissions.Permission;
import me.friwi.arterion.plugin.permissions.Rank;
import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.player.ArterionPlayerUtil;
import me.friwi.arterion.plugin.util.database.DatabaseObjectTask;
import me.friwi.arterion.plugin.util.database.entity.DatabasePlayer;
import me.friwi.arterion.plugin.util.language.api.LanguageAPI;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandAlias("rank|setrank|rang")
public class RankCommand extends BaseCommand {
    private CommandManager commandManager;

    public RankCommand(CommandManager commandManager) {
        this.commandManager = commandManager;
    }

    @Default
    @CommandCompletion("@players @rank")
    @Syntax("<player> <rank>")
    public void setPlayerRank(CommandSender sender, String player, String rank) {
        if (!Permission.getRank(sender).canAssignRanks()) {
            sender.sendMessage(LanguageAPI.translate(ArterionPlayerUtil.get((Player) sender), "command.notavailable"));
            return;
        }
        Rank r = null;
        try {
            r = Rank.valueOf(rank.toUpperCase());
        } catch (IllegalArgumentException e) {
            sender.sendMessage(LanguageAPI.translate(sender, "command.rank.notfound"));
            return;
        }
        Player p = Bukkit.getPlayer(player);
        if (p != null && p.isOnline()) {
            ArterionPlayer onlinePlayer = ArterionPlayerUtil.get(p);
            if (onlinePlayer != null) {
                onlinePlayer.setRank(this.commandManager.getPlugin(), sender, r);
                onlinePlayer.getPlayerScoreboard().updateAllPlayerRelations();
                return;
            }
        }

        Rank finalR = r;
        this.commandManager.getPlugin().getSchedulers().getDatabaseScheduler().executeInMyCircle(new DatabaseObjectTask<DatabasePlayer>(DatabasePlayer.class, "name", player) {

            @Override
            public void updateObject(DatabasePlayer databasePlayer) {
                databasePlayer.setRank(finalR);
            }

            @Override
            public void success() {
                sender.sendMessage(LanguageAPI.translate(sender, "command.rank.updated", player));
            }


            @Override
            public void fail() {
                sender.sendMessage(LanguageAPI.translate(sender, "command.rank.error"));
            }
        });
    }

    @HelpCommand
    @Default
    public void help(CommandSender sender) {
        sender.sendMessage(LanguageAPI.translate(sender, "command.rank.help"));
    }
}
