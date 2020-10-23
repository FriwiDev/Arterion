package me.friwi.arterion.plugin.ui.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.HelpCommand;
import co.aikar.commands.annotation.*;
import me.friwi.arterion.plugin.ArterionPlugin;
import me.friwi.arterion.plugin.permissions.TeleportPreconditions;
import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.player.ArterionPlayerUtil;
import me.friwi.arterion.plugin.ui.invite.InvitationHandler;
import me.friwi.arterion.plugin.util.language.api.LanguageAPI;
import me.friwi.arterion.plugin.util.scheduler.InternalTask;
import me.friwi.arterion.plugin.world.region.GuildRegion;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandAlias("tpa|tpanfrage|tpi|tpinvite")
public class TpaCommand extends BaseCommand {
    private CommandManager commandManager;

    public TpaCommand(CommandManager commandManager) {
        this.commandManager = commandManager;
    }

    @Default
    @CommandCompletion("@players")
    @Syntax("<player>")
    public void tpa(CommandSender sender, String[] args) {
        if (args.length < 1) {
            this.help(sender);
            return;
        }

        String player = args[0];

        Player p = Bukkit.getPlayer(player);
        if (p != null && p.isOnline() && !ArterionPlayerUtil.get(p).isVanished()) {
            ArterionPlayer from = ArterionPlayerUtil.get((Player) sender);
            ArterionPlayer to = ArterionPlayerUtil.get(p);
            if (to.getUUID().equals(from.getUUID())) {
                sender.sendMessage(LanguageAPI.translate(sender, "command.tpa.self"));
                return;
            }
            if (to.getPersistenceHolder().getIgnoredPlayers().contains(from.getUUID())) {
                sender.sendMessage(LanguageAPI.translate(sender, "command.tpa.error"));
                return;
            }
            int money = 0;
            if (!from.hasNewbieProtection()) {
                money = ArterionPlugin.getInstance().getFormulaManager().TPA_PRICE.evaluateInt();
            }
            int finalMoney = money;
            from.getBagMoneyBearer().transferMoney(money, null, succ -> {
                if (!succ) {
                    from.sendTranslation("command.tpa.money", finalMoney / 100d);
                } else {
                    ArterionPlugin.getInstance().getInvitationSystem().invite(from, from.getUUID() + "tpa", to, () -> {
                        to.sendTranslation("command.tpa.invite_received", from);
                    }, new InvitationHandler() {
                        @Override
                        public void onAccept(ArterionPlayer player) {
                            from.sendTranslation("command.tpa.invite_accept", to);
                            to.sendTranslation("command.tpa.you_invite_accept", from);
                            ArterionPlugin.getInstance().getSchedulers().getMainScheduler().executeInSpigotCircleTimer(new InternalTask() {
                                int seconds = 60;

                                @Override
                                public void run() {
                                    if (!from.getBukkitPlayer().isOnline()) {
                                        cancel();
                                        to.sendTranslation("command.tpa.loggedout", from);
                                        return;
                                    }
                                    if (!to.getBukkitPlayer().isOnline()) {
                                        cancel();
                                        from.sendTranslation("command.tpa.loggedout", to);
                                        return;
                                    }
                                    String tp = TeleportPreconditions.canTeleport(from, false);
                                    if (to.isInTemporaryWorld()) tp = "arena";
                                    if (to.getRegion() instanceof GuildRegion) {
                                        GuildRegion r = ((GuildRegion) to.getRegion());
                                        if (r.getGuild().isInLocalFight() && r.getGuild().getLocalFight().getDefender().equals(r.getGuild())) {
                                            tp = "fight";
                                        }
                                    }
                                    if (tp != null) {
                                        cancel();
                                        from.sendTranslation("combat.incombat." + tp);
                                        from.sendTranslation("command.tpa.youcombat", to);
                                        to.sendTranslation("command.tpa.combat", from);
                                        from.getBagMoneyBearer().transferMoney(-finalMoney, null, succ -> {
                                        });
                                        return;
                                    }
                                    if (seconds <= 0) {
                                        cancel();
                                        from.sendTranslation("command.tpa.youtp", to);
                                        to.sendTranslation("command.tpa.tp", from);
                                        from.getBukkitPlayer().teleport(to.getBukkitPlayer());
                                        from.setHealth(20);
                                        from.setMana(0);
                                        from.getPlayerScoreboard().updateHealth();
                                    } else if (seconds % 10 == 0) {
                                        from.sendTranslation("command.tpa.youremaining", to, seconds);
                                        to.sendTranslation("command.tpa.remaining", from, seconds);
                                    }
                                    seconds--;
                                }
                            }, 0, 20);
                        }

                        @Override
                        public void onTimeout(ArterionPlayer player) {
                            from.sendTranslation("command.tpa.invite_timeout", to);
                            to.sendTranslation("command.tpa.you_invite_timeout", from);
                            from.getBagMoneyBearer().transferMoney(-finalMoney, null, succ -> {
                            });
                        }

                        @Override
                        public void onDeny(ArterionPlayer player) {
                            from.sendTranslation("command.tpa.invite_deny", to);
                            to.sendTranslation("command.tpa.you_invite_deny", from);
                            from.getBagMoneyBearer().transferMoney(-finalMoney, null, succ -> {
                            });
                        }
                    });
                }
            });
        } else {
            sender.sendMessage(LanguageAPI.translate(sender, "command.tpa.error"));
            return;
        }
    }

    @HelpCommand
    @Default
    public void help(CommandSender sender) {
        sender.sendMessage(LanguageAPI.translate(sender, "command.tpa.help"));
    }
}
