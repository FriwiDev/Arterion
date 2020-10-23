package me.friwi.arterion.plugin.combat.hook;

import me.friwi.arterion.plugin.combat.skill.Skill;
import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.recordable.ProjectileHitTargetEvent;
import org.bukkit.Location;
import org.bukkit.entity.*;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

public class Hooks {
    /**
     * Damage received after applying armor and potions
     */
    public static final Hook<Entity, Double> FINAL_DAMAGE_RECEIVED_HOOK = new Hook<>();
    /**
     * Damage received after applying armor and potions by another entity
     */
    public static final Hook<Entity, Tuple<Entity, Double>> DAMAGE_RECEIVE_FROM_ENTITY_HOOK = new Hook<>();
    /**
     * Damage dealt with primary attacks
     */
    public static final Hook<Entity, Tuple<Entity, Double>> PRIMARY_ATTACK_DAMAGE_DEALT_HOOK = new Hook<>();
    /**
     * Absolute damage dealt with any attacks
     */
    public static final Hook<Entity, Tuple<Entity, Double>> ABSOLUTE_DAMAGE_DEALT_HOOK = new Hook<>();
    /**
     * Relative damage dealt with any attacks
     */
    public static final Hook<Entity, Tuple<Entity, Double>> RELATIVE_DAMAGE_DEALT_HOOK = new Hook<>();
    /**
     * Damage dealt with arrow attacks from player
     */
    public static final Hook<Player, TriTuple<Arrow, LivingEntity, Double>> PLAYER_ARROW_ATTACK_DAMAGE_DEALT_HOOK = new Hook<>();
    /**
     * Damage received from primary attacks
     */
    public static final Hook<Entity, Tuple<Entity, Double>> PRIMARY_ATTACK_DAMAGE_RECEIVE_HOOK = new Hook<>();
    /**
     * Resistance calculated from armor
     */
    public static final Hook<Entity, Double> ARMOR_RESISTANCE_HOOK = new Hook<>();
    /**
     * Player shoot bow with generated projectile and projectile damage
     */
    public static final Hook<ArterionPlayer, Tuple<Projectile, Double>> PLAYER_SHOOT_BOW_HOOK = new Hook<>();
    /**
     * Hook for projectile hit event
     */
    public static final Hook<Projectile, ProjectileHitTargetEvent> PROJECTILE_HIT_TARGET_EVENT_HOOK = new Hook<>();
    /**
     * Hook for entity explode event
     */
    public static final Hook<Entity, EntityExplodeEvent> ENTITY_EXPLODE_EVENT_HOOK = new Hook<>();
    /**
     * Damage dealt with entity explosion
     */
    public static final Hook<Entity, Tuple<Entity, Double>> ENTITY_EXPLOSION_DAMAGE_DEAL_HOOK = new Hook<>();
    /**
     * Hook for activation of ArrowActiveSkills (cancels all previously running skills)
     */
    public static final Hook<ArterionPlayer, Void> ARROW_ACTIVE_SKILL_ACTIVATE_HOOK = new Hook<>();
    /**
     * Hook that is executed by the printCastMessage method
     */
    public static final Hook<ArterionPlayer, Skill> PLAYER_POST_SKILL_CAST_HOOK = new Hook<>();
    /**
     * Hook that is executed when a falling block (not handled by ExplosionHandler) hits the ground
     */
    public static final Hook<FallingBlock, EntityChangeBlockEvent> FALLING_BLOCK_HIT_GROUND_HOOK = new Hook<>();
    /**
     * Hook that is executed when a block is broken
     */
    public static final Hook<Location, BlockBreakEvent> BLOCK_BREAK_EVENT_HOOK = new LocationBasedHook<>(0);
    /**
     * Hook that is executed when a block flows to another block
     */
    public static final Hook<Location, BlockFromToEvent> BLOCK_FLOW_TO_HOOK = new LocationBasedHook<>(1);
    /**
     * Hook that is executed when a block is removed by explosion
     */
    public static final Hook<Location, Boolean> BLOCK_REMOVE_BY_EXPLOSION_HOOK = new LocationBasedHook<>(2);
}
