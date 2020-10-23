package me.friwi.arterion.plugin.guild.fight;

import io.papermc.lib.PaperLib;
import me.friwi.arterion.plugin.ArterionPlugin;
import me.friwi.arterion.plugin.combat.gamemode.artefact.ArtefactCarrier;
import me.friwi.arterion.plugin.combat.skill.Objective;
import me.friwi.arterion.plugin.combat.skill.SkillSlots;
import me.friwi.arterion.plugin.guild.Guild;
import me.friwi.arterion.plugin.listener.CreatureSpawnListener;
import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.player.ArterionPlayerUtil;
import me.friwi.arterion.plugin.replay.acl.ReplayACLEntryType;
import me.friwi.arterion.plugin.replay.acl.ReplayACLFile;
import me.friwi.arterion.plugin.stats.StatType;
import me.friwi.arterion.plugin.stats.TrackedStatistic;
import me.friwi.arterion.plugin.stats.context.StatContext;
import me.friwi.arterion.plugin.stats.context.StatContextType;
import me.friwi.arterion.plugin.stats.list.FightStats;
import me.friwi.arterion.plugin.stats.object.StatObjectType;
import me.friwi.arterion.plugin.stats.tracker.StatContextTracker;
import me.friwi.arterion.plugin.ui.progress.ProgressBar;
import me.friwi.arterion.plugin.util.database.DatabaseObjectTask;
import me.friwi.arterion.plugin.util.database.entity.DatabaseGuild;
import me.friwi.arterion.plugin.util.database.entity.DatabaseGuildFight;
import me.friwi.arterion.plugin.util.database.entity.DatabasePlayer;
import me.friwi.arterion.plugin.util.language.api.LanguageAPI;
import me.friwi.arterion.plugin.util.scheduler.InternalTask;
import me.friwi.arterion.plugin.world.chunk.ArterionChunkUtil;
import me.friwi.arterion.plugin.world.region.GuildRegion;
import me.friwi.arterion.plugin.world.region.Region;
import me.friwi.recordable.RecordingCreaterFactory;
import me.friwi.recordable.RecordingCreator;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Consumer;

public class GuildFight implements StatContext<GuildFight> {
    public static final int PROGRESSBAR_LENGTH = 25;
    int tick;
    private DatabaseGuildFight persistenceHolder;
    private File replayFile;
    private Location beginLocation;
    private RecordingCreator recording;
    private Guild attacker, defender, winner;
    private final List<UUID> attackerWhitelist = new ArrayList<>(15);
    private final List<UUID> defenderWhitelist = new ArrayList<>(15);
    private int blockHp, maxBlockHp;
    private long lastAttack;
    private ArmorStand hologram;
    private boolean prefight = true;
    private int jackpot;
    private int jackpotOnce;
    private Map<ArterionPlayer, Long> stealingPlayers = new HashMap<>();
    private Map<UUID, Long> homeCd = new HashMap<>();
    private boolean postDestroy = false;
    private Set<UUID> classChangers = new HashSet<>();
    private Objective attackerObjective, defenderObjective;
    private long timeRemaining;
    private Map<UUID, DatabasePlayer> attackerCache, defenderCache;
    long defenderLoginExpires;
    boolean defenderLoginExpired = false;

    private StatContextTracker<GuildFight> statContextTracker;

