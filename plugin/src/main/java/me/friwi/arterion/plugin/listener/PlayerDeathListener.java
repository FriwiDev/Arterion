package me.friwi.arterion.plugin.listener;

import me.friwi.arterion.plugin.ArterionPlugin;
import me.friwi.arterion.plugin.combat.gamemode.external.ExternalFight;
import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.player.ArterionPlayerUtil;
import me.friwi.arterion.plugin.stats.StatType;
import me.friwi.arterion.plugin.ui.killfeed.KillFeed;
import me.friwi.arterion.plugin.util.database.entity.DatabasePlayer;
import me.friwi.arterion.plugin.util.language.api.LanguageAPI;
import me.friwi.arterion.plugin.world.item.GoldItem;
import me.friwi.arterion.plugin.world.item.NBTItemUtil;
import me.friwi.recordable.RecordingCreator;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.LinkedList;
import java.util.List;

public class PlayerDeathListener implements Listener {
    private ArterionPlugin plugin;

    public PlayerDeathListener(ArterionPlugin plugin) {
        this.plugin = plugin;
    }

    public static List<ItemStack> calculateDrops(ArterionPlayer ep, boolean removeFromInv) {
        Player ent = ep.getBukkitPlayer();

        //Only drop items when player does not have noob protection anymore
        if (ep.hasNewbieProtection()) return new LinkedList<>();

        List<ItemStack> drops = new LinkedList<>();
        for (int i = 0; i < ent.getInventory().getContents().length; i++) {
            ItemStack stack = ent.getInventory().getContents()[i];
            if (stack == null) continue;
            if (NBTItemUtil.getShouldDropItem(stack)) {
                if (removeFromInv) ent.getInventory().setItem(i, null);
                if (stack.getType() != Material.AIR) drops.add(stack);
            }
        }
        ItemStack[] stacks = ent.getInventory().getArmorContents();
        for (int i = 0; i < stacks.length; i++) {
            ItemStack stack = stacks[i];
            if (stack == null) continue;
            if (NBTItemUtil.getShouldDropItem(stack)) {
                if (removeFromInv) stacks[i] = null;
                if (stack.getType() != Material.AIR) drops.add(stack);
            }
        }
        if (removeFromInv) ent.getInventory().setArmorContents(stacks);
        if (ep.getBagMoneyBearer().getCachedMoney() > 0) {
            drops.add(new GoldItem(ep.getBagMoneyBearer().getCachedMoney()).toItemStack());
            if (removeFromInv) {
                ep.getBagMoneyBearer().addMoney(-ep.getBagMoneyBearer().getCachedMoney(), success -> {
                });
            }
        }
        return drops;
    }

