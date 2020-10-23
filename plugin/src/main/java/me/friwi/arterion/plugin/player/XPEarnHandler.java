package me.friwi.arterion.plugin.player;

import me.friwi.arterion.plugin.ArterionPlugin;
import me.friwi.arterion.plugin.combat.gamemode.capturepoint.CapturePoints;

public class XPEarnHandler {
    public static void earnXP(ArterionPlayer player, int xp) {
        earnXP(player, xp, true);
    }

    public static void earnXP(ArterionPlayer player, int xp, boolean applyBoosts) {
        double alreadyAppliedBoost = 1;
        if (applyBoosts) {
            if (ArterionPlugin.getInstance().getArterionConfig().boostExpire > System.currentTimeMillis()) {
                alreadyAppliedBoost = 2;
            }
            xp *= alreadyAppliedBoost;
            if (player.getGuild() != null && CapturePoints.GRAVE_RUIN.getClaimedBy() != null && player.getGuild().equals(CapturePoints.GRAVE_RUIN.getClaimedBy())) {
                xp *= ArterionPlugin.getInstance().getFormulaManager().CAPTUREPOINT_GRAVERUIN_XP_MULTIPLIER.evaluateDouble();
                alreadyAppliedBoost *= ArterionPlugin.getInstance().getFormulaManager().CAPTUREPOINT_GRAVERUIN_XP_MULTIPLIER.evaluateDouble();
            }
        }
        if (player.getGuild() != null && player.getGuild().isXpShare()) {
            xp *= ArterionPlugin.getInstance().getFormulaManager().GUILD_XP_MULTIPLIER.evaluateFloat(player.getGuild());
            int lvlablecount = 0;
            for (ArterionPlayer p : player.getGuild().getOnlineMembers()) {
                if (p.getLevel() < PlayerLevelCalculator.getMaxLevel() || p.getPrestigeLevel() < PlayerPrestigeLevelCalculator.getMaxLevel())
                    lvlablecount++;
            }
            if (lvlablecount == 0) return;
            xp /= lvlablecount;
            for (ArterionPlayer p : player.getGuild().getOnlineMembers()) {
                if (p.getLevel() < PlayerLevelCalculator.getMaxLevel() || p.getPrestigeLevel() < PlayerPrestigeLevelCalculator.getMaxLevel())
                    p.addXP(xp, alreadyAppliedBoost);
            }
        } else if (player.getGroup() != null && player.getGroup().isXpShare()) {
            xp *= ArterionPlugin.getInstance().getFormulaManager().GROUP_XP_MULTIPLIER.evaluateFloat(player.getGroup());
            xp /= player.getGroup().getPlayerCount();
            player.getGroup().getLeader().addXP(xp);
            for (ArterionPlayer p : player.getGroup().getMembers()) {
                p.addXP(xp, alreadyAppliedBoost);
            }
        } else {
            player.addXP(xp, alreadyAppliedBoost);
        }
    }
}