    public GuildFight(Guild attacker, Guild defender, DatabaseGuildFight databaseGuildFight) {
        this.beginLocation = defender.getHomeLocation().add(0, 2, 0);
        this.persistenceHolder = databaseGuildFight;
        this.recording = RecordingCreaterFactory.newRecording();
        try {
            this.recording.initializeRecording(new File(ArterionPlugin.REPLAY_DIR + File.separator + "guild_fights",
                            databaseGuildFight.getUuid().toString().replace("-", "")), beginLocation,
                    defender.getRegion().getLowX() - 2, defender.getRegion().getHighX() + 2,
                    defender.getRegion().getLowZ() - 2, defender.getRegion().getHighZ() + 2);
            this.recording.beginRecording();
            ArterionPlugin.getInstance().getRecordingManager().getActiveRecordings().add(this.recording);
        } catch (IOException e) {
            e.printStackTrace();
            this.recording = null;
        }

        this.attacker = attacker;
        this.defender = defender;
        this.maxBlockHp = ArterionPlugin.getInstance().getFormulaManager().FIGHT_GUILDBLOCK_HP.evaluateInt(defender);
        this.blockHp = maxBlockHp;

        this.defenderLoginExpires = System.currentTimeMillis() + ArterionPlugin.getInstance().getFormulaManager().FIGHT_LOGIN_DEFENDER_DURATION.evaluateInt();

        this.attackerCache = new HashMap<>();
        for (DatabasePlayer p : attacker.getAllMembersIncludingOfficersAndLeader()) {
            attackerCache.put(p.getUuid(), p);
        }

        this.defenderCache = new HashMap<>();
        for (DatabasePlayer p : defender.getAllMembersIncludingOfficersAndLeader()) {
            defenderCache.put(p.getUuid(), p);
        }

        this.jackpot = (int) (defender.getMoneyBearer().getCachedMoney() * ArterionPlugin.getInstance().getFormulaManager().FIGHT_STEAL_JACKPOT.evaluateFloat());
        if (this.jackpot < 0) this.jackpot = 0;
        this.jackpotOnce = (int) (jackpot * ArterionPlugin.getInstance().getFormulaManager().FIGHT_STEAL_ONCE.evaluateFloat());

        CreatureSpawnListener.isSpawningWithCommand = true;
        this.hologram = (ArmorStand) defender.getHomeLocation().getWorld().spawnEntity(defender.getHomeLocation().add(0.5, 1, 0.5), EntityType.ARMOR_STAND);
        CreatureSpawnListener.isSpawningWithCommand = false;
        this.hologram.setVisible(false);
        this.hologram.setCustomName(ProgressBar.generate("\247a", 1f, PROGRESSBAR_LENGTH));
        this.hologram.setCustomNameVisible(true);
        this.hologram.setGravity(false);
        this.hologram.setCanPickupItems(false);
        this.hologram.setNoDamageTicks(30000);
        this.hologram.setArms(false);
        this.hologram.setMarker(true);
        this.setupRecording();

        this.statContextTracker = new StatContextTracker<>(this);

        for (Player bp : ArterionPlugin.getOnlinePlayers()) {
            ArterionPlayer p = ArterionPlayerUtil.get(bp);
            if (p.getRegion() != null && p.getRegion().equals(this.getDefender().getRegion())) {
                ArterionPlugin.getInstance().getSchedulers().getDatabaseScheduler().executeInMyCircle(new InternalTask() {
                    @Override
                    public void run() {
                        getStatTracker().beginTracking(p, p.getGuild() == null ? null : p.getGuild().getUUID());
                    }
                });
            }
        }

        for (ArterionPlayer ap : defender.getOnlineMembers()) {
            signupDefender(ap);
        }
    }


    protected void setupRecording() {
        if (getRecording() != null) {
            String time = DateTimeFormatter.ISO_DATE_TIME.format(LocalDateTime.ofInstant(Instant.ofEpochMilli(getTimeBegin()), ArterionPlugin.SERVER_TIME_ZONE).atZone(ArterionPlugin.SERVER_TIME_ZONE).withZoneSameInstant(ArterionPlugin.TIME_ZONE)).replaceFirst("T", " ");
            getRecording().setTablistHeaderFooter(getTranslation("fight.guild.replay.title", attacker, defender), getTranslation("fight.guild.replay.subtitle", time));
            getRecording().setServerName(getTranslation("fight.guild.replay.servername", attacker, defender, time).replace("&", "\247"));
            appendTranslationToRecordingChat("fight.guild.replay.intro1", attacker, defender);
            appendTranslationToRecordingChat("fight.guild.replay.intro2");
            appendTranslationToRecordingChat("fight.guild.replay.intro3", time);

            getRecording().createObjective("hp", "\247c\u2764");
            getRecording().displayObjectiveBelowName("hp");

            for (Player p : ArterionPlugin.getOnlinePlayers()) {
                ArterionPlayer ap = ArterionPlayerUtil.get(p);
                if (ap != null) {
                    getRecording().addOrUpdateScore(ap.getName(), "hp", ap.getHealth());
                }
            }
        }
    }

