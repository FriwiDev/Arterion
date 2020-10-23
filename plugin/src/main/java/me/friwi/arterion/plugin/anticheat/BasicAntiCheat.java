package me.friwi.arterion.plugin.anticheat;

import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.player.ArterionPlayerUtil;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class BasicAntiCheat {
    //Hover
    int hoverCalls = 0;
    //Speed
    int speedTicks = 0;
    long lastSpeed = 0;
    long disableUntil = 0;
    //Last reported
    long lastReported = 0;
    private ArterionPlayer player;

    public BasicAntiCheat(ArterionPlayer player) {
        this.player = player;
    }

    public void check(PlayerMoveEvent evt) {
        if (lastReported + 10000 > System.currentTimeMillis()) return;
        if (System.currentTimeMillis() < disableUntil) {
            hoverCalls = 0;
            speedTicks = 0;
            lastSpeed = 0;
            return;
        }
        boolean isSpeeding = checkSpeed(evt);
        if (isSpeeding) {
            reportToStaff("SPEED");
            return;
        }
        boolean isHovering = checkHover(evt);
        if (isHovering) {
            reportToStaff("HOVER");
            return;
        }
    }

    public void reportToStaff(String type) {
        lastReported = System.currentTimeMillis();
        for (Player p : Bukkit.getOnlinePlayers()) {
            ArterionPlayer ap = ArterionPlayerUtil.get(p);
            if (ap != null && ap.isAnticheatDebug()) {
                ap.sendTranslation("acdebug.received", player, type);
            }
        }
    }

    private boolean checkHover(PlayerMoveEvent event) {
        int cc = hoverCalls;
        Player p = player.getBukkitPlayer();
        if (p.isFlying() || p.getGameMode() == GameMode.CREATIVE
                || p.getGameMode() == GameMode.SPECTATOR || p.isInsideVehicle()// || p.isSwimming()
                || p.getLocation().getBlock().isLiquid()) {
            return false;
        }
        Boolean onlyliquid = UtilBlock.evaluateOnlyLiquidSurroundingIgnoreAir(p.getLocation().getBlock(), true);
        if (onlyliquid) {
            return false;
        }
        Boolean onlyair = UtilBlock.evaluateOnlyAirSurrounding(p.getLocation().getBlock(), true);
        Double mpx = event.getFrom().getY() - event.getTo().getY();
        /*if (event.getTo().getY() == event.getFrom().getY() && !p.getLocation().getBlock().getRelative(BlockFace.DOWN).isLiquid()// && !p.isSwimming()
                && onlyair) {
            cc++;
        } else */
        if (mpx <= 0.007 && !p.getLocation().getBlock().getRelative(BlockFace.DOWN).isLiquid()
                && !p.getLocation().getBlock().getRelative(BlockFace.DOWN).isLiquid() //&& !p.isSwimming()
                && onlyair) {
            cc++;
        } /*else if (mpx >= -0.03 && mpx <= 0.07) {
            cc++;
        } */ else {
            if (cc > 0) {
                cc--;
            }
        }
        if (cc > 7) {
            hoverCalls = 2;

            return true;
        } else {
            hoverCalls = cc;
        }
        return false;
    }

    private boolean checkSpeed(PlayerMoveEvent event) {
        Integer Count = 0;
        Player p = player.getBukkitPlayer();
        if (p.isFlying() || p.getGameMode() == GameMode.CREATIVE
                || p.getGameMode() == GameMode.SPECTATOR || p.isInsideVehicle()) {
            return false;
        }
        double Offset = 0;
        double Limit = 0.35;
        if (lastSpeed != 0) {
            if (event.getFrom().getY() > event.getTo().getY()) {
                Offset = UtilMath.offset2d(event.getFrom(), event.getTo());
            } else {
                Offset = UtilMath.offset(event.getFrom(), event.getTo());
            }

            if (/*p.isGliding() ||*/
                    (event.getTo().getY() < event.getFrom().getY() && p.getLocation().getBlock().getRelative(BlockFace.DOWN).getType() == Material.SLIME_BLOCK)
                            || event.getTo().getY() < event.getFrom().getY() || p.isInsideVehicle()) {
                return false;
            }
            if (UtilBlock.onBlock(p)) {
                Limit = 0.56;
            }
            if (UtilBlock.onStairs(p)) {
                Limit = 0.77;
            }
            if (Limit < 0.77 && UtilBlock.getBlockAbove(p).getType() != Material.AIR) {
                Limit = 0.77;
            }
            Boolean ice = false, ice2 = false;
            Material below = p.getLocation().getBlock().getRelative(BlockFace.DOWN).getType();
            if (below == Material.ICE || below == Material.PACKED_ICE) {
                ice = true;
            }
            Material below2 = p.getLocation().getBlock().getRelative(0, -2, 0).getType();
            if (below2 == Material.ICE || below2 == Material.PACKED_ICE) {
                ice2 = true;
            }
            if (ice || ice2) {
                Limit += 2.0;
            }
            /*if (ice && (p.isGliding() || u.LastElytraFly() <= 650)) {
                Limit += 0.38D;
            }*/
            /*if (p.isSwimming()) {
                Limit += 0.15D;
            }*/
            for (PotionEffect e : p.getActivePotionEffects()) {
                if (e.getType().equals(PotionEffectType.SPEED)) {
                    if (UtilBlock.onStairs(p)) {
                        Limit += 0.14D * (e.getAmplifier() + 1);
                    } else if (UtilBlock.onBlock(p)) {
                        Limit += 0.08D * (e.getAmplifier() + 1);
                    } else {
                        Limit += 0.04D * (e.getAmplifier() + 1);
                    }
                } else if (e.getType().equals(PotionEffectType.JUMP)) {
                    Limit += 0.18D * (e.getAmplifier() + 1);
                }
            }
            if (Offset > Limit * 1.2 && lastSpeed + 200 > System.currentTimeMillis()) {
                Count = speedTicks + 2;
            } else {
                Count = 0;
            }
        }
        Boolean call = true;
        if (Count > 3) {
            speedTicks = 4;
            lastSpeed = System.currentTimeMillis();
            /*if (!UtilBlock.onBlock(p)) {
                call = true;
            }*/
            if (call) {
                return true;
            }
        } else {
            if (Count > 0)
                Count--;

            speedTicks = Count;
            lastSpeed = System.currentTimeMillis();
        }
        return false;
    }

    public void disableMovementChecksFor(long time) {
        this.disableUntil = System.currentTimeMillis() + time;
    }
}
