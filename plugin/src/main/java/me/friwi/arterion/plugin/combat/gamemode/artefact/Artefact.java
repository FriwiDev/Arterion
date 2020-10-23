package me.friwi.arterion.plugin.combat.gamemode.artefact;

import me.friwi.arterion.plugin.ArterionPlugin;
import me.friwi.arterion.plugin.combat.hook.Tuple;
import me.friwi.arterion.plugin.combat.replay.EventReplay;
import me.friwi.arterion.plugin.guild.Guild;
import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.util.database.entity.DatabasePlayer;
import me.friwi.arterion.plugin.util.language.api.LanguageAPI;
import me.friwi.arterion.plugin.util.scheduler.InternalTask;
import me.friwi.arterion.plugin.world.region.ArtefactRegion;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.io.File;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.*;
import java.util.function.BiConsumer;

public class Artefact {
    private static Guild owner;
    private static ArterionPlayer carrier;
    private static ArtefactBlock block;
    private static ArtefactCristal[] cristals = new ArtefactCristal[3];
    private static ArtefactFight fight;
    private static EventReplay eventReplay;

    public static void reset() {
        if (getOwner() != null) {
            getOwner().setHasArtefact(false, succ -> {
            });
            setOwner(null);
        }
        if (getCarrier() != null) {
            getCarrier().setArtefactCarrier(null);
            setCarrier(null);
        }
        LanguageAPI.broadcastMessage("line");
        LanguageAPI.broadcastMessage("artefact.reset");
        LanguageAPI.broadcastMessage("line");
        for (Player p : ArterionPlugin.getOnlinePlayers()) {
            p.playSound(p.getLocation(), Sound.AMBIENCE_THUNDER, 0.8f, 1f);
        }
        //Start a new fight or use old fight
        if (fight == null) {
            fight = new ArtefactFight();
        }
        //Reset cristals
        for (ArtefactCristal cristal : cristals) {
            cristal.setArtefactFight(fight);
        }
        //Reset block
        block.setHasArtefact(true);
        //Begin replay
        beginEventReplay(LocalDateTime.ofInstant(Instant.now(), ArterionPlugin.SERVER_TIME_ZONE));
        //Update objectives
        updateObjectives();
    }

    public static void init() {
        //Locate artefact
        for (Guild g : ArterionPlugin.getInstance().getGuildManager().getGuilds()) {
            if (g.hasArtefact()) {
                setOwner(g);
                break;
            }
        }
        //Register custom block
        block = new ArtefactBlock(ArterionPlugin.getInstance().getArterionConfig().artefact);
        ArterionPlugin.getInstance().getSpecialBlockManager().add(block);
        if (owner != null) {
            block.setHasArtefact(false);
        }
        //Register cristals
        cristals[0] = new ArtefactCristal(ArterionPlugin.getInstance().getArterionConfig().artefact_cristal_1);
        cristals[1] = new ArtefactCristal(ArterionPlugin.getInstance().getArterionConfig().artefact_cristal_2);
        cristals[2] = new ArtefactCristal(ArterionPlugin.getInstance().getArterionConfig().artefact_cristal_3);
        //Start reset task
        ArterionPlugin.getInstance().getSchedulers().getMainScheduler().executeInSpigotCircleTimer(new InternalTask() {
            long lastReset = 0;

            @Override
            public void run() {
                if (lastReset + 100000 < System.currentTimeMillis()) {
                    LocalDateTime time = LocalDateTime.ofInstant(Instant.now(), ArterionPlugin.SERVER_TIME_ZONE);
                    ZonedDateTime zoned = time.atZone(ArterionPlugin.SERVER_TIME_ZONE).withZoneSameInstant(ArterionPlugin.TIME_ZONE);
                    if (zoned.getDayOfWeek() == DayOfWeek.WEDNESDAY || zoned.getDayOfWeek() == DayOfWeek.SATURDAY) {
                        if (zoned.getHour() == 19 && zoned.getMinute() == 45) {
                            lastReset = System.currentTimeMillis();
                            zoned = zoned.withHour(20);
                            zoned = zoned.withMinute(0);
                            zoned = zoned.withSecond(0);
                            zoned = zoned.withNano(0);
                            beginEventReplay(zoned.toLocalDateTime());
                        } else if (zoned.getHour() == 20 && zoned.getMinute() == 0) {
                            lastReset = System.currentTimeMillis();
                            reset();
                        }
                    }
                }
            }
        }, 200, 200);
        //Create region
        ArterionPlugin.getInstance().getRegionManager().registerRegion(new ArtefactRegion(Bukkit.getWorlds().get(0), -15, -8, -23, -14));
        //Reset arte if it has no owner
        if (owner == null) reset();
    }

