package me.friwi.arterion.plugin.combat.logging;

import de.tr7zw.changeme.nbtapi.*;
import de.tr7zw.changeme.nbtapi.utils.nmsmappings.ReflectionMethod;
import de.tr7zw.nbtinjector.NBTInjector;
import me.friwi.arterion.plugin.ArterionPlugin;
import me.friwi.arterion.plugin.combat.Combat;
import me.friwi.arterion.plugin.formula.ArterionFormula;
import me.friwi.arterion.plugin.guild.Guild;
import me.friwi.arterion.plugin.listener.CreatureSpawnListener;
import me.friwi.arterion.plugin.listener.PlayerDeathListener;
import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.stats.StatType;
import me.friwi.arterion.plugin.util.database.DatabaseObjectTask;
import me.friwi.arterion.plugin.util.database.entity.DatabasePlayer;
import me.friwi.arterion.plugin.util.language.api.LanguageAPI;
import me.friwi.arterion.plugin.util.scheduler.InternalTask;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.*;

public class CombatLoggingHandler {
    public Constructor<?> NBT_FLOAT, NBT_DOUBLE, NBT_LIST;
    public Method NBT_LIST_ADD;
    private boolean disableNPCSpawning = false;
    private Map<Villager, DatabasePlayer> villagerMap = new HashMap<>();
    private Map<Villager, List<ItemStack>> droppedItems = new HashMap<>();
    private Map<Villager, Guild> guildMap = new HashMap<>();
    private Map<Villager, Integer> xpMap = new HashMap<>();
    private ArterionPlugin plugin;

    public CombatLoggingHandler(ArterionPlugin plugin) {
        this.plugin = plugin;
    }

    public void startScheduler() throws ClassNotFoundException, NoSuchMethodException {
        NBT_FLOAT = Class.forName("net.minecraft.server." + ArterionPlugin.REFLECTION_VERSION + ".NBTTagFloat").getConstructor(float.class);
        NBT_DOUBLE = Class.forName("net.minecraft.server." + ArterionPlugin.REFLECTION_VERSION + ".NBTTagDouble").getConstructor(double.class);
        NBT_LIST = Class.forName("net.minecraft.server." + ArterionPlugin.REFLECTION_VERSION + ".NBTTagList").getConstructor();
        NBT_LIST_ADD = Class.forName("net.minecraft.server." + ArterionPlugin.REFLECTION_VERSION + ".NBTTagList")
                .getMethod("add", Class.forName("net.minecraft.server." + ArterionPlugin.REFLECTION_VERSION + ".NBTBase"));
        this.plugin.getSchedulers().getMainScheduler().executeInSpigotCircleTimer(new InternalTask() {
            @Override
            public void run() {
                Iterator<Villager> it = villagerMap.keySet().iterator();
                while (it.hasNext()) {
                    Villager v = it.next();
                    updateOfflinePlayer(v, false);
                    if (updateState(v, false)) {
                        it.remove();
                        droppedItems.remove(v);
                        guildMap.remove(v);
                        xpMap.remove(v);
                    }
                }
            }
        }, 3, 20);
    }

    public void handleLogout(ArterionPlayer player) {
        if (disableNPCSpawning) return;
        if (Combat.isPlayerInCombat(player) != null && player.getBukkitPlayer().getHealth() > 0) {
            CreatureSpawnListener.isSpawningWithCommand = true;
            Villager villager = (Villager) player.getBukkitPlayer().getWorld().spawnEntity(player.getBukkitPlayer().getLocation(), EntityType.VILLAGER);
            villager = (Villager) NBTInjector.patchEntity(villager);
            CreatureSpawnListener.isSpawningWithCommand = false;
            villager.setMaxHealth(player.getMaxHealth());
            villager.setHealth(player.getHealth());
            NBTCompound compound = NBTInjector.getNbtData(villager);
            if (!compound.hasKey("original_max")) compound.setDouble("original_max", 20d);
            if (!compound.hasKey("player_ref"))
                compound.setString("player_ref", player.getBukkitPlayer().getUniqueId().toString());
            if (!compound.hasKey("player_time")) compound.setLong("player_time", System.currentTimeMillis());
            if (!compound.hasKey("last_health")) compound.setDouble("last_health", villager.getHealth());
            villagerMap.put(villager, player.getPersistenceHolder());
            droppedItems.put(villager, PlayerDeathListener.calculateDrops(player, false));
            guildMap.put(villager, player.getGuild());
            xpMap.put(villager, player.calculateDroppedExperience());
            villager.setFireTicks(player.getBukkitPlayer().getFireTicks());
            villager.setFallDistance(player.getBukkitPlayer().getFallDistance());
            villager.addPotionEffects(player.getBukkitPlayer().getActivePotionEffects());
            if (updateState(villager, false)) {
                villagerMap.remove(villager);
                droppedItems.remove(villager);
                guildMap.remove(villager);
                xpMap.remove(villager);
                villager.remove();
            } else {
                villager.setCustomNameVisible(true);
            }
            player.trackStatistic(StatType.DEATHS, 0, v -> v + 1);
            player.addDeath();
            if (player.getGuild() != null) player.getGuild().trackStatistic(StatType.CLAN_DEATHS, 0, v -> v + 1);
        }
    }

