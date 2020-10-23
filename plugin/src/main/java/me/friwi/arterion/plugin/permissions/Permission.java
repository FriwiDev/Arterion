package me.friwi.arterion.plugin.permissions;

import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.player.ArterionPlayerUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Permission {
    public static Rank getRank(CommandSender sender) {
        Rank rank = Rank.NORMAL;
        ArterionPlayer ep = null;
        if (sender instanceof Player) {
            ep = ArterionPlayerUtil.get((Player) sender);
            if (ep != null) rank = ep.getRank();
        }
        if (sender.isOp()) rank = Rank.ADMIN;
        return rank;
    }
}
