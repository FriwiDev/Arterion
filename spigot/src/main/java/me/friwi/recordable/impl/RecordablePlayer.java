package me.friwi.recordable.impl;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.v1_8_R3.EntityPlayer;
import net.minecraft.server.v1_8_R3.MinecraftServer;
import net.minecraft.server.v1_8_R3.PlayerInteractManager;
import net.minecraft.server.v1_8_R3.WorldServer;

public class RecordablePlayer extends EntityPlayer {
    private Recording recording;

    public RecordablePlayer(MinecraftServer minecraftserver, WorldServer worldserver, GameProfile gameprofile, PlayerInteractManager playerinteractmanager, Recording recording) {
        super(minecraftserver, worldserver, gameprofile, playerinteractmanager);
        this.recording = recording;
    }

    public Recording getRecording() {
        return recording;
    }
}
