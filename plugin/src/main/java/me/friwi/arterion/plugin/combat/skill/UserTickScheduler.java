package me.friwi.arterion.plugin.combat.skill;

import me.friwi.arterion.plugin.ArterionPlugin;
import me.friwi.arterion.plugin.combat.classes.ClassEnum;
import me.friwi.arterion.plugin.formula.ArterionFormula;
import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.player.ArterionPlayerUtil;
import me.friwi.arterion.plugin.ui.progress.ProgressBar;
import me.friwi.arterion.plugin.util.scheduler.InternalTask;
import org.bukkit.entity.Player;

public class UserTickScheduler {
    private ArterionPlugin plugin;
    private int tick = 0;

    public UserTickScheduler(ArterionPlugin plugin) {
        this.plugin = plugin;
    }

    public void startScheduler() {
        this.plugin.getSchedulers().getMainScheduler().executeInSpigotCircleTimer(new InternalTask() {
            @Override
            public void run() {
                tick++;
                for (Player p : plugin.getOnlinePlayers()) {
                    ArterionPlayer ap = ArterionPlayerUtil.get(p);
                    if (ap != null) tickUser(ap);
                }
            }
        }, 1, 1);
    }

    public void tickUser(ArterionPlayer player) {
        //Mana regeneration
        if (tick % 20 == 0) {
            int regen = 0;
            ArterionFormula f = UserTickScheduler.this.plugin.getFormulaManager().SKILL_PLAYER_MANAREGEN.get(player.getSelectedClass().toString());
            if (f != null && f.isDeclared()) {
                regen = f.evaluateInt(player);
            } else {
                f = UserTickScheduler.this.plugin.getFormulaManager().SKILL_PLAYER_MANAREGEN.get("other");
                if (f != null && f.isDeclared()) {
                    regen = f.evaluateInt(player);
                }
            }
            player.addMana(regen);
        }

        //Update hotbar message
        if (tick % 4 == 0) {
            if (player.getSelectedClass() == null || player.getSelectedClass() == ClassEnum.NONE) {
                player.setHotbarMessage(this.plugin, player.getTranslation("hotbar.selectclass"));
            } else {
                if (player.usesMod()) {
                    player.setHotbarMessage(this.plugin, "");
                } else {
                    String manabar = ProgressBar.generate("\2479", (player.getMana() + 0f) / (player.getMaxMana() + 0f), 40);
                    String mana = String.valueOf(player.getMana());
                    String maxmana = String.valueOf(player.getMaxMana());
                    double gold = player.getBagMoneyBearer().getCachedMoney() / 100d;
                    String region = player.getRegion() == null ? "undefinded" : player.getRegion().getName(player.getLanguage());
                    player.setHotbarMessage(this.plugin, player.getTranslation("hotbar.info", manabar, mana, maxmana, gold, region, player.getHealth(), player.getMaxHealth()));
                }
            }
            player.getPlayerScoreboard().updateSidebar();
        }

        //Update skill discs
        if (tick % 4 == 2) {
            player.getSkillSlots().updateSkillDisks();
        }

        //Update compass targets
        if (tick % 4 == 1) {
            player.getBukkitPlayer().setCompassTarget(
                    player.getBukkitPlayer().getWorld().getBlockAt(
                            (int) player.getBukkitPlayer().getLocation().getX(),
                            0,
                            -12550820
                    ).getLocation()
            );
        }

        //Update potions
        player.getPotionTracker().updateTracking();
    }
}
