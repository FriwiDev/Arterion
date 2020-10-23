package me.friwi.arterion.plugin.combat.gamemode;

import com.google.common.collect.Lists;
import me.friwi.arterion.plugin.ArterionPlugin;
import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.player.ArterionPlayerUtil;
import me.friwi.arterion.plugin.util.config.api.ConfigAPI;
import me.friwi.arterion.plugin.util.file.FileUtils;
import me.friwi.arterion.plugin.util.scheduler.InternalTask;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.UUID;
import java.util.function.Consumer;

public class TemporaryWorld {
    private File templateDir;
    private File worldDir;

    private String name;
    private World world;
    private TemporaryWorldConfig config;

    private boolean checkInBounds = true;

    public TemporaryWorld(File templateDir) {
        this.templateDir = templateDir;
    }

    protected void loadWorldAsync(Consumer<Boolean> successCallback) {
        this.name = UUID.randomUUID().toString().replace("-", "");
        this.worldDir = new File(ArterionPlugin.getInstance().getDataFolder().getParentFile().getParentFile(), name);
        System.out.println("Creating a new temporary world!");
        new Thread(() -> {
            try {
                System.out.println("Copying files...");
                FileUtils.copyFolder(templateDir, worldDir);
                config = ConfigAPI.readConfig(new TemporaryWorldConfig(), new File(worldDir, "temporaryWorld.cfg"));
                ArterionPlugin.getInstance().getSchedulers().getMainScheduler().executeInSpigotCircle(new InternalTask() {
                    @Override
                    public void run() {
                        System.out.println("Loading world...");
                        world = Bukkit.getServer().createWorld(new WorldCreator(name));
                        world.setGameRuleValue("keepInventory", "true");
                        world.setGameRuleValue("announceAdvancements", "false");
                        world.setGameRuleValue("naturalRegeneration", "true");
                        System.out.println("Loading chunks...");
                        ArterionPlugin.getInstance().getSchedulers().getMainScheduler().executeInSpigotCircleTimer(new InternalTask() {
                            int xmin = Math.min(config.min_chunk_x, config.max_chunk_x);
                            int zmin = Math.min(config.min_chunk_z, config.max_chunk_z);
                            int xmax = Math.max(config.min_chunk_x, config.max_chunk_x);
                            int zmax = Math.max(config.min_chunk_z, config.max_chunk_z);
                            int x = xmin;
                            int z = zmin;
                            boolean once = false;

                            @Override
                            public void run() {
                                if (world == null) {
                                    cancel();
                                    return;
                                }
                                if (x > xmax) {
                                    x = xmin;
                                    z++;
                                }
                                if (z > zmax) {
                                    cancel();
                                    if (once) return;
                                    once = true;
                                    System.out.println("World loaded successfully!");
                                    ArterionPlugin.getInstance().getSchedulers().getMainScheduler().executeInSpigotCircleTimer(new InternalTask() {
                                        @Override
                                        public void run() {
                                            if (world == null) {
                                                cancel();
                                                return;
                                            }
                                            if (!checkInBounds) {
                                                cancel();
                                                return;
                                            }
                                            for (Player p : getWorld().getPlayers()) {
                                                ArterionPlayer ap = ArterionPlayerUtil.get(p);
                                                if (ap.getTemporaryWorldBackupLocation() != null && ap.getTemporaryWorldBackupLocation().getWorld() != null && ap.getTemporaryWorldBackupLocation().getWorld().equals(getWorld())) {
                                                    if (!isWantedChunk(ap.getBukkitPlayer().getLocation().getChunk().getX(), ap.getBukkitPlayer().getLocation().getChunk().getZ())) {
                                                        ap.getBukkitPlayer().teleport(ap.getTemporaryWorldBackupLocation());
                                                        ap.sendTranslation("world.edge");
                                                        continue;
                                                    }
                                                }
                                                ap.setTemporaryWorldBackupLocation(p.getLocation().clone());
                                            }
                                        }
                                    }, 5l, 5l);
                                    successCallback.accept(true);
                                    return;
                                }
                                world.getChunkAt(x, z).load();
                                x++;
                            }
                        }, 1, 1);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                try {
                    FileUtils.deleteFolder(worldDir);
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
                successCallback.accept(false);
            }
        }).start();
    }

    protected void unloadWorld(boolean instant) {
        System.out.println("Unmounting world...");
        checkInBounds = false;
        for (Player p : Lists.newCopyOnWriteArrayList(world.getPlayers())) {
            ArterionPlayer ap = ArterionPlayerUtil.get(p);
            p.setGameMode(GameMode.SURVIVAL);
            p.teleport(ArterionPlugin.getInstance().getArterionConfig().spawn);
            ap.updateRegion(ArterionPlugin.getInstance().getArterionConfig().spawn);
            p.setGameMode(GameMode.SURVIVAL);
        }
        boolean success = Bukkit.unloadWorld(world, false);
        Runnable delete = () -> {
            try {
                System.out.println("Deleting files...");
                try {
                    FileUtils.deleteFolder(worldDir);
                    if (!instant && worldDir.exists()) {
                        Thread.sleep(5000);
                        FileUtils.deleteFolder(worldDir);
                    }
                    if (worldDir.exists()) worldDir.deleteOnExit();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
                System.out.println("Done unloading world!");
            } catch (Exception e) {
                e.printStackTrace();
            }
        };

        if (!instant && !success) {
            ArterionPlugin.getInstance().getSchedulers().getMainScheduler().executeInSpigotCircleTimer(new InternalTask() {
                @Override
                public void run() {
                    if (world == null) {
                        cancel();
                        return;
                    }
                    System.out.println("Unmounting world " + world.getName() + " again...");
                    boolean success = Bukkit.unloadWorld(world, false);
                    if (success) {
                        cancel();
                        new Thread(delete).start();
                    }
                }
            }, 20 * 3, 20 * 3);
        } else {
            if (instant) delete.run();
            else new Thread(delete).start();
        }
        world = null;
    }

    public String getName() {
        return name;
    }

    public World getWorld() {
        return world;
    }

    public TemporaryWorldConfig getConfig() {
        return config;
    }

    public boolean isWantedChunk(int x, int z) {
        if (config == null) return false;
        int xmin = Math.min(config.min_chunk_x, config.max_chunk_x);
        int zmin = Math.min(config.min_chunk_z, config.max_chunk_z);
        int xmax = Math.max(config.min_chunk_x, config.max_chunk_x);
        int zmax = Math.max(config.min_chunk_z, config.max_chunk_z);
        return x >= xmin && x <= xmax && z >= zmin && z <= zmax;
    }

    public File getWorldDir() {
        return worldDir;
    }
}
