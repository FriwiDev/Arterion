package me.friwi.arterion.plugin.economy;

import me.friwi.arterion.plugin.ArterionPlugin;
import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.util.database.Database;
import me.friwi.arterion.plugin.util.database.entity.DatabasePlayer;

public class BankMoneyBearer implements MoneyBearer {
    private ArterionPlayer player;
    private long money;

    public BankMoneyBearer(ArterionPlayer player, long money) {
        this.player = player;
        this.money = money;
    }

    @Override
    public long getCachedMoney() {
        return money;
    }

    @Override
    public long getCapacity() {
        return ArterionPlugin.getInstance().getFormulaManager().PLAYER_BANK_LIMIT.evaluateInt(player);
    }

    @Override
    public void notifyInternal(long amount) {
        if (amount == 0) return;
        float display = amount / 100f;
        if (amount > 0) {
            player.sendTranslation("economy.money.bank.gain", display);
        } else {
            player.sendTranslation("economy.money.bank.loose", display);
        }
    }

    @Override
    public long getMoneyFromDatabaseInternal(Database db) {
        DatabasePlayer dp = db.find(DatabasePlayer.class, this.player.getBukkitPlayer().getUniqueId());
        return dp.getBank();
    }

    @Override
    public void setMoneyToDatabaseInternal(Database db, long money) {
        DatabasePlayer dp = db.find(DatabasePlayer.class, this.player.getBukkitPlayer().getUniqueId());
        dp.setBank(money);
        db.save(dp);
    }

    @Override
    public void setCachedMoneyInternal(long money) {
        this.money = money;
    }
}
