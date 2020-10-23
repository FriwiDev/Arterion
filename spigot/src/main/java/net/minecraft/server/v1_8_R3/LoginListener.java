//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package net.minecraft.server.v1_8_R3;

import com.google.common.base.Charsets;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.exceptions.AuthenticationUnavailableException;
import com.mojang.authlib.properties.Property;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.util.concurrent.GenericFutureListener;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.security.PrivateKey;
import java.util.Arrays;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import javax.crypto.SecretKey;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.craftbukkit.v1_8_R3.CraftServer;
import org.bukkit.craftbukkit.v1_8_R3.util.Waitable;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerPreLoginEvent;
import org.bukkit.event.player.PlayerPreLoginEvent.Result;

public class LoginListener implements PacketLoginInListener, IUpdatePlayerListBox {
    private static final AtomicInteger b = new AtomicInteger(0);
    private static final Logger c = LogManager.getLogger();
    private static final Random random = new Random();
    private final byte[] e = new byte[4];
    private final MinecraftServer server;
    public final NetworkManager networkManager;
    private LoginListener.EnumProtocolState g;
    private int h;
    private GameProfile i;
    private String j;
    private SecretKey loginKey;
    private EntityPlayer l;
    public String hostname = "";

    public LoginListener(MinecraftServer minecraftserver, NetworkManager networkmanager) {
        this.g = LoginListener.EnumProtocolState.HELLO;
        this.j = "";
        this.server = minecraftserver;
        this.networkManager = networkmanager;
        random.nextBytes(this.e);
    }

    public void c() {
        if (this.g == LoginListener.EnumProtocolState.READY_TO_ACCEPT) {
            this.b();
        } else if (this.g == LoginListener.EnumProtocolState.e) {
            EntityPlayer entityplayer = this.server.getPlayerList().a(this.i.getId());
            if (entityplayer == null) {
                this.g = LoginListener.EnumProtocolState.READY_TO_ACCEPT;
                this.server.getPlayerList().a(this.networkManager, this.l);
                this.l = null;
            }
        }

        if (this.h++ == 600) {
            this.disconnect("Took too long to log in");
        }

    }

    public void disconnect(String s) {
        try {
            c.info("Disconnecting " + this.d() + ": " + s);
            ChatComponentText chatcomponenttext = new ChatComponentText(s);
            this.networkManager.handle(new PacketLoginOutDisconnect(chatcomponenttext));
            this.networkManager.close(chatcomponenttext);
        } catch (Exception var3) {
            c.error("Error whilst disconnecting player", var3);
        }

    }

    public void initUUID() {
        UUID uuid;
        if (this.networkManager.spoofedUUID != null) {
            uuid = this.networkManager.spoofedUUID;
        } else {
            uuid = UUID.nameUUIDFromBytes(("OfflinePlayer:" + this.i.getName()).getBytes(Charsets.UTF_8));
        }

        this.i = new GameProfile(uuid, this.i.getName());
        if (this.networkManager.spoofedProfile != null) {
            Property[] var2;
            int var3 = (var2 = this.networkManager.spoofedProfile).length;

            for(int var4 = 0; var4 < var3; ++var4) {
                Property property = var2[var4];
                this.i.getProperties().put(property.getName(), property);
            }
        }

    }

    public void b() {
        EntityPlayer s = this.server.getPlayerList().attemptLogin(this, this.i, this.hostname);
        if (s != null) {
            this.g = LoginListener.EnumProtocolState.ACCEPTED;
            if (this.server.aK() >= 0 && !this.networkManager.c()) {
                this.networkManager.a(new PacketLoginOutSetCompression(this.server.aK()), new ChannelFutureListener() {
                    public void a(ChannelFuture channelfuture) throws Exception {
                        LoginListener.this.networkManager.a(LoginListener.this.server.aK());
                    }

                    public void operationComplete(ChannelFuture future) throws Exception {
                        this.a(future);
                    }
                }, new GenericFutureListener[0]);
            }

            this.networkManager.handle(new PacketLoginOutSuccess(this.i));
            EntityPlayer entityplayer = this.server.getPlayerList().a(this.i.getId());
            if (entityplayer != null) {
                this.g = LoginListener.EnumProtocolState.e;
                this.l = this.server.getPlayerList().processLogin(this.i, s);
            } else {
                this.server.getPlayerList().a(this.networkManager, this.server.getPlayerList().processLogin(this.i, s));
            }
        }

    }

    public void a(IChatBaseComponent ichatbasecomponent) {
        c.info(this.d() + " lost connection: " + ichatbasecomponent.c());
    }

    public String d() {
        try {
            return this.i != null ? this.i.toString() + " (" + this.networkManager.getSocketAddress().toString() + ")" : String.valueOf(this.networkManager.getSocketAddress());
        }catch (Exception e){
            //TODO Force connection close
            this.networkManager.close(new ChatMessage("disconnect.timeout", new Object[0]));
            return "npe exploit caught";
        }
    }

    long lastRequest = 0;

