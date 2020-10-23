package me.friwi.arterion.plugin.ui.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.HelpCommand;
import co.aikar.commands.annotation.*;
import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.player.ArterionPlayerUtil;
import me.friwi.arterion.plugin.tutorial.TutorialEnum;
import me.friwi.arterion.plugin.util.language.api.LanguageAPI;
import me.friwi.arterion.plugin.world.region.BankRegion;
import me.friwi.arterion.plugin.world.region.SpawnRegion;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandAlias("tutorial")
public class TutorialCommand extends BaseCommand {
    private CommandManager commandManager;

    public TutorialCommand(CommandManager commandManager) {
        this.commandManager = commandManager;
    }

    @Default
    @CommandCompletion("")
    @Syntax("")
    public void tutorial(CommandSender sender, String[] args) {
        ArterionPlayer ap = ArterionPlayerUtil.get((Player) sender);
        if (ap.getRegion() instanceof SpawnRegion || ap.getRegion() instanceof BankRegion) {
            int i = 0;
            if (args.length >= 1) {
                try {
                    i = Integer.parseInt(args[0]);
                } catch (NumberFormatException e) {
                    //We dont care, begin again
                }
            }
            if (i < 0) i = 0;
            else if (i >= TutorialEnum.values().length) {
                i = TutorialEnum.values().length - 1;
            }
            TutorialEnum.values()[i].sendToPlayer(ap);
        } else {
            ap.sendTranslation("command.tutorial.nospawn");
        }
    }

    @HelpCommand
    @Default
    public void help(CommandSender sender) {
        sender.sendMessage(LanguageAPI.translate(sender, "command.tutorial.help"));
    }
}