    public void setupPlayersForReplay(Collection<UUID> players) {
        if (getRecording() != null) {
            //Setup players
            Map<String, List<String>> registeredTeams = new HashMap<>();
            Set<DatabasePlayer> attacking = new HashSet<>();
            Set<DatabasePlayer> defending = new HashSet<>();
            for (UUID u : players) {
                DatabasePlayer p;
                if ((p = attackerCache.get(u)) != null && attackerWhitelist.contains(u)) {
                    int lvl = p.getLevel();
                    getRecording().setPlayerListName(u, "\2477[\247c" + attacker.getTag() + "\2477] \247c" + p.getName() + " \2477[\2476" + lvl + "\2477]");
                    String teamName = "a" + lvl;
                    if (registeredTeams.containsKey(teamName)) {
                        registeredTeams.get(teamName).add(p.getName());
                    } else {
                        List<String> ps = new LinkedList<>();
                        ps.add(p.getName());
                        registeredTeams.put(teamName, ps);
                    }
                    attacking.add(p);
                } else if ((p = defenderCache.get(u)) != null) {
                    int lvl = p.getLevel();
                    getRecording().setPlayerListName(u, "\2477[\247b" + defender.getTag() + "\2477] \247b" + p.getName() + " \2477[\2476" + lvl + "\2477]");
                    String teamName = "d" + lvl;
                    if (registeredTeams.containsKey(teamName)) {
                        registeredTeams.get(teamName).add(p.getName());
                    } else {
                        List<String> ps = new LinkedList<>();
                        ps.add(p.getName());
                        registeredTeams.put(teamName, ps);
                    }
                    defending.add(p);
                } else {
                    String name = Bukkit.getOfflinePlayer(u).getName();
                    getRecording().setPlayerListName(u, "\2478" + name);
                    String teamName = "others";
                    if (registeredTeams.containsKey(teamName)) {
                        registeredTeams.get(teamName).add(name);
                    } else {
                        List<String> ps = new LinkedList<>();
                        ps.add(name);
                        registeredTeams.put(teamName, ps);
                    }
                }
            }
            this.setPlayersAttackingAndDefending(attacking, defending, succ -> {
            });
            for (Map.Entry<String, List<String>> e : registeredTeams.entrySet()) {
                String color = e.getKey().startsWith("a") ? ChatColor.RED.name() : (e.getKey().startsWith("d") ? ChatColor.AQUA.name() : ChatColor.DARK_GRAY.name());
                String prefix = "\2478";
                String suffix = "";
                if (e.getKey().startsWith("a")) {
                    int lvl = Integer.parseInt(e.getKey().substring(1));
                    prefix = "\2477[\247c" + attacker.getTag() + "\2477] \247c";
                    suffix = " \2477[\2476" + lvl + "\2477]";
                } else if (e.getKey().startsWith("d")) {
                    int lvl = Integer.parseInt(e.getKey().substring(1));
                    prefix = "\2477[\247b" + defender.getTag() + "\2477] \247b";
                    suffix = " \2477[\2476" + lvl + "\2477]";
                }
                getRecording().createTeam(e.getKey(), color, e.getKey(), prefix, suffix, e.getValue());
            }
            appendTranslationToRecordingChat("fight.guild.replay.outro1");
            if (winner != null) appendTranslationToRecordingChat("fight.guild.replay.outro2", winner);
            else appendTranslationToRecordingChat("fight.guild.replay.outro2_aborted", winner);
        }
    }

    public List<UUID> getAttackerWhitelist() {
        return attackerWhitelist;
    }

    public void signup(ArterionPlayer other) {
        if (!other.getGuild().equals(attacker)) return;
        if (prefight) {
            other.sendTranslation("fight.guild.notyet");
            return;
        }
        int advantage = ArterionPlugin.getInstance().getFormulaManager().FIGHT_ATTACKER_ADVANTAGE.evaluateInt(attacker);
        if (defenderWhitelist.size() + advantage > attackerWhitelist.size()) {
            attackerWhitelist.add(other.getBukkitPlayer().getUniqueId());
        } else {
            other.sendTranslation("fight.guild.maxattackers", defenderWhitelist.size() + advantage);
        }
    }

    public void signupDefender(ArterionPlayer other) {
        if (!other.getGuild().equals(defender)) return;
        if (defenderLoginExpired) {
            Region region = ArterionChunkUtil.getNonNull(other.getBukkitPlayer().getLocation().getChunk()).getRegion();
            if (region instanceof GuildRegion) {
                other.getBukkitPlayer().teleport(ArterionPlugin.getInstance().getArterionConfig().spawn.clone());
                other.updateRegion(ArterionPlugin.getInstance().getArterionConfig().spawn.clone());
                ArterionPlugin.getInstance().getSchedulers().getMainScheduler().executeInSpigotCircleLater(new InternalTask() {
                    @Override
                    public void run() {
                        //Teleport once more to prevent players getting stuck
                        PaperLib.teleportAsync(other.getBukkitPlayer(), ArterionPlugin.getInstance().getArterionConfig().spawn.clone());
                    }
                }, 3l);
            }
            other.sendTranslation("fight.guild.login.ended");
        } else {
            if (!defenderWhitelist.contains(other.getUUID())) defenderWhitelist.add(other.getUUID());
        }
    }

