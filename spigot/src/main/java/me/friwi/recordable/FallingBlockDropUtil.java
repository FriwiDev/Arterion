package me.friwi.recordable;

import org.bukkit.entity.FallingBlock;

import java.lang.reflect.Method;

public class FallingBlockDropUtil {
    private static Method getHandle, setOnDrop;

    static{
        try{
            getHandle = Class.forName("org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity").getMethod("getHandle");
            setOnDrop = Class.forName("net.minecraft.server.v1_8_R3.EntityFallingBlock").getMethod("setOnDropItem", Runnable.class);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void setOnDropItem(FallingBlock entity, Runnable onDropItem){
        try{
            Object mce = getHandle.invoke(entity);
            setOnDrop.invoke(mce, onDropItem);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
