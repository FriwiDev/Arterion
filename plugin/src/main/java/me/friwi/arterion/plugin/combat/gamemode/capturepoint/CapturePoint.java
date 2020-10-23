package me.friwi.arterion.plugin.combat.gamemode.capturepoint;

import me.friwi.arterion.plugin.ArterionPlugin;
import me.friwi.arterion.plugin.combat.hook.Tuple;
import me.friwi.arterion.plugin.combat.replay.EventReplay;
import me.friwi.arterion.plugin.combat.skill.Objective;
import me.friwi.arterion.plugin.combat.skill.SkillSlots;
import me.friwi.arterion.plugin.guild.Guild;
import me.friwi.arterion.plugin.listener.CreatureSpawnListener;
import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.player.ArterionPlayerUtil;
import me.friwi.arterion.plugin.stats.StatType;
import me.friwi.arterion.plugin.ui.progress.ProgressBar;
import me.friwi.arterion.plugin.util.database.entity.DatabasePlayer;
import me.friwi.arterion.plugin.util.language.api.Language;
import me.friwi.arterion.plugin.util.language.api.LanguageAPI;
import me.friwi.arterion.plugin.util.scheduler.InternalTask;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.BiConsumer;

public class CapturePoint implements Listener {
    public static final int PROGRESSBAR_LENGTH = 50;

    private String name;
    private Location glassBlock;
    private Location capCenter;

    private Guild claimedBy;
    private int claimedTicks;
    private Guild capturer;
    private int captureProgress;
    private int currentSpeed;
    private ArmorStand hologram;
    private List<ArterionPlayer> onGrounds = new LinkedList<>();
    private CapturePointFight capturePointFight;
    private EventReplay eventReplay;
    private boolean contested = false;
    private int ordinal;
    private BiConsumer<Guild, Long> saveState;


    public CapturePoint(String name, Location glassBlock, Location capCenter, int ordinal, Guild currentCap, long until, BiConsumer<Guild, Long> saveState) {
        this.saveState = saveState;
        this.ordinal = ordinal;
        this.name = name;
        this.glassBlock = glassBlock;
        this.capCenter = capCenter;
        ArterionPlugin.getInstance().getServer().getPluginManager().registerEvents(this, ArterionPlugin.getInstance());
        if (currentCap == null) {
            onAvailable();
        } else {
            onCapture(currentCap, until);
        }
        ArterionPlugin.getInstance().getSchedulers().getMainScheduler().executeInSpigotCircleTimer(new InternalTask() {
            @Override
            public void run() {
                doTick(20);
            }
        }, 20, 20);
    }

    public String getName(Language lang) {
        return lang.getTranslation("capturepoint." + name).translate().getMessage();
    }

    public Location getGlassBlock() {
        return glassBlock;
    }

    public void setGlassBlock(Location glassBlock) {
        this.glassBlock = glassBlock;
    }

    public Location getCapCenter() {
        return capCenter;
    }

    public void setCapCenter(Location capCenter) {
        this.capCenter = capCenter;
    }

    public Guild getClaimedBy() {
        return claimedBy;
    }

    public Guild getCapturer() {
        return capturer;
    }

    public int getCaptureProgress() {
        return captureProgress;
    }

    public int getClaimedTicks() {
        return claimedTicks;
    }

    public int getMaxPoints() {
        return ArterionPlugin.getInstance().getFormulaManager().CAPTUREPOINT_MAX_POINTS.evaluateInt();
    }

    public int getMaxPointsPerSecond() {
        return ArterionPlugin.getInstance().getFormulaManager().CAPTUREPOINT_MAX_POINTS_PER_SECOND.evaluateInt();
    }

    public double getCaptureDistance() {
        return ArterionPlugin.getInstance().getFormulaManager().CAPTUREPOINT_CAPTURE_DISTANCE.evaluateDouble();
    }

    public int getMaxClaimTicks() {
        return ArterionPlugin.getInstance().getFormulaManager().CAPTUREPOINT_CLAIM_TICKS.evaluateInt();
    }

    public void updateGlassBlock() {
        if (getClaimedBy() != null) {
            glassBlock.getBlock().setType(Material.OBSIDIAN);
        } else if (getCapturer() == null) {
            glassBlock.getBlock().setTypeIdAndData(Material.STAINED_GLASS.getId(), (byte) 5, true);
        } else {
            glassBlock.getBlock().setTypeIdAndData(Material.STAINED_GLASS.getId(), (byte) 14, true);
        }
    }

