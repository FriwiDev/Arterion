package me.friwi.arterion.client.network.client;

import java.nio.ByteBuffer;
import java.util.Map;
import java.util.TreeMap;

import me.friwi.arterion.client.network.ModPacket;
import me.friwi.arterion.client.network.packet.Packet01ModVersion;
import me.friwi.arterion.client.network.packet.Packet02IntValue;
import me.friwi.arterion.client.network.packet.Packet03StringValue;
import me.friwi.arterion.client.network.packet.Packet04FriendlyCreateOrUpdate;
import me.friwi.arterion.client.network.packet.Packet05FriendlyRemove;
import me.friwi.arterion.client.network.packet.Packet06Objective;
import me.friwi.arterion.client.network.packet.Packet07SkillSlotData;
import me.friwi.arterion.client.network.packet.Packet08TextGui;
import me.friwi.arterion.client.network.packet.Packet09Killfeed;

public class ModPacketList {
	private static Map<Byte, Class<? extends ModPacket>> packetMap = new TreeMap<>();
    private static final boolean DEBUG = true;
    private static final ByteBuffer WRITE_BUFFER = ByteBuffer.allocate(65536);

    static{
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

    public static void registerPacket(Class<? extends ModPacket> clasz){
        try {
            packetMap.put(clasz.newInstance().getId(), clasz);
        } catch (InstantiationException e) {
            if(DEBUG)e.printStackTrace();
        } catch (IllegalAccessException e) {
            if(DEBUG)e.printStackTrace();
        }
    }

    public static ModPacket fromBytes(byte id, byte[] data){
        ByteBuffer buff = ByteBuffer.wrap(data);
        Class<? extends ModPacket> clasz = packetMap.get(id);
        if(clasz==null)return null;
        try {
            ModPacket packet = clasz.newInstance();
            packet.readData(buff);
            return packet;
        } catch (InstantiationException e) {
            if(DEBUG)e.printStackTrace();
        } catch (IllegalAccessException e) {
            if(DEBUG)e.printStackTrace();
        }
        return null;
    }

    /**
     * Use only in sync!
     * @param packet
     * @return
     */
    public static byte[] toBytes(ModPacket packet){
        WRITE_BUFFER.position(0);
        WRITE_BUFFER.put(packet.getId());
        packet.writeData(WRITE_BUFFER);
        byte[] data = new byte[WRITE_BUFFER.position()];
        WRITE_BUFFER.position(0);
        WRITE_BUFFER.get(data);
        return data;
    }
}
