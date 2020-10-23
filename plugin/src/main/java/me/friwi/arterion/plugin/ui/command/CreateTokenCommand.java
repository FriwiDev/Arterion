package me.friwi.arterion.plugin.ui.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.HelpCommand;
import co.aikar.commands.annotation.*;
import me.friwi.arterion.plugin.ui.chat.FormattedChat;
import me.friwi.arterion.plugin.util.database.Database;
import me.friwi.arterion.plugin.util.database.DatabaseTask;
import me.friwi.arterion.plugin.util.database.entity.DatabaseLoginToken;
import me.friwi.arterion.plugin.util.language.api.LanguageAPI;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandAlias("createtoken")
public class CreateTokenCommand extends BaseCommand {
    public static final long ALLOWED_DELAY = 3 * 60 * 1000;

    private CommandManager commandManager;

    public CreateTokenCommand(CommandManager commandManager) {
        this.commandManager = commandManager;
    }

    @Default
    @CommandCompletion("")
    @Syntax("")
    public void website(CommandSender sender, String[] args) {
        new DatabaseTask() {
            DatabaseLoginToken token;

            @Override
            public boolean performTransaction(Database db) {
                token = new DatabaseLoginToken(((Player) sender).getUniqueId(), System.currentTimeMillis() + ALLOWED_DELAY);
                db.save(token);
                return true;
            }

            @Override
            public void onTransactionCommitOrRollback(boolean committed) {
                sender.sendMessage(LanguageAPI.translate(sender, "line"));
                FormattedChat.sendFormattedChat((Player) sender, LanguageAPI.translate(sender, "command.createtoken.msg", token.getTokenId().toString()));
                sender.sendMessage(LanguageAPI.translate(sender, "line"));
            }

            @Override
            public void onTransactionError() {
                sender.sendMessage(LanguageAPI.translate(sender, "command.createtoken.dberr"));
            }
        }.execute();
    }

    @HelpCommand
    @Default
    public void help(CommandSender sender) {
        sender.sendMessage(LanguageAPI.translate(sender, "command.createtoken.help"));
    }
}
