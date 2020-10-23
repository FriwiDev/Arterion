package me.friwi.arterion.plugin.ui.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.HelpCommand;
import co.aikar.commands.annotation.*;
import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.player.ArterionPlayerUtil;
import me.friwi.arterion.plugin.ui.mod.packet.Packet01ModVersion;
import me.friwi.arterion.plugin.ui.mod.server.ModConnection;
import me.friwi.arterion.plugin.util.language.api.LanguageAPI;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandAlias("fixhud|fixmod|hud")
public class HudCommand extends BaseCommand {
    private CommandManager commandManager;

    public HudCommand(CommandManager commandManager) {
        this.commandManager = commandManager;
    }

    @Default
    @CommandCompletion("")
    @Syntax("")
    public void hud(CommandSender sender, String[] args) {
        if (sender instanceof Player) {
            ArterionPlayer ep = ArterionPlayerUtil.get((Player) sender);
            ep.getPlayerScoreboard().handleModPacket(new Packet01ModVersion(ModConnection.PROTOCOL_VERSION));
            ep.sendTranslation("command.hud.enabled");
        }
    }

    @HelpCommand
    @Default
    public void help(CommandSender sender) {
        sender.sendMessage(LanguageAPI.translate(sender, "command.hud.help"));
    }
}
