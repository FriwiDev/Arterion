//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package net.minecraft.server.v1_8_R3;

import java.io.IOException;

public class PacketPlayOutSpawnEntityWeather implements Packet<PacketListenerPlayOut> {
    public int b;
    public int d;
    private int a;
    private int c;
    private int e;

    public PacketPlayOutSpawnEntityWeather() {
    }

    public PacketPlayOutSpawnEntityWeather(Entity var1) {
        this.a = var1.getId();
        this.b = MathHelper.floor(var1.locX * 32.0D);
        this.c = MathHelper.floor(var1.locY * 32.0D);
        this.d = MathHelper.floor(var1.locZ * 32.0D);
        if (var1 instanceof EntityLightning) {
            this.e = 1;
        }

    }

    public void a(PacketDataSerializer var1) throws IOException {
        this.a = var1.e();
        this.e = var1.readByte();
        this.b = var1.readInt();
        this.c = var1.readInt();
        this.d = var1.readInt();
    }

    public void b(PacketDataSerializer var1) throws IOException {
        var1.b(this.a);
        var1.writeByte(this.e);
        var1.writeInt(this.b);
        var1.writeInt(this.c);
        var1.writeInt(this.d);
    }

    public void a(PacketListenerPlayOut var1) {
        var1.a(this);
    }
}
