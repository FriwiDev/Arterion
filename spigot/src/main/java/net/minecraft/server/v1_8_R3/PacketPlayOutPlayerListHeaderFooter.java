//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package net.minecraft.server.v1_8_R3;

import java.io.IOException;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;

public class PacketPlayOutPlayerListHeaderFooter implements Packet<PacketListenerPlayOut> {
    public BaseComponent[] header;
    public BaseComponent[] footer;
    private IChatBaseComponent a;
    private IChatBaseComponent b;

    public PacketPlayOutPlayerListHeaderFooter() {
    }

    public PacketPlayOutPlayerListHeaderFooter(IChatBaseComponent ichatbasecomponent) {
        this.a = ichatbasecomponent;
    }

    //TODO
    public PacketPlayOutPlayerListHeaderFooter(IChatBaseComponent header, IChatBaseComponent footer) {
        this.a = header;
        this.b = footer;
    }

    public void a(PacketDataSerializer packetdataserializer) throws IOException {
        this.a = packetdataserializer.d();
        this.b = packetdataserializer.d();
    }

    public void b(PacketDataSerializer packetdataserializer) throws IOException {
        if (this.header != null) {
            packetdataserializer.a(ComponentSerializer.toString(this.header));
        } else {
            packetdataserializer.a(this.a);
        }

        if (this.footer != null) {
            packetdataserializer.a(ComponentSerializer.toString(this.footer));
        } else {
            packetdataserializer.a(this.b);
        }

    }

    public void a(PacketListenerPlayOut packetlistenerplayout) {
        packetlistenerplayout.a(this);
    }
}
