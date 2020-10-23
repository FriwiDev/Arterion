package me.friwi.arterion.plugin.combat.damage;

import com.darkblade12.particleeffect.ParticleEffect;
import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.collect.ImmutableMap;
import de.tr7zw.changeme.nbtapi.NBTCompound;
import de.tr7zw.nbtinjector.NBTInjector;
import me.friwi.arterion.plugin.ArterionPlugin;
import me.friwi.arterion.plugin.combat.classes.ClassEnum;
import me.friwi.arterion.plugin.combat.damage.handlers.*;
import me.friwi.arterion.plugin.combat.gamemode.capturepoint.CapturePoints;
import me.friwi.arterion.plugin.combat.hook.Hooks;
import me.friwi.arterion.plugin.combat.hook.Tuple;
import me.friwi.arterion.plugin.combat.skill.Skill;
import me.friwi.arterion.plugin.formula.ArterionFormula;
import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.player.ArterionPlayerUtil;
import me.friwi.arterion.plugin.player.XPEarnHandler;
import me.friwi.arterion.plugin.stats.StatType;
import me.friwi.arterion.plugin.util.scheduler.InternalTask;
import me.friwi.arterion.plugin.world.item.GoldItem;
import me.friwi.arterion.plugin.world.item.MorgothDungeonKeyItem;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.projectiles.ProjectileSource;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

public class DamageManager {
    public static final double BOSS_MULTIPLIER = 10;
    public static final double BOSS_DAMAGE_MULTIPLIER = 2.5;

    private static final Function<? super Double, Double> ZERO = Functions.constant(-0.0);
    private static final long DAMAGER_DELAY_TIME = 30000;

    private DamageHandler[] handlers = new DamageHandler[EntityDamageEvent.DamageCause.values().length];
    private DefaultDamageHandler defaultDamageHandler = new DefaultDamageHandler();

    private Map<LivingEntity, Tuple<Long, ArterionPlayer>> lastDamager = new HashMap<>();

    private Random random = new Random();

    private boolean globalDisable = false;
    private Method arrowGetHandle;
    private Field arrowFromPlayer;

    public DamageManager() {
        //When handling new damage types make sure to also calculate their armor in ArmorDamageReductionCalculator
        //Else default armor is used!
        //ENTITY_ATTACK
        setHandler(EntityDamageEvent.DamageCause.ENTITY_ATTACK, new EntityAttackDamageHandler());
        //PROJECTILE
        setHandler(EntityDamageEvent.DamageCause.PROJECTILE, new ProjectileDamageHandler());
        //FALL
        setHandler(EntityDamageEvent.DamageCause.FALL, new FallDamageHandler());
        //FIRE_TICK
        setHandler(EntityDamageEvent.DamageCause.FIRE_TICK, new FireTickDamageHandler());
        //ENTITY_EXPLOSION
        setHandler(EntityDamageEvent.DamageCause.ENTITY_EXPLOSION, new EntityExplosionDamageHandler());
        //POISON
        setHandler(EntityDamageEvent.DamageCause.POISON, new PoisonDamageHandler());
        //POISON
        setHandler(EntityDamageEvent.DamageCause.WITHER, new WitherDamageHandler());
        //THORNS
        setHandler(EntityDamageEvent.DamageCause.THORNS, new ThornsDamageHandler());
        //CUSTOM
        setHandler(EntityDamageEvent.DamageCause.CUSTOM, new CustomDamageHandler());
    }

