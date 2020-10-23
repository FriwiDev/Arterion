package me.friwi.recordable;

import net.minecraft.server.v1_8_R3.*;

public class CustomPlayerConnection extends PlayerConnection {
    public static final long PACKET_SPEED = 50;

    long violations = 0;
    long lastPacket = 0;
    public CustomPlayerConnection(MinecraftServer minecraftserver, NetworkManager networkmanager, EntityPlayer entityplayer) {
        super(minecraftserver, networkmanager, entityplayer);
    }

    public void a(PacketPlayInCustomPayload packetplayincustompayload) {
        /*if(lastPacket+10000<System.currentTimeMillis()){
            violations = 0;
        }
        if(lastPacket+PACKET_SPEED>System.currentTimeMillis()){
            violations++;
            lastPacket = System.currentTimeMillis();
            if(violations>50){
                this.disconnect("Too many packets!");
                return;
            }
        }
        lastPacket = System.currentTimeMillis();*/
        if("MC|BEdit".equals(packetplayincustompayload.a()))return;
        super.a(packetplayincustompayload);
    }
}
