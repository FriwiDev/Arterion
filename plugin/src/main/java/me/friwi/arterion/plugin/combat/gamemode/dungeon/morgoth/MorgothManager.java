package me.friwi.arterion.plugin.combat.gamemode.dungeon.morgoth;

import me.friwi.arterion.plugin.ArterionPlugin;
import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.util.language.api.LanguageAPI;
import me.friwi.arterion.plugin.util.scheduler.InternalTask;
import me.friwi.arterion.plugin.world.item.MorgothDungeonKeyItem;

public class MorgothManager {
    private static boolean inFight = false;
    private static long portalCooldown = 0;
    private static boolean morgothBlocked = false;
    private static MorgothDungeonFight fight = null;
    private static boolean openingSoon = false;

    public static void reset() {
        inFight = false;
        portalCooldown = 0;
        openingSoon = true;
        fight = new MorgothDungeonFight(() -> {
            openingSoon = false;
            MorgothPortal.enable();
            LanguageAPI.broadcastMessage("dungeon.morgoth.portalopen");
        });
    }

    public static void onBeginFight() {
        fight = null;
        inFight = true;
        MorgothPortal.disable();
        LanguageAPI.broadcastMessage("dungeon.morgoth.portalclose");
    }

    public static void onEndFight() {
        inFight = false;
        portalCooldown = System.currentTimeMillis() + ArterionPlugin.getInstance().getFormulaManager().DUNGEON_MORGOTH_COOLDOWN.evaluateInt();
        ArterionPlugin.getInstance().getSchedulers().getMainScheduler().executeInSpigotCircleTimer(new InternalTask() {
            @Override
            public void run() {
                if (portalCooldown == 0) {
                    cancel();
                    return;
                }
                if (portalCooldown < System.currentTimeMillis()) {
                    portalCooldown = 0;
                    cancel();
                    reset();
                    return;
                }
            }
        }, 20, 20);
    }

    public static boolean isInFight() {
        return inFight;
    }

    public static long getPortalCooldown() {
        return portalCooldown - System.currentTimeMillis();
    }

    public static boolean isMorgothBlocked() {
        return morgothBlocked;
    }

    public static void setMorgothBlocked(boolean morgothBlocked) {
        MorgothManager.morgothBlocked = morgothBlocked;
    }

    public static void onPlayerPassPortal(ArterionPlayer player) {
        if (isMorgothBlocked() || fight == null) {
            MorgothDungeonKeyItem.restoreKey(player);
            return;
        }
        fight.joinPlayer(player);
    }

    public static boolean isOpeningSoon() {
        return openingSoon;
    }
}
