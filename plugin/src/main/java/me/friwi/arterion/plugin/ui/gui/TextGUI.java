package me.friwi.arterion.plugin.ui.gui;

import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.ui.mod.packet.Packet08TextGui;
import me.friwi.arterion.plugin.ui.mod.server.ModConnection;
import me.friwi.arterion.plugin.ui.title.TitleAPI;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class TextGUI extends GUI {
    private Supplier<String[]> print;
    private Consumer<String> action;
    private String subtitle;
    private Runnable onClose;

    public TextGUI(ArterionPlayer player, String subtitle, Supplier<String[]> print, Consumer<String> action) {
        this(player, subtitle, print, action, () -> {
        });
    }

    public TextGUI(ArterionPlayer player, String subtitle, Supplier<String[]> print, Consumer<String> action, Runnable onClose) {
        super(player);
        this.subtitle = subtitle;
        this.print = print;
        this.action = action;
        this.onClose = onClose;
    }

    @Override
    public void open() {
        String[] st = print.get();
        if (getPlayer().getSkillSlots().usesMod()) {
            String desc = st[0];
            for (int i = 1; i < st.length; i++) desc += "\n" + st[i];
            ModConnection.sendModPacket(getPlayer(), new Packet08TextGui(subtitle, desc));
        } else {
            TitleAPI.send(this.getPlayer(), "", subtitle);
            this.getPlayer().sendTranslation("line");
            for (String s : st) this.getPlayer().getBukkitPlayer().sendMessage(s);
            this.getPlayer().sendTranslation("line");
            this.getPlayer().sendTranslation("gui.abort");
        }
    }

    @Override
    public void close() {
        TitleAPI.send(this.getPlayer(), "", this.getPlayer().getTranslation("gui.aborted"));
        this.getPlayer().sendTranslation("gui.aborted");
        this.getPlayer().closeGui(true);
        onClose.run();
    }

    @Override
    public boolean onChat(String msg) {
        if (msg.equalsIgnoreCase("q") || msg.equalsIgnoreCase("quit")) {
            close();
            return true;
        }
        action.accept(msg);
        this.getPlayer().closeGui(true);
        return true;
    }

    @Override
    public boolean onPlayerClickItem(Inventory clickedInventory, ClickType click, int slot) {
        return false;
    }

    @Override
    public void onClose() {

    }
}