    public void handleLogin(ArterionPlayer player) {
        Villager remove = null;
        for (Map.Entry<Villager, DatabasePlayer> entry : villagerMap.entrySet()) {
            if (entry.getValue().getUuid().equals(player.getPersistenceHolder().getUuid())) {
                remove = entry.getKey();
                break;
            }
        }
        if (remove != null) {
            if (player.getBukkitPlayer().getHealth() > 0)
                player.getBukkitPlayer().setHealth(remove.getHealth() / remove.getMaxHealth() * player.getBukkitPlayer().getMaxHealth());
            player.getBukkitPlayer().teleport(remove.getLocation());
            player.getBukkitPlayer().setFireTicks(remove.getFireTicks());
            player.getBukkitPlayer().setFallDistance(remove.getFallDistance());
            player.getPotionTracker().removeAllPotionEffects(PotionEffectType.values());
            player.getPotionTracker().addPotionEffects(remove.getActivePotionEffects());
            if (updateState(remove, true)) {
                villagerMap.remove(remove);
                droppedItems.remove(remove);
                guildMap.remove(remove);
                xpMap.remove(remove);
            }
        }
    }

    public void updateOfflinePlayer(Villager villager, boolean force) {
        NBTCompound compound = NBTInjector.getNbtData(villager);
        if (compound.hasKey("player_ref")) {
            String uuid = compound.getString("player_ref");
            if (force || compound.getDouble("last_health") != villager.getHealth())
                updateOfflinePlayer(uuid, villager.getHealth() <= 0, (float) villager.getHealth(), villager.getLocation());
            compound.setDouble("last_health", villager.getHealth());
        }
    }

    public boolean onEntityDeath(Entity ent) {
        if (ent instanceof Villager) {
            DatabasePlayer p;
            if ((p = villagerMap.remove(ent)) != null) {
                List<ItemStack> drops = droppedItems.remove(ent);
                guildMap.remove(ent);
                int xp = xpMap.remove(ent);
                Player check = Bukkit.getPlayer(p.getUuid());
                if (check != null && check.isOnline()) return true; //Prevent item duping by killing player and villager
                updateOfflinePlayer(p.getUuid().toString(), !drops.isEmpty(), 0, ent.getLocation()); //Only remove items when drops contains items (noob protection)
                for (ItemStack drop : drops) {
                    ent.getWorld().dropItem(ent.getLocation(), drop);
                }

                //Drop XP
                if (!drops.isEmpty())
                    ((ExperienceOrb) ent.getWorld().spawn(ent.getLocation(), ExperienceOrb.class)).setExperience(xp);

                if (!drops.isEmpty()) {
                    //Player does not have noob protection, lets remove the gold that was dropped before from the player inventory
                    new DatabaseObjectTask<DatabasePlayer>(DatabasePlayer.class, p.getUuid()) {

                        @Override
                        public void updateObject(DatabasePlayer databasePlayer) {
                            databasePlayer.setGold(0);
                        }

                        @Override
                        public void success() {

                        }

                        @Override
                        public void fail() {

                        }
                    }.execute();
                }
                //Print chat message and killfeed
                PlayerDeathListener.playKillSurroundings(p, (LivingEntity) ent);
                return true;
            }
        }
        return false;
    }