    public static void applyDamage(EntityDamageEvent evt, double damage, boolean trueDamage) {
        //Apply damage dealt hook
        if (evt instanceof EntityDamageByEntityEvent) {
            Entity damager = ((EntityDamageByEntityEvent) evt).getDamager();
            if (damager instanceof Projectile && ((Projectile) damager).getShooter() instanceof Entity) {
                damager = (Entity) ((Projectile) damager).getShooter();
            }
            damage = Hooks.RELATIVE_DAMAGE_DEALT_HOOK.execute(damager, new Tuple<>(evt.getEntity(), damage)).getSecondValue();
        }

        if (!trueDamage) {
            //Reduce damage by armor
            if (evt.getEntity() instanceof HumanEntity) {
                double armorpercentage = ArmorDamageReductionCalculator.calculateDamageReduction((HumanEntity) evt.getEntity(), evt.getCause());
                armorpercentage /= 100; //Scale from percentage to 0-1
                if (armorpercentage > 0.8) armorpercentage = 0.8;
                if (armorpercentage < 0) armorpercentage = 0;
                double multiplier = 1 - armorpercentage; //Remaining damage is 1 minus reduction
                damage *= multiplier; //Apply to our damage
            }

            //Reduce damage by potion effects
            if (evt.getEntity() instanceof LivingEntity) {
                double potionpercentage = PotionDamageReductionCalculator.calculateDamageReduction((LivingEntity) evt.getEntity(), evt.getCause());
                potionpercentage /= 100; //Scale from percentage to 0-1
                if (potionpercentage > 1) potionpercentage = 1;
                double multiplier = 1 - potionpercentage; //Remaining damage is 1 minus reduction
                damage *= multiplier; //Apply to our damage
            }
        }

        //Apply damage dealt hook
        if (evt instanceof EntityDamageByEntityEvent) {
            Entity damager = ((EntityDamageByEntityEvent) evt).getDamager();
            if (damager instanceof Projectile && ((Projectile) damager).getShooter() instanceof Entity) {
                damager = (Entity) ((Projectile) damager).getShooter();
            }
            damage = Hooks.ABSOLUTE_DAMAGE_DEALT_HOOK.execute(damager, new Tuple<>(evt.getEntity(), damage)).getSecondValue();
            damage = Hooks.DAMAGE_RECEIVE_FROM_ENTITY_HOOK.execute(evt.getEntity(), new Tuple<>(damager, damage)).getSecondValue();
        }

        //Apply final damage hook
        damage = Hooks.FINAL_DAMAGE_RECEIVED_HOOK.execute(evt.getEntity(), damage);

        //Stats
        if (evt instanceof EntityDamageByEntityEvent) {
            Entity damager = ((EntityDamageByEntityEvent) evt).getDamager();
            if (damager instanceof Projectile && ((Projectile) damager).getShooter() instanceof Entity) {
                damager = (Entity) ((Projectile) damager).getShooter();
            }
            if (damager instanceof Player) {
                ArterionPlayer dmp = ArterionPlayerUtil.get((Player) damager);
                double finalDamage = damage;
                if (evt.getEntity() instanceof Player) {
                    dmp.trackStatistic(StatType.DAMAGE_DEALT_PLAYERS, 0, v -> (long) (v + finalDamage));
                } else if (evt.getEntity() instanceof LivingEntity) {
                    dmp.trackStatistic(StatType.DAMAGE_DEALT_MOBS, evt.getEntityType().ordinal(), v -> (long) (v + finalDamage));
                }
            }
        }
        if (evt.getEntity() instanceof Player) {
            ArterionPlayer dmp = ArterionPlayerUtil.get((Player) evt.getEntity());
            double finalDamage = damage;
            dmp.trackStatistic(StatType.DAMAGE_RECEIVED, evt.getCause().ordinal(), v -> (long) (v + finalDamage));
        }

        //Apply it
        debug(evt, damage);
        if (evt.getEntity() instanceof Player) {
            ArterionPlayer player = ArterionPlayerUtil.get((Player) evt.getEntity());
            if (player != null) {
                damage *= ((Player) evt.getEntity()).getMaxHealth(); //Multiply with minecraft max health
                damage /= player.getMaxHealth(); //Divide by our max health
            }
        }
        for (EntityDamageEvent.DamageModifier mod : EntityDamageEvent.DamageModifier.values()) {
            if (evt.isApplicable(mod)) evt.setDamage(mod, 0);
        }
        evt.setDamage(EntityDamageEvent.DamageModifier.BASE, damage);

        if (damage <= 0) evt.setCancelled(true);
    }

