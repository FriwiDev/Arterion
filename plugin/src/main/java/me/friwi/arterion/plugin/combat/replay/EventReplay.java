package me.friwi.arterion.plugin.combat.replay;

import me.friwi.arterion.plugin.ArterionPlugin;
import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.player.ArterionPlayerUtil;
import me.friwi.arterion.plugin.util.language.api.Language;
import me.friwi.arterion.plugin.util.language.api.LanguageAPI;
import me.friwi.arterion.plugin.util.scheduler.InternalTask;
import me.friwi.recordable.RecordingCreaterFactory;
import me.friwi.recordable.RecordingCreator;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class EventReplay {
    public static final int TIMEOUT = 20 * 60 * 1000;
    public static final ChatColor[] PARTICIPANT_COLORS = new ChatColor[]{
            ChatColor.RED,
            ChatColor.AQUA,
            ChatColor.GREEN,
            ChatColor.YELLOW,
            ChatColor.BLUE,
            ChatColor.GOLD,
            ChatColor.DARK_PURPLE,
            ChatColor.DARK_AQUA,
            ChatColor.LIGHT_PURPLE,
            ChatColor.DARK_BLUE,
            ChatColor.DARK_GREEN,
            ChatColor.DARK_RED
    };

    private UUID uuid;
    private String dir;
    private Location location;
    private int range;
    private Consumer<RecordingCreator> intro, outro;
    private BiConsumer<RecordingCreator, Collection<UUID>> setupPlayersForReplay;
    private int replayCount = 0;
    private RecordingCreator recording;
    private List<File> allRecordings = new LinkedList<>();
    private List<RecordingCreator> allCreators = new LinkedList<>();
    private boolean finalizeable = true;
    private Object finalizeLock = new Object();
    private long lastEvent = 0;

    public EventReplay(UUID uuid, String dir, Location location, int range, Consumer<RecordingCreator> intro, Consumer<RecordingCreator> outro, BiConsumer<RecordingCreator, Collection<UUID>> setupPlayersForReplay) {
        this.uuid = uuid;
        this.dir = dir;
        this.location = location;
        this.range = range;
        this.intro = intro;
        this.outro = outro;
        this.setupPlayersForReplay = setupPlayersForReplay;
    }

    private void beginReplay() {
        this.replayCount++;
        this.recording = RecordingCreaterFactory.newRecording();
        try {
            this.recording.initializeRecording(new File(ArterionPlugin.REPLAY_DIR + File.separator + dir,
                            uuid.toString().replace("-", "") + File.separator + "part_" + replayCount), location,
                    location.getChunk().getX() - range, location.getChunk().getX() + range,
                    location.getChunk().getZ() - range, location.getChunk().getZ() + range);
            this.recording.beginRecording();
            ArterionPlugin.getInstance().getRecordingManager().getActiveRecordings().add(this.recording);
            allCreators.add(this.recording);
        } catch (IOException e) {
            e.printStackTrace();
            this.recording = null;
        }
        finalizeable = false;
        setupRecording();
    }

    private void setupRecording() {
        if (getRecording() != null) {
            this.intro.accept(recording);

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

    protected void saveReplay(Consumer<File> callback) {
        if (this.recording != null) {
            this.setupPlayersForReplay.accept(this.recording, this.recording.getOccuringPlayers());
            this.outro.accept(recording);
            //Unsubscribe from updates
            ArterionPlugin.getInstance().getRecordingManager().getActiveRecordings().remove(getRecording());
            recording.endRecording(callback);
        } else {
            callback.accept(null);
        }
    }

    private void endReplay() {
        saveReplay(file -> {
            if (file != null) allRecordings.add(file);
            synchronized (finalizeLock) {
                finalizeable = true;
                finalizeLock.notifyAll();
            }
        });
        recording = null;
    }

    private RecordingCreator getRecording() {
        return recording;
    }

    public void finalize(BiConsumer<File, UUID> callback) {
        if (recording != null) endReplay();
        new Thread() {
            public void run() {
                setName("Event-Replay-Compressor-" + uuid);
                try {
                    synchronized (finalizeLock) {
                        if (!finalizeable) {
                            finalizeLock.wait();
                        }
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    callback.accept(null, null);
                    return;
                }
                File save = new File(ArterionPlugin.REPLAY_DIR + File.separator + dir,
                        uuid.toString().replace("-", "") + File.separator + uuid.toString().replace("-", "") + ".zip");
                if (!save.getParentFile().exists()) save.getParentFile().mkdirs();
                try {
                    save.createNewFile();
                    ZipOutputStream out = new ZipOutputStream(new FileOutputStream(save));
                    for (File f : allRecordings) {
                        ZipEntry e = new ZipEntry(f.getName());
                        out.putNextEntry(e);
                        FileInputStream fis = new FileInputStream(f);
                        byte[] copy = new byte[4096];
                        while (fis.available() > 0) {
                            int r = fis.read(copy);
                            out.write(copy, 0, r);
                        }
                        fis.close();
                        out.closeEntry();
                        f.delete();
                        f.getParentFile().delete(); //Delete containing dir
                    }
                    out.close();
                    callback.accept(save, uuid);
                } catch (IOException e) {
                    e.printStackTrace();
                    callback.accept(null, null);
                }
            }
        }.start();
    }

    public String getTranslation(String key, Object... values) {
        return LanguageAPI.getLanguage(LanguageAPI.DEFAULT_LANGUAGE).getTranslation(key).translate(values).getMessage();
    }

    public void appendTranslationToRecordingChat(String key, Object... values) {
        this.appendChatToRecording(this.getTranslation(key, values));
    }

    public void appendChatToRecording(String msg) {
        if (this.recording != null) this.recording.addChat(msg);
    }

    public void appendChatJsonToRecording(String json) {
        if (this.recording != null) this.recording.addChatJson(json);
    }

    public Language getLanguage() {
        return LanguageAPI.getLanguage(LanguageAPI.DEFAULT_LANGUAGE);
    }

    public Set<UUID> getOccuringPlayers() {
        Set<UUID> set = new HashSet<>();
        for (RecordingCreator c : allCreators) {
            set.addAll(c.getOccuringPlayers());
        }
        return set;
    }

    public void onEvent() {
        lastEvent = System.currentTimeMillis();
        if (recording == null) {
            beginReplay();
            ArterionPlugin.getInstance().getSchedulers().getMainScheduler().executeInSpigotCircleTimer(new InternalTask() {
                @Override
                public void run() {
                    if (recording == null) {
                        //Recording was terminated from elsewhere
                        cancel();
                        return;
                    }
                    if (lastEvent + TIMEOUT < System.currentTimeMillis()) {
                        endReplay();
                    }
                }
            }, 20, 20);
        }
    }
}
