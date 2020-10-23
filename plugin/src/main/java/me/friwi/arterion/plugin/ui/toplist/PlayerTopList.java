package me.friwi.arterion.plugin.ui.toplist;

import me.friwi.arterion.plugin.ui.gui.HeadCacheUtil;
import me.friwi.arterion.plugin.util.database.Database;
import me.friwi.arterion.plugin.util.database.DatabaseTask;
import me.friwi.arterion.plugin.util.database.entity.DatabasePlayer;
import me.friwi.arterion.plugin.util.language.api.LanguageAPI;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import java.util.List;

public class PlayerTopList extends TopList {
    public PlayerTopList(int length, Block begin, BlockFace direction) {
        super(length, begin, direction);
    }

    @Override
    public void refreshTopList() {
        new DatabaseTask() {

            @Override
            public boolean performTransaction(Database db) {
                List<DatabasePlayer> top = db.findAllSortByColumnDescWithLimit(DatabasePlayer.class, "kills", length);
                for (int i = 0; i < length; i++) {
                    DatabasePlayer p = i >= top.size() ? null : top.get(i);
                    if (p == null) {
                        PlayerTopList.this.setEntry(i, HeadCacheUtil.QUESTION_MARK, new String[]{
                                LanguageAPI.translate("none.first"),
                                LanguageAPI.translate("none.second"),
                                LanguageAPI.translate("none.third"),
                                LanguageAPI.translate("none.fourth")
                        });
                    } else {
                        PlayerTopList.this.setEntry(i, p.getUuid(), new String[]{
                                LanguageAPI.translate("player.first", i + 1),
                                LanguageAPI.translate("player.second"),
                                LanguageAPI.translate("player.third", p.getName()),
                                LanguageAPI.translate("player.fourth", p.getKills())
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