    public void startTimer() {
        int minutes = ArterionPlugin.getInstance().getFormulaManager().FIGHT_LOGIN_DEFENDER_DURATION.evaluateInt() / 60000;
        attacker.sendTranslation("fight.guild.prefight.begin.attacker", minutes);
        defender.sendTranslation("fight.guild.prefight.begin.defender", minutes);
        appendTranslationToRecordingChat("fight.guild.replay.prefight.begin");
        long expires = System.currentTimeMillis() + ArterionPlugin.getInstance().getFormulaManager().FIGHT_PRE_DURATION.evaluateInt();
        setAttackerObjective(new Objective(new ItemStack(Material.IRON_SWORD, 1), null, expires, "fight.guild.badlion.prepare_attacker"));
        setDefenderObjective(new Objective(new ItemStack(Material.IRON_SWORD, 1), null, expires, "fight.guild.badlion.prepare_defender"));

        int fightDuration = ArterionPlugin.getInstance().getFormulaManager().FIGHT_DURATION.evaluateInt() / 50;
        tick = fightDuration + ArterionPlugin.getInstance().getFormulaManager().FIGHT_PRE_DURATION.evaluateInt() / 50;

        ArterionPlugin.getInstance().getSchedulers().getMainScheduler().executeInSpigotCircleTimer(new InternalTask() {
            int cd = ArterionPlugin.getInstance().getFormulaManager().FIGHT_NOENEMIES_COOLDOWN.evaluateInt() / 50;
            int notongrounds = -1;

            @Override
            public void run() {
                timeRemaining = tick * 50;
                if (!defenderLoginExpired && System.currentTimeMillis() > defenderLoginExpires) {
                    defenderLoginExpired = true;
                    attacker.sendTranslation("fight.guild.login.end");
                    defender.sendTranslation("fight.guild.login.end");
                }
                if (!postDestroy) {
                    if (fightDuration < tick) {
                        //Prefight
                        int remaining = tick - fightDuration;
                        if (remaining % (10 * 20) == 0) {
                            attacker.sendTranslation("fight.guild.prefight", remaining / 20);
                            defender.sendTranslation("fight.guild.prefight", remaining / 20);
                            appendTranslationToRecordingChat("fight.guild.replay.prefight", remaining / 20);
                        }
                        tick -= 20;
                        return;
                    } else if (fightDuration == tick) {
                        //Begin fight
                        prefight = false;
                        attacker.sendTranslation("fight.guild.prefight.end");
                        defender.sendTranslation("fight.guild.prefight.end");
                        appendTranslationToRecordingChat("fight.guild.replay.prefight.end");
                        attacker.sendTranslation("fight.guild.jackpot", jackpot / 100f);
                        defender.sendTranslation("fight.guild.jackpot", jackpot / 100f);
                        appendTranslationToRecordingChat("fight.guild.replay.jackpot", jackpot / 100f);
                        long expires = System.currentTimeMillis() + (fightDuration * 50);
                        setAttackerObjective(new Objective(new ItemStack(Material.IRON_SWORD, 1), null, expires, "fight.guild.badlion.fight_attacker"));
                        setDefenderObjective(new Objective(new ItemStack(Material.IRON_SWORD, 1), null, expires, "fight.guild.badlion.fight_defender"));
                    }
                }
                if (tick == 0) {
                    attacker.sendTranslation("fight.guild.end");
                    defender.sendTranslation("fight.guild.end");
                    if (winner == null) win(defender);
                    endFight(file -> {
                    }, true, false);
                    cancel();
                    return;
                }
                if ((tick % (5 * 60 * 20) == 0 && tick > 0) || tick == 3 * 60 * 20 || tick == 2 * 60 * 20 || tick == 1 * 60 * 20) {
                    attacker.sendTranslation("fight.guild.remaining", tick / 60 / 20);
                    defender.sendTranslation("fight.guild.remaining", tick / 60 / 20);
                    appendTranslationToRecordingChat("fight.guild.replay.time", tick / 60 / 20);
                }
                if (tick < 60 * 20 && tick % (10 * 20) == 0 && tick > 0) {
                    attacker.sendTranslation("fight.guild.remaining.seconds", tick / 20);
                    defender.sendTranslation("fight.guild.remaining.seconds", tick / 20);
                    appendTranslationToRecordingChat("fight.guild.replay.time.seconds", tick / 20);
                }
                if (winner == null && tick < fightDuration - 30 * 20) { //Do not trigger the countdown in the first half minute
                    boolean found = false;
                    for (ArterionPlayer p : attacker.getOnlineMembers()) {
                        if (p.getRegion().equals(defender.getRegion())) {
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        if (notongrounds == -1) {
                            attacker.sendTranslation("fight.guild.notongrounds", cd / 20);
                            appendTranslationToRecordingChat("fight.guild.replay.notongrounds", cd / 20);
                            notongrounds = tick;
                        } else {
                            int remaining = cd - (notongrounds - tick);
                            if (remaining <= 0) {
                                attacker.sendTranslation("fight.guild.end_left");
                                defender.sendTranslation("fight.guild.end_left");
                                win(defender);
                                endFight(file -> {
                                }, true, false);
                                cancel();
                                return;
                            } else if (remaining % (10 * 20) == 0) {
                                attacker.sendTranslation("fight.guild.notongrounds", remaining / 20);
                                appendTranslationToRecordingChat("fight.guild.replay.notongrounds", remaining / 20);
                            }
                        }
                    } else {
                        notongrounds = -1;
                    }
                }
                //Gold stealing
                int stealDelay = ArterionPlugin.getInstance().getFormulaManager().FIGHT_STEAL_DURATION.evaluateInt();
                Iterator<Map.Entry<ArterionPlayer, Long>> it = stealingPlayers.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry<ArterionPlayer, Long> sp = it.next();
                    if (!updateSteal(sp.getKey(), false)) {
                        it.remove();
                        sp.getKey().sendTranslation("fight.guild.steal.aborted");
                        continue;
                    }
                    if (sp.getValue() < System.currentTimeMillis() - stealDelay) {
                        //Steal successfull
                        it.remove();
                        stealSuccess(sp.getKey(), jackpotOnce);
                    }
                }
                tick -= 20;
            }
        }, 0, 20);
    }

