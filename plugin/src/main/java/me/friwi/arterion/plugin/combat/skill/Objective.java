package me.friwi.arterion.plugin.combat.skill;

import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.ui.mod.packet.Packet06Objective;
import me.friwi.arterion.plugin.ui.mod.server.ModConnection;
import me.friwi.arterion.plugin.util.time.TimeFormatUtil;
import org.bukkit.inventory.ItemStack;

public class Objective {
    private ItemStack item;
    private String nmsMaterial;
    private String translate;
    private Object[] values;
    private long expires;

    public Objective(ItemStack item, String nmsMaterial, long expires, String translate, Object... values) {
        this.item = item;
        this.nmsMaterial = nmsMaterial;
        this.translate = translate;
        this.values = values;
        this.expires = expires;
    }

    public String getMessage(ArterionPlayer player) {
        String post = "";
        if (expires != -1) {
            post = "\247f: ";
            long remaining = expires - System.currentTimeMillis();
            post += TimeFormatUtil.formatSeconds(remaining / 1000);
        }
        return player.getTranslation(translate, values) + post;
    }

    public void sendToMod(ArterionPlayer player) {
        String title = player.getTranslation(translate, values);
        long remaining = expires == -1 ? Long.MIN_VALUE : (expires - System.currentTimeMillis());
        ModConnection.sendModPacket(player, new Packet06Objective(remaining, title));
    }

    public ItemStack getItem() {
        return item;
    }

    public void setItem(ItemStack item) {
        this.item = item;
    }

    public String getNmsMaterial() {
        return nmsMaterial;
    }

    public void setNmsMaterial(String nmsMaterial) {
        this.nmsMaterial = nmsMaterial;
    }

    public String getTranslate() {
        return translate;
    }

    public void setTranslate(String translate) {
        this.translate = translate;
    }

    public Object[] getValues() {
        return values;
    }

    public void setValues(Object[] values) {
        this.values = values;
    }

    public long getExpires() {
        return expires;
    }

    public void setExpires(long expires) {
        this.expires = expires;
    }

    public boolean isExpired() {
        return expires != -1 && this.expires <= System.currentTimeMillis();
    }
}
