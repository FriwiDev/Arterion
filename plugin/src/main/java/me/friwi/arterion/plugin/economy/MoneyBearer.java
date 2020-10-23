package me.friwi.arterion.plugin.economy;

import me.friwi.arterion.plugin.util.database.Database;
import me.friwi.arterion.plugin.util.database.DatabaseTask;

import java.util.function.Consumer;

public interface MoneyBearer {
    long getCachedMoney();

    long getCapacity();

    long getMoneyFromDatabaseInternal(Database db);

    void notifyInternal(long amount);

    void setMoneyToDatabaseInternal(Database db, long money);

    void setCachedMoneyInternal(long money);

    default void addMoney(long money, Consumer<Boolean> callback) {
        this.addMoney(money, callback, false);
    }

    default void addMoney(long money, Consumer<Boolean> callback, boolean allowOverdraw) {
        //Negative transfer to server (null)
        this.transferMoney(-money, null, callback, allowOverdraw);
    }

    default void transferMoney(long money, MoneyBearer target, Consumer<Boolean> callback) {
        this.transferMoney(money, target, callback, false);
    }

    default void transferMoney(long money, MoneyBearer target, Consumer<Boolean> callback, boolean allowOverdraw) {
        //Positive transfer to party target. Transfer is performed in one database transaction
        new DatabaseTask() {
            long from, to = 0;

            @Override
            public boolean performTransaction(Database db) {
                from = MoneyBearer.this.getMoneyFromDatabaseInternal(db);
                to = target == null ? Long.MAX_VALUE / 2 : target.getMoneyFromDatabaseInternal(db);
                //Check for long overflow
                if (money > (Long.MAX_VALUE / 2 - 100) || money < (Long.MIN_VALUE / 2 + 100)) return false;
                //Check for positive and negative transactions!
                if ((from >= money || allowOverdraw) && (to >= -money || allowOverdraw) && (target == null || (to + money <= target.getCapacity()) && (from - money <= MoneyBearer.this.getCapacity()))) {
                    from -= money;
                    to += money;
                    MoneyBearer.this.setMoneyToDatabaseInternal(db, from);
                    if (target != null) target.setMoneyToDatabaseInternal(db, to);
                    return true;
                } else {
                    return false;
                }
            }

            @Override
            public void onTransactionCommitOrRollback(boolean committed) {
                if (committed) {
                    MoneyBearer.this.setCachedMoneyInternal(from);
                    MoneyBearer.this.notifyInternal(-money);
                    if (target != null) {
                        target.setCachedMoneyInternal(to);
                        target.notifyInternal(money);
                    }
                } else {
                    //Money was retrieved from db but not modified
                    //We can update our cached values to the db version here
                    MoneyBearer.this.setCachedMoneyInternal(from);
                    if (target != null) {
                        target.setCachedMoneyInternal(to);
                    }
                }
                callback.accept(committed);
            }

            @Override
            public void onTransactionError() {
                callback.accept(false);
            }
        }.execute();
    }
}