    public void attackBlock(ArterionPlayer player) {
        if (lastAttack > System.currentTimeMillis() - 1000 || blockHp <= 0 || winner != null) return;
        lastAttack = System.currentTimeMillis();
        Material mat = player.getBukkitPlayer().getItemInHand() == null ? Material.AIR : player.getBukkitPlayer().getItemInHand().getType();
        int dmg = ArterionPlugin.getInstance().getFormulaManager().FIGHT_GUILDBLOCK_DMG.get(mat.name()).evaluateInt();
        this.blockHp -= dmg;
        if (blockHp <= 0) {
            blockHp = 0;
            this.hologram.setCustomName(LanguageAPI.getLanguage(LanguageAPI.DEFAULT_LANGUAGE).getTranslation("fight.guild.destroyed").translate().getMessage());
            stealSuccess(player, jackpot);
            if (defender.getVault() != null)
                defender.getVault().drop((float) (ArterionPlugin.getInstance().getFormulaManager().GUILD_VAULT_DROP.evaluateDouble() / 100d));
            win(attacker);

            tick = ArterionPlugin.getInstance().getFormulaManager().FIGHT_BLOCKDESTROY_DURATION.evaluateInt() / 50;
            timeRemaining = tick * 50;
            postDestroy = true;

            long expires = System.currentTimeMillis() + (timeRemaining);
            setAttackerObjective(new Objective(new ItemStack(Material.IRON_SWORD, 1), null, expires, "fight.guild.badlion.post_attacker"));
            setDefenderObjective(new Objective(new ItemStack(Material.IRON_SWORD, 1), null, expires, "fight.guild.badlion.post_defender"));

            attacker.sendTranslation("fight.guild.loot_phase", tick / 60 / 20);

            if (defender.hasArtefact()) {
                defender.setHasArtefact(false, succ -> {
                });
                player.setArtefactCarrier(new ArtefactCarrier(player));
            }
        } else {
            float percentage = (blockHp + 0f) / (maxBlockHp + 0f);
            String color = "\247a";
            if (percentage <= 0.5f) {
                color = "\247e";
            }
            if (percentage <= 0.25f) {
                color = "\247c";
            }
            this.hologram.setCustomName(ProgressBar.generate(color, percentage, PROGRESSBAR_LENGTH));
            player.sendTranslation("fight.guild.dmg", dmg, blockHp);
            defender.sendTranslation("fight.guild.owndmg", dmg, blockHp);
            appendTranslationToRecordingChat("fight.guild.replay.blockdmg", dmg, player, blockHp);
        }
    }