    public static Guild getOwner() {
        return owner;
    }

    public static void setOwner(Guild owner) {
        Artefact.owner = owner;
    }

    public static ArterionPlayer getCarrier() {
        return carrier;
    }

    public static void setCarrier(ArterionPlayer carrier) {
        Artefact.carrier = carrier;
    }

    public static boolean areCristalsAlive() {
        for (ArtefactCristal cristal : cristals) {
            if (cristal.getHealth() > 0) return true;
        }
        return false;
    }

    public static boolean hasClaim(Guild guild) {
        for (ArtefactCristal cristal : cristals) {
            if (cristal.getClaimedBy() != null && cristal.getClaimedBy().equals(guild)) return true;
        }
        return false;
    }

    public static ArtefactBlock getBlock() {
        return block;
    }

    public static ArtefactCristal[] getCristals() {
        return cristals;
    }

    public static int countLivingCristals() {
        int c = 0;
        for (ArtefactCristal cristal : cristals) {
            if (cristal.getHealth() > 0) c++;
        }
        return c;
    }

    public static ArtefactFight getFight() {
        return fight;
    }

    protected static void setFight(ArtefactFight fight) {
        Artefact.fight = fight;
    }

    public static void updateObjectives() {
        if (getFight() != null) {
            getFight().updateObjectives();
        }
    }

    public static void beginEventReplay(LocalDateTime localTime) {
        if (eventReplay == null) {
            eventReplay = new EventReplay(UUID.randomUUID(),
                    "artefact_fights",
                    ArterionPlugin.getInstance().getArterionConfig().artefact,
                    ArterionPlugin.getInstance().getFormulaManager().ARTEFACT_REPLAY_CHUNK_DISTANCE.evaluateInt(),
                    r -> {
                        //Print intro and set tab list header
                        String time = DateTimeFormatter.ISO_DATE_TIME.format(localTime.atZone(ArterionPlugin.SERVER_TIME_ZONE).withZoneSameInstant(ArterionPlugin.TIME_ZONE)).replaceFirst("T", " ");
                        r.setTablistHeaderFooter(eventReplay.getTranslation("artefact.replay.title"), eventReplay.getTranslation("artefact.replay.subtitle", time));
                        r.setServerName(eventReplay.getTranslation("artefact.replay.servername", time).replace("&", "\247"));
                        eventReplay.appendTranslationToRecordingChat("artefact.replay.intro1");
                        eventReplay.appendTranslationToRecordingChat("artefact.replay.intro2");
                        eventReplay.appendTranslationToRecordingChat("artefact.replay.intro3", time);
                    },
                    r -> {
                        //Print outro
                        eventReplay.appendTranslationToRecordingChat("artefact.replay.outro1");
                        eventReplay.appendTranslationToRecordingChat("artefact.replay.outro2");
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

    public static void endEventReplay(BiConsumer<File, UUID> callback) {
        if (eventReplay != null) {
            eventReplay.finalize(callback);
            eventReplay = null;
        }
    }

    public static void doReplayEvent() {
        if (eventReplay != null) {
            eventReplay.onEvent();
        }
    }

    public static EventReplay getEventReplay() {
        return eventReplay;
    }

    public static void appendTranslationToRecordingChat(String key, Object... values) {
        if (eventReplay != null) {
            eventReplay.appendTranslationToRecordingChat(key, values);
        }
    }

    public static TemporalAccessor getNextReset() {
        LocalDateTime time = LocalDateTime.ofInstant(Instant.now(), ArterionPlugin.SERVER_TIME_ZONE);
        ZonedDateTime zoned = time.atZone(ArterionPlugin.SERVER_TIME_ZONE).withZoneSameInstant(ArterionPlugin.TIME_ZONE);
        if (zoned.getHour() >= 20) {
            zoned = zoned.plusHours(5);
        }
        zoned = zoned.withHour(20);
        zoned = zoned.withMinute(0);
        zoned = zoned.withSecond(0);
        zoned = zoned.withNano(0);
        switch (zoned.getDayOfWeek()) {
            case SUNDAY:
                zoned = zoned.plusDays(3);
                break;
            case MONDAY:
            case THURSDAY:
                zoned = zoned.plusDays(2);
                break;
            case TUESDAY:
            case FRIDAY:
                zoned = zoned.plusDays(1);
                break;
            case WEDNESDAY:
            case SATURDAY:
                break;
        }
        return zoned;
    }
}
