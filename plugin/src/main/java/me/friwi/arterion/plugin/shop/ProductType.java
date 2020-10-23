package me.friwi.arterion.plugin.shop;

import me.friwi.arterion.plugin.ArterionPlugin;
import me.friwi.arterion.plugin.permissions.Rank;
import me.friwi.arterion.plugin.player.ArterionPlayer;
import org.bukkit.Sound;

public enum ProductType {
    PREMIUM(true) {
        @Override
        public void applyProduct(ArterionPlayer player) {
            player.updateInDB(dbp -> dbp.setRemainingBoosters(dbp.getRemainingBoosters() + 2), succ -> {
            });
            if (player.getRank().isLowerOrEqualThan(Rank.PREMIUM)) {
                player.setRank(ArterionPlugin.getInstance(), Rank.PREMIUM);
            }
            player.sendTranslation("premium.unlock");
            player.getBukkitPlayer().playSound(player.getBukkitPlayer().getLocation(), Sound.LEVEL_UP, 0.8f, 1f);
        }
    },
    BOOSTER(false) {
        @Override
        public void applyProduct(ArterionPlayer player) {
            player.updateInDB(dbp -> dbp.setRemainingBoosters(dbp.getRemainingBoosters() + 1), succ -> {
            });
            player.sendTranslation("booster.unlock");
            player.getBukkitPlayer().playSound(player.getBukkitPlayer().getLocation(), Sound.LEVEL_UP, 0.8f, 1f);
        }
    };
    private boolean isPremiumUnlock;

    ProductType(boolean isPremiumUnlock) {
        this.isPremiumUnlock = isPremiumUnlock;
    }

    public boolean isPremiumUnlock() {
        return isPremiumUnlock;
    }

    public void applyProduct(ArterionPlayer player) {

    }
}
