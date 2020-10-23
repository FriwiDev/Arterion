package me.friwi.recordable.impl;

import com.mojang.authlib.GameProfile;
import io.netty.buffer.Unpooled;
import net.minecraft.server.v1_8_R3.*;
import org.apache.commons.io.Charsets;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.scheduler.BukkitRunnable;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Recording {
    private final boolean debug = false;
    private static final byte[] THUMB_MAGIC_NUMBERS = {0, 1, 1, 2, 3, 5, 8};

    private File dir;
    private File tmcpr, inittmcpr, infotmcpr, mcpr;
    private OutputStream fos, fosi, fosin;
    private PacketDataSerializer serializer;
    private ByteBuffer prefix;
    private boolean active;
    private long startTime;
    private long stopTime;
    private ConcurrentLinkedQueue<Packet> packetQueue, initPacketQueue, infoPacketQueue;
    private ConcurrentLinkedQueue<Packet>[] queues;
    private World w;
    private int lcx, hcx, lcz, hcz;
    private double vx, vy, vz;
    private float vyaw, vpitch;
    private UUID myPlayerId = UUID.randomUUID();
    private EntityPlayer fakePlayer;

    private Set<UUID> occuringPlayers;
    private Map<UUID, EntityPlayer> occuringPlayerMap;

    private String serverName = "Arterion.de";
    private String mcVersion = "1.8.9";
    private String generator = "Arterion.de Replay Generator";
    private BufferedImage thumbnail = null;

    private Consumer<File> onFinishedReplay;

    public Recording(File dir, Location initialViewPoint, int lcx, int hcx, int lcz, int hcz) throws IOException {
        this(dir, ((CraftWorld) initialViewPoint.getWorld()).getHandle(),
                initialViewPoint.getX(), initialViewPoint.getY(), initialViewPoint.getZ(), initialViewPoint.getYaw(), initialViewPoint.getPitch(),
                lcx, hcx, lcz, hcz);
    }

    public Recording(File dir, World w, double vx, double vy, double vz, float vyaw, float vpitch, int lcx, int hcx, int lcz, int hcz) throws IOException {
        this.dir = dir;
        if (!this.dir.exists()) this.dir.mkdirs();
        this.tmcpr = new File(dir, "recording.tmcpr");
        this.inittmcpr = new File(dir, "init.tmcpr");
        this.infotmcpr = new File(dir, "info.tmcpr");
        this.mcpr = new File(dir, dir.getName() + ".mcpr");
        tmcpr.createNewFile();
        fos = new FileOutputStream(tmcpr);
        inittmcpr.createNewFile();
        fosi = new FileOutputStream(inittmcpr);
        infotmcpr.createNewFile();
        fosin = new FileOutputStream(infotmcpr);
        serializer = new PacketDataSerializer(Unpooled.buffer(Short.MAX_VALUE));
        prefix = ByteBuffer.allocate(8);
        packetQueue = new ConcurrentLinkedQueue<Packet>();
        initPacketQueue = new ConcurrentLinkedQueue<Packet>();
        infoPacketQueue = new ConcurrentLinkedQueue<Packet>();
        queues = new ConcurrentLinkedQueue[]{initPacketQueue, infoPacketQueue, packetQueue};
        this.w = w;
        this.lcx = lcx < hcx ? lcx : hcx;
        this.hcx = lcx >= hcx ? lcx : hcx;
        this.lcz = lcz < hcz ? lcz : hcz;
        this.hcz = lcz >= hcz ? lcz : hcz;
        this.occuringPlayers = new HashSet<UUID>();
        this.occuringPlayerMap = new HashMap<>();
        this.vx = vx;
        this.vy = vy;
        this.vz = vz;
        this.vyaw = vyaw;
        this.vpitch = vpitch;
    }

    public void beginRecording() {
        //Subscribe to updates
        active = true;
        startTime = System.currentTimeMillis();

        //Initialize injected player
        final WorldServer ws = (WorldServer) w;
        fakePlayer = new RecordablePlayer(MinecraftServer.getServer(), ws, new GameProfile(myPlayerId, "Recorder"), new PlayerInteractManager(ws), this);
        fakePlayer.playerConnection = new PlayerConnection(MinecraftServer.getServer(), new NetworkManager(EnumProtocolDirection.SERVERBOUND), fakePlayer) {
            @Override
            public void sendPacket(Packet packet) {
                if(!(packet instanceof PacketPlayOutPlayerInfo)) { //We have our own implementation for this
                    appendPacket(packet, 2);
                }
            }
        };
        fakePlayer.setPosition(((lcx + hcx) / 2) << 4, -5, ((lcz + hcz) / 2) << 4);
        fakePlayer.viewDistance = Math.max(Math.abs(lcx - hcx) / 2, Math.abs(lcz - hcz) / 2);

        if (debug) System.out.println("Position: " + fakePlayer.locX + " " + fakePlayer.locY + " " + fakePlayer.locZ);
        if (debug) System.out.println("Viewdistance: " + fakePlayer.viewDistance);

        //Add to PlayerChunkMap to receive initial chunks and keep everything in sync
        ws.getPlayerChunkMap().addPlayer(fakePlayer);

        //Add to EntityTracker
        ws.getTracker().recorders.add(fakePlayer);
        ws.getTracker().updatePlayers();

        //Play weather and time
        appendPacket(new PacketPlayOutLogin(fakePlayer.getId(), w.getWorldData().getGameType(), w.getWorldData().isHardcore(), ((WorldServer) w).getWorld().getEnvironment().getId(), w.getDifficulty(), Bukkit.getMaxPlayers(), ws.getWorldData().getType(), false), 0);
        appendPacket(new PacketPlayOutUpdateTime(w.getTime(), w.getDayTime(), true), 0);
        if (w.getWorldData().isThundering()) appendPacket(new PacketPlayOutGameStateChange(2, 0), 0);
        appendPacket(new PacketPlayOutPosition(vx, vy, vz, vyaw, vpitch, PacketPlayOutPosition.EnumPlayerTeleportFlags.a(0)), 0);

        //Tick our newly generated player
        new BukkitRunnable() {
            public void run() {
                if (!isActive()) {
                    cancel();
                    return;
                }
                fakePlayer.t_();
            }
        }.runTaskTimer(Bukkit.getPluginManager().getPlugins()[0], 1, 1);
        if (debug) System.out.println("Add performed!");
        this.startEncodingThread();
    }

    public void endRecording(Consumer<File> onFinishedReplay) {
        this.onFinishedReplay = onFinishedReplay;
        //Unsubscribe from all packet events
        active = false;

        //Unsubscribe from PlayerChunkMap
        WorldServer ws = (WorldServer) w;
        ws.getPlayerChunkMap().removePlayer(fakePlayer);

        //Unsubscribe from EntityTracker
        ws.getTracker().recorders.remove(fakePlayer);
        ws.getTracker().updatePlayers();

        if (debug) System.out.println("Recording stopped!");
    }

    public void appendPacket(Packet packet, int queue) {
        appendPacket(packet, false, queue);
    }

    public void appendPacket(Packet packet, int cx, int cz, int queue) {
        if (isInViewRange(w, cx << 4, cz << 4)) {
            appendPacket(packet, false, queue);
        }
    }

    private void appendPacket(Packet packet, boolean force, int queue) {
        if (active || force) {
            if (packet == null) return;
            queues[queue].add(packet);
        }
    }

    private void writePacket(OutputStream out, int time, Packet packet) throws IOException {
        if (packet == null) return;
        //int timestamp
        //int packet.length
        //byte[] packet
        serializer.resetWriterIndex();
        Integer packetId = EnumProtocol.a(0).a(EnumProtocolDirection.CLIENTBOUND, packet); //a(0) is PLAY
        serializer.b(packetId); //VarInt
        packet.b(serializer);
        prefix.position(0);
        prefix.putInt(time);
        prefix.putInt(serializer.writerIndex());
        //if(debug)System.out.println(packet.getClass());
        out.write(prefix.array());
        out.write(serializer.array(), 0, serializer.writerIndex());
    }

    public boolean isActive() {
        return active;
    }

    public boolean isInViewRange(World w, int x, int z) {
        if (!w.equals(this.w)) return false;
        return lcx <= (x >> 4) && hcx >= (x >> 4) && lcz <= (z >> 4) && hcz >= (z >> 4);
    }

    public void startEncodingThread() {
        new Thread("Replay-Encoder-" + dir.getName()) {
            public void run() {
                while (active) {
                    try {
                        Thread.sleep(20);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    while (!packetQueue.isEmpty()) {
                        Packet p = packetQueue.poll();
                        if (p instanceof PacketPlayOutNamedEntitySpawn) {
                            if(((PacketPlayOutNamedEntitySpawn) p).human instanceof EntityPlayer) {
                                if (occuringPlayers.add(((PacketPlayOutNamedEntitySpawn) p).b)) {
                                    //New player! Play out tab!
                                    occuringPlayerMap.put(((PacketPlayOutNamedEntitySpawn) p).b, (EntityPlayer) ((PacketPlayOutNamedEntitySpawn) p).human);
                                    Packet p2 = new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, (EntityPlayer) ((PacketPlayOutNamedEntitySpawn) p).human);
                                    initPacketQueue.add(p2);
                                }
                            }
                        }
                        try {
                            writePacket(fos, (int) (System.currentTimeMillis() - startTime), p);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    while (!initPacketQueue.isEmpty()) {
                        Packet p = initPacketQueue.poll();
                        try {
                            writePacket(fosi, 0, p);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    while (!infoPacketQueue.isEmpty()) {
                        Packet p = infoPacketQueue.poll();
                        try {
                            writePacket(fosin, 0, p);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                //Replay finished. Save metadata and finalize
                stopTime = System.currentTimeMillis();
                try {
                    fosi.flush();
                    fosi.close();
                    fosin.flush();
                    fosin.close();
                    fos.flush();
                    fos.close();
                    String timelines = "{\"\":[{\"keyframes\":[],\"segments\":[],\"interpolators\":[]},{\"keyframes\":[],\"segments\":[],\"interpolators\":[]}]}";
                    String mods = "{\"requiredMods\":[]}";
                    long duration = stopTime - startTime;
                    String playerList = "";
                    boolean first = true;
                    for (UUID u : occuringPlayers) {
                        if (!first) playerList += ", ";
                        first = false;
                        playerList += "\"" + u.toString() + "\"";
                    }
                    String metaData = "{\"singleplayer\":false,\"serverName\":\"" + serverName + "\",\"duration\":" + duration + ",\"date\":" + startTime + ",\"mcversion\":\"" + mcVersion + "\",\"fileFormat\":\"MCPR\",\"fileFormatVersion\":1,\"protocol\":47,\"generator\":\"" + generator + "\",\"selfId\":-1,\"players\":[" + playerList + "]}";
                    CRC32 crc = new CRC32();

                    mcpr.createNewFile();
                    ZipOutputStream out = new ZipOutputStream(new FileOutputStream(mcpr));

                    ZipEntry e = new ZipEntry(tmcpr.getName());
                    out.putNextEntry(e);
                    FileInputStream fis = new FileInputStream(inittmcpr);
                    byte[] copy = new byte[4096];
                    while (fis.available() > 0) {
                        int r = fis.read(copy);
                        out.write(copy, 0, r);
                        crc.update(copy, 0, r);
                    }
                    fis.close();
                    fis = new FileInputStream(infotmcpr);
                    while (fis.available() > 0) {
                        int r = fis.read(copy);
                        out.write(copy, 0, r);
                        crc.update(copy, 0, r);
                    }
                    fis.close();
                    fis = new FileInputStream(tmcpr);
                    while (fis.available() > 0) {
                        int r = fis.read(copy);
                        out.write(copy, 0, r);
                        crc.update(copy, 0, r);
                    }
                    fis.close();
                    out.closeEntry();
                    inittmcpr.delete();
                    infotmcpr.delete();
                    tmcpr.delete();

                    prefix.position(0);
                    prefix.putLong(crc.getValue());
                    byte[] crc32 = prefix.array();

                    insertZip(out, "recording.tmcpr.crc32", crc32);
                    insertZip(out, "timelines.json", timelines.getBytes(Charsets.ISO_8859_1));
                    insertZip(out, "mods.json", mods.getBytes(Charsets.ISO_8859_1));
                    insertZip(out, "metaData.json", metaData.getBytes(Charsets.ISO_8859_1));

                    if(thumbnail!=null){
                        e = new ZipEntry("thumb");
                        out.putNextEntry(e);
                        out.write(THUMB_MAGIC_NUMBERS);
                        ImageIO.write(thumbnail, "jpg", out);
                        out.closeEntry();
                    }

                    out.close();

                    onFinishedReplay.accept(mcpr);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (debug) System.out.println("Recording completed!");
            }
        }.start();
    }

    private void insertZip(ZipOutputStream out, String s, byte[] b) throws IOException {
        ZipEntry e = new ZipEntry(s);
        out.putNextEntry(e);
        out.write(b, 0, b.length);
        out.closeEntry();
    }

    public World getWorld() {
        return w;
    }

    public void addChat(String message){
        this.addChatJson("{\"text\": \""+ message + "\"}");
    }

    public void addChatJson(String message){
        appendPacket(new PacketPlayOutChat(IChatBaseComponent.ChatSerializer.a(message)), 2);
    }

    public void setServerName(String serverName){
        this.serverName = serverName;
    }

    public void setThumbnail(BufferedImage image){
        if(image.getWidth()!=1280||image.getHeight()!=720)throw new IllegalArgumentException("Invalid replay thumbnail dimensions. Needs to be 1280x720");
        this.thumbnail = image;
    }

    public Collection<UUID> getOccuringPlayers(){
        return occuringPlayers;
    }

    public void setTablistHeaderFooter(String header, String footer){
        this.setTablistHeaderFooterJson("{\"text\": \""+ header + "\"}", "{\"text\": \""+ footer + "\"}");
    }

    public void setTablistHeaderFooterJson(String header, String footer){
        appendPacket(new PacketPlayOutPlayerListHeaderFooter(IChatBaseComponent.ChatSerializer.a(header), IChatBaseComponent.ChatSerializer.a(footer)), 1);
    }

    public void setPlayerListName(UUID uuid, String listName){
        EntityPlayer player = occuringPlayerMap.get(uuid);
        if(player!=null){
            appendPacket(new PacketPlayOutPlayerInfo(player, IChatBaseComponent.ChatSerializer.a("{\"text\": \""+ listName + "\"}")), 1);
        }
    }

    public void createObjective(String objectiveName, String text){
        createObjective(objectiveName, text, false);
    }

    public void createObjective(String objectiveName, String text, boolean displayAsHeart){
        appendPacket(new PacketPlayOutScoreboardObjective(objectiveName, text, displayAsHeart?IScoreboardCriteria.EnumScoreboardHealthDisplay.HEARTS: IScoreboardCriteria.EnumScoreboardHealthDisplay.INTEGER, 0), 1);
    }

    public void displayObjectiveList(String objectiveName){
        appendPacket(new PacketPlayOutScoreboardDisplayObjective(0, objectiveName), 1);
    }

    public void displayObjectiveSidebar(String objectiveName){
        appendPacket(new PacketPlayOutScoreboardDisplayObjective(1, objectiveName), 1);
    }

    public void displayObjectiveBelowName(String objectiveName){
        appendPacket(new PacketPlayOutScoreboardDisplayObjective(2, objectiveName), 1);
    }

    public void addOrUpdateScore(String score, String objectiveName, int value){
        appendPacket(new PacketPlayOutScoreboardScore(score, objectiveName, value), 2);
    }

    public void removeScore(String score){
        appendPacket(new PacketPlayOutScoreboardScore(score), 2);
    }

    public void createTeam(String team, String color, String displayName, String prefix, String suffix, Collection<String> players){
        appendPacket(new PacketPlayOutScoreboardTeam(team, color, displayName, prefix, suffix, players, 0), 1);
    }

    public void removeTeam(String team){
        appendPacket(new PacketPlayOutScoreboardTeam(team, null, null, null, null, null, 1), 2);
    }

    public void updateTeamInfo(String team, String color, String displayName, String prefix, String suffix){
        appendPacket(new PacketPlayOutScoreboardTeam(team, color, displayName, prefix, suffix, null, 2), 2);
    }

    public void addPlayersToTeam(String team, Collection<String> players){
        appendPacket(new PacketPlayOutScoreboardTeam(team, null, null, null, null, players, 3), 2);
    }

    public void removePlayersFromTeam(String team, Collection<String> players){
        appendPacket(new PacketPlayOutScoreboardTeam(team, null, null, null, null, players, 4), 2);
    }
}
