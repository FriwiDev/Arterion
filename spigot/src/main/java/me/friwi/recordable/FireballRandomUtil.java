package me.friwi.recordable;

import org.bukkit.entity.Fireball;

import java.lang.reflect.Method;

public class FireballRandomUtil {
    private static Method getHandle, setEliminateRandomFactors, getEliminateRandomFactors;

    static{
        try{
            getHandle = Class.forName("org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity").getMethod("getHandle");
            setEliminateRandomFactors = Class.forName("net.minecraft.server.v1_8_R3.EntityFireball").getMethod("setEliminateRandomFactors", boolean.class);
            getEliminateRandomFactors = Class.forName("net.minecraft.server.v1_8_R3.EntityFireball").getMethod("getEliminateRandomFactors");
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void setEliminateRandomFactors(Fireball entity, boolean eliminateRandom){
        try{
            Object mce = getHandle.invoke(entity);
            setEliminateRandomFactors.invoke(mce, eliminateRandom);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static boolean getEliminateRandomFactors(Fireball entity){
        try{
            Object mce = getHandle.invoke(entity);
            return (boolean) getEliminateRandomFactors.invoke(mce);
        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }
}