    public static void playKillSurroundings(DatabasePlayer dbp, LivingEntity died) {
        //Strike lightning
        died.getWorld().strikeLightningEffect(died.getLocation());

        //Custom death message
        ArterionPlayer l = ArterionPlugin.getInstance().getDamageManager().getLastDamager(died);
        Entity lastDamager = l == null ? null : l.getBukkitPlayer();
        if (died.getLastDamageCause() instanceof EntityDamageByEntityEvent) {
            lastDamager = ((EntityDamageByEntityEvent) died.getLastDamageCause()).getDamager();
            if (lastDamager instanceof Projectile) {
                if (((Projectile) lastDamager).getShooter() != null && ((Projectile) lastDamager).getShooter() instanceof Entity) {
                    lastDamager = (Entity) ((Projectile) lastDamager).getShooter();
                } else {
                    lastDamager = null;
                }
            }
        }
        String killedName = LanguageAPI.getLanguage(LanguageAPI.DEFAULT_LANGUAGE).translateObject(dbp);

        for (Player p : ArterionPlugin.getOnlinePlayers()) {
            ArterionPlayer ap = ArterionPlayerUtil.get(p);
            if (ap != null && p.getWorld().equals(died.getWorld()) && p.getLocation().distance(died.getLocation()) < 120) {
                if (lastDamager == null) {
                    ap.sendTranslation("death.withoutdamager", killedName);
                } else {
                    ap.sendTranslation("death.withdamager", killedName, ap.getLanguage().translateObject(lastDamager));
                }
            }
        }
        if (lastDamager == null) {
            Bukkit.getServer().getConsoleSender().sendMessage(LanguageAPI.getLanguage(LanguageAPI.DEFAULT_LANGUAGE).getTranslation("death.withoutdamager").translate(killedName).getMessage());
        } else {
            Bukkit.getServer().getConsoleSender().sendMessage(LanguageAPI.getLanguage(LanguageAPI.DEFAULT_LANGUAGE).getTranslation("death.withdamager").translate(killedName, LanguageAPI.getLanguage(LanguageAPI.DEFAULT_LANGUAGE).translateObject(lastDamager)).getMessage());
        }
        for (RecordingCreator r : ArterionPlugin.getInstance().getRecordingManager().getActiveRecordings()) {
            if (r.isLocationRelevant(died.getLocation())) {
                if (lastDamager == null) {
                    r.addChat(LanguageAPI.getLanguage(LanguageAPI.DEFAULT_LANGUAGE).getTranslation("death.replay.withoutdamager").translate(killedName).getMessage());
                } else {
                    r.addChat(LanguageAPI.getLanguage(LanguageAPI.DEFAULT_LANGUAGE).getTranslation("death.replay.withdamager").translate(killedName, LanguageAPI.getLanguage(LanguageAPI.DEFAULT_LANGUAGE).translateObject(lastDamager)).getMessage());
                }
            }
        }
        //Killfeed
        ArterionPlayer killerPlayer = null;
        if (lastDamager == null) {
            KillFeed.handleDeath(died, dbp, null, null, died.getLastDamageCause() == null ? EntityDamageEvent.DamageCause.SUICIDE : died.getLastDamageCause().getCause());
        } else {
            if (lastDamager instanceof Player) {
                killerPlayer = ArterionPlayerUtil.get((Player) lastDamager);
            }
            KillFeed.handleDeath(died, dbp, killerPlayer, lastDamager, died.getLastDamageCause().getCause());
        }

        //Clan kills
        if (killerPlayer != null && killerPlayer.getGuild() != null && !ArterionPlugin.getInstance().getTemporaryWorldManager().isTemporaryWorld(died.getWorld())) {
            killerPlayer.getGuild().setClanKills(killerPlayer.getGuild().getClanKills() + 1, succ -> {
            });
            killerPlayer.setClanKills(killerPlayer.getClanKills() + 1, succ -> {
            });
        }

        //Stats
        if (killerPlayer != null) {
            killerPlayer.trackStatistic(StatType.KILLS, 0, v -> v + 1);
            killerPlayer.addKill();
            if (killerPlayer.getGuild() != null)
                killerPlayer.getGuild().trackStatistic(StatType.CLAN_KILLS, 0, v -> v + 1);
        }
        if (died instanceof Player) {
            ArterionPlayer ap = ArterionPlayerUtil.get((Player) died);
            ap.trackStatistic(StatType.DEATHS, 0, v -> v + 1);
            ap.addDeath();
            if (ap.getGuild() != null) ap.getGuild().trackStatistic(StatType.CLAN_DEATHS, 0, v -> v + 1);
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent evt) {
        //Disable default message
        evt.setDeathMessage("");
        evt.setKeepInventory(true);

        Player bukkitPlayer = evt.getEntity();
        ArterionPlayer ep = ArterionPlayerUtil.get(bukkitPlayer);

        //External fights
        boolean shouldDropStuff = true;
        ExternalFight fight = ArterionPlugin.getInstance().getExternalFightManager().getFightByPlayer(ep);
        if (fight != null) {
            shouldDropStuff = fight.onDeath(ep);
            ep.setRespawnFight(fight);
        }

        //Reset artefact
        if (ep.isArtefactCarrier()) ep.getArtefactCarrier().onDie();

        //Reset player skills
        ep.getSkillSlots().resetSkillSlots();

        //Reset player potions
        ep.getPotionTracker().clearEffects();

        //Abort guild fight actions
        plugin.getGuildFightManager().onDeathOrLeave(ep);

        if (shouldDropStuff) {
            //Drop XP
            if (!ep.hasNewbieProtection()) {
                bukkitPlayer.getWorld().spawn(bukkitPlayer.getLocation(), ExperienceOrb.class).setExperience(ep.calculateDroppedExperience());
                bukkitPlayer.setLevel(0);
                bukkitPlayer.setExp(0);
            }
            evt.setDroppedExp(0);

            //Drop all items except custom ones
            List<ItemStack> drops = calculateDrops(ep, true);
            for (ItemStack drop : drops) evt.getEntity().getWorld().dropItem(evt.getEntity().getLocation(), drop);
        }

        //Print chat message and killfeed
        playKillSurroundings(ep.getPersistenceHolder(), ep.getBukkitPlayer());
    }
}