    public boolean isCombatLoggingVillager(Entity ent) {
        if (ent instanceof Villager) {
            return villagerMap.containsKey(ent);
        }
        return false;
    }

    public boolean updateState(Villager villager, boolean remove) {
        DatabasePlayer p;
        if ((p = villagerMap.get(villager)) != null) {
            ArterionFormula f = plugin.getFormulaManager().PLAYER_COMBATLOG_DURATION;
            long villager_dur = f != null && f.isDeclared() ? f.evaluateInt() : 0;
            long time = 0;
            NBTCompound compound = NBTInjector.getNbtData(villager);
            if (compound.hasKey("player_time")) {
                time = compound.getLong("player_time");
            }
            if (remove || time + villager_dur <= System.currentTimeMillis()) {
                //Remove our villager, player is safe
                updateOfflinePlayer(villager, true); //Force to save location
                villager.remove();
                return true;
            } else {
                long seconds = (time + villager_dur - System.currentTimeMillis()) / 1000;
                villager.setCustomName(LanguageAPI.getLanguage(LanguageAPI.DEFAULT_LANGUAGE).getTranslation("combat.logging.villagername").translate(p.getName(), seconds).getMessage());
            }
        }
        return false;
    }

    public void updateOfflinePlayer(String uuid, boolean killed, float health, Location location) {
        File worldFolder = Bukkit.getWorlds().get(0).getWorldFolder();
        File playerdataFolder = new File(worldFolder, "playerdata");
        try {
            NBTFile file = new NBTFile(new File(playerdataFolder, uuid + ".dat"));
            try {
                if (file.hasKey("Rotation")) {
                    file.removeKey("Rotation");
                    Object list = NBT_LIST.newInstance();
                    NBT_LIST_ADD.invoke(list, NBT_FLOAT.newInstance(location.getYaw()));
                    NBT_LIST_ADD.invoke(list, NBT_FLOAT.newInstance(location.getPitch()));
                    NBTReflectionUtil.setData(file, ReflectionMethod.COMPOUND_SET, "Rotation", list);
                }
                if (file.hasKey("Pos")) {
                    file.removeKey("Pos");
                    Object list = NBT_LIST.newInstance();
                    NBT_LIST_ADD.invoke(list, NBT_DOUBLE.newInstance(location.getX()));
                    NBT_LIST_ADD.invoke(list, NBT_DOUBLE.newInstance(location.getY()));
                    NBT_LIST_ADD.invoke(list, NBT_DOUBLE.newInstance(location.getZ()));
                    NBTReflectionUtil.setData(file, ReflectionMethod.COMPOUND_SET, "Pos", list);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            file.setShort("Health", (short) health);
            file.setFloat("HealF", health);
            if (killed) {
                file.setInteger("DeathTime", 100);
                NBTCompoundList inventory = file.getCompoundList("Inventory");
                List<NBTListCompound> keep = new LinkedList<>();
                Iterator<NBTListCompound> it = inventory.iterator();
                while (it.hasNext()) {
                    NBTListCompound compound = it.next();
                    if (compound.hasKey("tag") && compound.getCompound("tag").hasKey("art_drop")) {
                        if (!compound.getCompound("tag").getBoolean("art_drop")) {
                            keep.add(compound);
                        }
                    }
                }
                //Finally remove all items
                file.removeKey("Inventory");
                //Add items to new inventory compount
                inventory = file.getCompoundList("Inventory");
                for (NBTListCompound k : keep) inventory.addCompound(k);
                //Remove xp
                file.setInteger("XpLevel", 0);
                file.setInteger("XpP", 0);
                file.setInteger("XpTotal", 0);
            }
            file.save();
        } catch (IOException | NbtApiException e) {
            e.printStackTrace();
        }
    }

    public DatabasePlayer getDatabasePlayer(LivingEntity obj) {
        return villagerMap.get(obj);
    }

    public Guild getGuild(LivingEntity villager) {
        return guildMap.get(villager);
    }

    public void setDisableNPCSpawning(boolean disableNPCSpawning) {
        this.disableNPCSpawning = disableNPCSpawning;
    }
}