    public void a(PacketLoginInStart packetlogininstart) {
        Validate.validState(this.g == LoginListener.EnumProtocolState.HELLO, "Unexpected hello packet", new Object[0]);
        this.i = packetlogininstart.a();
        if (this.server.getOnlineMode() && !this.networkManager.c()) {
            this.g = LoginListener.EnumProtocolState.KEY;
            this.networkManager.handle(new PacketLoginOutEncryptionBegin(this.j, this.server.Q().getPublic(), this.e));
        } else {
            this.initUUID();
            (new Thread(new Runnable() {
                public void run() {
                    try {
                        (LoginListener.this.new LoginHandler()).fireEvents();
                    } catch (Exception var2) {
                        LoginListener.this.disconnect("Failed to verify username!");
                        LoginListener.this.server.server.getLogger().log(Level.WARNING, "Exception verifying " + LoginListener.this.i.getName(), var2);
                    }

                }
            })).start();
        }

    }

    public void a(PacketLoginInEncryptionBegin packetlogininencryptionbegin) {
        Validate.validState(this.g == LoginListener.EnumProtocolState.KEY, "Unexpected key packet", new Object[0]);
        PrivateKey privatekey = this.server.Q().getPrivate();
        if (!Arrays.equals(this.e, packetlogininencryptionbegin.b(privatekey))) {
            throw new IllegalStateException("Invalid nonce!");
        } else {
            this.loginKey = packetlogininencryptionbegin.a(privatekey);
            this.g = LoginListener.EnumProtocolState.AUTHENTICATING;
            this.networkManager.a(this.loginKey);
            (new Thread("User Authenticator #" + b.incrementAndGet()) {
                public void run() {
                    GameProfile gameprofile = LoginListener.this.i;

                    try {
                        String s = (new BigInteger(MinecraftEncryption.a(LoginListener.this.j, LoginListener.this.server.Q().getPublic(), LoginListener.this.loginKey))).toString(16);
                        LoginListener.this.i = LoginListener.this.server.aD().hasJoinedServer(new GameProfile((UUID)null, gameprofile.getName()), s);
                        if (LoginListener.this.i != null) {
                            if (!LoginListener.this.networkManager.g()) {
                                return;
                            }

                            (LoginListener.this.new LoginHandler()).fireEvents();
                        } else if (LoginListener.this.server.T()) {
                            LoginListener.c.warn("Failed to verify username but will let them in anyway!");
                            LoginListener.this.i = LoginListener.this.a(gameprofile);
                            LoginListener.this.g = LoginListener.EnumProtocolState.READY_TO_ACCEPT;
                        } else {
                            LoginListener.this.disconnect("Failed to verify username!");
                            LoginListener.c.error("Username '" + gameprofile.getName() + "' tried to join with an invalid session");
                        }
                    } catch (AuthenticationUnavailableException var3) {
                        if (LoginListener.this.server.T()) {
                            LoginListener.c.warn("Authentication servers are down but will let them in anyway!");
                            LoginListener.this.i = LoginListener.this.a(gameprofile);
                            LoginListener.this.g = LoginListener.EnumProtocolState.READY_TO_ACCEPT;
                        } else {
                            LoginListener.this.disconnect("Authentication servers are down. Please try again later, sorry!");
                            LoginListener.c.error("Couldn't verify username because servers are unavailable");
                        }
                    } catch (Exception var4) {
                        LoginListener.this.disconnect("Failed to verify username!");
                        LoginListener.this.server.server.getLogger().log(Level.WARNING, "Exception verifying " + gameprofile.getName(), var4);
                    }

                }
            }).start();
        }
    }

    protected GameProfile a(GameProfile gameprofile) {
        UUID uuid = UUID.nameUUIDFromBytes(("OfflinePlayer:" + gameprofile.getName()).getBytes(Charsets.UTF_8));
        return new GameProfile(uuid, gameprofile.getName());
    }

    static enum EnumProtocolState {
        HELLO,
        KEY,
        AUTHENTICATING,
        READY_TO_ACCEPT,
        e,
        ACCEPTED;

        private EnumProtocolState() {
        }
    }

    public class LoginHandler {
        public LoginHandler() {
        }

        public void fireEvents() throws Exception {
            String playerName = LoginListener.this.i.getName();
            InetAddress address = ((InetSocketAddress)LoginListener.this.networkManager.getSocketAddress()).getAddress();
            UUID uniqueId = LoginListener.this.i.getId();
            final CraftServer server = LoginListener.this.server.server;
            AsyncPlayerPreLoginEvent asyncEvent = new AsyncPlayerPreLoginEvent(playerName, address, uniqueId);
            server.getPluginManager().callEvent(asyncEvent);
            if (PlayerPreLoginEvent.getHandlerList().getRegisteredListeners().length != 0) {
                final PlayerPreLoginEvent event = new PlayerPreLoginEvent(playerName, address, uniqueId);
                if (asyncEvent.getResult() != Result.ALLOWED) {
                    event.disallow(asyncEvent.getResult(), asyncEvent.getKickMessage());
                }

                Waitable<Result> waitable = new Waitable<Result>() {
                    protected Result evaluate() {
                        server.getPluginManager().callEvent(event);
                        return event.getResult();
                    }
                };
                LoginListener.this.server.processQueue.add(waitable);
                if (waitable.get() != Result.ALLOWED) {
                    LoginListener.this.disconnect(event.getKickMessage());
                    return;
                }
            } else if (asyncEvent.getLoginResult() != org.bukkit.event.player.AsyncPlayerPreLoginEvent.Result.ALLOWED) {
                LoginListener.this.disconnect(asyncEvent.getKickMessage());
                return;
            }

            LoginListener.c.info("UUID of player " + LoginListener.this.i.getName() + " is " + LoginListener.this.i.getId());
            LoginListener.this.g = LoginListener.EnumProtocolState.READY_TO_ACCEPT;
        }
    }
}
