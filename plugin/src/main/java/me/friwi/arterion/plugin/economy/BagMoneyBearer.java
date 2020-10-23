package me.friwi.arterion.plugin.economy;

import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.ui.hotbar.HotbarMoneyCard;
import me.friwi.arterion.plugin.ui.mod.server.ModValueEnum;
import me.friwi.arterion.plugin.util.database.Database;
import me.friwi.arterion.plugin.util.database.entity.DatabasePlayer;

public class BagMoneyBearer implements MoneyBearer {
    private ArterionPlayer player;
    private long money;

    public BagMoneyBearer(ArterionPlayer player, long money) {
        this.player = player;
        this.money = money;
    }

    @Override
    public long getCachedMoney() {
        return money;
    }

    @Override
    public long getCapacity() {
        return Long.MAX_VALUE;
    }

    @Override
    public void notifyInternal(long amount) {
        if (amount == 0) return;
        player.scheduleHotbarCard(new HotbarMoneyCard(player, amount));
    }

    @Override
    public long getMoneyFromDatabaseInternal(Database db) {
        DatabasePlayer dp = db.find(DatabasePlayer.class, this.player.getBukkitPlayer().getUniqueId());
        return dp.getGold();
    }

    @Override
    public void setMoneyToDatabaseInternal(Database db, long money) {
        DatabasePlayer dp = db.find(DatabasePlayer.class, this.player.getBukkitPlayer().getUniqueId());
        dp.setGold(money);
        db.save(dp);
    }

    @Override
    public void setCachedMoneyInternal(long money) {
        this.money = money;
        player.getPlayerScoreboard().updateModValue(ModValueEnum.GOLD);
    }
}
