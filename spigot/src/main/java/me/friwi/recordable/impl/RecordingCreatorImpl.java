package me.friwi.recordable.impl;

import me.friwi.recordable.RecordingCreator;
import net.minecraft.server.v1_8_R3.Packet;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.UUID;
import java.util.function.Consumer;

public class RecordingCreatorImpl implements RecordingCreator {
    private Recording recording;
    private World world;

    public RecordingCreatorImpl(){}

    @Override
    public void initializeRecording(File dir, Location initLocation, int lcx, int hcx, int lcz, int hcz) throws IOException {
        world = initLocation.getWorld();
        recording = new Recording(dir, initLocation, lcx, hcx, lcz, hcz);
        PacketCreationListener.getRecordings().add(recording);
    }

    @Override
    public void beginRecording() {
        recording.beginRecording();
    }

    @Override
    public void endRecording(Consumer<File> onComplete) {
        PacketCreationListener.getRecordings().remove(recording);
        recording.endRecording(onComplete);
    }

    @Override
    public void setThumbnail(BufferedImage image) {
        recording.setThumbnail(image);
    }

    @Override
    public void setServerName(String serverName) {
        recording.setServerName(serverName);
    }

    @Override
    public void setTablistHeaderFooter(String header, String footer) {
        recording.setTablistHeaderFooter(header, footer);
    }

    @Override
    public void setPlayerListName(UUID uuid, String listName) {
        recording.setPlayerListName(uuid, listName);
    }

    @Override
    public void createObjective(String objectiveName, String text) {
        recording.createObjective(objectiveName, text);
    }

    @Override
    public void createObjective(String objectiveName, String text, boolean displayAsHeart) {
        recording.createObjective(objectiveName, text, displayAsHeart);
    }

    @Override
    public void displayObjectiveList(String objectiveName) {
        recording.displayObjectiveList(objectiveName);
    }

    @Override
    public void displayObjectiveSidebar(String objectiveName) {
        recording.displayObjectiveSidebar(objectiveName);
    }

    @Override
    public void displayObjectiveBelowName(String objectiveName) {
        recording.displayObjectiveBelowName(objectiveName);
    }

    @Override
    public void createTeam(String team, String color, String displayName, String prefix, String suffix, Collection<String> players) {
        recording.createTeam(team, color, displayName, prefix, suffix, players);
    }

    @Override
    public Collection<UUID> getOccuringPlayers() {
        return recording.getOccuringPlayers();
    }

    @Override
    public World getWorld() {
        return world;
    }

    @Override
    public void appendPacket(Object packet, int x, int z) {
        recording.appendPacket((Packet) packet, x, z, 2);
    }

    @Override
    public void addChat(String msg) {
        recording.addChat(msg);
    }

    @Override
    public void addChatJson(String json) {
        recording.addChatJson(json);
    }

    @Override
    public void addOrUpdateScore(String score, String objectiveName, int value) {
        recording.addOrUpdateScore(score, objectiveName, value);
    }

    @Override
    public void removeScore(String score) {
        recording.removeScore(score);
    }

    @Override
    public void removeTeam(String team) {
        recording.removeTeam(team);
    }

    @Override
    public void updateTeamInfo(String team, String color, String displayName, String prefix, String suffix) {
        recording.updateTeamInfo(team, color, displayName, prefix, suffix);
    }

    @Override
    public void addPlayersToTeam(String team, Collection<String> players) {
        recording.addPlayersToTeam(team, players);
    }

    @Override
    public void removePlayersFromTeam(String team, Collection<String> players) {
        recording.removePlayersFromTeam(team, players);
    }

    @Override
    public boolean isLocationRelevant(Location loc) {
        return recording.isInViewRange(((CraftWorld)loc.getWorld()).getHandle(), loc.getBlockX(), loc.getBlockZ());
    }
}
