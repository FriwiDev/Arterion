package net.minecraft.server.v1_8_R3;

import me.friwi.recordable.impl.PacketCreationListener;

import java.util.Iterator;

public class WorldManager
        implements IWorldAccess {
    private MinecraftServer a;
    private WorldServer world;

    public WorldManager(MinecraftServer minecraftserver, WorldServer worldserver) {
        this.a = minecraftserver;
        this.world = worldserver;
    }

    public void a(int i, boolean flag, double d0, double d1, double d2, double d3, double d4, double d5, int... aint) {
    }

    public void a(Entity entity) {
        this.world.getTracker().track(entity);
    }

    public void b(Entity entity) {
        this.world.getTracker().untrackEntity(entity);
        this.world.getScoreboard().a(entity);
    }

    public void a(String s, double d0, double d1, double d2, float f, float f1) {
        //TODO
        Packet p = new PacketPlayOutNamedSoundEffect(s, d0, d1, d2, f, f1);
        PacketCreationListener.interceptSendWithPosition(world, (int) d0, (int) d2, p);

        this.a.getPlayerList().sendPacketNearby(d0, d1, d2, f > 1.0F ? 16.0F * f : 16.0D, this.world.dimension, p);
    }

    public void a(EntityHuman entityhuman, String s, double d0, double d1, double d2, float f, float f1) {
        //TODO
        Packet p = new PacketPlayOutNamedSoundEffect(s, d0, d1, d2, f, f1);
        PacketCreationListener.interceptSendWithPosition(world, (int) d0, (int) d2, p);

        this.a.getPlayerList().sendPacketNearby(entityhuman, d0, d1, d2, f > 1.0F ? 16.0F * f : 16.0D, this.world.dimension, p);
    }

    public void a(int i, int j, int k, int l, int i1, int j1) {
    }

    public void a(BlockPosition blockposition) {
        this.world.getPlayerChunkMap().flagDirty(blockposition);
    }

    public void b(BlockPosition blockposition) {
    }

    public void a(String s, BlockPosition blockposition) {
    }

    public void a(EntityHuman entityhuman, int i, BlockPosition blockposition, int j) {
        //TODO
        Packet p = new PacketPlayOutWorldEvent(i, blockposition, j, false);
        PacketCreationListener.interceptSendWithPosition(world, blockposition.getX(), blockposition.getZ(), p);

        this.a.getPlayerList().sendPacketNearby(entityhuman, blockposition.getX(), blockposition.getY(), blockposition.getZ(), 64.0D, this.world.dimension, p);
    }

    public void a(int i, BlockPosition blockposition, int j) {
        //TODO
        Packet p = new PacketPlayOutWorldEvent(i, blockposition, j, true);
        PacketCreationListener.interceptSendWithPosition(world, blockposition.getX(), blockposition.getZ(), p);

        this.a.getPlayerList().sendAll(p);
    }

    public void b(int i, BlockPosition blockposition, int j) {
        //TODO
        Packet p = new PacketPlayOutBlockBreakAnimation(i, blockposition, j);
        PacketCreationListener.interceptSendWithPosition(world, blockposition.getX(), blockposition.getZ(), p);

        Iterator iterator = this.a.getPlayerList().v().iterator();

        EntityHuman entityhuman = null;
        Entity entity = this.world.a(i);
        if ((entity instanceof EntityHuman)) {
            entityhuman = (EntityHuman) entity;
        }
        while (iterator.hasNext()) {
            EntityPlayer entityplayer = (EntityPlayer) iterator.next();
            if ((entityplayer != null) && (entityplayer.world == this.world) && (entityplayer.getId() != i)) {
                double d0 = blockposition.getX() - entityplayer.locX;
                double d1 = blockposition.getY() - entityplayer.locY;
                double d2 = blockposition.getZ() - entityplayer.locZ;
                if ((entityhuman == null) || (!(entityhuman instanceof EntityPlayer)) || (entityplayer.getBukkitEntity().canSee(((EntityPlayer) entityhuman).getBukkitEntity()))) {
                    if (d0 * d0 + d1 * d1 + d2 * d2 < 1024.0D) {
                        entityplayer.playerConnection.sendPacket(p);
                    }
                }
            }
        }
    }
}
