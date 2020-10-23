//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package net.minecraft.server.v1_8_R3;

import java.io.IOException;

public class PacketPlayOutScoreboardDisplayObjective implements Packet<PacketListenerPlayOut> {
    private int a;
    private String b;

    public PacketPlayOutScoreboardDisplayObjective() {
    }

    public PacketPlayOutScoreboardDisplayObjective(int var1, ScoreboardObjective var2) {
        this.a = var1;
        if (var2 == null) {
            this.b = "";
        } else {
            this.b = var2.getName();
        }

    }

    //TODO Additional constructor
    public PacketPlayOutScoreboardDisplayObjective(int var1, String objectiveName) {
        this.a = var1;
        this.b = objectiveName;
    }

    public void a(PacketDataSerializer var1) throws IOException {
        this.a = var1.readByte();
        this.b = var1.c(16);
    }

    public void b(PacketDataSerializer var1) throws IOException {
        var1.writeByte(this.a);
        var1.a(this.b);
    }

    public void a(PacketListenerPlayOut var1) {
        var1.a(this);
    }
}
