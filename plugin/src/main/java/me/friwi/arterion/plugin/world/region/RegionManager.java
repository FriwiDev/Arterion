package me.friwi.arterion.plugin.world.region;

import me.friwi.arterion.plugin.ArterionPlugin;
import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.player.ArterionPlayerUtil;
import me.friwi.arterion.plugin.util.scheduler.InternalTask;
import me.friwi.arterion.plugin.world.chunk.ArterionChunk;
import me.friwi.arterion.plugin.world.chunk.ArterionChunkUtil;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class RegionManager {
    private ArterionPlugin plugin;
    private ArrayList<Region> regions;
    private Region WILDERNESS = new WildernessRegion();

    public RegionManager(ArterionPlugin plugin) {
        this.plugin = plugin;
        this.regions = new ArrayList<>(50);
    }

    public void init() {
        //Register regions
        this.registerRegion(new SpawnRegion(Bukkit.getWorlds().get(0), -4, 1, 6, 6));
        this.registerRegion(new SpawnRegion(Bukkit.getWorlds().get(0), 2, 3, -4, -4));
        this.registerRegion(new SpawnRegion(Bukkit.getWorlds().get(0), 2, 2, -8, -8));
        this.registerRegion(new SpawnRegion(Bukkit.getWorlds().get(0), 2, 6, 0, 4));
        this.registerRegion(new SpawnRegion(Bukkit.getWorlds().get(0), 2, 6, 0, 4));
        this.registerRegion(new SpawnRegion(Bukkit.getWorlds().get(0), 4, 7, -3, -1));
        this.registerRegion(new SpawnRegion(Bukkit.getWorlds().get(0), 2, 6, 0, 4));
        this.registerRegion(new SpawnRegion(Bukkit.getWorlds().get(0), 4, 5, -4, -5));
        this.registerRegion(new SpawnRegion(Bukkit.getWorlds().get(0), 4, 4, -6, -6));
        this.registerRegion(new SpawnRegion(Bukkit.getWorlds().get(0), -7, -5, -5, 4));
        this.registerRegion(new SpawnRegion(Bukkit.getWorlds().get(0), -4, 4, 6, 6));
        this.registerRegion(new SpawnRegion(Bukkit.getWorlds().get(0), -8, -8, -2, 3));
        this.registerRegion(new SpawnRegion(Bukkit.getWorlds().get(0), 2, 5, 5, 5));
        this.registerRegion(new SpawnRegion(Bukkit.getWorlds().get(0), -5, -5, -8, -6));
        this.registerRegion(new SpawnRegion(Bukkit.getWorlds().get(0), -6, -6, -6, -6));
        this.registerRegion(new SpawnRegion(Bukkit.getWorlds().get(0), -6, -5, 5, 5));
        this.registerRegion(new SpawnRegion(Bukkit.getWorlds().get(0), -1, 1, 7, 7));
        this.registerRegion(new SpawnRegion(Bukkit.getWorlds().get(0), 7, 7, 0, 1));
        this.registerRegion(new SpawnRegion(Bukkit.getWorlds().get(0), 6, 6, -4, -4));
        this.registerRegion(new SpawnRegion(Bukkit.getWorlds().get(0), 2, 2, -3, -3));
        this.registerRegion(new SpawnRegion(Bukkit.getWorlds().get(0), 2, 2, -1, -1));
        this.registerRegion(new SpawnRegion(Bukkit.getWorlds().get(0), -4, 1, -9, 5));
        this.registerRegion(new SpawnRegion(Bukkit.getWorlds().get(0), 2, 3, -7, -5));
        this.registerRegion(new SpawnRegion(Bukkit.getWorlds().get(0), -7, -5, -5, 4));
        this.registerRegion(new SpawnRegion(Bukkit.getWorlds().get(0), 3, 4, 6, 6));

        this.registerRegion(new PvPRegion("north", Bukkit.getWorlds().get(0), -7, 10, -10, -10));
        this.registerRegion(new PvPRegion("north", Bukkit.getWorlds().get(0), 11, 19, -32, -28));
        this.registerRegion(new PvPRegion("north", Bukkit.getWorlds().get(0), 11, 15, -27, -24));
        this.registerRegion(new PvPRegion("north", Bukkit.getWorlds().get(0), 11, 11, -23, -11));
        this.registerRegion(new PvPRegion("north", Bukkit.getWorlds().get(0), 12, 13, -23, -22));
        this.registerRegion(new PvPRegion("north", Bukkit.getWorlds().get(0), 12, 12, -21, -21));
        this.registerRegion(new PvPRegion("north", Bukkit.getWorlds().get(0), 14, 14, -23, -23));
        this.registerRegion(new PvPRegion("north", Bukkit.getWorlds().get(0), 16, 17, -27, -26));
        this.registerRegion(new PvPRegion("north", Bukkit.getWorlds().get(0), 16, 16, -25, -25));
        this.registerRegion(new PvPRegion("north", Bukkit.getWorlds().get(0), 18, 18, -27, -27));
        this.registerRegion(new PvPRegion("north", Bukkit.getWorlds().get(0), 20, 22, -32, -31));
        this.registerRegion(new PvPRegion("north", Bukkit.getWorlds().get(0), 23, 23, -32, -32));
        this.registerRegion(new PvPRegion("north", Bukkit.getWorlds().get(0), 20, 21, -30, -30));
        this.registerRegion(new PvPRegion("north", Bukkit.getWorlds().get(0), 20, 20, -29, -29));
        this.registerRegion(new PvPRegion("north", Bukkit.getWorlds().get(0), -24, -8, -32, -24));
        this.registerRegion(new PvPRegion("north", Bukkit.getWorlds().get(0), -19, -16, -23, -19));
        this.registerRegion(new PvPRegion("north", Bukkit.getWorlds().get(0), -22, -20, -23, -22));
        this.registerRegion(new PvPRegion("north", Bukkit.getWorlds().get(0), -21, -20, -21, -21));
        this.registerRegion(new PvPRegion("north", Bukkit.getWorlds().get(0), -20, -20, -20, -20));
        this.registerRegion(new PvPRegion("north", Bukkit.getWorlds().get(0), -23, -23, -23, -23));
        this.registerRegion(new PvPRegion("north", Bukkit.getWorlds().get(0), -26, -25, -32, -26));
        this.registerRegion(new PvPRegion("north", Bukkit.getWorlds().get(0), -25, -25, -25, -25));
        this.registerRegion(new PvPRegion("north", Bukkit.getWorlds().get(0), -29, -27, -32, -29));
        this.registerRegion(new PvPRegion("north", Bukkit.getWorlds().get(0), -28, -27, -28, -28));
        this.registerRegion(new PvPRegion("north", Bukkit.getWorlds().get(0), -27, -27, -27, -27));
        this.registerRegion(new PvPRegion("north", Bukkit.getWorlds().get(0), -31, -30, -32, -31));
        this.registerRegion(new PvPRegion("north", Bukkit.getWorlds().get(0), -30, -30, -30, -30));
        this.registerRegion(new PvPRegion("north", Bukkit.getWorlds().get(0), -32, -32, -32, -32));
        this.registerRegion(new PvPRegion("north", Bukkit.getWorlds().get(0), -15, -8, -13, -13));
        this.registerRegion(new PvPRegion("north", Bukkit.getWorlds().get(0), -10, -8, -12, -10));
        this.registerRegion(new PvPRegion("north", Bukkit.getWorlds().get(0), -11, -11, -12, -12));
        this.registerRegion(new PvPRegion("north", Bukkit.getWorlds().get(0), -9, -5, -9, -9));
        this.registerRegion(new PvPRegion("north", Bukkit.getWorlds().get(0), -7, -6, -8, -7));
        this.registerRegion(new PvPRegion("north", Bukkit.getWorlds().get(0), -8, -8, -8, -8));
        this.registerRegion(new PvPRegion("north", Bukkit.getWorlds().get(0), -7, -7, -6, -6));
        this.registerRegion(new PvPRegion("north", Bukkit.getWorlds().get(0), 2, 9, -9, -9));
        this.registerRegion(new PvPRegion("north", Bukkit.getWorlds().get(0), 3, 8, -8, -8));
        this.registerRegion(new PvPRegion("north", Bukkit.getWorlds().get(0), 4, 7, -7, -7));
        this.registerRegion(new PvPRegion("north", Bukkit.getWorlds().get(0), 5, 6, -6, -6));
        this.registerRegion(new PvPRegion("north", Bukkit.getWorlds().get(0), -7, 10, -11, -32));

        this.registerRegion(new PvPRegion("west", Bukkit.getWorlds().get(0), -32, -9, -8, 3));
        this.registerRegion(new PvPRegion("west", Bukkit.getWorlds().get(0), -32, -23, 4, 22));
        this.registerRegion(new PvPRegion("west", Bukkit.getWorlds().get(0), -22, -16, 4, 10));
        this.registerRegion(new PvPRegion("west", Bukkit.getWorlds().get(0), -32, -29, 23, 28));
        this.registerRegion(new PvPRegion("west", Bukkit.getWorlds().get(0), -28, -26, 23, 25));
        this.registerRegion(new PvPRegion("west", Bukkit.getWorlds().get(0), -32, -32, 29, 32));
        this.registerRegion(new PvPRegion("west", Bukkit.getWorlds().get(0), -31, -30, 29, 29));
        this.registerRegion(new PvPRegion("west", Bukkit.getWorlds().get(0), -31, -31, 30, 30));
        this.registerRegion(new PvPRegion("west", Bukkit.getWorlds().get(0), -22, -20, 11, 15));
        this.registerRegion(new PvPRegion("west", Bukkit.getWorlds().get(0), -22, -22, 16, 19));
        this.registerRegion(new PvPRegion("west", Bukkit.getWorlds().get(0), -21, -21, 16, 17));
        this.registerRegion(new PvPRegion("west", Bukkit.getWorlds().get(0), -19, -17, 11, 11));
        this.registerRegion(new PvPRegion("west", Bukkit.getWorlds().get(0), -19, -18, 12, 12));
        this.registerRegion(new PvPRegion("west", Bukkit.getWorlds().get(0), -19, -19, 13, 13));
        this.registerRegion(new PvPRegion("west", Bukkit.getWorlds().get(0), -15, -13, 4, 7));
        this.registerRegion(new PvPRegion("west", Bukkit.getWorlds().get(0), -12, -11, 4, 5));
        this.registerRegion(new PvPRegion("west", Bukkit.getWorlds().get(0), -10, -10, 4, 4));
        this.registerRegion(new PvPRegion("west", Bukkit.getWorlds().get(0), -12, -12, 6, 6));
        this.registerRegion(new PvPRegion("west", Bukkit.getWorlds().get(0), -15, -14, 8, 8));
        this.registerRegion(new PvPRegion("west", Bukkit.getWorlds().get(0), -15, -15, 9, 9));
        this.registerRegion(new PvPRegion("west", Bukkit.getWorlds().get(0), -28, -28, 26, 27));
        this.registerRegion(new PvPRegion("west", Bukkit.getWorlds().get(0), -27, -27, 26, 26));
        this.registerRegion(new PvPRegion("west", Bukkit.getWorlds().get(0), -25, -24, 23, 23));
        this.registerRegion(new PvPRegion("west", Bukkit.getWorlds().get(0), -25, -25, 24, 24));
        this.registerRegion(new PvPRegion("west", Bukkit.getWorlds().get(0), -8, -8, -7, -3));
        this.registerRegion(new PvPRegion("west", Bukkit.getWorlds().get(0), -15, -12, -12, -9));
        this.registerRegion(new PvPRegion("west", Bukkit.getWorlds().get(0), -11, -11, -11, -9));
        this.registerRegion(new PvPRegion("west", Bukkit.getWorlds().get(0), -10, -10, -9, -9));
        this.registerRegion(new PvPRegion("west", Bukkit.getWorlds().get(0), -24, -22, -21, -19));
        this.registerRegion(new PvPRegion("west", Bukkit.getWorlds().get(0), -24, -24, -23, -22));
        this.registerRegion(new PvPRegion("west", Bukkit.getWorlds().get(0), -23, -23, -22, -22));
        this.registerRegion(new PvPRegion("west", Bukkit.getWorlds().get(0), -21, -21, -20, -19));
        this.registerRegion(new PvPRegion("west", Bukkit.getWorlds().get(0), -20, -20, -19, -19));
        this.registerRegion(new PvPRegion("west", Bukkit.getWorlds().get(0), -32, -29, -28, -25));
        this.registerRegion(new PvPRegion("west", Bukkit.getWorlds().get(0), -28, -27, -26, -25));
        this.registerRegion(new PvPRegion("west", Bukkit.getWorlds().get(0), -26, -26, -25, -25));
        this.registerRegion(new PvPRegion("west", Bukkit.getWorlds().get(0), -28, -28, -27, -27));
        this.registerRegion(new PvPRegion("west", Bukkit.getWorlds().get(0), -32, -31, -30, -29));
        this.registerRegion(new PvPRegion("west", Bukkit.getWorlds().get(0), -30, -30, -29, -29));
        this.registerRegion(new PvPRegion("west", Bukkit.getWorlds().get(0), -32, -32, -31, -31));
        this.registerRegion(new PvPRegion("west", Bukkit.getWorlds().get(0), -32, -25, -24, -9));


        this.registerRegion(new PvPRegion("south", Bukkit.getWorlds().get(0), -26, 2, 26, 32));
        this.registerRegion(new PvPRegion("south", Bukkit.getWorlds().get(0), -19, 2, 14, 25));
        this.registerRegion(new PvPRegion("south", Bukkit.getWorlds().get(0), -14, 13, 9, 13));
        this.registerRegion(new PvPRegion("south", Bukkit.getWorlds().get(0), -13, 12, 8, 8));
        this.registerRegion(new PvPRegion("south", Bukkit.getWorlds().get(0), -12, -2, 7, 7));
        this.registerRegion(new PvPRegion("south", Bukkit.getWorlds().get(0), 2, 11, 7, 7));
        this.registerRegion(new PvPRegion("south", Bukkit.getWorlds().get(0), -11, -5, 6, 6));
        this.registerRegion(new PvPRegion("south", Bukkit.getWorlds().get(0), -10, -7, 5, 5));
        this.registerRegion(new PvPRegion("south", Bukkit.getWorlds().get(0), -9, -8, 4, 4));
        this.registerRegion(new PvPRegion("south", Bukkit.getWorlds().get(0), 3, 30, 29, 32));
        this.registerRegion(new PvPRegion("south", Bukkit.getWorlds().get(0), 19, 25, 23, 28));
        this.registerRegion(new PvPRegion("south", Bukkit.getWorlds().get(0), 19, 21, 19, 22));
        this.registerRegion(new PvPRegion("south", Bukkit.getWorlds().get(0), 14, 14, 12, 13));
        this.registerRegion(new PvPRegion("south", Bukkit.getWorlds().get(0), 15, 15, 13, 13));
        this.registerRegion(new PvPRegion("south", Bukkit.getWorlds().get(0), 19, 20, 18, 18));
        this.registerRegion(new PvPRegion("south", Bukkit.getWorlds().get(0), 19, 19, 17, 17));
        this.registerRegion(new PvPRegion("south", Bukkit.getWorlds().get(0), 22, 22, 20, 22));
        this.registerRegion(new PvPRegion("south", Bukkit.getWorlds().get(0), 23, 23, 21, 22));
        this.registerRegion(new PvPRegion("south", Bukkit.getWorlds().get(0), 24, 24, 22, 22));
        this.registerRegion(new PvPRegion("south", Bukkit.getWorlds().get(0), 26, 28, 26, 28));
        this.registerRegion(new PvPRegion("south", Bukkit.getWorlds().get(0), 26, 27, 25, 25));
        this.registerRegion(new PvPRegion("south", Bukkit.getWorlds().get(0), 26, 26, 24, 24));
        this.registerRegion(new PvPRegion("south", Bukkit.getWorlds().get(0), 29, 29, 28, 28));
        this.registerRegion(new PvPRegion("south", Bukkit.getWorlds().get(0), 31, 31, 31, 32));
        this.registerRegion(new PvPRegion("south", Bukkit.getWorlds().get(0), -30, -27, 30, 32));
        this.registerRegion(new PvPRegion("south", Bukkit.getWorlds().get(0), -31, -31, 31, 32));
        this.registerRegion(new PvPRegion("south", Bukkit.getWorlds().get(0), -28, -27, 28, 29));
        this.registerRegion(new PvPRegion("south", Bukkit.getWorlds().get(0), -29, -29, 29, 29));
        this.registerRegion(new PvPRegion("south", Bukkit.getWorlds().get(0), -27, -27, 27, 27));
        this.registerRegion(new PvPRegion("south", Bukkit.getWorlds().get(0), -22, -20, 20, 25));
        this.registerRegion(new PvPRegion("south", Bukkit.getWorlds().get(0), -25, -23, 25, 25));
        this.registerRegion(new PvPRegion("south", Bukkit.getWorlds().get(0), -24, -23, 24, 24));
        this.registerRegion(new PvPRegion("south", Bukkit.getWorlds().get(0), -23, -23, 23, 23));
        this.registerRegion(new PvPRegion("south", Bukkit.getWorlds().get(0), -20, -20, 16, 19));
        this.registerRegion(new PvPRegion("south", Bukkit.getWorlds().get(0), -21, -21, 18, 19));
        this.registerRegion(new PvPRegion("south", Bukkit.getWorlds().get(0), -16, -15, 11, 13));
        this.registerRegion(new PvPRegion("south", Bukkit.getWorlds().get(0), -15, -15, 10, 10));
        this.registerRegion(new PvPRegion("south", Bukkit.getWorlds().get(0), -18, -17, 13, 13));
        this.registerRegion(new PvPRegion("south", Bukkit.getWorlds().get(0), -17, -17, 12, 12));
        this.registerRegion(new PvPRegion("south", Bukkit.getWorlds().get(0), 5, 6, 6, 6));
        this.registerRegion(new PvPRegion("south", Bukkit.getWorlds().get(0), 6, 6, 5, 5));

        this.registerRegion(new BankRegion(Bukkit.getWorlds().get(0), 3, 3, -3, -1));
        this.registerRegion(new BankRegion(Bukkit.getWorlds().get(0), 2, 2, -2, -2));

        this.registerRegion(new PvPRegion("east", Bukkit.getWorlds().get(0), 12, 32, -20, 7));
        this.registerRegion(new PvPRegion("east", Bukkit.getWorlds().get(0), 7, 11, 2, 6));
        this.registerRegion(new PvPRegion("east", Bukkit.getWorlds().get(0), 8, 11, -7, 1));
        this.registerRegion(new PvPRegion("east", Bukkit.getWorlds().get(0), 16, 32, 8, 13));
        this.registerRegion(new PvPRegion("east", Bukkit.getWorlds().get(0), 19, 32, 14, 16));
        this.registerRegion(new PvPRegion("east", Bukkit.getWorlds().get(0), 14, 15, 8, 11));
        this.registerRegion(new PvPRegion("east", Bukkit.getWorlds().get(0), 24, 32, -32, -21));
        this.registerRegion(new PvPRegion("east", Bukkit.getWorlds().get(0), 32, 32, 17, 32));
        this.registerRegion(new PvPRegion("east", Bukkit.getWorlds().get(0), 31, 31, 17, 30));
        this.registerRegion(new PvPRegion("east", Bukkit.getWorlds().get(0), 30, 30, 17, 28));
        this.registerRegion(new PvPRegion("east", Bukkit.getWorlds().get(0), 29, 29, 17, 27));
        this.registerRegion(new PvPRegion("east", Bukkit.getWorlds().get(0), 28, 28, 17, 25));
        this.registerRegion(new PvPRegion("east", Bukkit.getWorlds().get(0), 27, 27, 17, 24));
        this.registerRegion(new PvPRegion("east", Bukkit.getWorlds().get(0), 26, 26, 17, 23));
        this.registerRegion(new PvPRegion("east", Bukkit.getWorlds().get(0), 25, 25, 17, 22));
        this.registerRegion(new PvPRegion("east", Bukkit.getWorlds().get(0), 24, 24, 17, 21));
        this.registerRegion(new PvPRegion("east", Bukkit.getWorlds().get(0), 23, 23, 17, 20));
        this.registerRegion(new PvPRegion("east", Bukkit.getWorlds().get(0), 22, 22, 17, 19));
        this.registerRegion(new PvPRegion("east", Bukkit.getWorlds().get(0), 21, 21, 17, 18));
        this.registerRegion(new PvPRegion("east", Bukkit.getWorlds().get(0), 20, 20, 17, 17));
        this.registerRegion(new PvPRegion("east", Bukkit.getWorlds().get(0), 15, 15, 12, 12));
        this.registerRegion(new PvPRegion("east", Bukkit.getWorlds().get(0), 13, 13, 8, 8));
        this.registerRegion(new PvPRegion("east", Bukkit.getWorlds().get(0), 9, 11, -8, -8));
        this.registerRegion(new PvPRegion("east", Bukkit.getWorlds().get(0), 10, 11, -9, -9));
        this.registerRegion(new PvPRegion("east", Bukkit.getWorlds().get(0), 11, 11, -10, -10));
        this.registerRegion(new PvPRegion("east", Bukkit.getWorlds().get(0), 7, 7, -6, -4));
        this.registerRegion(new PvPRegion("east", Bukkit.getWorlds().get(0), 6, 6, -5, -5));
        this.registerRegion(new PvPRegion("east", Bukkit.getWorlds().get(0), 13, 23, -21, -21));
        this.registerRegion(new PvPRegion("east", Bukkit.getWorlds().get(0), 14, 23, -22, -22));
        this.registerRegion(new PvPRegion("east", Bukkit.getWorlds().get(0), 15, 23, -23, -23));
        this.registerRegion(new PvPRegion("east", Bukkit.getWorlds().get(0), 16, 23, -24, -24));
        this.registerRegion(new PvPRegion("east", Bukkit.getWorlds().get(0), 17, 23, -25, -25));
        this.registerRegion(new PvPRegion("east", Bukkit.getWorlds().get(0), 18, 23, -26, -26));
        this.registerRegion(new PvPRegion("east", Bukkit.getWorlds().get(0), 19, 23, -27, -27));
        this.registerRegion(new PvPRegion("east", Bukkit.getWorlds().get(0), 20, 23, -28, -28));
        this.registerRegion(new PvPRegion("east", Bukkit.getWorlds().get(0), 21, 23, -29, -29));
        this.registerRegion(new PvPRegion("east", Bukkit.getWorlds().get(0), 22, 23, -30, -30));
        this.registerRegion(new PvPRegion("east", Bukkit.getWorlds().get(0), 23, 23, -31, -31));

        //End spawn
        Chunk endSpawn = Bukkit.getWorlds().get(2).getSpawnLocation().getChunk();
        this.registerRegion(new PvPRegion("end", endSpawn.getWorld(), endSpawn.getX() - 1, endSpawn.getX() + 1, endSpawn.getZ() - 1, endSpawn.getZ() + 1));

        //Calculate regions for all already loaded chunks
        for (World w : Bukkit.getWorlds()) {
            for (Chunk c : w.getLoadedChunks()) {
                ArterionChunkUtil.getNonNull(c).setRegion(this.calculateRegionAt(w, c.getX(), c.getZ()));
            }
        }

        //Start player position checks
        plugin.getSchedulers().getMainScheduler().executeInSpigotCircleTimer(new InternalTask() {
            @Override
            public void run() {
                for (Player p : ArterionPlugin.getOnlinePlayers()) {
                    ArterionPlayer ep = ArterionPlayerUtil.get(p);
                    if (ep != null) {
                        ep.updatePlayer();
                    }
                }
            }
        }, 5L, 5L);
    }

    public void reset() {
        regions.clear();
        for (World w : Bukkit.getWorlds()) {
            for (Chunk c : w.getLoadedChunks()) {
                ArterionChunkUtil.getNonNull(c).setRegion(null);
            }
        }
    }

    public void registerRegion(Region region) {
        this.regions.add(region);
        region.forEachChunkAsync(c -> {
            ArterionChunk ec = ArterionChunkUtil.getNonNull(c);
            ec.setRegion(region);
        });
    }

    public void registerRegionParallel(Region region, int batchSize, Runnable finished) {
        this.regions.add(region);
        region.forEachChunkParallel(c -> {
            ArterionChunk ec = ArterionChunkUtil.getNonNull(c);
            ec.setRegion(region);
        }, batchSize, finished);
    }

    public void unRegisterRegion(Region region) {
        if (!this.regions.remove(region)) return; //Only recalculate when region was present
        region.forEachChunkAsync(c -> {
            ArterionChunk ec = ArterionChunkUtil.getNonNull(c);
            ec.setRegion(this.calculateRegionAt(c.getWorld(), c.getX(), c.getZ()));
        });
    }

    public void unRegisterRegionParallel(Region region, int batchSize, Runnable finished) {
        if (!this.regions.remove(region)) {
            finished.run();
            return; //Only recalculate when region was present
        }
        region.forEachChunkParallel(c -> {
            ArterionChunk ec = ArterionChunkUtil.getNonNull(c);
            ec.setRegion(this.calculateRegionAt(c.getWorld(), c.getX(), c.getZ()));
        }, batchSize, finished);
    }

    public void onChunkLoad(Chunk c) {
        ArterionChunk arterionChunk = new ArterionChunk(c);
        ArterionChunkUtil.set(c, arterionChunk);
        arterionChunk.setRegion(this.calculateRegionAt(c.getWorld(), c.getX(), c.getZ()));
    }

    private Region calculateRegionAt(World world, int x, int z) {
        for (Region r : regions) {
            if (r.isInZone(world, x, z)) {
                return r;
            }
        }
        return WILDERNESS;
    }

    public Region getWilderness() {
        return WILDERNESS;
    }

    public List<Region> all() {
        return regions;
    }
}