    public static void applyDefaultDamage(EntityDamageEvent evt) {
        double damage = evt.getDamage();
        //Transform from mc damage values to our higher damage values
        if (evt.getEntity() instanceof LivingEntity) {
            //Fetch the original max health of the entity
            double originalMaxHealth = 1;
            if (evt.getEntity() instanceof Player) {
                originalMaxHealth = 20;
            } else {
                NBTCompound compound = NBTInjector.getNbtData(evt.getEntity());
                if (compound != null && compound.hasKey("original_max")) {
                    originalMaxHealth = compound.getDouble("original_max");
                } else {
                    originalMaxHealth = ((LivingEntity) evt.getEntity()).getMaxHealth();
                }
            }
            damage /= originalMaxHealth;
            if (evt.getEntity() instanceof Player) {
                ArterionPlayer player = ArterionPlayerUtil.get((Player) evt.getEntity());
                if (player != null) {
                    damage *= player.getMaxHealth();
                } else {
                    damage *= ((Player) evt.getEntity()).getMaxHealth();
                }
            } else {
                damage *= ((LivingEntity) evt.getEntity()).getMaxHealth();
            }
        }
        applyDamage(evt, damage, false);
    }

    public static void debug(EntityDamageEvent evt, double dmg) {
        String damage = String.valueOf(Math.round(dmg * 100) / 100d);
        if (evt instanceof EntityDamageByEntityEvent) {
            Entity dmgr = ((EntityDamageByEntityEvent) evt).getDamager();
            if (dmgr instanceof Projectile) {
                ProjectileSource s = ((Projectile) dmgr).getShooter();
                if (s != null && s instanceof Entity) dmgr = (Entity) s;
            }

            if (evt.getEntity() instanceof Player) {
                ArterionPlayer p = ArterionPlayerUtil.get((Player) evt.getEntity());
                if (p.isDamageDebug()) {
                    p.sendTranslation("damage.debug.receivedfrom", p.getLanguage().translateObject(dmgr), damage, evt.getCause().toString());
                }
            }
            if (dmgr instanceof Player) {
                ArterionPlayer p = ArterionPlayerUtil.get((Player) dmgr);
                if (p.isDamageDebug()) {
                    p.sendTranslation("damage.debug.dealtto", p.getLanguage().translateObject(evt.getEntity()), damage, evt.getCause().toString());
                }
            }
        } else {
            if (evt.getEntity() instanceof Player) {
                ArterionPlayer p = ArterionPlayerUtil.get((Player) evt.getEntity());
                if (p.isDamageDebug()) {
                    p.sendTranslation("damage.debug.received", damage, evt.getCause().toString());
                }
            }
        }
    }

    public static String entityTypeToString(Entity damager) {
        if (damager instanceof Slime) {
            int size = ((Slime) damager).getSize();
            if (size == 1 || size == 2 || size == 4) {
                return damager.getType().toString() + "_" + size;
            }
            return "other";
        }
        if (damager instanceof MagmaCube) {
            int size = ((MagmaCube) damager).getSize();
            if (size == 1 || size == 2 || size == 4) {
                return damager.getType().toString() + "_" + size;
            }
            return "other";
        }
        if (damager instanceof Guardian) {
            if (((Guardian) damager).isElder()) return EntityType.GUARDIAN + "_ELDER";
            return EntityType.GUARDIAN.toString();
        }
        if (damager instanceof Creeper) {
            if (((Creeper) damager).isPowered()) return EntityType.CREEPER + "_CHARGED";
            return EntityType.CREEPER.toString();
        }
        return damager.getType().toString();
    }

    private void setHandler(EntityDamageEvent.DamageCause cause, DamageHandler handler) {
        this.handlers[cause.ordinal()] = handler;
    }

