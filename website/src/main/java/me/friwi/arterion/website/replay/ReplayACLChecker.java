package me.friwi.arterion.website.replay;

import me.friwi.arterion.plugin.util.database.Database;
import me.friwi.arterion.plugin.util.database.entity.DatabaseGuild;
import me.friwi.arterion.plugin.util.database.entity.DatabasePlayer;
import me.friwi.arterion.plugin.util.database.enums.Rank;
import me.friwi.arterion.website.WebApplication;

import java.util.List;
import java.util.UUID;

public class ReplayACLChecker {
    public static boolean hasAccess(Object principal, ReplayACLFile acl) {
        UUID player = null;
        UUID guild = null;
        Rank rank = Rank.NORMAL;
        if (principal instanceof DatabasePlayer) {
            player = ((DatabasePlayer) principal).getUuid();
            rank = ((DatabasePlayer) principal).getRank();
            Database db = WebApplication.getDatabase();
            db.beginTransaction();
            List<DatabaseGuild> guilds = db.findAll(DatabaseGuild.class);
            db.commit();
            for (DatabaseGuild g : guilds) {
                if (g.getDeleted() == DatabaseGuild.NOT_DELETED) {
                    if (g.getLeader().equals(principal) || g.getOfficers().contains(principal) || g.getMembers().contains(principal)) {
                        guild = g.getUuid();
                        break;
                    }
                }
            }
        }
        return acl.hasAccess(player, guild, rank);
    }
}