    private void spawn() {
        this.getCapCenter().getChunk().load();
        CreatureSpawnListener.isSpawningWithCommand = true;
        this.hologram = (ArmorStand) this.getCapCenter().getWorld().spawnEntity(this.getCapCenter().clone().add(0, 3, 0), EntityType.ARMOR_STAND);
        CreatureSpawnListener.isSpawningWithCommand = false;
        this.hologram.setVisible(false);
        updateHoloText();
        this.hologram.setCustomNameVisible(true);
        this.hologram.setGravity(false);
        this.hologram.setCanPickupItems(false);
        this.hologram.setNoDamageTicks(30000);
        this.hologram.setArms(false);
        this.hologram.setMarker(true);
    }

    void remove() {
        if (this.hologram != null) hologram.remove();
        hologram = null;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChunkUnload(ChunkUnloadEvent evt) {
        if (hologram != null && evt.getChunk().equals(hologram.getLocation().getChunk())) {
            evt.setCancelled(true);
        }
    }

    private void updateHoloText() {
        if (this.hologram == null || this.hologram.isDead()) return;
        if (capturer == null) {
            if (contested) {
                this.hologram.setCustomName(LanguageAPI.getLanguage(LanguageAPI.DEFAULT_LANGUAGE).getTranslation("capturepoint.contested").translate().getMessage());
            } else {
                this.hologram.setCustomName(LanguageAPI.getLanguage(LanguageAPI.DEFAULT_LANGUAGE).getTranslation("capturepoint.available").translate().getMessage());
            }
        } else {
            this.hologram.setCustomName(generateProgressText());
        }
    }

    private String generateProgressText() {
        float perc = (captureProgress + 0f) / (getMaxPoints() + 0f);
        String prefix = "";
        String suffix = "";
        if (currentSpeed < 0) {
            prefix = "\2474<" + (-currentSpeed) + " ";
        } else if (currentSpeed > 0) {
            suffix = " \2472" + currentSpeed + ">";
        }
        return prefix + "\247e" + capturer.getName() + " " + ProgressBar.generate("\247c", perc, PROGRESSBAR_LENGTH) + " \247e" + Math.round(perc * 100f) + "%" + suffix;
    }

    private void doTick(int interval) {
        if (claimedBy != null) {
            claimedTicks -= interval;
            if (claimedTicks < 0) {
                onAvailable();
            } else if (claimedTicks < 5 * 60 * 20 && eventReplay == null) {
                beginEventReplay(LocalDateTime.ofInstant(Instant.ofEpochMilli(System.currentTimeMillis() + claimedTicks * 50), ArterionPlugin.SERVER_TIME_ZONE));
            }
        }//No else!
        if (claimedBy == null) {
            if (hologram == null) spawn();
            doCaptureTick();
        }
    }

    private void doCaptureTick() {
        Map<Guild, Integer> occupants = new HashMap<>();
        int totalPlayers = 0;
        for (Entity e : hologram.getNearbyEntities(getCaptureDistance(), getCaptureDistance(), getCaptureDistance())) {
            if (e instanceof Player) {
                ArterionPlayer ap = ArterionPlayerUtil.get((Player) e);
                if (ap.getGuild() != null) {
                    if (occupants.containsKey(ap.getGuild())) {
                        occupants.put(ap.getGuild(), occupants.get(ap.getGuild()) + 1);
                    } else {
                        occupants.put(ap.getGuild(), 1);
                    }
                    totalPlayers++;
                }
            }
        }
        if (totalPlayers > 0) {
            doReplayEvent();
            contested = true;
        } else {
            contested = false;
        }
        if (occupants.isEmpty()) {
            capture(null, 1);
        } else {
            Guild maxGuild = null;
            int maxValue = 0;
            for (Map.Entry<Guild, Integer> e : occupants.entrySet()) {
                if (maxValue == e.getValue()) {
                    //Found second guild with same member amount
                    maxGuild = null;
                } else if (maxValue < e.getValue()) {
                    maxValue = e.getValue();
                    maxGuild = e.getKey();
                }
            }
            if (maxGuild == null) {
                //Point is equally contested => pause capture
                capture(null, 0);
            } else {
                int points = maxValue - (totalPlayers - maxValue);
                if (points < 0) points = 0;
                else if (points > getMaxPointsPerSecond()) points = getMaxPointsPerSecond();
                capture(maxGuild, points);
            }
        }
    }

    private void capture(Guild capturer, int points) {
        if (capturer == null) {
            captureProgress -= points;
            if (captureProgress <= 0) {
                this.currentSpeed = 0;
            } else {
                this.currentSpeed = -points;
            }
        } else {
            if (this.getCapturer() != null) {
                if (this.capturer.equals(capturer)) {
                    this.captureProgress += points;
                    this.currentSpeed = points;
                } else {
                    this.captureProgress -= points;
                    this.currentSpeed = -points;
                }
            }
        }
        if (captureProgress <= 0) {
            captureProgress = 0;
            this.capturer = capturer;
        } else if (captureProgress >= getMaxPoints()) {
            onCapture(capturer);
            return;
        }
        updateHoloText();
        updateGlassBlock();
        updateObjectives();
    }

    private void onAvailable() {
        this.claimedBy = null;
        this.claimedTicks = 0;
        this.capturer = null;
        this.captureProgress = 0;
        this.currentSpeed = 0;
        updateGlassBlock();
        updateObjectives();
        remove();
        spawn();
        beginEventReplay(LocalDateTime.ofInstant(Instant.now(), ArterionPlugin.SERVER_TIME_ZONE));
        doReplayEvent();
        if (capturePointFight == null) {
            capturePointFight = new CapturePointFight(this);
        }
        LanguageAPI.broadcastMessage("capturepoint." + getRawName() + ".available");
        appendTranslationToRecordingChat("capturepoint.replay." + getRawName() + ".available");
    }

    private void onCapture(Guild capturer) {
        onCapture(capturer, -1);
    }

    private void onCapture(Guild capturer, long until) {
        this.claimedBy = capturer;
        this.capturer = null;
        this.captureProgress = 0;
        this.currentSpeed = 0;
        remove();
        if (until != -1) {
            long remain = (until - System.currentTimeMillis()) / 50;
            if (remain <= 5) {
                onAvailable();
                return;
            } else {
                this.claimedTicks = (int) remain;
            }
        } else {
            this.claimedTicks = getMaxClaimTicks();
        }
        updateGlassBlock();
        updateObjectives();
        if (until == -1) {
            capturePointFight.end(capturer.getPersistenceHolder(), false);
            capturePointFight = null;
            capturer.trackStatistic(StatType.CAPTURE_POINT_TAKEN, ordinal, v -> v + 1);
            LanguageAPI.broadcastMessage("capturepoint." + getRawName() + ".captured", capturer.getName());
            appendTranslationToRecordingChat("capturepoint.replay." + getRawName() + ".captured", capturer.getName());
            saveState.accept(capturer, System.currentTimeMillis() + (claimedTicks * 50));
        }
    }

    public void enterRegion(ArterionPlayer player) {
        onGrounds.add(player);
        doReplayEvent();
        updateObjective(player);
        if (getFight() != null) getFight().enterRegion(player);
    }

    public void leaveRegion(ArterionPlayer player) {
        onGrounds.remove(player);
        player.getSkillSlots().setObjective(null, SkillSlots.EVENT_OBJECTIVE_PRIORITY);
        if (getFight() != null) getFight().leaveRegion(player);
    }

    public String getRawName() {
        return name;
    }

    public void updateObjectives() {
        for (ArterionPlayer player : onGrounds) {
            updateObjective(player);
        }
    }

    public void updateObjective(ArterionPlayer player) {
        Objective objective = null;
        if (getClaimedBy() != null) {
            objective = new Objective(new ItemStack(Material.BARRIER), "barrier", System.currentTimeMillis() + claimedTicks * 50, "capturepoint.claimed", getClaimedBy().getName());
        } else if (getCapturer() != null) {
            objective = new Objective(new ItemStack(Material.IRON_SWORD), "iron_sword", -1, "capturepoint.incap", generateProgressText());
        } else if (contested) {
            objective = new Objective(new ItemStack(Material.IRON_SWORD), "iron_sword", -1, "capturepoint.contested_obj");
        } else {
            objective = new Objective(new ItemStack(Material.IRON_SWORD), "iron_sword", -1, "capturepoint.claim");
        }
        player.getSkillSlots().setObjective(objective, SkillSlots.EVENT_OBJECTIVE_PRIORITY);
    }

    public CapturePointFight getFight() {
        return capturePointFight;
    }

    public void setFight(CapturePointFight capturePointFight) {
        this.capturePointFight = capturePointFight;
    }

    public void beginEventReplay(LocalDateTime localTime) {
        if (eventReplay == null) {
            eventReplay = new EventReplay(UUID.randomUUID(),
                    "capture_point_fights",
                    getCapCenter(),
                    ArterionPlugin.getInstance().getFormulaManager().CAPTUREPOINT_REPLAY_CHUNK_DISTANCE.evaluateInt(),
                    r -> {
                        //Print intro and set tab list header
                        String time = DateTimeFormatter.ISO_DATE_TIME.format(localTime.atZone(ArterionPlugin.SERVER_TIME_ZONE).withZoneSameInstant(ArterionPlugin.TIME_ZONE)).replaceFirst("T", " ");
                        r.setTablistHeaderFooter(eventReplay.getTranslation("capturepoint.replay.title", getName(eventReplay.getLanguage())), eventReplay.getTranslation("capturepoint.replay.subtitle", time));
                        r.setServerName(eventReplay.getTranslation("capturepoint.replay.servername", time, getName(eventReplay.getLanguage())).replace("&", "\247"));
                        eventReplay.appendTranslationToRecordingChat("capturepoint.replay.intro1", getName(eventReplay.getLanguage()));
                        eventReplay.appendTranslationToRecordingChat("capturepoint.replay.intro2");
                        eventReplay.appendTranslationToRecordingChat("capturepoint.replay.intro3", time);
                    },
                    r -> {
                        //Print outro
                        eventReplay.appendTranslationToRecordingChat("capturepoint.replay.outro1");
                        eventReplay.appendTranslationToRecordingChat("capturepoint.replay.outro2");
                    },
                    (r, players) -> {
                        //Setup player teams etc
                        Map<Tuple<String, Integer>, List<Tuple<UUID, String>>> registeredTeams = new HashMap<>();
                        Set<String> others = new HashSet<>();
                        for (UUID u : players) {
                            Guild guild = ArterionPlugin.getInstance().getGuildManager().getGuildByMemberUUID(u);
                            if (guild == null) {
                                String name = Bukkit.getOfflinePlayer(u).getName();
                                others.add(name);
                                r.setPlayerListName(u, "\2478" + name);
                            } else {
                                DatabasePlayer player = guild.getMember(u);
                                Tuple<String, Integer> regex = new Tuple<>(guild.getTag(), player.getLevel());
                                List<Tuple<UUID, String>> members = registeredTeams.get(regex);
                                if (members == null) {
                                    members = new LinkedList<>();
                                    registeredTeams.put(regex, members);
                                }
                                members.add(new Tuple<>(u, player.getName()));
                            }
                        }
                        //Calculate colors
                        Map<String, ChatColor> colorMap = new HashMap<>();
                        int colorIndex = 0;
                        for (Map.Entry<Tuple<String, Integer>, List<Tuple<UUID, String>>> e : registeredTeams.entrySet()) {
                            if (!colorMap.containsKey(e.getKey().getFirstValue())) {
                                colorMap.put(e.getKey().getFirstValue(), EventReplay.PARTICIPANT_COLORS[colorIndex % EventReplay.PARTICIPANT_COLORS.length]);
                                colorIndex++;
                            }
                        }
                        //Register teams
                        for (Map.Entry<Tuple<String, Integer>, List<Tuple<UUID, String>>> e : registeredTeams.entrySet()) {
                            ChatColor color = colorMap.get(e.getKey().getFirstValue());
                            String prefix = "\2477[" + color.toString() + e.getKey().getFirstValue() + "\2477] " + color.toString();
                            String suffix = " \2477[\2476" + e.getKey().getSecondValue() + "\2477]";
                            List<String> members = new LinkedList<>();
                            for (Tuple<UUID, String> member : e.getValue()) {
                                r.setPlayerListName(member.getFirstValue(), prefix + member.getSecondValue() + suffix);
                                members.add(member.getSecondValue());
                            }
                            r.createTeam(e.getKey().getFirstValue() + e.getKey().getSecondValue(), color.name(), e.getKey().getFirstValue() + e.getKey().getSecondValue(), prefix, suffix, members);
                        }
                        if (others.size() > 0) {
                            r.createTeam("others", ChatColor.DARK_GRAY.name(), "others", "\2478", "", others);
                        }
                    });
            eventReplay.onEvent();
        }
    }

    public void endEventReplay(BiConsumer<File, UUID> callback) {
        if (eventReplay != null) {
            eventReplay.finalize(callback);
            eventReplay = null;
        }
    }

    public void doReplayEvent() {
        if (eventReplay != null) {
            eventReplay.onEvent();
        }
    }

    public EventReplay getEventReplay() {
        return eventReplay;
    }

    public void appendTranslationToRecordingChat(String key, Object... values) {
        if (eventReplay != null) {
            eventReplay.appendTranslationToRecordingChat(key, values);
        }
    }

    public void reset() {
        onAvailable();
    }

    public List<ArterionPlayer> getOnGrounds() {
        return onGrounds;
    }
}
