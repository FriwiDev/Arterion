package me.friwi.arterion.plugin.ui.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.HelpCommand;
import co.aikar.commands.annotation.Syntax;
import me.friwi.arterion.plugin.jobs.JobEnum;
import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.player.ArterionPlayerUtil;
import me.friwi.arterion.plugin.player.PlayerJobLevelCalculator;
import me.friwi.arterion.plugin.ui.gui.ItemGUI;
import me.friwi.arterion.plugin.ui.gui.NamedItemUtil;
import me.friwi.arterion.plugin.ui.progress.ProgressBar;
import me.friwi.arterion.plugin.util.language.api.LanguageAPI;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.LinkedList;
import java.util.List;

@CommandAlias("jobs|job|berufe|beruf")
public class JobsCommand extends BaseCommand {
    @Default
    @Syntax("")
    public void jobs(CommandSender sender, String args[]) {
        if (args.length >= 1) {
            help(sender);
            return;
        }

        if (sender instanceof Player) {
            ArterionPlayer player = ArterionPlayerUtil.get((Player) sender);
            player.openGui(new ItemGUI(player, player.getTranslation("command.jobs.title"), () -> {
                ItemStack[] stacks = new ItemStack[9];
                int ind = 1;
                for (JobEnum job : JobEnum.values()) {
                    List<String> list = new LinkedList<>();
                    if (player.getJobLevel(job) >= PlayerJobLevelCalculator.getMaxLevel()) {
                        list.add(player.getTranslation("command.jobs.output2b", PlayerJobLevelCalculator.getMaxLevel()));
                    } else {
                        int current = PlayerJobLevelCalculator.getCurrentOverflowFromXP(player.getJobXp(job));
                        int max = PlayerJobLevelCalculator.getXPNeededForLevel(player.getJobLevel(job));
                        float perc = (current + 0f) / (max + 0f);
                        String bar = ProgressBar.generate(ChatColor.DARK_GREEN.toString(), perc, 35);
                        list.add(player.getTranslation("command.jobs.output2a", player.getJobLevel(job), current, max, bar));
                    }
                    list.addAll(formatTextWithLimit(job.getDescription(player), "\2477", 30));
                    stacks[ind] = NamedItemUtil.create(job.getJobMaterial(), "\2477" + job.getName(player.getLanguage()), list);
                    ind += 2;
                }
                return stacks;
            }, ((clickType, integer) -> {
            })));
        }
    }

    @HelpCommand
    @Default
    public void help(CommandSender sender) {
        sender.sendMessage(LanguageAPI.translate(sender, "command.jobs.help"));
    }

    public List<String> formatTextWithLimit(String text, String prefix, int limit) {
        List<String> ret = new LinkedList<>();
        String[] desc = text.split(" ");
        String s = "";
        for (String x : desc) {
            if (s.length() + x.length() < limit) s += x + " ";
            else {
                ret.add(prefix + s);
                s = "";
                s += x + " ";
            }
        }
        if (s.length() > 0) ret.add(prefix + s);
        return ret;
    }
}
