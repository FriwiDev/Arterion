package me.friwi.recordable;

import org.bukkit.entity.Entity;

import java.lang.reflect.Method;

public class InvisibleEntityUtil {
    private static Method getHandle, setInvisible, getInvisible;

    static{
        try{
            getHandle = Class.forName("org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity").getMethod("getHandle");
            setInvisible = Class.forName("net.minecraft.server.v1_8_R3.Entity").getMethod("setArterionInvisible", boolean.class);
            getInvisible = Class.forName("net.minecraft.server.v1_8_R3.Entity").getMethod("getArterionInvisible");
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void setInvisible(Entity entity, boolean invisible){
        try{
            Object mce = getHandle.invoke(entity);
            setInvisible.invoke(mce, invisible);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static boolean getInvisible(Entity entity){
        try{
            Object mce = getHandle.invoke(entity);
            return (boolean) getInvisible.invoke(mce);
        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }
}
