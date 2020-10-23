package me.friwi.arterion.plugin.combat.gamemode.dungeon.morgoth;

import com.darkblade12.particleeffect.ParticleEffect;
import me.friwi.arterion.mobs.MorgothSummoner;
import me.friwi.arterion.plugin.ArterionPlugin;
import me.friwi.arterion.plugin.combat.hook.Hooks;
import me.friwi.arterion.plugin.combat.skill.ParticleEffectUtil;
import me.friwi.arterion.plugin.combat.skill.Skill;
import me.friwi.arterion.plugin.listener.CreatureSpawnListener;
import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.player.ArterionPlayerUtil;
import me.friwi.arterion.plugin.util.language.api.LanguageAPI;
import me.friwi.arterion.plugin.util.scheduler.InternalTask;
import me.friwi.recordable.RecordingCreator;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class MorgothBoss {
    private MorgothSummoner summoner;

    private Map<ArterionPlayer, Double> damagePerPlayer = new HashMap<>();
    private int shootCounter;

    public MorgothBoss(Location loc, Runnable onDefeat) {
        CreatureSpawnListener.isSpawningWithCommand = true;
        CreatureSpawnListener.forceSpawn = true;
        this.summoner = new MorgothSummoner(loc, getMaxHealth(), this::onShoot);
        CreatureSpawnListener.isSpawningWithCommand = false;
        CreatureSpawnListener.forceSpawn = false;
        this.summoner.applyCustomAttributes();
        Hooks.DAMAGE_RECEIVE_FROM_ENTITY_HOOK.subscribe(this.summoner.getHorse(), dmg -> {
            trackDamage(dmg.getFirstValue(), dmg.getSecondValue());
            dmg.setSecondValue(dmg.getSecondValue() * 0.1);
            return dmg;
        });
        Hooks.DAMAGE_RECEIVE_FROM_ENTITY_HOOK.subscribe(this.summoner.getSkeleton(), dmg -> {
            trackDamage(dmg.getFirstValue(), dmg.getSecondValue());
            dmg.setSecondValue(dmg.getSecondValue() * 0.1);
            return dmg;
        });
        Hooks.RELATIVE_DAMAGE_DEALT_HOOK.subscribe(this.summoner.getSkeleton(), evt -> {
            evt.setSecondValue(ArterionPlugin.getInstance().getFormulaManager().DUNGEON_MORGOTH_DAMAGE.evaluateDouble());
            return evt;
        });
        summoner.getHorse().setRemoveWhenFarAway(false);
        summoner.getSkeleton().setRemoveWhenFarAway(false);
        summoner.getHorse().setCustomName(LanguageAPI.translate("dungeon.morgoth.bossname"));
        summoner.getHorse().setCustomNameVisible(false);
        summoner.getSkeleton().setCustomName(LanguageAPI.translate("dungeon.morgoth.bossname"));
        summoner.getSkeleton().getEquipment().setItemInHand(new ItemStack(Material.BOW));
        int tickInterval = 10;
        ArterionPlugin.getInstance().getSchedulers().getMainScheduler().executeInSpigotCircleTimer(new InternalTask() {
            int tick = 0;

            @Override
            public void run() {
                if (summoner.getSkeleton().isDead()) {
                    cancel();
                    onDefeat.run();
                    return;
                }
                ParticleEffect.FLAME.display(1f, 1f, 1f, 0f, 20, summoner.getSkeleton().getLocation(), Skill.PARTICLE_RANGE);
                tick += tickInterval;
                if (tick % 200 == 0) {
                    for (int i = 0; i < 7; i++) {
                        double angle = Math.PI * 2 / 7 * i;
                        Block loc = summoner.getHorse().getLocation().clone().add(Math.sin(angle) * 3, 0, Math.cos(angle) * 3).getBlock();
                        Class<? extends Entity> spawnClass = i <= 2 ? Skeleton.class : Spider.class;
                        for (int a = 0; a < 5; a++) {
                            if (loc.getType().isSolid() || loc.getRelative(BlockFace.UP).getType().isSolid())
                                loc = loc.getRelative(BlockFace.UP);
                            else break;
                        }
                        if (!loc.getType().isSolid() && !loc.getRelative(BlockFace.UP).getType().isSolid()) {
                            loc.getWorld().spawn(loc.getLocation().add(0.5, 0, 0.5), spawnClass);
                            ParticleEffect.FLAME.display(1f, 1f, 1f, 0f, 20, loc.getLocation().add(0, 1, 0), Skill.PARTICLE_RANGE);
                        }
                    }
                    summoner.getHorse().getWorld().playSound(summoner.getHorse().getLocation(), Sound.SKELETON_HURT, 0.8f, 1f);
                }
            }
        }, tickInterval, tickInterval);
    }

    private void trackDamage(Entity firstValue, Double secondValue) {
        if (firstValue instanceof Player) {
            ArterionPlayer ap = ArterionPlayerUtil.get((Player) firstValue);
            Double prev = damagePerPlayer.get(ap);
            if (prev == null) {
                damagePerPlayer.put(ap, secondValue);
            } else {
                damagePerPlayer.put(ap, prev + secondValue);
            }
        }
    }

    public double getMaxHealth() {
        return ArterionPlugin.getInstance().getFormulaManager().DUNGEON_MORGOTH_MAXHP.evaluateDouble();
    }

    public void onShoot(LivingEntity entity, float v) {
        this.summoner.shootArrow(entity, v);
        shootCounter++;
        if (shootCounter % 6 == 0) {
            arkanHook();
        } else if (shootCounter % 6 == 3) {
            rapidFire(entity, v);
        }
    }

    public void arkanHook() {
        Player entity = null;
        double maxdist = 0;
        for (Player p : summoner.getSkeleton().getWorld().getPlayers()) {
            if (p.getGameMode() == GameMode.CREATIVE || p.getGameMode() == GameMode.SPECTATOR || ArterionPlayerUtil.get(p).isVanished())
                continue;
            double dist = p.getLocation().distance(summoner.getSkeleton().getLocation());
            if (dist > maxdist) {
                maxdist = dist;
                entity = p;
            }
        }
        if (entity == null) return;
        for (Player p : summoner.getSkeleton().getWorld().getPlayers()) {
            ArterionPlayer ap = ArterionPlayerUtil.get(p);
            ap.sendTranslation("dungeon.morgoth.arcanhook", ap.getLanguage().translateObject(entity));
        }
        for (RecordingCreator r : ArterionPlugin.getInstance().getRecordingManager().getActiveRecordings()) {
            if (r.isLocationRelevant(summoner.getSkeleton().getLocation())) {
                r.addChat(LanguageAPI.getLanguage(LanguageAPI.DEFAULT_LANGUAGE).getTranslation("dungeon.replay.morgoth.arcanhook").translate(LanguageAPI.getLanguage(LanguageAPI.DEFAULT_LANGUAGE).translateObject(entity)).getMessage());
            }
        }
        //Draw line
        Location from = entity.getLocation().clone().add(0, 1, 0);
        Location to = summoner.getSkeleton().getLocation().clone().add(0, 1, 0);
        ParticleEffectUtil.drawLine(from, to, 3, locc -> ParticleEffect.REDSTONE.display(new ParticleEffect.OrdinaryColor(200, 0, 200), locc, Skill.PARTICLE_RANGE));

        //Draw ender effects
        entity.getLocation().getWorld().playEffect(entity.getLocation(), Effect.ENDER_SIGNAL, 0);
        summoner.getHorse().getWorld().playEffect(summoner.getSkeleton().getLocation(), Effect.ENDER_SIGNAL, 0);

        //Play sounds
        entity.getLocation().getWorld().playSound(entity.getLocation(), Sound.ENDERMAN_TELEPORT, 1f, 1f);
        summoner.getHorse().getWorld().playSound(summoner.getSkeleton().getLocation(), Sound.ENDERMAN_TELEPORT, 1f, 1f);

        //Teleport player
        Location tp = summoner.getSkeleton().getLocation().clone();
        tp.setDirection(entity.getLocation().getDirection());
        entity.teleport(tp);
    }

    public void rapidFire(LivingEntity entity, float v) {
        if (entity == null) return;
        for (Player p : summoner.getSkeleton().getWorld().getPlayers()) {
            ArterionPlayer ap = ArterionPlayerUtil.get(p);
            ap.sendTranslation("dungeon.morgoth.rapidfire", ap.getLanguage().translateObject(entity));
        }
        for (RecordingCreator r : ArterionPlugin.getInstance().getRecordingManager().getActiveRecordings()) {
            if (r.isLocationRelevant(summoner.getSkeleton().getLocation())) {
                r.addChat(LanguageAPI.getLanguage(LanguageAPI.DEFAULT_LANGUAGE).getTranslation("dungeon.replay.morgoth.rapidfire").translate(LanguageAPI.getLanguage(LanguageAPI.DEFAULT_LANGUAGE).translateObject(entity)).getMessage());
            }
        }
        summoner.getHorse().getWorld().playSound(summoner.getHorse().getLocation(), Sound.GHAST_SCREAM, 0.8f, 1f);
        ArterionPlugin.getInstance().getSchedulers().getMainScheduler().executeInSpigotCircleTimer(new InternalTask() {
            int arrows = 1;

            @Override
            public void run() {
                if (arrows >= 15 || summoner.getSkeleton().isDead() || entity.isDead()) {
                    cancel();
                    return;
                }
                arrows++;
                summoner.shootArrow(entity, v);
            }
        }, 3, 3);
    }

    public Map<ArterionPlayer, Double> getDamagePerPlayer() {
        return damagePerPlayer;
    }

    public double getHealth() {
        return summoner.getSkeleton().getHealth();
    }

    public boolean isDead() {
        return summoner.getSkeleton().isDead();
    }
}
