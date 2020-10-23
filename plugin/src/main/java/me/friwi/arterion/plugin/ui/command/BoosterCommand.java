package me.friwi.arterion.plugin.ui.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.HelpCommand;
import co.aikar.commands.annotation.*;
import me.friwi.arterion.plugin.ArterionPlugin;
import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.player.ArterionPlayerUtil;
import me.friwi.arterion.plugin.ui.gui.ConfirmItemGUI;
import me.friwi.arterion.plugin.ui.gui.ItemGUI;
import me.friwi.arterion.plugin.ui.gui.NamedItemUtil;
import me.friwi.arterion.plugin.util.language.api.LanguageAPI;
import me.friwi.arterion.plugin.util.scheduler.InternalTask;
import me.friwi.arterion.plugin.util.time.TimeFormatUtil;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

@CommandAlias("booster|xpboost")
public class BoosterCommand extends BaseCommand {
    public static final long BOOST_DURATION = 60 * 60 * 1000;

    private CommandManager commandManager;

    public BoosterCommand(CommandManager commandManager) {
        this.commandManager = commandManager;
    }

    public static void scheduleBoosterExpireMessage() {
        if (ArterionPlugin.getInstance().getArterionConfig().boostExpire < System.currentTimeMillis()) return;
        ArterionPlugin.getInstance().getSchedulers().getMainScheduler().executeInMyCircleLater(new InternalTask() {

            @Override
            public void run() {
                LanguageAPI.broadcastMessage("line");
                LanguageAPI.broadcastMessage("booster.expire", ArterionPlugin.getInstance().getArterionConfig().currentBooster);
                LanguageAPI.broadcastMessage("line");
                for (Player p : ArterionPlugin.getOnlinePlayers()) {
                    p.playSound(p.getLocation(), Sound.NOTE_PIANO, 0.8f, 1f);
                }
            }
        }, (ArterionPlugin.getInstance().getArterionConfig().boostExpire - System.currentTimeMillis()) / 50);
    }

    @Default
    @CommandCompletion("")
    @Syntax("")
    public void booster(CommandSender sender, String[] args) {
        ArterionPlayer ep = ArterionPlayerUtil.get((Player) sender);

        ep.openGui(new ItemGUI(ep, ep.getTranslation("booster.gui.title"), () -> {
            ItemStack[] stacks = new ItemStack[18];
            if (ArterionPlugin.getInstance().getArterionConfig().boostExpire > System.currentTimeMillis()) {
                String left = TimeFormatUtil.formatSeconds((ArterionPlugin.getInstance().getArterionConfig().boostExpire - System.currentTimeMillis()) / 1000);
                String user = ArterionPlugin.getInstance().getArterionConfig().currentBooster;
                stacks[4] = NamedItemUtil.create(Material.EYE_OF_ENDER, ep.getTranslation("booster.gui.active"),
                        ep.getTranslation("booster.gui.booster", user),
                        ep.getTranslation("booster.gui.expire", left));
                if (ep.getPersistenceHolder().getRemainingBoosters() <= 0) {
                    stacks[13] = NamedItemUtil.create(Material.CLAY, ep.getTranslation("booster.gui.nonowned"), ep.getTranslation("booster.gui.encouragebuy"), ep.getTranslation("booster.gui.encouragebuy2"));
                } else {
                    stacks[13] = NamedItemUtil.create(Material.INK_SACK, ep.getPersistenceHolder().getRemainingBoosters(), (byte) 1, ep.getTranslation("booster.gui.cantusenow"), ep.getTranslation("booster.gui.cantusenow2"));
                }
            } else {
                stacks[4] = NamedItemUtil.create(Material.BARRIER, ep.getTranslation("booster.gui.noactive"), ep.getTranslation("booster.gui.encourage"), ep.getTranslation("booster.gui.encourage2"));
                if (ep.getPersistenceHolder().getRemainingBoosters() <= 0) {
                    stacks[13] = NamedItemUtil.create(Material.CLAY, ep.getTranslation("booster.gui.nonowned"), ep.getTranslation("booster.gui.encouragebuy"), ep.getTranslation("booster.gui.encouragebuy2"));
                } else {
                    stacks[13] = NamedItemUtil.create(Material.ENDER_CHEST, ep.getPersistenceHolder().getRemainingBoosters(), ep.getTranslation("booster.gui.usenow"));
                }
            }
            return stacks;
        }, ((clickType, i) -> {
            if (i == 13) {
                if (ep.getPersistenceHolder().getRemainingBoosters() > 0 && ArterionPlugin.getInstance().getArterionConfig().boostExpire < System.currentTimeMillis()) {
                    ep.openGui(new ConfirmItemGUI(ep, ep.getTranslation("booster.gui.confirm"), () -> {
                        ep.closeGui();
                        ep.updateInDB(dbp -> dbp.setRemainingBoosters(dbp.getRemainingBoosters() - 1), succ -> {
                            if (succ) {
                                ArterionPlugin.getInstance().getArterionConfig().boostExpire = System.currentTimeMillis() + BOOST_DURATION;
                                ArterionPlugin.getInstance().getArterionConfig().currentBooster = ep.getName();
                                ArterionPlugin.getInstance().saveConfig();
                                LanguageAPI.broadcastMessage("line");
                                LanguageAPI.broadcastMessage("booster.use", ep);
                                LanguageAPI.broadcastMessage("line");
                                for (Player p : ArterionPlugin.getOnlinePlayers()) {
                                    p.playSound(p.getLocation(), Sound.LEVEL_UP, 0.8f, 1f);
                                }
                                scheduleBoosterExpireMessage();
                            }
                        });
                    }, () -> {
                        booster(sender, args);
                    }));
                }
            }
        })));
    }

    @HelpCommand
    @Default
    public void help(CommandSender sender) {
        sender.sendMessage(LanguageAPI.translate(sender, "command.booster.help"));
    }
}