    public boolean hasHandler(EntityDamageEvent.DamageCause cause) {
        return this.handlers[cause.ordinal()] != null;
    }

    public void setLastDamager(LivingEntity l, ArterionPlayer p) {
        lastDamager.put(l, new Tuple<>(System.currentTimeMillis(), p));
    }

    public ArterionPlayer getLastDamager(LivingEntity e) {
        ArterionPlayer ret = null;
        Iterator<Map.Entry<LivingEntity, Tuple<Long, ArterionPlayer>>> ld = lastDamager.entrySet().iterator();
        while (ld.hasNext()) {
            Map.Entry<LivingEntity, Tuple<Long, ArterionPlayer>> current = ld.next();
            if (e.equals(current.getKey())) {
                if (current.getValue().getFirstValue() + DAMAGER_DELAY_TIME >= System.currentTimeMillis()) {
                    ret = current.getValue().getSecondValue();
                }
                ld.remove();
            } else {
                if (current.getKey().isDead() || current.getValue().getFirstValue() + DAMAGER_DELAY_TIME < System.currentTimeMillis())
                    ld.remove();
            }
        }
        return ret;
    }

    /**
     * Not used for players
     *
     * @param entity
     * @param reason
     */
    public void applyMaxHealth(LivingEntity entity, CreatureSpawnEvent.SpawnReason reason) {
        if (entity instanceof Player) {
            return;
        }
        NBTCompound compound = NBTInjector.getNbtData(entity);
        boolean boss = compound.hasKey("art_boss") ? compound.getBoolean("art_boss") : false;
        if (!boss && (entity instanceof Skeleton || entity instanceof Spider || entity instanceof Zombie) && !ArterionPlugin.getInstance().getTemporaryWorldManager().isTemporaryWorld(entity.getWorld())) {
            boss = Math.random() < ArterionPlugin.getInstance().getFormulaManager().DMG_BOSS_CHANCE.evaluateDouble();
        }
        if (entity instanceof Wither) {
            Hooks.FINAL_DAMAGE_RECEIVED_HOOK.subscribe(entity, dmg -> dmg / 2.5); //300 hp to 750 effective hp
        }
        if (entity instanceof EnderDragon) {
            Hooks.FINAL_DAMAGE_RECEIVED_HOOK.subscribe(entity, dmg -> dmg / 50); //200 hp to effective 10000 hp
        }
        if (boss) {
            entity.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Short.MAX_VALUE, 0));
            if (entity instanceof Skeleton) {
                entity.getEquipment().setHelmet(new ItemStack(Material.DIAMOND_HELMET, 1, (byte) 100));
                entity.getEquipment().setChestplate(new ItemStack(Material.DIAMOND_CHESTPLATE, 1, (byte) 100));
                entity.getEquipment().setLeggings(new ItemStack(Material.DIAMOND_LEGGINGS, 1, (byte) 100));
                entity.getEquipment().setBoots(new ItemStack(Material.DIAMOND_BOOTS, 1, (byte) 100));
                ItemStack bow = new ItemStack(Material.BOW, 1, (byte) 100);
                bow.addEnchantment(Enchantment.ARROW_FIRE, 1);
                entity.getEquipment().setItemInHand(bow);
                entity.getEquipment().setHelmetDropChance(0.01f);
                entity.getEquipment().setChestplateDropChance(0.01f);
                entity.getEquipment().setLeggingsDropChance(0.01f);
                entity.getEquipment().setBootsDropChance(0.01f);
                entity.getEquipment().setItemInHandDropChance(0.01f);
            }
            if (entity instanceof Zombie) {
                entity.getEquipment().setHelmet(new ItemStack(Material.DIAMOND_HELMET, 1, (byte) 100));
                entity.getEquipment().setChestplate(new ItemStack(Material.DIAMOND_CHESTPLATE, 1, (byte) 100));
                entity.getEquipment().setLeggings(new ItemStack(Material.DIAMOND_LEGGINGS, 1, (byte) 100));
                entity.getEquipment().setBoots(new ItemStack(Material.DIAMOND_BOOTS, 1, (byte) 100));
                ItemStack sword = new ItemStack(Material.DIAMOND_SWORD, 1, (byte) 100);
                sword.addEnchantment(Enchantment.FIRE_ASPECT, 1);
                entity.getEquipment().setItemInHand(sword);
                entity.getEquipment().setHelmetDropChance(0.01f);
                entity.getEquipment().setChestplateDropChance(0.01f);
                entity.getEquipment().setLeggingsDropChance(0.01f);
                entity.getEquipment().setBootsDropChance(0.01f);
                entity.getEquipment().setItemInHandDropChance(0.01f);
            }
            ArterionPlugin.getInstance().getSchedulers().getMainScheduler().executeInSpigotCircleTimer(new InternalTask() {
                @Override
                public void run() {
                    if (entity.isDead() || !entity.isValid()) {
                        cancel();
                        return;
                    }
                    ParticleEffect.FLAME.display(0.8f, 0.8f, 0.8f, 0f, 15, entity.getLocation().clone().add(0, entity.getEyeHeight(), 0), Skill.PARTICLE_RANGE);
                }
            }, 10, 10);
            Hooks.ABSOLUTE_DAMAGE_DEALT_HOOK.subscribe(entity, tuple -> {
                tuple.setSecondValue(tuple.getSecondValue() * BOSS_DAMAGE_MULTIPLIER);
                return tuple;
            });
        }
        compound.setBoolean("art_boss", boss);
        if (!compound.hasKey("original_max"))
            compound.setDouble("original_max", entity.getMaxHealth());
        if (!compound.hasKey("xp_multiplier")) {
            double xp_multiplier = 1;
            boolean spawnerCreature = entity instanceof Blaze || entity instanceof CaveSpider;
            if (entity.getWorld().getTime() < 13000 || entity.getWorld().getTime() > 23250 || (reason == CreatureSpawnEvent.SpawnReason.SPAWNER && !spawnerCreature)) {
                xp_multiplier = ArterionPlugin.getInstance().getFormulaManager().DMG_XP_DAY_MODIFIER.evaluateDouble();
            }
            if (boss) xp_multiplier *= BOSS_MULTIPLIER;
            compound.setDouble("xp_multiplier", xp_multiplier);
        }
        ArterionFormula formula = ArterionPlugin.getInstance().getFormulaManager().DMG_ENTITY_MAXHEALTH.get(DamageManager.entityTypeToString(entity));
        if (formula == null || !formula.isDeclared()) return; //Nothing to change
        entity.setMaxHealth(formula.evaluateDouble(random.nextDouble()) * (boss ? BOSS_MULTIPLIER : 1));
        if (entity.getHealth() > 0) entity.setHealth(entity.getMaxHealth());
    }

    public void handle(EntityDamageEvent evt) {
        if (globalDisable) return;

        //handle dmg
        DamageHandler handler = handlers[evt.getCause().ordinal()];
        if (handler == null) handler = defaultDamageHandler;
        if (!(evt.getEntity() instanceof LivingEntity)) return;
        if (evt instanceof EntityDamageByEntityEvent) {
            handler.handleDamage(evt.getEntity(), ((EntityDamageByEntityEvent) evt).getDamager(), evt);
            if (((EntityDamageByEntityEvent) evt).getDamager() instanceof Player) {
                setLastDamager((LivingEntity) evt.getEntity(), ArterionPlayerUtil.get((Player) ((EntityDamageByEntityEvent) evt).getDamager()));
            } else if (((EntityDamageByEntityEvent) evt).getDamager() instanceof Projectile) {
                if (((Projectile) ((EntityDamageByEntityEvent) evt).getDamager()).getShooter() instanceof Player) {
                    setLastDamager((LivingEntity) evt.getEntity(), ArterionPlayerUtil.get((Player) ((Projectile) ((EntityDamageByEntityEvent) evt).getDamager()).getShooter()));
                }
            }
        } else {
            handler.handleDamage(evt.getEntity(), null, evt);
        }
        if (evt.getEntity() instanceof Player) {
            ArterionPlayer ap = ArterionPlayerUtil.get((Player) evt.getEntity());
            if (ap != null && ap.getPlayerScoreboard() != null) {
                ArterionPlugin.getInstance().getSchedulers().getMainScheduler().executeInSpigotCircle(new InternalTask() {
                    @Override
                    public void run() {
                        ap.getPlayerScoreboard().updateHealth();
                    }
                });
            }
        }
    }

    public void handleDeath(EntityDeathEvent evt) {
        ArterionPlayer p = getLastDamager(evt.getEntity());
        if (p == null) {
            evt.getDrops().clear();
        } else {
            //Quests
            if (p.getQuest() != null) {
                p.getQuest().onKillEntity(p, evt.getEntity());
            }

            //Stats
            if (evt.getEntity() instanceof LivingEntity) {
                p.trackStatistic(StatType.MOB_KILLS, evt.getEntityType().getTypeId(), v -> v + 1);
            }
            if (evt.getEntity().getWorld().getGameRuleValue("doMobLoot").equalsIgnoreCase("false")) return;
            ArterionFormula gold = ArterionPlugin.getInstance().getFormulaManager().DMG_ENTITY_GOLD.get(DamageManager.entityTypeToString(evt.getEntity()));
            if (gold != null && gold.isDeclared()) {
                int amount = gold.evaluateInt(random.nextDouble());
                if (amount != 0) {
                    if (p.getGuild() != null && CapturePoints.DESERT_TEMPLE.getClaimedBy() != null && p.getGuild().equals(CapturePoints.DESERT_TEMPLE.getClaimedBy())) {
                        amount *= ArterionPlugin.getInstance().getFormulaManager().CAPTUREPOINT_DESERTTEMPLE_GOLD_MULTIPLIER.evaluateDouble();
                    }
                    evt.getDrops().add(new GoldItem(amount).toItemStack());
                }
            }
            double xp_multiplier = 1;
            NBTCompound compound = NBTInjector.getNbtData(evt.getEntity());
            if (compound != null) {
                if (compound.hasKey("xp_multiplier")) xp_multiplier = compound.getDouble("xp_multiplier");
            }
            ArterionFormula xp = ArterionPlugin.getInstance().getFormulaManager().DMG_ENTITY_XP.get(DamageManager.entityTypeToString(evt.getEntity()));
            if (xp != null && xp.isDeclared()) {
                int amount = (int) (xp.evaluateInt(random.nextDouble()) * xp_multiplier);
                if (amount != 0) {
                    XPEarnHandler.earnXP(p, amount);
                }
            }
            //Dungeon keys
            if (evt.getEntity() instanceof Monster) {
                if (Math.random() <= ArterionPlugin.getInstance().getFormulaManager().DUNGEON_MORGOTH_KEY_CHANCE.evaluateDouble() / 100d) {
                    evt.getDrops().add(new MorgothDungeonKeyItem().toItemStack());
                }
            }
        }
    }

    public void applyDamageToArrow(EntityShootBowEvent evt) {
        double damage = 0;
        boolean creative = false;
        boolean canUseBow = true;

        ArterionPlayer p = null;

        if (evt.getEntity() instanceof Player) {
            p = ArterionPlayerUtil.get((Player) evt.getEntity());

            if (p.getSelectedClass() != null && p.getSelectedClass().isWeaponAllowed(Material.BOW)) {
                damage = WeaponDamageCalculator.calculateBowDamage(evt.getBow(), evt.getForce());
            } else {
                damage = WeaponDamageCalculator.calculateDamage(null, evt.getEntity());
            }

            //Apply prestige point
            damage *= ArterionPlugin.getInstance().getFormulaManager().PRESTIGE_ATTACK.evaluateDouble(p.getPointsAttack());

            creative = ((Player) evt.getEntity()).getGameMode() == GameMode.CREATIVE;
            if (!p.getSelectedClass().isWeaponAllowed(Material.BOW)) canUseBow = false;
        } else {
            //Skeleton
            ArterionFormula entityDamage = ArterionPlugin.getInstance().getFormulaManager().DMG_ENTITY_ATTACK.get(EntityType.SKELETON.toString());
            if (entityDamage != null && entityDamage.isDeclared()) {
                damage = entityDamage.evaluateDouble(random.nextDouble());
            }
        }
        Entity e = NBTInjector.patchEntity(evt.getProjectile());
        if (p != null) {
            damage = Hooks.PLAYER_SHOOT_BOW_HOOK.execute(p, new Tuple<>((Projectile) e, damage)).getSecondValue();
        }
        NBTCompound ent = NBTInjector.getNbtData(e);
        ent.setDouble("art_dmg", damage);
        try {
            if (arrowGetHandle == null) arrowGetHandle = e.getClass().getMethod("getHandle");
            Object mce = arrowGetHandle.invoke(e);
            if (arrowFromPlayer == null) arrowFromPlayer = mce.getClass().getField("fromPlayer");
            arrowFromPlayer.set(mce, (evt.getBow().containsEnchantment(Enchantment.ARROW_INFINITE) || creative || !(evt.getEntity() instanceof Player)) ? 0 : 1);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        if (canUseBow) ((Arrow) e).setKnockbackStrength(((Arrow) evt.getProjectile()).getKnockbackStrength());
        else {
            ((Arrow) e).setKnockbackStrength(0);
            e.setFireTicks(0);
        }
        ((Arrow) e).setBounce(false);
        ((Projectile) e).setShooter(evt.getEntity());

        //Stats
        if (p != null && p.getSelectedClass() == ClassEnum.FORESTRUNNER) {
            p.trackStatistic(StatType.CLICKS, Material.BOW.getId(), v -> v + 1);
        }
    }

    public void damage(LivingEntity e, ArterionPlayer damager, double damage, Skill skill) {
        if (e instanceof Player) {
            ArterionPlayerUtil.get((Player) e).setLastAffectedDamageSkill(skill);
        }
        setLastDamager(e, damager);
        Map<EntityDamageEvent.DamageModifier, Double> modifiers = new HashMap<>();
        modifiers.put(EntityDamageEvent.DamageModifier.BASE, damage);
        EntityDamageByEntityEvent evt = new EntityDamageByEntityEvent(damager.getBukkitPlayer(), e, EntityDamageEvent.DamageCause.CUSTOM, modifiers, new EnumMap<EntityDamageEvent.DamageModifier, Function<? super Double, Double>>(ImmutableMap.of(EntityDamageEvent.DamageModifier.BASE, ZERO)));
        this.handle(evt);
        globalDisable = true;
        e.setLastDamageCause(evt);
        e.damage(evt.getDamage());
        globalDisable = false;
    }

    public void damage(LivingEntity e, double damage, Skill skill) {
        if (e instanceof Player) {
            ArterionPlayerUtil.get((Player) e).setLastAffectedDamageSkill(skill);
        }
        Map<EntityDamageEvent.DamageModifier, Double> modifiers = new HashMap<>();
        modifiers.put(EntityDamageEvent.DamageModifier.BASE, damage);
        EntityDamageEvent evt = new EntityDamageEvent(e, EntityDamageEvent.DamageCause.CUSTOM, modifiers, new EnumMap<EntityDamageEvent.DamageModifier, Function<? super Double, Double>>(ImmutableMap.of(EntityDamageEvent.DamageModifier.BASE, ZERO)));
        this.handle(evt);
        globalDisable = true;
        e.setLastDamageCause(evt);
        e.damage(evt.getDamage());
        globalDisable = false;
    }
}
