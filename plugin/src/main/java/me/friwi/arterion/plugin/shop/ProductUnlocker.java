package me.friwi.arterion.plugin.shop;

import me.friwi.arterion.plugin.ArterionPlugin;
import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.player.ArterionPlayerUtil;
import me.friwi.arterion.plugin.util.database.entity.DatabaseBuyEntry;
import me.friwi.arterion.plugin.util.scheduler.InternalTask;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

public class ProductUnlocker {
    public static boolean hasPremiumUnlock(UUID buyerId) {
        ArterionPlugin.getInstance().getExternalDatabase().beginTransaction();
        List<DatabaseBuyEntry> entries = ArterionPlugin.getInstance().getExternalDatabase().findAllByColumn(DatabaseBuyEntry.class, "buyerId", buyerId);
        ArterionPlugin.getInstance().getExternalDatabase().commit();
        for (DatabaseBuyEntry ent : entries) {
            if (ent.getProduct().isPremiumUnlock()) return true;
        }
        return false;
    }

    public static void unlockAllProducts(ArterionPlayer player) {
        ArterionPlugin.getInstance().getExternalDatabase().beginTransaction();
        List<DatabaseBuyEntry> entries = ArterionPlugin.getInstance().getExternalDatabase().findAllByColumn(DatabaseBuyEntry.class, "buyerId", player.getUUID());
        for (DatabaseBuyEntry ent : entries) {
            ArterionPlugin.getInstance().getSchedulers().getMainScheduler().executeInSpigotCircle(new InternalTask() {
                @Override
                public void run() {
                    ent.getProduct().applyProduct(player);
                }
            });
            ArterionPlugin.getInstance().getExternalDatabase().delete(ent);
        }
        ArterionPlugin.getInstance().getExternalDatabase().commit();
    }

    public static void unlockAllProducts() {
        ArterionPlugin.getInstance().getExternalDatabase().beginTransaction();
        List<DatabaseBuyEntry> entries = ArterionPlugin.getInstance().getExternalDatabase().findAll(DatabaseBuyEntry.class);
        for (Player p : ArterionPlugin.getOnlinePlayers()) {
            ArterionPlayer ap = ArterionPlayerUtil.get(p);
            for (DatabaseBuyEntry ent : entries) {
                if (ent.getBuyerId().equals(ap.getUUID())) {
                    ArterionPlugin.getInstance().getSchedulers().getMainScheduler().executeInSpigotCircle(new InternalTask() {
                        @Override
                        public void run() {
                            ent.getProduct().applyProduct(ap);
                        }
                    });
                    ArterionPlugin.getInstance().getExternalDatabase().delete(ent);
                }
            }
        }
        ArterionPlugin.getInstance().getExternalDatabase().commit();
    }

    public static void startProductUnlockScheduler() {
        ArterionPlugin.getInstance().getSchedulers().getDatabaseScheduler().executeInMyCircleTimer(new InternalTask() {
            @Override
            public void run() {
                unlockAllProducts();
            }
        }, 20 * 60 * 5, 20 * 60 * 5);
    }
}
