package me.friwi.arterion.plugin.ui.toplist;

import me.friwi.arterion.plugin.ui.gui.HeadCacheUtil;
import me.friwi.arterion.plugin.util.database.Database;
import me.friwi.arterion.plugin.util.database.DatabaseTask;
import me.friwi.arterion.plugin.util.database.entity.DatabaseGuild;
import me.friwi.arterion.plugin.util.language.api.LanguageAPI;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import java.util.List;

public class GuildTopList extends TopList {
    public GuildTopList(int length, Block begin, BlockFace direction) {
        super(length, begin, direction);
    }

    @Override
    public void refreshTopList() {
        new DatabaseTask() {

            @Override
            public boolean performTransaction(Database db) {
                List<DatabaseGuild> top = db.findAllSortByColumnDescWithLimitWithColumnMatch(DatabaseGuild.class, "clanKills", length, "deleted", -1);
                for (int i = 0; i < length; i++) {
                    DatabaseGuild p = i >= top.size() ? null : top.get(i);
                    if (p == null) {
                        GuildTopList.this.setEntry(i, HeadCacheUtil.QUESTION_MARK, new String[]{
                                LanguageAPI.translate("none.first"),
                                LanguageAPI.translate("none.second"),
                                LanguageAPI.translate("none.third"),
                                LanguageAPI.translate("none.fourth")
                        });
                    } else {
                        GuildTopList.this.setEntry(i, p.getLeader().getUuid(), new String[]{
                                LanguageAPI.translate("guild.first", i + 1),
                                LanguageAPI.translate("guild.second"),
                                LanguageAPI.translate("guild.third", p.getName()),
                                LanguageAPI.translate("guild.fourth", p.getClanKills())
                        });
                    }
                }
                return true;
            }

            @Override
            public void onTransactionCommitOrRollback(boolean committed) {

            }

            @Override
            public void onTransactionError() {

            }
        }.execute();
    }
}
