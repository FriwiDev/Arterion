package me.friwi.arterion.plugin.ui.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.HelpCommand;
import co.aikar.commands.annotation.*;
import com.google.common.collect.Lists;
import me.friwi.arterion.plugin.formula.ArterionFormula;
import me.friwi.arterion.plugin.permissions.Permission;
import me.friwi.arterion.plugin.permissions.Rank;
import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.player.ArterionPlayerUtil;
import me.friwi.arterion.plugin.util.evaluation.api.ReflectionBinding;
import me.friwi.arterion.plugin.util.language.api.LanguageAPI;
import me.friwi.arterion.plugin.util.language.translateables.CollectionTranslateable;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@CommandAlias("formula")
public class FormulaCommand extends BaseCommand {
    private CommandManager commandManager;

    public FormulaCommand(CommandManager commandManager) {
        this.commandManager = commandManager;
    }

    @Default
    @CommandCompletion("@formula")
    @Syntax("<formula> [new evaluation]")
    public void formula(CommandSender sender, String[] args) {
        if (!Permission.getRank(sender).isHigherOrEqualThan(Rank.ADMIN)) {
            sender.sendMessage(LanguageAPI.translate(ArterionPlayerUtil.get((Player) sender), "command.notavailable"));
            return;
        }

        if (args.length < 1) {
            this.help(sender);
            return;
        }

        ArterionPlayer ep = sender instanceof Player ? ArterionPlayerUtil.get((Player) sender) : null;

        String formula = args[0];

        if (args.length < 2) {
            if (ep == null) return;
            ArterionFormula form = this.commandManager.getPlugin().getFormulaManager().getFormula(formula.toLowerCase());
            if (form != null) {
                ReflectionBinding[] bindings = form.getBindings();
                ep.sendTranslation("command.formula.name", ep.getTranslation("formula." + form.getTranslateableKey().toLowerCase() + ".name"));
                ep.sendTranslation("command.formula.description", ep.getTranslation("formula." + form.getTranslateableKey().toLowerCase() + ".desc"));
                String tr = this.commandManager.getPlugin().getFormulaManager().getTextRepresentation(formula.toLowerCase());
                if (tr == null) tr = "\247onull";
                ep.sendTranslation("command.formula.current", tr);
                for (ReflectionBinding binding : bindings) {
                    ep.sendTranslation("binding." + binding.getName().toLowerCase());
                }
            } else {
                this.sendSuggestions(ep, formula);
            }
            return;
        }

        String evaluation = "";
        for (int i = 1; i < args.length; i++) {
            evaluation += args[i] + (i < args.length - 1 ? " " : "");
        }

        try {
            boolean exists = this.commandManager.getPlugin().getFormulaManager().updateFormula(formula.toLowerCase(), evaluation);
            if (exists) {
                if (ep != null) ep.sendTranslation("command.formula.updated");
                else
                    sender.sendMessage(LanguageAPI.getLanguage(LanguageAPI.DEFAULT_LANGUAGE).getTranslation("command.formula.updated").translate().getMessage());
            } else {
                if (ep != null) ep.sendTranslation("command.formula.notfound");
                else
                    sender.sendMessage(LanguageAPI.getLanguage(LanguageAPI.DEFAULT_LANGUAGE).getTranslation("command.formula.notfound").translate().getMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (ep != null) ep.sendTranslation("command.formula.error", e.getMessage());
        }
    }

    private void sendSuggestions(ArterionPlayer ep, String formula) {
        Set<String> formulas = this.commandManager.getPlugin().getFormulaManager().getAvailableFormulas();
        Set<String> nextPossible = new HashSet<>();
        for (String s : formulas) {
            if (s.startsWith(formula.toLowerCase())) {
                String next = s.substring(formula.length());
                if (!next.isEmpty() && next.charAt(0) == '.') next = next.substring(1);
                nextPossible.add(next.split("\\.")[0]);
            }
        }
        List<String> nextPossible1 = Lists.newLinkedList(nextPossible);
        Collections.sort(nextPossible1);
        CollectionTranslateable t = new CollectionTranslateable(nextPossible1);
        ep.sendTranslation("command.formula.suggestion");
        ep.sendMessage(t.getCaption(ep.getLanguage(), "\2477", "\247a"));
    }

    @HelpCommand
    @Default
    public void help(CommandSender sender) {
        sender.sendMessage(LanguageAPI.translate(sender, "command.formula.help"));
        if (sender instanceof Player) {
            this.sendSuggestions(ArterionPlayerUtil.get((Player) sender), "");
        }
    }
}
