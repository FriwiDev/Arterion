package me.friwi.arterion.plugin.ui.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.HelpCommand;
import co.aikar.commands.annotation.*;
import me.friwi.arterion.plugin.permissions.Permission;
import me.friwi.arterion.plugin.permissions.Rank;
import me.friwi.arterion.plugin.player.ArterionPlayerUtil;
import me.friwi.arterion.plugin.util.language.api.LanguageAPI;
import me.friwi.arterion.plugin.world.villager.CustomVillagerUtil;
import me.friwi.arterion.plugin.world.villager.VillagerType;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandAlias("villager")
public class VillagerCommand extends BaseCommand {
    private CommandManager commandManager;

    public VillagerCommand(CommandManager commandManager) {
        this.commandManager = commandManager;
    }

    @Default
    @CommandCompletion("@villagertype")
    @Syntax("<villagerType>")
    public void setSpawn(Player sender, String[] args) {
        if (!Permission.getRank(sender).isHigherOrEqualThan(Rank.ADMIN)) {
            sender.sendMessage(LanguageAPI.translate(ArterionPlayerUtil.get((Player) sender), "command.notavailable"));
            return;
        }
        if (args.length < 1) {
            this.help(sender);
            return;
        }
        VillagerType r = null;
        try {
            r = VillagerType.valueOf(args[0].toUpperCase());
        } catch (IllegalArgumentException e) {
            sender.sendMessage(LanguageAPI.translate(sender, "command.villager.notfound"));
            return;
        }
        CustomVillagerUtil.spawnVillager(sender.getLocation().clone(), r);
        sender.sendMessage(LanguageAPI.translate(sender, "command.villager.success"));
    }

    @HelpCommand
    public void help(CommandSender sender) {
        sender.sendMessage(LanguageAPI.translate(sender, "command.villager.help"));
    }
}
