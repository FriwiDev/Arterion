package me.friwi.arterion.plugin.combat.gamemode.external;

import me.friwi.arterion.plugin.ArterionPlugin;
import me.friwi.arterion.plugin.combat.gamemode.TemporaryWorld;
import me.friwi.arterion.plugin.combat.gamemode.TemporaryWorldConfig;
import me.friwi.arterion.plugin.combat.hook.TriTuple;
import me.friwi.arterion.plugin.combat.hook.Tuple;
import me.friwi.arterion.plugin.combat.skill.Objective;
import me.friwi.arterion.plugin.combat.team.Team;
import me.friwi.arterion.plugin.guild.Guild;
import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.player.ArterionPlayerUtil;
import me.friwi.arterion.plugin.util.config.api.ConfigAPI;
import me.friwi.arterion.plugin.util.database.entity.DatabasePlayer;
import me.friwi.arterion.plugin.util.language.api.LanguageAPI;
import me.friwi.recordable.RecordingCreaterFactory;
import me.friwi.recordable.RecordingCreator;
import org.bukkit.*;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;

public abstract class ExternalFight<T> {
    private TemporaryWorld world;
    private List<Team> teams;
    private ExternalFightConfig config;
    private RecordingCreator recording;
    private Consumer<RecordingCreator> intro, outro;
    private String replayType;
    private T gameModeConfig;
    private UUID replayUUID;

    public ExternalFight(File templateDir, T gameModeConfig, String replayType, Consumer<RecordingCreator> intro, Consumer<RecordingCreator> outro) {
        this.intro = intro;
        this.outro = outro;
        this.replayType = replayType;
        this.teams = new LinkedList<>();
        this.config = new ExternalFightConfig();
        this.gameModeConfig = gameModeConfig;
        this.world = ArterionPlugin.getInstance().getTemporaryWorldManager().createWorld(templateDir, succ -> {
            if (succ) {
                ConfigAPI.readConfig(config, new File(world.getWorldDir(), "fight.cfg"));
                ConfigAPI.readConfig(gameModeConfig, new File(world.getWorldDir(), "gamemode.cfg"));
                onWorldLoaded();
            } else {
                onFailWorldLoad();
            }
        });
        ArterionPlugin.getInstance().getExternalFightManager().registerFight(this);
    }

    public abstract void onWorldLoaded();

    public abstract void onFailWorldLoad();

    public abstract void onWorldUnload(boolean instant);

    public abstract void onReplaySaved(File file, boolean instant);

    public abstract boolean onDeath(ArterionPlayer player);

    public abstract boolean onQuit(ArterionPlayer player, boolean defineSpawnPosition);

    public abstract Location onRespawn(ArterionPlayer player);

