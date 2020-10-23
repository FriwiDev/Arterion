package me.friwi.arterion.plugin.ui.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.HelpCommand;
import co.aikar.commands.annotation.*;
import me.friwi.arterion.plugin.permissions.Permission;
import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.player.ArterionPlayerUtil;
import me.friwi.arterion.plugin.sanctions.SanctionType;
import me.friwi.arterion.plugin.util.database.Database;
import me.friwi.arterion.plugin.util.database.DatabaseTask;
import me.friwi.arterion.plugin.util.database.entity.DatabasePlayer;
import me.friwi.arterion.plugin.util.language.api.LanguageAPI;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandAlias("unmute")
public class UnmuteCommand extends BaseCommand {
    private CommandManager commandManager;

    public UnmuteCommand(CommandManager commandManager) {
        this.commandManager = commandManager;
    }

    @Default
    @CommandCompletion("@players")
    @Syntax("<player>")
    public void unmute(CommandSender sender, String[] args) {
        if (!Permission.getRank(sender).canMute()) {
            sender.sendMessage(LanguageAPI.translate(ArterionPlayerUtil.get((Player) sender), "command.notavailable"));
            return;
        }

        if (args.length < 1) {
            this.help(sender);
            return;
        }

        String player = args[0];

        Player p = Bukkit.getPlayer(player);
        if (p != null && p.isOnline()) {
            ArterionPlayer onlinePlayer = ArterionPlayerUtil.get(p);
            onlinePlayer.sendTranslation("chat.unmute");
        }

        ArterionPlayer ep = ArterionPlayerUtil.get((Player) sender);

        new DatabaseTask() {
            boolean fail = false;

            @Override
            public boolean performTransaction(Database db) {
                DatabasePlayer affected = db.findOneByColumn(DatabasePlayer.class, "name", player);
                if (affected == null) {
                    fail = true;
                } else {
                    commandManager.getPlugin().getSanctionManager().pardon(affected.getUuid(), SanctionType.MUTE);
                }
                return true;
            }

            @Override
            public void onTransactionCommitOrRollback(boolean committed) {
                if (fail) sender.sendMessage(LanguageAPI.translate(sender, "command.unmute.error"));
                else sender.sendMessage(LanguageAPI.translate(sender, "command.unmute.completed", player));
            }

            @Override
            public void onTransactionError() {
                sender.sendMessage(LanguageAPI.translate(sender, "command.unmute.error"));
            }
        }.execute();
    }

    @HelpCommand
    @Default
    public void help(CommandSender sender) {
        sender.sendMessage(LanguageAPI.translate(sender, "command.unmute.help"));
    }
}
