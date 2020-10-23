package me.friwi.arterion.plugin.ui.killfeed;

import me.friwi.arterion.plugin.ArterionPlugin;
import me.friwi.arterion.plugin.combat.PlayerRelation;
import me.friwi.arterion.plugin.combat.skill.Skill;
import me.friwi.arterion.plugin.combat.skill.impl.barbar.StompSkill;
import me.friwi.arterion.plugin.combat.skill.impl.cleric.BlindingExplosionSkill;
import me.friwi.arterion.plugin.combat.skill.impl.mage.ChainLightningSkill;
import me.friwi.arterion.plugin.combat.skill.impl.mage.FireStormSkill;
import me.friwi.arterion.plugin.combat.skill.impl.mage.FireballSkill;
import me.friwi.arterion.plugin.combat.skill.impl.shadowrunner.AcidBombSkill;
import me.friwi.arterion.plugin.combat.skill.impl.shadowrunner.ThroatCutSkill;
import me.friwi.arterion.plugin.guild.Guild;
import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.player.ArterionPlayerUtil;
import me.friwi.arterion.plugin.ui.mod.packet.Packet09Killfeed;
import me.friwi.arterion.plugin.ui.mod.server.ModConnection;
import me.friwi.arterion.plugin.util.database.entity.DatabasePlayer;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class KillFeed {
    public static void handleDeath(LivingEntity died, DatabasePlayer killedPlayer, ArterionPlayer killerPlayer, Entity killerNameProvider, EntityDamageEvent.DamageCause lastDamageCause) {
        UUID diedUUID = killedPlayer.getUuid();
        ArterionPlayer diedp = died instanceof Player ? ArterionPlayerUtil.get((Player) died) : null;

        Skill killSkill = diedp == null ? null : diedp.getLastAffectedDamageSkill();
        if (diedp != null) diedp.setLastAffectedDamageSkill(null);

        int itemid = -1;
        int subid = 0;
        KillFeedIcon icon = null;

        switch (lastDamageCause) {
            case CONTACT:
                itemid = Material.CACTUS.getId();
                break;
            case ENTITY_ATTACK:
                if (killerPlayer == null) {
                    icon = KillFeedIcon.KILLED_WITH_AIR;
                } else {
                    ItemStack inhand = killerPlayer.getBukkitPlayer().getItemInHand();
                    if (inhand == null || inhand.getType() == Material.AIR) {
                        icon = KillFeedIcon.KILLED_WITH_AIR;
                    } else {
                        itemid = inhand.getTypeId();
                        subid = inhand.getDurability();
                    }
                }
                break;
            case PROJECTILE:
                itemid = Material.BOW.getId();
                break;
            case SUFFOCATION:
            case SUICIDE:
            case WITHER:
            case VOID:
                icon = KillFeedIcon.DEFAULT;
                break;
            case FALL:
                icon = KillFeedIcon.FALL;
                break;
            case FIRE:
            case FIRE_TICK:
                icon = KillFeedIcon.FIRE;
                break;
            case LAVA:
                itemid = Material.LAVA_BUCKET.getId();
                break;
            case DROWNING:
                itemid = Material.WATER_BUCKET.getId();
                break;
            case BLOCK_EXPLOSION:
            case ENTITY_EXPLOSION:
                itemid = Material.TNT.getId();
                break;
            case LIGHTNING:
                icon = KillFeedIcon.LIGHTNING;
                break;
            case STARVATION:
                icon = KillFeedIcon.STARVATION;
                break;
            case POISON:
                //Poison potion
                itemid = 373;
                subid = 8260;
                break;
            case MAGIC:
                //Harm potion
                itemid = 373;
                subid = 16460;
                break;
            case FALLING_BLOCK:
                itemid = Material.ANVIL.getId();
                break;
            case THORNS:
                icon = KillFeedIcon.THORNS_REFLECTED;
                break;
            case CUSTOM:
                if (killSkill instanceof FireStormSkill) {
                    itemid = Material.BLAZE_POWDER.getId();
                } else if (killSkill instanceof BlindingExplosionSkill) {
                    icon = KillFeedIcon.SKILL_BLINDING_EXPLOSION;
                } else if (killSkill instanceof AcidBombSkill) {
                    //Posion throw potion
                    itemid = 373;
                    subid = 16452;
                } else if (killSkill instanceof StompSkill) {
                    icon = KillFeedIcon.SKILL_STOMP;
                } else if (killSkill instanceof ChainLightningSkill) {
                    itemid = Material.NETHER_STAR.getId();
                } else if (killSkill instanceof ThroatCutSkill) {
                    icon = KillFeedIcon.SKILL_THROAT_CUT;
                } else if (killSkill instanceof FireballSkill) {
                    itemid = Material.FIREBALL.getId();
                } else {
                    icon = KillFeedIcon.DEFAULT;
                }
                break;
        }


        //Send result to all players on server
        if (itemid == -1 && icon == null) {
            icon = KillFeedIcon.DEFAULT;
        }
        String killerTag = "";
        String killedTag = "";
        if (killerPlayer != null && killerPlayer.getGuild() != null) {
            killerTag = "\2478[\247e" + killerPlayer.getGuild().getTag() + "\2478] \247f";
        }
        Guild killedGuild = ArterionPlugin.getInstance().getGuildManager().getGuildByMemberUUID(killedPlayer.getUuid());
        if (killedGuild != null) {
            killedTag = "\2478[\247e" + killedGuild.getTag() + "\2478] \247f";
        }
        for (Player s : ArterionPlugin.getOnlinePlayers()) {
            ArterionPlayer send = ArterionPlayerUtil.get(s);
            if (send.usesMod()) {
                int color = 0xAA2C0863;
                int duration = 10000;
                if (diedp != null && (diedp.equals(send) || diedp.getPlayerRelation(send) == PlayerRelation.FRIENDLY)) {
                    color = 0xFFAA0000;
                    duration = 15000;
                } else if (killerPlayer != null && killerPlayer.getPlayerRelation(send) == PlayerRelation.FRIENDLY) {
                    color = 0xFF00AA00;
                    duration = 15000;
                }
                ModConnection.sendModPacket(s, new Packet09Killfeed(color,
                        duration,
                        killerPlayer == null ? null : killerPlayer.getBukkitPlayer().getUniqueId(),
                        killerNameProvider == null ? "" : killerTag + send.getLanguage().translateObject(killerNameProvider),
                        (byte) (icon == null ? 0 : 1),
                        (short) (icon == null ? itemid : icon.ordinal()),
                        (short) (icon == null ? subid : 0),
                        diedUUID,
                        killedTag + send.getLanguage().translateObject(killedPlayer)));
            }
        }
    }
}
