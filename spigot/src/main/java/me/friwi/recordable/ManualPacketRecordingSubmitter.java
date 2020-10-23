package me.friwi.recordable;

import org.bukkit.Location;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ManualPacketRecordingSubmitter {
    private static Method packetCreationListenerSubmitPacket;
    private static Method getWorldHandle;

    static{
        try {
            packetCreationListenerSubmitPacket = Class.forName("me.friwi.recordable.impl.PacketCreationListener")
                    .getDeclaredMethod("interceptSendWithPosition", Class.forName("net.minecraft.server.v1_8_R3.World"),
                            int.class, int.class, Class.forName("net.minecraft.server.v1_8_R3.Packet"));
            getWorldHandle = Class.forName("org.bukkit.craftbukkit.v1_8_R3.CraftWorld").getDeclaredMethod("getHandle");
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }


    public static void submitPacket(Location loc, Object packet){
        try {
            packetCreationListenerSubmitPacket.invoke(null, getWorldHandle.invoke(loc.getWorld()), (int)loc.getX(), (int)loc.getZ());
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}