    public void beginReplay() {
        this.recording = RecordingCreaterFactory.newRecording();
        try {
            int xmin = Math.min(getWorldConfig().min_chunk_x, getWorldConfig().max_chunk_x);
            int zmin = Math.min(getWorldConfig().min_chunk_z, getWorldConfig().max_chunk_z);
            int xmax = Math.max(getWorldConfig().min_chunk_x, getWorldConfig().max_chunk_x);
            int zmax = Math.max(getWorldConfig().min_chunk_z, getWorldConfig().max_chunk_z);
            replayUUID = UUID.randomUUID();
            this.recording.initializeRecording(new File(ArterionPlugin.REPLAY_DIR + File.separator + replayType,
                            replayUUID.toString().replace("-", "")),
                    new Location(getWorld(), getConfig().replay_spawn_x, getConfig().replay_spawn_y, getConfig().replay_spawn_z, getConfig().replay_spawn_yaw, getConfig().replay_spawn_pitch),
                    xmin, xmax,
                    zmin, zmax);
            this.recording.beginRecording();
            ArterionPlugin.getInstance().getRecordingManager().getActiveRecordings().add(this.recording);
            intro.accept(recording);

            getRecording().createObjective("hp", "\247c\u2764");
            getRecording().displayObjectiveBelowName("hp");

            for (Player p : ArterionPlugin.getOnlinePlayers()) {
                ArterionPlayer ap = ArterionPlayerUtil.get(p);
                if (ap != null) {
                    getRecording().addOrUpdateScore(ap.getName(), "hp", ap.getHealth());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            this.recording = null;
        }
    }

    protected void endReplay(Consumer<File> callback) {
        if (this.recording != null) {
            //Setup player teams etc
            Map<TriTuple<ChatColor, Integer, String>, List<Tuple<UUID, String>>> registeredTeams = new HashMap<>();
            Set<String> others = new HashSet<>();
            for (UUID u : this.recording.getOccuringPlayers()) {
                Team team = getTeamByFormerMember(u);
                if (team == null) {
                    String name = Bukkit.getOfflinePlayer(u).getName();
                    others.add(name);
                    getRecording().setPlayerListName(u, "\2478" + name);
                } else {
                    DatabasePlayer player = team.getFormerMember(u).getPersistenceHolder();
                    Guild guild = ArterionPlugin.getInstance().getGuildManager().getGuildByMemberUUID(u);
                    TriTuple<ChatColor, Integer, String> regex = new TriTuple<>(team.getColor(), player.getLevel(), guild == null ? null : guild.getTag());
                    List<Tuple<UUID, String>> members = registeredTeams.get(regex);
                    if (members == null) {
                        members = new LinkedList<>();
                        registeredTeams.put(regex, members);
                    }
                    members.add(new Tuple<>(u, player.getName()));
                }
            }
            //Register teams
            for (Map.Entry<TriTuple<ChatColor, Integer, String>, List<Tuple<UUID, String>>> e : registeredTeams.entrySet()) {
                ChatColor color = e.getKey().getFirstValue();
                String prefix = color.toString();
                if (e.getKey().getThirdValue() != null) {
                    prefix = "\2477[" + color.toString() + e.getKey().getThirdValue() + "\2477] " + color.toString();
                }
                String suffix = " \2477[\2476" + e.getKey().getSecondValue() + "\2477]";
                List<String> members = new LinkedList<>();
                for (Tuple<UUID, String> member : e.getValue()) {
                    getRecording().setPlayerListName(member.getFirstValue(), prefix + member.getSecondValue() + suffix);
                    members.add(member.getSecondValue());
                }
                getRecording().createTeam(e.getKey().getFirstValue().name() + e.getKey().getSecondValue() + e.getKey().getThirdValue(), color.name(), e.getKey().getFirstValue().name() + e.getKey().getSecondValue() + e.getKey().getThirdValue(), prefix, suffix, members);
            }
            if (others.size() > 0) {
                getRecording().createTeam("others", ChatColor.DARK_GRAY.name(), "others", "\2478", "", others);
            }
            outro.accept(recording);
            //Unsubscribe from updates
            ArterionPlugin.getInstance().getRecordingManager().getActiveRecordings().remove(getRecording());
            recording.endRecording(callback);
        } else {
            callback.accept(null);
        }
    }

    public Team addTeam(ChatColor color) {
        Team team = new Team(color);
        teams.add(team);
        return team;
    }

    public Team getTeam(ChatColor color) {
        for (Team t : teams) {
            if (t.getColor().equals(color)) return t;
        }
        return null;
    }

    public boolean removeTeam(Team team) {
        team.disband();
        return teams.remove(team);
    }

    public Team removeTeam(ChatColor color) {
        Team t = null;
        Iterator<Team> it = teams.iterator();
        while (it.hasNext()) {
            Team f = it.next();
            if (f.getColor().equals(color)) {
                t = f;
                f.disband();
                it.remove();
            }
        }
        return t;
    }

    public void disbandTeams() {
        Iterator<Team> it = teams.iterator();
        while (it.hasNext()) {
            Team f = it.next();
            f.disband();
            it.remove();
        }
    }

    public ExternalFightConfig getConfig() {
        return config;
    }

    public TemporaryWorldConfig getWorldConfig() {
        return world.getConfig();
    }

    public World getWorld() {
        if (world == null) return null;
        return world.getWorld();
    }

    public boolean isChunkInGame(int x, int z) {
        return world.isWantedChunk(x, z);
    }

    public RecordingCreator getRecording() {
        return recording;
    }

    public List<Team> getTeams() {
        return teams;
    }

    public Team getTeamByMember(UUID uuid) {
        for (Team team : teams) {
            if (team.getMember(uuid) != null) return team;
        }
        return null;
    }

    public Team getTeamByFormerMember(UUID uuid) {
        for (Team team : teams) {
            if (team.getFormerMember(uuid) != null) return team;
        }
        return null;
    }

    public void endFight() {
        endFight(false, true);
    }

    public void endFight(boolean instant, boolean autoRemove) {
        if (autoRemove) ArterionPlugin.getInstance().getExternalFightManager().removeFight(this);
        onWorldUnload(instant);
        if (this.recording != null) {
            Object lock = new Object();
            endReplay(file -> {
                onReplaySaved(file, instant);
                if (instant) {
                    synchronized (lock) {
                        lock.notifyAll();
                    }
                }
            });
            if (instant) {
                synchronized (lock) {
                    try {
                        lock.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        setObjectives(null);
        disbandTeams();
        ArterionPlugin.getInstance().getTemporaryWorldManager().unloadWorld(world, instant);
        world = null;
    }

    public void sendTranslation(String key, Object... values) {
        for (Team team : teams) {
            for (ArterionPlayer p : team.getMembers()) {
                p.sendTranslation(key, values);
            }
        }
    }

    public boolean isParticipating(ArterionPlayer player) {
        for (Team t : teams) {
            for (ArterionPlayer p : t.getMembers()) {
                if (player.equals(p)) return true;
            }
        }
        if (getWorld() != null && getWorld().equals(player.getBukkitPlayer().getWorld())) return true;
        return false;
    }

    public void setObjectives(Objective objective) {
        for (Team team : teams) team.setObjective(objective);
    }

    public void playSound(Sound sound, float volume, float pitch) {
        for (Team team : teams) {
            for (ArterionPlayer p : team.getMembers()) {
                p.getBukkitPlayer().playSound(p.getBukkitPlayer().getLocation(), sound, volume, pitch);
            }
        }
    }

    public void appendTranslationToRecordingChat(String key, Object... values) {
        if (getRecording() != null) {
            getRecording().addChat(LanguageAPI.getLanguage(LanguageAPI.DEFAULT_LANGUAGE).getTranslation(key).translate(values).getMessage());
        }
    }

    public String getTranslation(String key, Object... values) {
        return LanguageAPI.getLanguage(LanguageAPI.DEFAULT_LANGUAGE).getTranslation(key).translate(values).getMessage();
    }

    public T getGameModeConfig() {
        return gameModeConfig;
    }

    public UUID getReplayUUID() {
        return replayUUID;
    }
}
