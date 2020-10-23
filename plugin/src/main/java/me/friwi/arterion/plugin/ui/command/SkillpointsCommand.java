package me.friwi.arterion.plugin.ui.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.HelpCommand;
import co.aikar.commands.annotation.*;
import me.friwi.arterion.plugin.ArterionPlugin;
import me.friwi.arterion.plugin.formula.ArterionFormula;
import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.player.ArterionPlayerUtil;
import me.friwi.arterion.plugin.ui.gui.ItemGUI;
import me.friwi.arterion.plugin.ui.gui.NamedItemUtil;
import me.friwi.arterion.plugin.util.language.api.LanguageAPI;
import me.friwi.arterion.plugin.util.scheduler.InternalTask;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

@CommandAlias("prestigepoints|skillpoints|skillpunkte|prestigepunkte")
public class SkillpointsCommand extends BaseCommand {
    @Default
    @CommandCompletion("")
    @Syntax("")
    public void skill(CommandSender sender, String args[]) {
        if (sender instanceof Player) {
            ArterionPlayer p = ArterionPlayerUtil.get((Player) sender);
            p.openGui(new ItemGUI(p, p.getTranslation("prestigepoints.title", p.getRemainingPrestigePoingts()), () -> {
                ItemStack[] stacks = new ItemStack[9];
                ArterionFormula f = ArterionPlugin.getInstance().getFormulaManager().PRESTIGE_ATTACK;
                double bonus = (f.evaluateDouble(p.getPointsAttack() + 1) - f.evaluateDouble(p.getPointsAttack())) * 100d;
                stacks[1] = NamedItemUtil.create(Material.IRON_SWORD, Math.max(p.getPointsAttack(), 1), p.getTranslation("prestigepoints.attack", p.getPointsAttack()), p.getTranslation("prestigepoints.attack.desc", bonus).split("\\n"));
                f = ArterionPlugin.getInstance().getFormulaManager().PRESTIGE_DEFENSE;
                bonus = (f.evaluateDouble(p.getPointsDefense() + 1) - f.evaluateDouble(p.getPointsDefense())) * 100d;
                stacks[3] = NamedItemUtil.create(Material.CHAINMAIL_CHESTPLATE, Math.max(p.getPointsAttack(), 1), p.getTranslation("prestigepoints.defense", p.getPointsDefense()), p.getTranslation("prestigepoints.defense.desc", bonus).split("\\n"));
                f = ArterionPlugin.getInstance().getFormulaManager().PRESTIGE_HEALTH;
                bonus = (f.evaluateDouble(p.getPointsHealth() + 1) - f.evaluateDouble(p.getPointsHealth()));
                stacks[5] = NamedItemUtil.create(Material.RED_ROSE, Math.max(p.getPointsAttack(), 1), p.getTranslation("prestigepoints.health", p.getPointsHealth()), p.getTranslation("prestigepoints.health.desc", bonus).split("\\n"));
                f = ArterionPlugin.getInstance().getFormulaManager().PRESTIGE_COOLDOWN;
                bonus = (f.evaluateDouble(p.getPointsCooldown() + 1) - f.evaluateDouble(p.getPointsCooldown())) * 100d;
                stacks[7] = NamedItemUtil.create(Material.WATCH, Math.max(p.getPointsAttack(), 1), p.getTranslation("prestigepoints.cooldown", p.getPointsCooldown()), p.getTranslation("prestigepoints.cooldown.desc", -bonus).split("\\n"));
                return stacks;
            }, ((clickType, i) -> {
                p.closeGui();
                if (p.getRemainingPrestigePoingts() <= 0) {
                    p.sendTranslation("prestigepoints.none");
                    return;
                }
                if (i == 1) {
                    if (p.getPointsAttack() >= 100) {
                        p.sendTranslation("prestigepoints.full");
                        return;
                    }
                    p.setPointsAttack(p.getPointsAttack() + 1, succ -> {
                        ArterionPlugin.getInstance().getSchedulers().getMainScheduler().executeInSpigotCircle(new InternalTask() {
                            @Override
                            public void run() {
                                p.getBukkitPlayer().playSound(p.getBukkitPlayer().getLocation(), Sound.LEVEL_UP, 0.8f, 1f);
                                skill(sender, args);
                            }
                        });
                    });
                } else if (i == 3) {
                    if (p.getPointsDefense() >= 100) {
                        p.sendTranslation("prestigepoints.full");
                        return;
                    }
                    p.setPointsDefense(p.getPointsDefense() + 1, succ -> {
                        ArterionPlugin.getInstance().getSchedulers().getMainScheduler().executeInSpigotCircle(new InternalTask() {
                            @Override
                            public void run() {
                                p.getBukkitPlayer().playSound(p.getBukkitPlayer().getLocation(), Sound.LEVEL_UP, 0.8f, 1f);
                                skill(sender, args);
                            }
                        });
                    });
                } else if (i == 5) {
                    if (p.getPointsHealth() >= 100) {
                        p.sendTranslation("prestigepoints.full");
                        return;
                    }
                    p.setPointsHealth(p.getPointsHealth() + 1, succ -> {
                        ArterionPlugin.getInstance().getSchedulers().getMainScheduler().executeInSpigotCircle(new InternalTask() {
                            @Override
                            public void run() {
                                p.getBukkitPlayer().playSound(p.getBukkitPlayer().getLocation(), Sound.LEVEL_UP, 0.8f, 1f);
                                skill(sender, args);
                            }
                        });
                    });
                } else {
                    if (p.getPointsCooldown() >= 100) {
                        p.sendTranslation("prestigepoints.full");
                        return;
                    }
                    p.setPointsCooldown(p.getPointsCooldown() + 1, succ -> {
                        ArterionPlugin.getInstance().getSchedulers().getMainScheduler().executeInSpigotCircle(new InternalTask() {
                            @Override
                            public void run() {
                                p.getBukkitPlayer().playSound(p.getBukkitPlayer().getLocation(), Sound.LEVEL_UP, 0.8f, 1f);
                                skill(sender, args);
                            }
                        });
                    });
                }
            })));
        }
    }

    @HelpCommand
    @Default
    public void help(CommandSender sender) {
        sender.sendMessage(LanguageAPI.translate(sender, "command.prestigepoints.help"));
    }
}
