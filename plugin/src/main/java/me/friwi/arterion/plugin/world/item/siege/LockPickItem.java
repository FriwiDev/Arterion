package me.friwi.arterion.plugin.world.item.siege;

import me.friwi.arterion.plugin.ArterionPlugin;
import me.friwi.arterion.plugin.guild.fight.GuildFight;
import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.player.ArterionPlayerUtil;
import me.friwi.arterion.plugin.player.PotionTrackerEntry;
import me.friwi.arterion.plugin.util.scheduler.InternalTask;
import me.friwi.arterion.plugin.world.chunk.ArterionChunkUtil;
import me.friwi.arterion.plugin.world.item.CustomItemType;
import me.friwi.arterion.plugin.world.lock.Lock;
import me.friwi.arterion.plugin.world.lock.LockUtil;
import me.friwi.arterion.plugin.world.lock.PrivateLock;
import me.friwi.arterion.plugin.world.region.GuildRegion;
import me.friwi.arterion.plugin.world.region.PlayerClaimRegion;
import me.friwi.arterion.plugin.world.region.Region;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.potion.PotionEffectType;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public class LockPickItem extends SiegeItem {
    private static List<UUID> usingPlayers = new LinkedList<>();

    public LockPickItem(ItemStack stack) {
        super(CustomItemType.SIEGE_LOCKPICK, stack);
    }

    public LockPickItem() {
        super(CustomItemType.SIEGE_LOCKPICK);
    }

    @Override
    protected void parseItem() {

    }

    @Override
    public ItemStack toItemStack() {
        if (stack != null) return stack;
        stack = toItemStack(Material.SHEARS, this.getType(), "siege.lockpick", getPrice());
        return stack;
    }

    @Override
    public boolean onPickup(ArterionPlayer player, Item item) {
        return true;
    }

    @Override
    public boolean onDrop(ArterionPlayer player, Item item) {
        return true;
    }

    @Override
    public boolean onPlaceWithoutChecks(ArterionPlayer arterionPlayer, Block block) {
        return false;
    }

    @Override
    public boolean onPlace(ArterionPlayer arterionPlayer, Block block) {
        return false;
    }

    @Override
    public boolean onInteract(ArterionPlayer arterionPlayer, Block b, BlockFace blockFace) {
        if (usingPlayers.contains(arterionPlayer.getBukkitPlayer().getUniqueId())) {
            arterionPlayer.sendTranslation("siege.lockpick.twice");
            return false;
        }

        int reducedChance = ArterionPlugin.getInstance().getFormulaManager().SIEGE_LOCKPICK_REDUCED_CHANCE.evaluateInt();
        int normalChance = ArterionPlugin.getInstance().getFormulaManager().SIEGE_LOCKPICK_NORMAL_CHANCE.evaluateInt();
        int delay = ArterionPlugin.getInstance().getFormulaManager().SIEGE_LOCKPICK_DELAY.evaluateInt();


        int chance = 0;
        if (b == null || (b.getType() != Material.CHEST && b.getType() != Material.TRAPPED_CHEST)) {
            arterionPlayer.sendTranslation("siege.lockpick.instructions");
            return false;
        }
        if (!checkUseOn(arterionPlayer, b)) return false;
        Chest bs = (Chest) b.getState();
        Lock lock = LockUtil.getLock(bs);
        if (lock == null) {
            arterionPlayer.sendTranslation("siege.lockpick.instructions");
            return false;
        }
        if (lock.isFriendlyLock(arterionPlayer)) {
            arterionPlayer.sendTranslation("siege.lockpick.notallowed");
            return false;
        }
        Region region = ArterionChunkUtil.getNonNull(b.getChunk()).getRegion();
        if (region == null) {
            chance = normalChance;
        } else if (region instanceof GuildRegion) {
            GuildFight fight = ((GuildRegion) region).getGuild().getLocalFight();
            if (fight != null && arterionPlayer.getGuild() != null && fight.getAttacker().equals(arterionPlayer.getGuild())) {
                if (fight.getBlockHp() <= 0) {
                    chance = normalChance;
                } else {
                    chance = reducedChance;
                }
                fight.getDefender().sendTranslation("siege.lockpick.begin_guild", arterionPlayer);
            } else {
                arterionPlayer.sendTranslation("siege.lockpick.need_attack");
                return false;
            }
        } else if (region instanceof PlayerClaimRegion) {
            if (lock instanceof PrivateLock) {
                Player p = Bukkit.getPlayer(((PrivateLock) lock).getOwner());
                if (p == null || !p.isOnline()) {
                    arterionPlayer.sendTranslation("siege.lockpick.need_online");
                    return false;
                } else {
                    ArterionPlayer ap = ArterionPlayerUtil.get(p);
                    ap.sendTranslation("siege.lockpick.begin_player", arterionPlayer);
                }
            }
            chance = normalChance;
        } else {
            chance = normalChance;
        }

        usingPlayers.add(arterionPlayer.getBukkitPlayer().getUniqueId());

        //Remove the item from hand
        this.printUseMessage(arterionPlayer);
        ItemStack inHand = arterionPlayer.getBukkitPlayer().getItemInHand();
        if (inHand.getAmount() <= 1) {
            inHand = null;
        } else {
            inHand.setAmount(inHand.getAmount() - 1);
        }
        arterionPlayer.getBukkitPlayer().setItemInHand(inHand);
        arterionPlayer.getBukkitPlayer().updateInventory();

        int intv = 20;
        Location backup = arterionPlayer.getBukkitPlayer().getLocation().clone();

        arterionPlayer.sendTranslation("siege.lockpick.begin", chance);
        PotionTrackerEntry slow = arterionPlayer.getPotionTracker().addPotionEffect(PotionEffectType.SLOW, 0, delay);
        PotionTrackerEntry weak = arterionPlayer.getPotionTracker().addPotionEffect(PotionEffectType.WEAKNESS, 2, delay);

        int finalChance = chance;
        ArterionPlugin.getInstance().getSchedulers().getMainScheduler().executeInSpigotCircleTimer(new InternalTask() {
            int tick = 0;

            @Override
            public void run() {
                if (!backup.getWorld().equals(arterionPlayer.getBukkitPlayer().getWorld()) || backup.distance(arterionPlayer.getBukkitPlayer().getLocation()) > 2
                        || arterionPlayer.getHealth() <= 0 || (b.getType() != Material.CHEST && b.getType() != Material.TRAPPED_CHEST)) {
                    arterionPlayer.sendTranslation("siege.lockpick.abort");
                    arterionPlayer.getPotionTracker().removePotionTrackerEntry(slow);
                    arterionPlayer.getPotionTracker().removePotionTrackerEntry(weak);
                    cancel();
                    usingPlayers.remove(arterionPlayer.getBukkitPlayer().getUniqueId());
                    return;
                }
                tick += intv;
                int remain = delay - tick;
                if (remain % 200 == 0 || remain == 100) {
                    arterionPlayer.sendTranslation("siege.lockpick.progress", remain / 20);
                }
                if (tick >= delay) {
                    usingPlayers.remove(arterionPlayer.getBukkitPlayer().getUniqueId());
                    cancel();
                    if (Math.random() <= (finalChance + 0f) / 100f) {
                        arterionPlayer.sendTranslation("siege.lockpick.success");
                        arterionPlayer.getBukkitPlayer().getWorld().playSound(arterionPlayer.getBukkitPlayer().getLocation(), Sound.DOOR_OPEN, 0.8f, 1f);
                        LockUtil.removeLock(bs, lock);
                    } else {
                        arterionPlayer.sendTranslation("siege.lockpick.fail");
                        arterionPlayer.getBukkitPlayer().getWorld().playSound(arterionPlayer.getBukkitPlayer().getLocation(), Sound.ITEM_BREAK, 0.8f, 1f);
                    }
                }
            }
        }, intv, intv);
        return false;
    }

    @Override
    public boolean onSwitchInventory(ArterionPlayer arterionPlayer) {
        return true;
    }

    @Override
    public boolean onInventoryPickup(Inventory inventory) {
        return true;
    }

    @Override
    public void registerRecipes() {
        // Create our custom recipe variable
        ShapedRecipe recipe = new ShapedRecipe(toItemStack());

        //Beware; this is case sensitive.
        recipe.shape(" H ", "SBS", " E ");

        // Set what the letters represent.
        recipe.setIngredient('E', Material.EMERALD);
        recipe.setIngredient('B', Material.BLAZE_ROD);
        recipe.setIngredient('S', Material.STRING);
        recipe.setIngredient('H', Material.TRIPWIRE_HOOK);

        // Finally, add the recipe to the bukkit recipes
        if (!Bukkit.addRecipe(recipe)) {
            System.out.println("Failed to add recipe!");
        }
    }

    @Override
    public String getName() {
        return "siege.lockpick";
    }
}
