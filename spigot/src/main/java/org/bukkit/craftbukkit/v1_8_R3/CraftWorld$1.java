package org.bukkit.craftbukkit.v1_8_R3;

import me.friwi.recordable.impl.PacketCreationListener;
import net.minecraft.server.v1_8_R3.BlockPosition;
import net.minecraft.server.v1_8_R3.EntityLightning;
import net.minecraft.server.v1_8_R3.EntityPlayer;
import net.minecraft.server.v1_8_R3.EnumParticle;
import net.minecraft.server.v1_8_R3.Packet;
import net.minecraft.server.v1_8_R3.PacketPlayOutWorldEvent;
import net.minecraft.server.v1_8_R3.PacketPlayOutWorldParticles;
import net.minecraft.server.v1_8_R3.PlayerConnection;
import net.minecraft.server.v1_8_R3.WorldServer;
import org.apache.commons.lang.Validate;
import org.bukkit.Effect;
import org.bukkit.Effect.Type;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.World.Spigot;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftLightningStrike;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.LightningStrike;
import org.bukkit.entity.Player;

class CraftWorld$1
        extends World.Spigot
{
    CraftWorld paramCraftWorld;
    CraftWorld$1(CraftWorld paramCraftWorld) {
        this.paramCraftWorld = paramCraftWorld;
    }

    public void playEffect(Location location, Effect effect, int id, int data, float offsetX, float offsetY, float offsetZ, float speed, int particleCount, int radius)
    {
        Validate.notNull(location, "Location cannot be null");
        Validate.notNull(effect, "Effect cannot be null");
        Validate.notNull(location.getWorld(), "World cannot be null");
        Packet packet;
        EnumParticle p;
        if (effect.getType() != Effect.Type.PARTICLE)
        {
            int packetData = effect.getId();
            packet = new PacketPlayOutWorldEvent(packetData, new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ()), id, false);
        }
        else
        {
            EnumParticle particle = null;
            int[] extra = null;
            EnumParticle[] arrayOfEnumParticle;
            int i = (arrayOfEnumParticle = EnumParticle.values()).length;
            for (int j = 0; j < i; j++)
            {
                p = arrayOfEnumParticle[j];
                if (effect.getName().startsWith(p.b().replace("_", "")))
                {
                    particle = p;
                    if (effect.getData() == null) {
                        break;
                    }
                    if (effect.getData().equals(Material.class))
                    {
                        extra = new int[] { id };
                        break;
                    }
                    extra = new int[] { data << 12 | id & 0xFFF };

                    break;
                }
            }
            if (extra == null) {
                extra = new int[0];
            }
            packet = new PacketPlayOutWorldParticles(particle, true, (float)location.getX(), (float)location.getY(), (float)location.getZ(), offsetX, offsetY, offsetZ, speed, particleCount, extra);
        }
        radius *= radius;
        for (Player player : paramCraftWorld.getPlayers()) {
            if (((CraftPlayer)player).getHandle().playerConnection != null) {
                if (location.getWorld().equals(player.getWorld()))
                {
                    int distance = (int)player.getLocation().distanceSquared(location);
                    if (distance <= radius) {
                        ((CraftPlayer)player).getHandle().playerConnection.sendPacket(packet);
                    }
                }
            }
        }

        //TODO Replay
        PacketCreationListener.interceptSendWithPosition(paramCraftWorld.getHandle(), location.getBlockX(), location.getBlockZ(), packet);
    }

    public void playEffect(Location location, Effect effect)
    {
        paramCraftWorld.playEffect(location, effect, 0);
    }

    public LightningStrike strikeLightning(Location loc, boolean isSilent)
    {
        EntityLightning lightning = new EntityLightning(paramCraftWorld.getHandle(), loc.getX(), loc.getY(), loc.getZ(), false, isSilent);
        paramCraftWorld.getHandle().strikeLightning(lightning);
        return new CraftLightningStrike(paramCraftWorld.getHandle().getServer(), lightning);
    }

    public LightningStrike strikeLightningEffect(Location loc, boolean isSilent)
    {
        EntityLightning lightning = new EntityLightning(paramCraftWorld.getHandle(), loc.getX(), loc.getY(), loc.getZ(), true, isSilent);
        paramCraftWorld.getHandle().strikeLightning(lightning);
        return new CraftLightningStrike(paramCraftWorld.getHandle().getServer(), lightning);
    }
}
