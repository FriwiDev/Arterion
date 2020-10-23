package me.friwi.arterion.plugin.ui.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.HelpCommand;
import co.aikar.commands.annotation.*;
import me.friwi.arterion.plugin.chat.ChatChannel;
import me.friwi.arterion.plugin.permissions.Permission;
import me.friwi.arterion.plugin.permissions.Rank;
import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.player.ArterionPlayerUtil;
import me.friwi.arterion.plugin.util.language.api.LanguageAPI;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandAlias("channel|ch|c")
public class ChannelCommand extends BaseCommand {
    private CommandManager commandManager;

    public ChannelCommand(CommandManager commandManager) {
        this.commandManager = commandManager;
    }

    @Default
    @CommandCompletion("@channel")
    @Syntax("<GLOBAL|LOCAL|GROUP|GUILD|SUPPORT|TEAM>")
    public void channel(CommandSender sender, String[] args) {
        if (args.length < 1) {
            this.help(sender);
            return;
        }

        Rank r = Permission.getRank(sender);

        ArterionPlayer ep = ArterionPlayerUtil.get((Player) sender);

        ChatChannel channel = null;
        try {
            channel = ChatChannel.valueOf(args[0].toUpperCase());
        } catch (IllegalArgumentException e) {
            ep.sendTranslation("command.channel.notfound");
            return;
        }

        if (channel == ChatChannel.GROUP) {
            if (ep.getGroup() == null) {
                ep.sendTranslation("command.channel.nogroup");
                return;
            }
        }

        if (channel == ChatChannel.GUILD) {
            if (ep.getGuild() == null) {
                ep.sendTranslation("command.channel.noguild");
                return;
            }
        }

        if (channel == ChatChannel.TEAM) {
            if (!r.isTeam()) {
                ep.sendTranslation("command.channel.noperm");
                return;
            }
        }

        ep.setChatChannel(channel);
    }

    @HelpCommand
    @Default
    public void help(CommandSender sender) {
        sender.sendMessage(LanguageAPI.translate(sender, "command.channel.help"));
    }
}
