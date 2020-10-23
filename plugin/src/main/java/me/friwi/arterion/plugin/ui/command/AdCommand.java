package me.friwi.arterion.plugin.ui.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.HelpCommand;
import co.aikar.commands.annotation.*;
import me.friwi.arterion.plugin.ArterionPlugin;
import me.friwi.arterion.plugin.permissions.Rank;
import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.player.ArterionPlayerUtil;
import me.friwi.arterion.plugin.util.language.api.LanguageAPI;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@CommandAlias("ad|werbung")
public class AdCommand extends BaseCommand {
    public Map<UUID, Long> adPlayers = new ConcurrentHashMap<>();
    private CommandManager commandManager;

    public AdCommand(CommandManager commandManager) {
        this.commandManager = commandManager;
    }

    @Default
    @CommandCompletion("")
    @Syntax("<message>")
    public void tell(CommandSender sender, String[] args) {
        ArterionPlayer ep = ArterionPlayerUtil.get((Player) sender);

        if (!ep.getRank().isHigherOrEqualThan(Rank.PREMIUM)) {
            ep.sendTranslation("premium.suggest");
            return;
        }

        if (args.length < 1) {
            this.help(sender);
            return;
        }

        ep.checkNotMuted(() -> {
            String msg = "";
            for (int i = 0; i < args.length; i++) {
                msg += args[i] + (i < args.length - 1 ? " " : "");
            }

            int waitSeconds = this.commandManager.getPlugin().getFormulaManager().PLAYER_AD_DELAY.evaluateInt(ep);
            int goldNeeded = this.commandManager.getPlugin().getFormulaManager().PLAYER_AD_FEE.evaluateInt(ep);

            if (adPlayers.containsKey(ep.getBukkitPlayer().getUniqueId())) {
                if (adPlayers.get(ep.getBukkitPlayer().getUniqueId()) > System.currentTimeMillis() - waitSeconds * 1000) {
                    ep.sendTranslation("command.ad.wait", waitSeconds / 60);
                    return;
                }
            }

            String finalMsg = msg;
            ep.getBagMoneyBearer().addMoney(-goldNeeded, success -> {
                if (success) {
                    adPlayers.put(ep.getBukkitPlayer().getUniqueId(), System.currentTimeMillis());
                    for (Player p : ArterionPlugin.getOnlinePlayers()) {
                        ArterionPlayer target = ArterionPlayerUtil.get(p);
                        boolean ignored = !ep.getRank().isHigherTeam() && target.getPersistenceHolder().getIgnoredPlayers().contains(ep.getUUID());
                        if (!ignored) {
                            target.sendTranslation("line");
                            target.sendTranslation("command.ad.format", finalMsg, ep);
                            target.sendTranslation("line");
                        }
                    }
                } else {
                    ep.sendTranslation("command.ad.error", goldNeeded / 100f);
                }
            });
        });
    }

    @HelpCommand
    @Default
    public void help(CommandSender sender) {
        sender.sendMessage(LanguageAPI.translate(sender, "command.ad.help"));
    }
}