    private void win(Guild winner) {
        this.winner = winner;
        LanguageAPI.broadcastMessage("line");
        if (attacker.equals(winner)) {
            LanguageAPI.broadcastMessage("fight.guild.win.attacker", attacker, defender);
            appendTranslationToRecordingChat("fight.guild.replay.win.attacker", attacker, defender);
            attacker.trackStatistic(StatType.GFIGHT_ATTACK_WINS, 0, v -> v + 1);
            defender.trackStatistic(StatType.GFIGHT_DEFENSE_LOSS, 0, v -> v + 1);
        } else {
            LanguageAPI.broadcastMessage("fight.guild.win.defender", attacker, defender);
            appendTranslationToRecordingChat("fight.guild.replay.win.defender", attacker, defender);
            attacker.trackStatistic(StatType.GFIGHT_ATTACK_LOSS, 0, v -> v + 1);
            defender.trackStatistic(StatType.GFIGHT_DEFENSE_WINS, 0, v -> v + 1);
        }
        LanguageAPI.broadcastMessage("line");
        if (winner != null) {
            this.setWinner(winner.getPersistenceHolder(), succ -> {
            });
        }
    }

    void endFight(Consumer<File> callback, boolean remove, boolean instant) {
        hologram.remove();
        if (winner != null && winner.equals(attacker)) {
            defender.setProtection(System.currentTimeMillis() + ArterionPlugin.getInstance().getFormulaManager().FIGHT_PROTECTION_COOLDOWN.evaluateInt(), x -> {
            });
        }
        this.endFightAndSave(file -> {
            attacker.sendTranslation("fight.stats.at", this.getPersistenceHolder().getUuid().toString());
            defender.sendTranslation("fight.stats.at", this.getPersistenceHolder().getUuid().toString());
            callback.accept(file);
        }, instant);
        if (remove) ArterionPlugin.getInstance().getGuildFightManager().getActiveFights().remove(this);
        getAttacker().setLocalFight(null);
        getDefender().setLocalFight(null);
        setAttackerObjective(null);
        setDefenderObjective(null);
        if (!instant) {
            ArterionPlugin.getInstance().getSchedulers().getDatabaseScheduler().executeInMyCircle(new InternalTask() {
                @Override
                public void run() {
                    getStatTracker().stopAllTrackers(ArterionPlugin.getInstance().getExternalDatabase());
                }
            });
        }
    }

    public Guild getAttacker() {
        return attacker;
    }

    public Guild getDefender() {
        return defender;
    }

    public boolean isPrefight() {
        return prefight;
    }

    public boolean updateSteal(ArterionPlayer p, boolean cmd) {
        Guild pg = p.getGuild();
        if (pg == null) {
            p.sendTranslation("guild.notinguild");
            return false;
        }
        if (pg != attacker) {
            p.sendTranslation("fight.guild.notinattack");
            return false;
        }
        if (jackpot <= 0) {
            p.sendTranslation("fight.guild.nothingleft");
            return false;
        }
        if (!p.getBukkitPlayer().getWorld().equals(defender.getHomeLocation().getWorld())
                || p.getBukkitPlayer().getLocation().distance(defender.getHomeLocation()) > ArterionPlugin.getInstance().getFormulaManager().FIGHT_STEAL_RANGE.evaluateFloat()) {
            p.sendTranslation("fight.guild.notinrange");
            return false;
        }
        if (stealingPlayers.containsKey(p)) {
            if (cmd) p.sendTranslation("fight.guild.alreadystealing");
            return true;
        }
        stealingPlayers.put(p, System.currentTimeMillis());
        p.sendTranslation("fight.guild.steal.begin");
        defender.sendTranslation("fight.guild.steal.otherbegin", p);
        appendTranslationToRecordingChat("fight.guild.replay.steal.otherbegin", p);
        return true;
    }

    public Map<ArterionPlayer, Long> getStealingPlayers() {
        return stealingPlayers;
    }

    private void stealSuccess(ArterionPlayer p, int amount) {
        if (amount > jackpot) amount = jackpot;
        jackpot -= amount;
        int finalAmount = amount;
        if (finalAmount <= 0) {
            p.sendTranslation("fight.guild.nothingleft");
            return;
        }
        defender.getMoneyBearer().transferMoney(amount, p.getBagMoneyBearer(), success -> {
            if (success) {
                attacker.sendTranslation("fight.guild.steal.end", p, finalAmount / 100f);
                defender.sendTranslation("fight.guild.steal.end", p, finalAmount / 100f);
                appendTranslationToRecordingChat("fight.guild.replay.steal.end", p, finalAmount / 100f);
            } else {
                p.sendTranslation("fight.guild.dberror");
                jackpot += finalAmount;
            }
        });
    }

