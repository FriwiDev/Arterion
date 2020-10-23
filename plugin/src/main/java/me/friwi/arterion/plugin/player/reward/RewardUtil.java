package me.friwi.arterion.plugin.player.reward;

import me.friwi.arterion.plugin.ArterionPlugin;
import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.player.ArterionPlayerUtil;
import me.friwi.arterion.plugin.util.database.DatabaseObjectTask;
import me.friwi.arterion.plugin.util.database.entity.DatabasePlayer;
import me.friwi.arterion.plugin.util.language.translateables.CollectionTranslateable;
import me.friwi.arterion.plugin.world.item.XPItem;
import me.friwi.arterion.plugin.world.item.lock.GuildLockItem;
import me.friwi.arterion.plugin.world.item.lock.OfficerLockItem;
import me.friwi.arterion.plugin.world.item.lock.PrivateLockItem;
import me.friwi.arterion.plugin.world.item.siege.*;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class RewardUtil {
    public static final List<ItemStack> randomVoteRewards = new ArrayList<>();

    static {
        randomVoteRewards.add(new ItemStack(Material.IRON_INGOT, 4));
        randomVoteRewards.add(new ItemStack(Material.DIAMOND, 1));
        ItemStack ret = new ItemStack(Material.ENCHANTED_BOOK);
        ret.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, 2);
        randomVoteRewards.add(ret);
        ret = new ItemStack(Material.ENCHANTED_BOOK);
        EnchantmentStorageMeta meta = (EnchantmentStorageMeta) ret.getItemMeta();
        meta.addStoredEnchant(Enchantment.DIG_SPEED, 3, true);
        ret.setItemMeta(meta);
        randomVoteRewards.add(ret);
        randomVoteRewards.add(new ItemStack(Material.EXP_BOTTLE, 8));
        randomVoteRewards.add(new ItemStack(Material.LOG, 10));
        randomVoteRewards.add(new ItemStack(Material.EMERALD, 2));
        randomVoteRewards.add(new ItemStack(Material.DIAMOND_PICKAXE));
        randomVoteRewards.add(new ItemStack(Material.GOLDEN_CARROT, 6));
        randomVoteRewards.add(new ItemStack(Material.OBSIDIAN, 2));
        randomVoteRewards.add(new ItemStack(Material.COAL_BLOCK, 2));
        ret = new ItemStack(Material.ENCHANTED_BOOK);
        meta = (EnchantmentStorageMeta) ret.getItemMeta();
        meta.addStoredEnchant(Enchantment.DURABILITY, 2, true);
        ret.setItemMeta(meta);
        randomVoteRewards.add(ret);
        ret = new ItemStack(Material.FISHING_ROD);
        ret.addUnsafeEnchantment(Enchantment.LURE, 2);
        randomVoteRewards.add(ret);
    }


    public static void onVote(String username) {
        Player p = Bukkit.getPlayer(username);
        if (p != null && p.isOnline()) {
            onVote(ArterionPlayerUtil.get(p), 1);
        } else {
            new DatabaseObjectTask<DatabasePlayer>(DatabasePlayer.class, "name", username) {

                @Override
                public void updateObject(DatabasePlayer databasePlayer) {
                    onVote(databasePlayer);
                }

                @Override
                public void success() {
                    //Congrats, but not important either
                }

                @Override
                public void fail() {
                    //Ignore, player probably not played before
                }
            }.execute();
        }
    }

    /**
     * Executed on main thread
     *
     * @param player
     */
    private static void onVote(ArterionPlayer player, int times) {
        List<ItemStack> rewards = new LinkedList<>();
        int gold = 0;
        long lastAction = player.getPersistenceHolder().getLastVote();
        long newAction = System.currentTimeMillis();
        StreakContinueEnum streakContinueEnum = getStreakContinue(lastAction, newAction);
        if (streakContinueEnum == StreakContinueEnum.NEXT_DAY) {
            int newStreak = player.getPersistenceHolder().getVoteDayStreak() + 1;
            player.setVoteInfo(newAction, newStreak, 0, succ -> {
            });
            ItemStack r = getVoteStreakReward(newStreak);
            if (r != null) rewards.add(r);
            gold += getVoteStreakGoldReward(newStreak);
            player.sendTranslation("rewards.vote.newstreak", newStreak);
        } else if (streakContinueEnum == StreakContinueEnum.STREAK_LOOSE && player.getPersistenceHolder().getVoteDayStreak() > 0) {
            player.setVoteInfo(newAction, 0, 0, succ -> {
            });
            player.sendTranslation("rewards.vote.loosestreak");
        } else {
            player.setVoteInfo(newAction, player.getPersistenceHolder().getVoteDayStreak(), 0, succ -> {
            });
            if (player.getPersistenceHolder().getVoteDayStreak() > 0) {
                player.sendTranslation("rewards.vote.continuestreak", player.getPersistenceHolder().getVoteDayStreak());
            } else {
                player.sendTranslation("rewards.vote.encouragestreak");
            }
        }
        player.sendTranslation("rewards.vote.received", times);
        for (int i = 0; i < times; i++) {
            ItemStack r = getGenericVoteReward();
            if (r != null) rewards.add(r);
            rewards.add(new XPItem(3000).toItemStack());
        }
        for (int i = 0; i < times; i++) gold += getGenericVoteGoldReward();
        giveToPlayer(player, rewards, gold);
    }

    /**
     * Executed on db thread
     *
     * @param player
     */
    private static void onVote(DatabasePlayer player) {
        player.setOfflineVotes(player.getOfflineVotes() + 1);
    }

    public static void onLogin(ArterionPlayer player) {
        List<ItemStack> rewards = new LinkedList<>();
        int gold = 0;
        long lastAction = player.getPersistenceHolder().getLastLogin();
        long newAction = System.currentTimeMillis();
        StreakContinueEnum streakContinueEnum = getStreakContinue(lastAction, newAction);
        if (streakContinueEnum == StreakContinueEnum.NEXT_DAY) {
            int newStreak = player.getPersistenceHolder().getLoginStreak() + 1;
            player.setLoginStreakInfo(newAction, newStreak, succ -> {
            });
            ItemStack r = getLoginStreakReward(newStreak);
            if (r != null) rewards.add(r);
            gold += getLoginStreakGoldReward(newStreak);
            player.sendTranslation("rewards.login.newstreak", newStreak);
        } else if (streakContinueEnum == StreakContinueEnum.STREAK_LOOSE && player.getPersistenceHolder().getVoteDayStreak() > 0) {
            player.setLoginStreakInfo(newAction, 0, succ -> {
            });
            player.sendTranslation("rewards.login.loosestreak");
        } else {
            player.setLoginStreakInfo(newAction, player.getPersistenceHolder().getLoginStreak(), succ -> {
            });
            if (player.getPersistenceHolder().getLoginStreak() > 0) {
                player.sendTranslation("rewards.login.continuestreak", player.getPersistenceHolder().getLoginStreak());
            } else {
                player.sendTranslation("rewards.login.encouragestreak");
            }
        }
        giveToPlayer(player, rewards, gold);
        //Process offline votes
        if (player.getPersistenceHolder().getOfflineVotes() > 0)
            onVote(player, player.getPersistenceHolder().getOfflineVotes());
        else player.sendTranslation("rewards.vote.encouragestreak");
    }

    public static StreakContinueEnum getStreakContinue(long lastAction, long newAction) {
        ZonedDateTime last = LocalDateTime.ofInstant(Instant.ofEpochMilli(lastAction), ArterionPlugin.SERVER_TIME_ZONE).atZone(ArterionPlugin.SERVER_TIME_ZONE).withZoneSameInstant(ArterionPlugin.TIME_ZONE);
        ZonedDateTime now = LocalDateTime.ofInstant(Instant.ofEpochMilli(newAction), ArterionPlugin.SERVER_TIME_ZONE).atZone(ArterionPlugin.SERVER_TIME_ZONE).withZoneSameInstant(ArterionPlugin.TIME_ZONE);
        long lastDay = Instant.from(last).getEpochSecond() / (24 * 60 * 60);
        long currentDay = Instant.from(now).getEpochSecond() / (24 * 60 * 60);
        if (lastDay >= currentDay) {
            return StreakContinueEnum.SAME_DAY;
        } else if (lastDay + 1 == currentDay) {
            return StreakContinueEnum.NEXT_DAY;
        } else {
            return StreakContinueEnum.STREAK_LOOSE;
        }
    }

    private static void giveToPlayer(ArterionPlayer player, List<ItemStack> rewards, int gold) {
        if (!rewards.isEmpty()) {
            List<String> rewardStrings = new LinkedList<>();
            for (ItemStack stack : rewards) {
                player.getBukkitPlayer().getInventory().addItem(stack);
                rewardStrings.add(stack.getAmount() + "x " + player.getLanguage().translateObject(stack));
            }
            player.getBukkitPlayer().updateInventory();
            CollectionTranslateable t = new CollectionTranslateable(rewardStrings);
            player.sendTranslation("rewards.itemreceived", t.getCaption(player.getLanguage(), "\2476", "\2477"));
        }
        if (gold > 0) {
            player.getBagMoneyBearer().transferMoney(-gold, null, succ -> {
            });
            player.sendTranslation("rewards.goldreceived", gold / 100d);
        }
        if (!rewards.isEmpty() || gold > 0) {
            player.getBukkitPlayer().playSound(player.getBukkitPlayer().getLocation(), Sound.LEVEL_UP, 0.7f, 1f);
        }
    }

    public static ItemStack getLoginStreakReward(int day) {
        if (day == 0) return null;
        day--;
        day %= 14;
        day++;
        switch (day) {
            case 1:
                return new GuildLockItem().toItemStack();
            case 2:
                ItemStack ret = new NormalTntItem().toItemStack();
                ret.setAmount(2);
                return ret;
            case 3:
                return new ItemStack(Material.EXP_BOTTLE, 16);
            case 4:
                ret = new PrivateLockItem().toItemStack();
                ret.setAmount(2);
                return ret;
            case 5:
                ret = new LockPickItem().toItemStack();
                ret.setAmount(2);
                return ret;
            case 6:
                ret = new ItemStack(Material.ENCHANTED_BOOK);
                EnchantmentStorageMeta meta = (EnchantmentStorageMeta) ret.getItemMeta();
                meta.addStoredEnchant(Enchantment.DURABILITY, 3, true);
                ret.setItemMeta(meta);
                return ret;
            case 7:
                return new TowerItem().toItemStack();
            case 8:
                ret = new SolidifyItem().toItemStack();
                ret.setAmount(3);
                return ret;
            case 9:
                return new ItemStack(Material.EXP_BOTTLE, 24);
            case 10:
                return new ItemStack(Material.COAL, 48);
            case 11:
                ret = new NormalTntItem().toItemStack();
                ret.setAmount(5);
                return ret;
            case 12:
                ret = new ObsidianTntItem().toItemStack();
                ret.setAmount(2);
                return ret;
            case 13:
                ret = new BridgeItem().toItemStack();
                ret.setAmount(3);
                return ret;
        }
        return null;
    }

    public static ItemStack getVoteStreakReward(int day) {
        if (day == 0) return null;
        day--;
        day %= 14;
        day++;
        switch (day) {
            case 1:
                return new PrivateLockItem().toItemStack();
            case 2:
                return new ItemStack(Material.EMERALD, 3);
            case 3:
                return new XPItem(6000).toItemStack();
            case 4:
                ItemStack ret = new NormalTntItem().toItemStack();
                ret.setAmount(3);
                return ret;
            case 5:
                ret = new ItemStack(Material.ENCHANTED_BOOK);
                EnchantmentStorageMeta meta = (EnchantmentStorageMeta) ret.getItemMeta();
                meta.addStoredEnchant(Enchantment.DAMAGE_ALL, 3, true);
                ret.setItemMeta(meta);
                return ret;
            case 6:
                ret = new FreezeItem().toItemStack();
                ret.setAmount(3);
                return ret;
            case 7:
                return new XPItem(300).toItemStack();
            case 8:
                ret = new ItemStack(Material.ENCHANTED_BOOK);
                meta = (EnchantmentStorageMeta) ret.getItemMeta();
                meta.addStoredEnchant(Enchantment.DURABILITY, 2, true);
                ret.setItemMeta(meta);
                return ret;
            case 9:
                ret = new TowerItem().toItemStack();
                ret.setAmount(2);
                return ret;
            case 11:
                ret = new GuildLockItem().toItemStack();
                ret.setAmount(4);
                return ret;
            case 12:
                ret = new OfficerLockItem().toItemStack();
                ret.setAmount(2);
                return ret;
            case 13:
                ret = new LockPickItem().toItemStack();
                ret.setAmount(3);
                return ret;
            case 14:
                ret = new ObsidianTntItem().toItemStack();
                ret.setAmount(4);
                return ret;
        }
        return null;
    }

    public static ItemStack getGenericVoteReward() {
        return randomVoteRewards.get((int) (Math.random() * randomVoteRewards.size()));
    }

    public static int getLoginStreakGoldReward(int day) {
        return day != 0 && day % 14 == 0 ? 10000 : 0;
    }

    public static int getVoteStreakGoldReward(int day) {
        return day % 14 == 10 ? 15000 : 0;
    }

    public static int getGenericVoteGoldReward() {
        return 0;
    }

    public enum StreakContinueEnum {
        SAME_DAY,
        NEXT_DAY,
        STREAK_LOOSE
    }
}
