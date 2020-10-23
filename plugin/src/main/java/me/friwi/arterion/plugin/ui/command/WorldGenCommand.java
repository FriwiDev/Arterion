package me.friwi.arterion.plugin.ui.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.HelpCommand;
import co.aikar.commands.annotation.*;
import me.friwi.arterion.plugin.ArterionPlugin;
import me.friwi.arterion.plugin.permissions.Permission;
import me.friwi.arterion.plugin.permissions.Rank;
import me.friwi.arterion.plugin.player.ArterionPlayerUtil;
import me.friwi.arterion.plugin.util.language.api.LanguageAPI;
import me.friwi.arterion.plugin.util.scheduler.InternalTask;
import me.friwi.arterion.plugin.util.time.TimeFormatUtil;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandAlias("admworldgen")
public class WorldGenCommand extends BaseCommand {
    private CommandManager commandManager;
    private boolean inProgress = false;

    public WorldGenCommand(CommandManager commandManager) {
        this.commandManager = commandManager;
    }

    @Default
    @CommandCompletion("@none")
    @Syntax("<radius>")
    public void setPlayerRank(CommandSender sender, String radius) {
        if (!Permission.getRank(sender).isHigherOrEqualThan(Rank.ADMIN)) {
            sender.sendMessage(LanguageAPI.translate(ArterionPlayerUtil.get((Player) sender), "command.notavailable"));
            return;
        }
        World world = ((Player) sender).getWorld();
        int r = Integer.parseInt(radius) / 16;
        if (inProgress) return;
        inProgress = true;
        System.out.println("Begin generating world...");
        ArterionPlugin.getInstance().getSchedulers().getMainScheduler().executeInSpigotCircleTimer(new InternalTask() {
            int x = -r;
            int z = -r;

            int todo = (r * 2 + 1) * (r * 2 + 1);
            int last = 0;
            long lastTime = System.currentTimeMillis();

            @Override
            public void run() {
                long start = System.currentTimeMillis();
                while (start + 10 > System.currentTimeMillis()) {
                    world.getChunkAt(x, z).load(true);
                    last++;
                    x++;
                    if (x > r) {
                        x = -r;
                        z++;
                        world.save();
                    }
                    if (z > r) {
                        cancel();
                        System.out.println("Done generating world!");
                        inProgress = false;
                        return;
                    }
                    if (lastTime + 10000 < System.currentTimeMillis()) {
                        todo -= last;
                        long time = System.currentTimeMillis() - lastTime;
                        long remainingSeconds = (long) ((todo + 0d) / (last + 0d) * 10);
                        System.out.println("World is generating. " + todo + " Chunks remaining at " + last + " Chunks per 10 seconds. Estimated time remaining: " + TimeFormatUtil.formatSeconds(remainingSeconds));
                        lastTime = System.currentTimeMillis();
                        last = 0;
                    }
                }
            }
        }, 1l, 1l);
    }

    @HelpCommand
    @Default
    public void help(CommandSender sender) {

    }
}
