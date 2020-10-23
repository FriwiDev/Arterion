package me.friwi.arterion.plugin.player;

import me.friwi.arterion.plugin.ArterionPlugin;

public class GoldEarnHandler {
    public static void earnGold(ArterionPlayer player, long gold) {
        if (player.getGuild() != null && player.getGuild().isGoldShare()) {
            gold *= ArterionPlugin.getInstance().getFormulaManager().GUILD_GOLD_MULTIPLIER.evaluateFloat(player.getGuild());
            gold /= player.getGuild().getOnlineMemberCount();
            for (ArterionPlayer p : player.getGuild().getOnlineMembers()) {
                p.getBagMoneyBearer().addMoney(gold, s -> {
                });
            }
        } else if (player.getGroup() != null && player.getGroup().isGoldShare()) {
            gold *= ArterionPlugin.getInstance().getFormulaManager().GROUP_GOLD_MULTIPLIER.evaluateFloat(player.getGroup());
            gold /= player.getGroup().getPlayerCount();
            player.getGroup().getLeader().getBagMoneyBearer().addMoney(gold, s -> {
            });
            for (ArterionPlayer p : player.getGroup().getMembers()) {
                p.getBagMoneyBearer().addMoney(gold, s -> {
                });
            }
        } else {
            player.getBagMoneyBearer().addMoney(gold, s -> {
            });
        }
    }
}
