package me.friwi.arterion.plugin.ui.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.HelpCommand;
import co.aikar.commands.annotation.*;
import me.friwi.arterion.plugin.combat.classes.ClassEnum;
import me.friwi.arterion.plugin.combat.skill.Skill;
import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.player.ArterionPlayerUtil;
import me.friwi.arterion.plugin.util.language.api.LanguageAPI;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandAlias("bind")
public class BindCommand extends BaseCommand {
    @Default
    @CommandCompletion("@skill")
    @Syntax("<skill>")
    public void skill(CommandSender sender, String args[]) {
        if (args.length < 1) {
            help(sender);
            return;
        }

        String build = "";
        for (String s : args) build += s + " ";

        if (sender instanceof Player) {
            ArterionPlayer p = ArterionPlayerUtil.get((Player) sender);
            if (p.getSelectedClass() != ClassEnum.NONE && p.getSelectedClass() != null) {
                Skill s = p.getSkillSlots().getSkill(build);
                if (s == null) {
                    sender.sendMessage(LanguageAPI.translate(sender, "command.bind.noskill"));
                } else {
                    p.setRightClickSkill(p.getSelectedClass(), s.getSkillType(), succ -> {
                        sender.sendMessage(LanguageAPI.translate(sender, "command.bind.success"));
                    });
                }
            } else {
                sender.sendMessage(LanguageAPI.translate(sender, "command.bind.noclass"));
            }
        }
    }

    @HelpCommand
    @Default
    public void help(CommandSender sender) {
        sender.sendMessage(LanguageAPI.translate(sender, "command.bind.help"));
    }
}
