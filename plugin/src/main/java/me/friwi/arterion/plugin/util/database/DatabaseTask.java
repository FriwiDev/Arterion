package me.friwi.arterion.plugin.util.database;

import me.friwi.arterion.plugin.ArterionPlugin;
import me.friwi.arterion.plugin.util.scheduler.InternalTask;

public abstract class DatabaseTask extends InternalTask {
    private int attempts = 0;

    /**
     * Attempts to perform the transaction
     *
     * @return true if the transaction should be performed, false otherwise
     */
    public abstract boolean performTransaction(Database db);

    public abstract void onTransactionCommitOrRollback(boolean committed);

    public abstract void onTransactionError();

    public void run() {
        Database db = ArterionPlugin.getInstance().getExternalDatabase();
        boolean b = db.beginTransaction();
        if (b) {
            boolean success = false;
            try {
                success = performTransaction(db);
            } catch (Throwable e) {
                e.printStackTrace();
            }
            if (success) b = db.commit();
            else b = db.rollback();
            if (b) {
                this.cancel();
                this.onTransactionCommitOrRollback(success);
                return;
            }
        }
        //Either transaction begin or transaction end failed
        //Retry 3 times, then error
        attempts++;
        if (attempts >= 3) {
            this.cancel();
            this.onTransactionError();
            return;
        }
    }

    public void execute() {
        ArterionPlugin.getInstance().getSchedulers().getDatabaseScheduler().executeInMyCircleTimer(this, 0, 20);
    }
}
