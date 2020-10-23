//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package net.minecraft.server.v1_8_R3;

import java.io.IOException;

public class PacketPlayOutGameStateChange implements Packet<PacketListenerPlayOut> {
    public static final String[] a = new String[]{"tile.bed.notValid"};
    public int b;
    private float c;

    public PacketPlayOutGameStateChange() {
    }

    public PacketPlayOutGameStateChange(int var1, float var2) {
        this.b = var1;
        this.c = var2;
    }

    public void a(PacketDataSerializer var1) throws IOException {
        this.b = var1.readUnsignedByte();
        this.c = var1.readFloat();
    }

    public void b(PacketDataSerializer var1) throws IOException {
        var1.writeByte(this.b);
        var1.writeFloat(this.c);
    }

    public void a(PacketListenerPlayOut var1) {
        var1.a(this);
    }
}
