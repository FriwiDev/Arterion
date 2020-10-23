package net.minecraft.server.v1_8_R3;

import java.io.IOException;

public class PacketPlayOutBlockBreakAnimation implements Packet<PacketListenerPlayOut> {
    public BlockPosition b;
    private int a;
    private int c;

    public PacketPlayOutBlockBreakAnimation() {
    }

    public PacketPlayOutBlockBreakAnimation(int var1, BlockPosition var2, int var3) {
        this.a = var1;
        this.b = var2;
        this.c = var3;
    }

    public void a(PacketDataSerializer var1) throws IOException {
        this.a = var1.e();
        this.b = var1.c();
        this.c = var1.readUnsignedByte();
    }

    public void b(PacketDataSerializer var1) throws IOException {
        var1.b(this.a);
        var1.a(this.b);
        var1.writeByte(this.c);
    }

    public void a(PacketListenerPlayOut var1) {
        var1.a(this);
    }
}
