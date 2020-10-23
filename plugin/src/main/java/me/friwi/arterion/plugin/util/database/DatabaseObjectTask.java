package me.friwi.arterion.plugin.util.database;


public abstract class DatabaseObjectTask<T extends DatabaseEntity> extends DatabaseTask {
    private Class<T> targetClass;
    private String column = null;
    private Object id;
    private boolean fail = false;

    public DatabaseObjectTask(Class<T> targetClass, Object id) {
        this.targetClass = targetClass;
        this.id = id;
    }

    public DatabaseObjectTask(Class<T> targetClass, String column, Object id) {
        this.targetClass = targetClass;
        this.column = column;
        this.id = id;
    }

    @Override
    public boolean performTransaction(Database db) {
        T t = column == null ? db.find(targetClass, id) : db.findOneByColumn(targetClass, column, id);
        if (t == null) {
            fail = true;
            return true;
        }
        updateObject(t);
        db.save(t);
        return true;
    }

    @Override
    public void onTransactionCommitOrRollback(boolean committed) {
        if (committed && !fail) success();
        else fail();
    }

    @Override
    public void onTransactionError() {
        fail();
    }

    public abstract void updateObject(T t);

    public abstract void success();

    public abstract void fail();
}
