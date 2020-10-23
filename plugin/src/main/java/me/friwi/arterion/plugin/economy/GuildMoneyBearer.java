package me.friwi.arterion.plugin.economy;

import me.friwi.arterion.plugin.guild.Guild;
import me.friwi.arterion.plugin.util.database.Database;
import me.friwi.arterion.plugin.util.database.entity.DatabaseGuild;

public class GuildMoneyBearer implements MoneyBearer {
    private Guild guild;
    private long money;

    public GuildMoneyBearer(Guild guild, long money) {
        this.guild = guild;
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
        float display = amount / 100f;
        if (amount > 0) {
            guild.sendTranslation("economy.money.guild.gain", display);
        } else {
            guild.sendTranslation("economy.money.guild.loose", display);
        }
    }

    @Override
    public long getMoneyFromDatabaseInternal(Database db) {
        DatabaseGuild dp = db.find(DatabaseGuild.class, this.guild.getUuid());
        return dp.getGold();
    }

    @Override
    public void setMoneyToDatabaseInternal(Database db, long money) {
        DatabaseGuild dp = db.find(DatabaseGuild.class, this.guild.getUuid());
        dp.setGold(money);
        db.save(dp);
    }

    @Override
    public void setCachedMoneyInternal(long money) {
        this.money = money;
    }
}
