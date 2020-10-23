package me.friwi.recordable;

import org.bukkit.Location;
import org.bukkit.World;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.UUID;
import java.util.function.Consumer;

public interface RecordingCreator {
    /**
     * Record initialization
     */
    void initializeRecording(File dir, Location initLocation, int lcx, int hcx, int lcz, int hcz) throws IOException;

    /**
     * Record begin and finalization
     */
    void beginRecording();
    void endRecording(Consumer<File> onComplete);

    /**
     * Settings for the whole file
     */
    void setThumbnail(BufferedImage image);
    void setServerName(String serverName);
    void setTablistHeaderFooter(String header, String footer);
    void setPlayerListName(UUID uuid, String listName);
    void createObjective(String objectiveName, String text);
    void createObjective(String objectiveName, String text, boolean displayAsHeart);
    void displayObjectiveList(String objectiveName);
    void displayObjectiveSidebar(String objectiveName);
    void displayObjectiveBelowName(String objectiveName);
    void createTeam(String team, String color, String displayName, String prefix, String suffix, Collection<String> players);
    Collection<UUID> getOccuringPlayers();
    World getWorld();

    /**
     * Time dependent settings
     */
    void appendPacket(Object packet, int x, int z);
    void addChat(String msg);
    void addChatJson(String json);
    void addOrUpdateScore(String score, String objectiveName, int value);
    void removeScore(String score);
    void removeTeam(String team);
    void updateTeamInfo(String team, String color, String displayName, String prefix, String suffix);
    void addPlayersToTeam(String team, Collection<String> players);
    void removePlayersFromTeam(String team, Collection<String> players);

    /**
     * Utils
     */
    boolean isLocationRelevant(Location loc);
}
