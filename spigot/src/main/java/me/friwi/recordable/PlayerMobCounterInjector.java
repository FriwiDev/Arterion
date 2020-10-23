package me.friwi.recordable;

import net.minecraft.server.v1_8_R3.SpawnerCreature;
import org.bukkit.entity.Entity;

import java.lang.reflect.Method;

public class PlayerMobCounterInjector {
    public static void setPlayerMobCounter(PlayerMobCounter playerMobCounter){
        SpawnerCreature.setPlayerMobCounter(playerMobCounter);
    }
}
