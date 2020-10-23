package me.friwi.arterion.plugin.ui.hotbar;

public abstract class HotbarCard {
    protected long duration;
    private long created;
    private long expires;

    public HotbarCard(long duration) {
        this.duration = duration;
    }

    public void start() {
        if (this.created == 0) {
            this.created = System.currentTimeMillis();
            this.expires = created + duration;
        } else {
            this.expires = System.currentTimeMillis() + duration;
        }
    }

    public long getCreated() {
        return created;
    }

    public long getExpires() {
        return expires;
    }

    public void setExpires(long expires) {
        this.expires = expires;
    }

    public boolean isExpired() {
        return expires <= System.currentTimeMillis();
    }

    public abstract String getMessage();
}
