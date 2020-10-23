package me.friwi.arterion.plugin.ui.mod.server;

import me.friwi.arterion.plugin.ui.mod.ModPacket;
import me.friwi.arterion.plugin.ui.mod.packet.*;

import java.nio.ByteBuffer;
import java.util.Map;
import java.util.TreeMap;

public class ModPacketList {
    private static final boolean DEBUG = true;
    private static final ByteBuffer WRITE_BUFFER = ByteBuffer.allocate(65536);
    private static Map<Byte, Class<? extends ModPacket>> packetMap = new TreeMap<>();

    static {
        registerPacket(Packet01ModVersion.class);
        registerPacket(Packet02IntValue.class);
        registerPacket(Packet03StringValue.class);
        registerPacket(Packet04FriendlyCreateOrUpdate.class);
        registerPacket(Packet05FriendlyRemove.class);
        registerPacket(Packet06Objective.class);
        registerPacket(Packet07SkillSlotData.class);
        registerPacket(Packet08TextGui.class);
        registerPacket(Packet09Killfeed.class);
    }

    public static void registerPacket(Class<? extends ModPacket> clasz) {
        try {
            packetMap.put(clasz.newInstance().getId(), clasz);
        } catch (InstantiationException e) {
            if (DEBUG) e.printStackTrace();
        } catch (IllegalAccessException e) {
            if (DEBUG) e.printStackTrace();
        }
    }

    public static ModPacket fromBytes(byte[] data) {
        ByteBuffer buff = ByteBuffer.wrap(data);
        Class<? extends ModPacket> clasz = packetMap.get(buff.get());
        if (clasz == null) return null;
        try {
            ModPacket packet = clasz.newInstance();
            packet.readData(buff);
            return packet;
        } catch (InstantiationException e) {
            if (DEBUG) e.printStackTrace();
        } catch (IllegalAccessException e) {
            if (DEBUG) e.printStackTrace();
        }
        return null;
    }

    /**
     * Use only in sync!
     *
     * @param packet
     * @return
     */
    public static byte[] toBytes(ModPacket packet) {
        WRITE_BUFFER.position(0);
        packet.writeData(WRITE_BUFFER);
        byte[] data = new byte[WRITE_BUFFER.position() + 4];
        WRITE_BUFFER.position(0);
        ByteBuffer dataBuffer = ByteBuffer.wrap(data);
        dataBuffer.put((byte) 0);
        dataBuffer.put(packet.getId());
        dataBuffer.putShort((short) (data.length - 4));
        WRITE_BUFFER.get(data, 4, data.length - 4);
        return data;
    }
}
