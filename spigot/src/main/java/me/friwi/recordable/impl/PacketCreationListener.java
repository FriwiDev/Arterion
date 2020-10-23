package me.friwi.recordable.impl;

import net.minecraft.server.v1_8_R3.*;
import java.util.LinkedList;
import java.util.List;

public class PacketCreationListener {
    private static List<Recording> recordings = new LinkedList<Recording>();
    private static Packet last = null;

    public static void interceptSend(World w, Packet packet) {
        if (packet == last) return;
        last = packet;
        for (Recording recording : recordings) {
            if (recording.getWorld().equals(w)) {
                if (packet instanceof PacketPlayOutBlockBreakAnimation) {
                    interceptCreation(recording, ((PacketPlayOutBlockBreakAnimation) packet).b, packet);
                } else if (packet instanceof PacketPlayOutExplosion) {
                    interceptCreation(recording, (int) ((PacketPlayOutExplosion) packet).a, (int) ((PacketPlayOutExplosion) packet).c, packet);
                }/**else if(packet instanceof PacketPlayOutNamedSoundEffect){
                 interceptCreation((int)((PacketPlayOutNamedSoundEffect) packet).x, (int)((PacketPlayOutNamedSoundEffect) packet).z, packet);
                 }*/
                else if (packet instanceof PacketPlayOutWorldParticles) {
                    interceptCreation(recording, (int) ((PacketPlayOutWorldParticles) packet).b, (int) ((PacketPlayOutWorldParticles) packet).d, packet);
                }/**else if(packet instanceof PacketPlayOutWorldEvent){
                 //Everything except block breaks
                 if(((PacketPlayOutWorldEvent) packet).a!=2001)interceptCreation(((PacketPlayOutWorldEvent) packet).b, packet);
                 }*/
                else if (packet instanceof PacketPlayOutUpdateTime) {
                    interceptCreation(recording, packet);
                } else if (packet instanceof PacketPlayOutGameStateChange) {
                    PacketPlayOutGameStateChange p = ((PacketPlayOutGameStateChange) packet);
                    if (p.b == 1 || p.b == 2 || p.b == 10) interceptCreation(recording, packet);
                } else if (packet instanceof PacketPlayOutSpawnEntityWeather) {
                    interceptCreation(recording, ((PacketPlayOutSpawnEntityWeather) packet).b, ((PacketPlayOutSpawnEntityWeather) packet).d, packet);
                }
            }
        }
    }

    public static void interceptSendWithPosition(World w, int x, int z, Packet packet) {
        for (Recording recording : recordings) {
            if (recording.getWorld().equals(w)) {
                interceptCreation(recording, x, z, packet);
            }
        }
    }

    public static void interceptSendWithPosition(int dimension, int x, int z, Packet packet) {
        for (Recording recording : recordings) {
            if (recording.getWorld().getWorld().getHandle().dimension == dimension) {
                interceptCreation(recording, x, z, packet);
            }
        }
    }

    private static void interceptCreation(Recording recording, Packet packet) {
        recording.appendPacket(packet, 2);
    }

    private static void interceptCreation(Recording recording, BlockPosition position, Packet packet) {
        recording.appendPacket(packet, position.getX() >> 4, position.getZ() >> 4, 2);
    }

    private static void interceptCreation(Recording recording, int x, int z, Packet packet) {
        recording.appendPacket(packet, x >> 4, z >> 4, 2);
    }

    public static List<Recording> getRecordings() {
        return recordings;
    }

    //TODO Remove
    /*public static boolean init = false;
    public static void init() {
        if (!init) {
            init = true;
            try {
                Recording rec = new Recording(new File("test_recording"), MinecraftServer.getServer().worlds.get(0), 16, 128, 16, 1f, 1f, 0, 20, 0, 20);
                getRecordings().add(rec);
                rec.beginRecording();
                rec.addChat("§6This is a test message");
                new BukkitRunnable() {
                    public void run() {
                        rec.endRecording(file->{
                            System.out.println("Finished writing "+file.getAbsolutePath());
                        });
                    }
                }.runTaskLater(Bukkit.getPluginManager().getPlugins()[0], 60 * 20);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }*/
}
