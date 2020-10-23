package net.minecraft.server.v1_8_R3;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.HashMap;

public class HandshakeListener implements PacketHandshakingInListener {

    private final MinecraftServer a;
    private final NetworkManager b;

    public HandshakeListener(MinecraftServer minecraftserver, NetworkManager networkmanager) {
        this.a = minecraftserver;
        this.b = networkmanager;
    }

    static long lastRequest = 0;
    static long maxAttempts = 1;
    private static final HashMap<InetAddress, Long> ddosTracker = new HashMap();

    public void a(PacketHandshakingInSetProtocol packethandshakinginsetprotocol) {
        switch (HandshakeListener.SyntheticClass_1.a[packethandshakinginsetprotocol.a().ordinal()]) {
            case 1:
                //TODO Block ddos
                InetAddress address = ((InetSocketAddress)this.b.getSocketAddress()).getAddress();
                Long attempts = ddosTracker.get(address);
                if(ddosTracker.size()>2000)ddosTracker.clear();
                if(attempts!=null && attempts>maxAttempts){
                    //this.b.channel.close();
                    //return;
                }else {
                    //TODO Block request bursts
                    if (lastRequest + 200 > System.currentTimeMillis()) {
                        lastRequest = System.currentTimeMillis();
                        if (attempts == null) attempts = Long.valueOf(2);
                        else attempts++;
                        ddosTracker.put(address, attempts);
                        if (attempts > maxAttempts) {
                            //System.out.println("Anti-DDOS | Whitelisting " + address);
                        } else {
                            //System.out.println("Anti-DDOS | Rejecting " + address);
                        }
                        this.b.channel.close();
                        return;
                    } else {
                        ddosTracker.remove(address);
                        lastRequest = System.currentTimeMillis();
                        System.out.println("Anti-DDOS | Pass " + address);
                    }
                }
                this.b.a(EnumProtocol.LOGIN);
                ChatComponentText chatcomponenttext;

                if (packethandshakinginsetprotocol.b() > 47) {
                    chatcomponenttext = new ChatComponentText("Outdated server! I\'m still on 1.8.8");
                    this.b.handle(new PacketLoginOutDisconnect(chatcomponenttext));
                    this.b.close(chatcomponenttext);
                } else if (packethandshakinginsetprotocol.b() < 47) {
                    chatcomponenttext = new ChatComponentText("Outdated client! Please use 1.8.8");
                    this.b.handle(new PacketLoginOutDisconnect(chatcomponenttext));
                    this.b.close(chatcomponenttext);
                } else {
                    this.b.a((PacketListener) (new LoginListener(this.a, this.b)));
                }
                break;

            case 2:
                this.b.a(EnumProtocol.STATUS);
                this.b.a((PacketListener) (new PacketStatusListener(this.a, this.b)));
                break;

            default:
                throw new UnsupportedOperationException("Invalid intention " + packethandshakinginsetprotocol.a());
        }

    }

    public void a(IChatBaseComponent ichatbasecomponent) {}

    static class SyntheticClass_1 {

        static final int[] a = new int[EnumProtocol.values().length];

        static {
            try {
                HandshakeListener.SyntheticClass_1.a[EnumProtocol.LOGIN.ordinal()] = 1;
            } catch (NoSuchFieldError nosuchfielderror) {
                ;
            }

            try {
                HandshakeListener.SyntheticClass_1.a[EnumProtocol.STATUS.ordinal()] = 2;
            } catch (NoSuchFieldError nosuchfielderror1) {
                ;
            }

        }
    }
}