    public Map<UUID, Long> getHomeCd() {
        return homeCd;
    }

    public DatabaseGuildFight getPersistenceHolder() {
        return persistenceHolder;
    }

    protected void setPersistenceHolder(DatabaseGuildFight persistenceHolder) {
        this.persistenceHolder = persistenceHolder;
    }

    public File getReplayFile() {
        return replayFile;
    }

    public RecordingCreator getRecording() {
        return recording;
    }

    public void appendChatToRecording(String msg) {
        if (this.recording != null) this.recording.addChat(msg);
    }

    public void appendChatJsonToRecording(String json) {
        if (this.recording != null) this.recording.addChatJson(json);
    }

    protected void endFightAndSave(Consumer<File> callback, boolean instant) {
        if (this.recording != null) {
            Collection<UUID> occuring = new LinkedList<>();
            for (UUID u : this.recording.getOccuringPlayers()) occuring.add(u);
            this.setupPlayersForReplay(occuring);
            this.updateInDB(gf -> {
                gf.setTimeEnd(System.currentTimeMillis());
                if (winner != null) gf.setWinner(winner.getPersistenceHolder());
            }, succ -> {
            });
            InternalTask saveReplayTask = new InternalTask() {
                @Override
                public void run() {
                    appendTranslationToRecordingChat("fight.guild.replay.outro3");
                    //Unsubscribe from updates
                    ArterionPlugin.getInstance().getRecordingManager().getActiveRecordings().remove(GuildFight.this.recording);
                    //Setup newly created players as non-participating
                    List<String> members = new LinkedList<>();
                    for (UUID u : GuildFight.this.recording.getOccuringPlayers()) {
                        if (!occuring.contains(u)) {
                            String name = Bukkit.getOfflinePlayer(u).getName();
                            getRecording().setPlayerListName(u, "\2478" + name);
                            members.add(name);
                        }
                    }
                    if (members.size() > 0) {
                        getRecording().createTeam("others1", ChatColor.DARK_GRAY.name(), "otherslategame", "\2478", "", members);
                    }
                    recording.endRecording(file -> {
                        GuildFight.this.replayFile = file;
                        callback.accept(file);
                        if (file != null) {
                            try {
                                ReplayACLFile acl = new ReplayACLFile(new File(ArterionPlugin.REPLAY_DIR + File.separator + "guild_fights",
                                        getPersistenceHolder().getUuid().toString().replace("-", "") + File.separator + "replayacl"));
                                acl.addEntry(ReplayACLEntryType.GUILD, defender.getUUID(), true);
                                acl.addEntry(ReplayACLEntryType.GUILD, attacker.getUUID(), true);
                                for (UUID u : recording.getOccuringPlayers()) {
                                    acl.addEntry(ReplayACLEntryType.PLAYER, u, true);
                                }
                                acl.save();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            GuildFight.this.setReplayLocation("guild_fights" + File.separator +
                                    getPersistenceHolder().getUuid().toString().replace("-", ""), succ -> {
                            });
                        }
                    });
                }
            };
            if (instant) {
                saveReplayTask.run();
            } else {
                ArterionPlugin.getInstance().getSchedulers().getMainScheduler().executeInSpigotCircleLater(saveReplayTask, ArterionPlugin.getInstance().getFormulaManager().FIGHT_POST_DURATION.evaluateInt() / 50);
            }
        } else {
            callback.accept(null);
        }
    }

    public String getTranslation(String key, Object... values) {
        return LanguageAPI.getLanguage(LanguageAPI.DEFAULT_LANGUAGE).getTranslation(key).translate(values).getMessage();
    }

    public void appendTranslationToRecordingChat(String key, Object... values) {
        this.appendChatToRecording(this.getTranslation(key, values));
    }

    private void updateInDB(Consumer<DatabaseGuildFight> apply, Consumer<Boolean> successCallBack) {
        new DatabaseObjectTask<DatabaseGuildFight>(DatabaseGuildFight.class, persistenceHolder.getUuid()) {
            DatabaseGuildFight updatedHolder;

            @Override
            public void updateObject(DatabaseGuildFight databaseGuildFight) {
                updatedHolder = databaseGuildFight;
                apply.accept(databaseGuildFight);
            }

            @Override
            public void success() {
                GuildFight.this.persistenceHolder = updatedHolder;
                successCallBack.accept(true);
            }

            @Override
            public void fail() {
                successCallBack.accept(false);
            }
        }.execute();
    }

    public UUID getUuid() {
        return persistenceHolder.getUuid();
    }

    public long getTimeBegin() {
        return persistenceHolder.getTimeBegin();
    }

    public long getTimeEnd() {
        return persistenceHolder.getTimeEnd();
    }

    public void setTimeEnd(long timeEnd, Consumer<Boolean> callback) {
        updateInDB(f -> f.setTimeEnd(timeEnd), callback);
    }

    public DatabaseGuild getDatabaseAttacker() {
        return persistenceHolder.getAttacker();
    }

    public DatabaseGuild getDatabaseDefender() {
        return persistenceHolder.getDefender();
    }

    public Set<DatabasePlayer> getPlayersAttacking() {
        return persistenceHolder.getPlayersAttacking();
    }

    public void setPlayersAttacking(Set<DatabasePlayer> playersAttacking, Consumer<Boolean> callback) {
        updateInDB(f -> f.setPlayersAttacking(playersAttacking), callback);
    }

    public Set<DatabasePlayer> getPlayersDefending() {
        return persistenceHolder.getPlayersDefending();
    }

    public void setPlayersDefending(Set<DatabasePlayer> playersDefending, Consumer<Boolean> callback) {
        updateInDB(f -> f.setPlayersDefending(playersDefending), callback);
    }

    public DatabaseGuild getWinner() {
        return persistenceHolder.getWinner();
    }

    public void setWinner(DatabaseGuild winner, Consumer<Boolean> callback) {
        updateInDB(f -> f.setWinner(winner), callback);
    }

    public String getReplayLocation() {
        return persistenceHolder.getReplayLocation();
    }

    public void setReplayLocation(String replayLocation, Consumer<Boolean> callback) {
        updateInDB(f -> f.setReplayLocation(replayLocation), callback);
    }

    public void setPlayersAttackingAndDefending(Set<DatabasePlayer> attacking, Set<DatabasePlayer> defending, Consumer<Boolean> callback) {
        updateInDB(f -> {
            f.setPlayersAttacking(attacking);
            f.setPlayersDefending(defending);
        }, callback);
    }

    public Set<UUID> getClassChangers() {
        return classChangers;
    }

    public void setAttackerObjective(Objective objective) {
        this.attackerObjective = objective;
        for (ArterionPlayer p : attacker.getOnlineMembers()) {
            p.getSkillSlots().setObjective(objective, SkillSlots.GUILD_OBJECTIVE_PRIORITY);
        }
    }

    public void setDefenderObjective(Objective objective) {
        this.defenderObjective = objective;
        for (ArterionPlayer p : defender.getOnlineMembers()) {
            p.getSkillSlots().setObjective(objective, SkillSlots.GUILD_OBJECTIVE_PRIORITY);
        }
    }

    public Objective getObjective(ArterionPlayer p) {
        if (p.getGuild().equals(attacker)) return attackerObjective;
        else return defenderObjective;
    }

    public int getBlockHp() {
        return blockHp;
    }

    public void enterRegion(ArterionPlayer player) {
        ArterionPlugin.getInstance().getSchedulers().getDatabaseScheduler().executeInMyCircle(new InternalTask() {
            @Override
            public void run() {
                getStatTracker().continueTracking(ArterionPlugin.getInstance().getExternalDatabase(), player, player.getGuild() == null ? null : player.getGuild().getUUID());
            }
        });
    }

    public void leaveRegion(ArterionPlayer player) {
        ArterionPlugin.getInstance().getSchedulers().getDatabaseScheduler().executeInMyCircle(new InternalTask() {
            @Override
            public void run() {
                getStatTracker().stopTracking(ArterionPlugin.getInstance().getExternalDatabase(), player);
            }
        });
    }

    @Override
    public StatContextType getType() {
        return StatContextType.GUILD_FIGHT;
    }

    @Override
    public UUID getUUID() {
        return getUuid();
    }

    @Override
    public long getTime() {
        return System.currentTimeMillis() - persistenceHolder.getTimeBegin();
    }

    @Override
    public StatContextTracker<GuildFight> getStatTracker() {
        return statContextTracker;
    }

    @Override
    public TrackedStatistic[] getTrackedStatistics(StatObjectType objectType) {
        return FightStats.getTrackedStatistics(objectType);
    }

    public List<UUID> getDefenderWhitelist() {
        return defenderWhitelist;
    }
}
