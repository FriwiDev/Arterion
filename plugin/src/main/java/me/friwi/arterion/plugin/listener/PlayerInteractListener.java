package me.friwi.arterion.plugin.listener;

import com.google.common.collect.ImmutableList;
import me.friwi.arterion.plugin.ArterionPlugin;
import me.friwi.arterion.plugin.combat.classes.ClassEnum;
import me.friwi.arterion.plugin.combat.skill.SkillEnum;
import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.player.ArterionPlayerUtil;
import me.friwi.arterion.plugin.stats.StatType;
import me.friwi.arterion.plugin.ui.gui.ItemGUI;
import me.friwi.arterion.plugin.ui.gui.NamedItemUtil;
import me.friwi.arterion.plugin.util.scheduler.InternalTask;
import me.friwi.arterion.plugin.world.banneditem.BannedItems;
import me.friwi.arterion.plugin.world.block.CustomBlock;
import me.friwi.arterion.plugin.world.block.CustomBlockUtil;
import me.friwi.arterion.plugin.world.block.nonbtblocks.SpecialBlock;
import me.friwi.arterion.plugin.world.chunk.ArterionChunkUtil;
import me.friwi.arterion.plugin.world.item.CustomItem;
import me.friwi.arterion.plugin.world.item.CustomItemUtil;
import me.friwi.arterion.plugin.world.lock.Lock;
import me.friwi.arterion.plugin.world.lock.LockUtil;
import me.friwi.arterion.plugin.world.region.BankRegion;
import me.friwi.arterion.plugin.world.region.Region;
import me.friwi.arterion.plugin.world.region.SpawnRegion;
import me.friwi.recordable.AnvilOpener;
import me.friwi.recordable.AnvilOpenerFactory;
import me.friwi.recordable.EnchantmentTableOpener;
import me.friwi.recordable.EnchantmentTableOpenerFactory;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

public class PlayerInteractListener implements Listener {
    private final List<Material> forbiddenItems = ImmutableList.of(
            Material.BOAT,
            Material.ARMOR_STAND,
            Material.ITEM_FRAME,
            Material.MINECART,
            Material.COMMAND_MINECART,
            Material.HOPPER_MINECART,
            Material.EXPLOSIVE_MINECART,
            Material.POWERED_MINECART,
            Material.STORAGE_MINECART,
            Material.PAINTING
    );

    private ArterionPlugin plugin;

    public PlayerInteractListener(ArterionPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent evt) {
        ArterionPlayer p = ArterionPlayerUtil.get(evt.getPlayer());

        if (p == null) {
            evt.setCancelled(true);
            return;
        }

        //Afk
        p.resetAfkTime();

        //World protection
        Material type = evt.getPlayer().getItemInHand() == null ? Material.AIR : evt.getPlayer().getItemInHand().getType();
        if (evt.getAction() == Action.RIGHT_CLICK_BLOCK && evt.getClickedBlock() != null && forbiddenItems.contains(type)) {
            Region region = ArterionChunkUtil.getNonNull(evt.getClickedBlock().getChunk()).getRegion();
            if (!region.canPlayerBuild(p) && !region.isModifyEntities()) {
                evt.setCancelled(true);
                p.sendTranslation("region.nobuild");
                return;
            }
        }
        if (evt.getAction() == Action.PHYSICAL && evt.getClickedBlock().getType() == Material.SOIL) {
            Region region = ArterionChunkUtil.getNonNull(evt.getClickedBlock().getChunk()).getRegion();
            if (!region.canPlayerBuild(p) || p.getBukkitPlayer().getGameMode() == GameMode.CREATIVE) {
                evt.setCancelled(true);
                return;
            }
        }
        if (evt.getAction() == Action.RIGHT_CLICK_BLOCK && (evt.getClickedBlock().getType() == Material.DIRT || evt.getClickedBlock().getType() == Material.GRASS || evt.getClickedBlock().getType() == Material.SOIL)) {
            if (evt.getPlayer().getItemInHand().getType() == Material.DIAMOND_HOE
                    || evt.getPlayer().getItemInHand().getType() == Material.IRON_HOE
                    || evt.getPlayer().getItemInHand().getType() == Material.GOLD_HOE
                    || evt.getPlayer().getItemInHand().getType() == Material.STONE_HOE
                    || evt.getPlayer().getItemInHand().getType() == Material.WOOD_HOE) {
                Region region = ArterionChunkUtil.getNonNull(evt.getClickedBlock().getChunk()).getRegion();
                if (!region.canPlayerBuild(p)) {
                    evt.setCancelled(true);
                    p.sendTranslation("region.nobuild");
                    return;
                }
            }
        }
        //Stop extinguish fire and remove painting
        if (evt.getAction() == Action.LEFT_CLICK_BLOCK) {
            Block b = evt.getPlayer().getTargetBlock((Set<Material>) null, 5);
            if (b != null) {
                Material type1 = b.getType();
                if (type1 == Material.FIRE || type1 == Material.PAINTING) {
                    Region region = ArterionChunkUtil.getNonNull(b.getChunk()).getRegion();
                    if (!region.canPlayerBuild(p)) {
                        evt.setCancelled(true);
                        p.sendTranslation("region.nobuild");
                        return;
                    }
                }
            }
        }

        //Endless cake
        if ((evt.getAction() == Action.RIGHT_CLICK_BLOCK || evt.getAction() == Action.LEFT_CLICK_BLOCK) && evt.getClickedBlock().getType() == Material.CAKE_BLOCK) {
            Region region = ArterionChunkUtil.getNonNull(evt.getClickedBlock().getChunk()).getRegion();
            if (region instanceof SpawnRegion || region instanceof BankRegion) {
                ArterionPlugin.getInstance().getSchedulers().getMainScheduler().executeInSpigotCircleLater(new InternalTask() {
                    @Override
                    public void run() {
                        evt.getClickedBlock().setType(Material.CAKE_BLOCK);
                    }
                }, 5l);
            }
        }

        //Banned items
        if (evt.getAction() == Action.RIGHT_CLICK_BLOCK || evt.getAction() == Action.RIGHT_CLICK_AIR) {
            if (BannedItems.isBanned(evt.getItem())) {
                evt.setCancelled(true);
                ArterionPlayerUtil.get(evt.getPlayer()).sendTranslation("nobanneditem");
                return;
            }
        }

        //Skills
        if (evt.getPlayer().getItemInHand() != null) {
            if (p.getSelectedClass() != ClassEnum.NONE && p.getSelectedClass() != null) {
                if (p.getSelectedClass().isWeaponAllowed(evt.getPlayer().getItemInHand().getType())) {
                    boolean left = evt.getAction() == Action.LEFT_CLICK_AIR || evt.getAction() == Action.LEFT_CLICK_BLOCK;
                    boolean right = evt.getAction() == Action.RIGHT_CLICK_AIR || evt.getAction() == Action.RIGHT_CLICK_BLOCK;
                    if ((p.getSelectedClass() == ClassEnum.FORESTRUNNER && left)
                            || (p.getSelectedClass() != ClassEnum.FORESTRUNNER && right)) {
                        SkillEnum binding = p.getRightClickSkill(p.getSelectedClass());
                        if (binding != null) {
                            p.getSkillSlots().castSkill(binding);
                            evt.setCancelled(true);
                        }
                    }
                }
            }
        }

        //Custom items
        if (evt.getPlayer().getItemInHand() != null && evt.getAction() != Action.PHYSICAL) {
            CustomItem item = CustomItemUtil.getCustomItem(evt.getPlayer().getItemInHand());
            if (!item.onInteract(ArterionPlayerUtil.get(evt.getPlayer()), evt.getClickedBlock(), evt.getBlockFace())) {
                evt.setCancelled(true);
                return;
            }
        }

        if (evt.getClickedBlock() != null) {
            BlockState bs = evt.getClickedBlock().getState();

            if (evt.getAction() == Action.RIGHT_CLICK_BLOCK) {
                //Custom blocks
                CustomBlock block = CustomBlockUtil.getCustomBlock(evt.getClickedBlock());
                if (!block.onInteract(ArterionPlayerUtil.get(evt.getPlayer()), evt.getAction())) {
                    evt.setCancelled(true);
                    return;
                }
            }
            if (evt.getClickedBlock().getType() == Material.ENDER_PORTAL_FRAME) {
                //Special blocks
                SpecialBlock block1 = plugin.getSpecialBlockManager().get(evt.getClickedBlock());
                if (block1 != null && !block1.onInteract(ArterionPlayerUtil.get(evt.getPlayer()), evt.getAction())) {
                    evt.setCancelled(true);
                    return;
                }
            }
            if (evt.getClickedBlock().getType() == Material.BED_BLOCK && evt.getAction() == Action.RIGHT_CLICK_BLOCK && evt.getClickedBlock().getWorld().equals(Bukkit.getWorlds().get(0))) {
                //Home bed
                evt.setCancelled(true);
                if (p.getHomeLocation() != null || p.getGuild() != null) {
                    p.sendTranslation("gui.home.alreadyhashome");
                } else {
                    p.getBukkitPlayer().setBedSpawnLocation(evt.getClickedBlock().getLocation().clone(), true);
                    p.getBukkitPlayer().saveData();
                    p.sendTranslation("gui.home.set");
                }
            }
            if (evt.getClickedBlock().getType() == Material.ENCHANTMENT_TABLE && evt.getAction() == Action.RIGHT_CLICK_BLOCK) {
                //Custom enchantment table
                evt.setCancelled(true);
                ArterionPlayer ep = ArterionPlayerUtil.get(evt.getPlayer());
                ep.openGui(new ItemGUI(ep, ep.getTranslation("gui.enchant.choose"), () -> {
                    ItemStack[] stacks = new ItemStack[9];
                    stacks[2] = NamedItemUtil.create(Material.IRON_SWORD, ep.getTranslation("gui.enchant.weapon"));
                    stacks[6] = NamedItemUtil.create(Material.IRON_HOE, ep.getTranslation("gui.enchant.tool"));
                    return stacks;
                }, (clickType, i) -> {
                    EnchantmentTableOpener opener = EnchantmentTableOpenerFactory.newOpener();
                    if (i == 2) {
                        opener.open(evt.getPlayer(), evt.getClickedBlock().getLocation(), true, true);
                    } else {
                        opener.open(evt.getPlayer(), evt.getClickedBlock().getLocation(), true, false);
                    }
                }));
                return;
            }
            if (evt.getClickedBlock().getType() == Material.ANVIL && evt.getAction() == Action.RIGHT_CLICK_BLOCK) {
                //Custom anvil
                evt.setCancelled(true);
                ArterionPlayer ep = ArterionPlayerUtil.get(evt.getPlayer());
                ep.openGui(new ItemGUI(ep, ep.getTranslation("gui.anvil.choose"), () -> {
                    ItemStack[] stacks = new ItemStack[9];
                    stacks[2] = NamedItemUtil.create(Material.IRON_SWORD, ep.getTranslation("gui.anvil.weapon"));
                    stacks[6] = NamedItemUtil.create(Material.IRON_HOE, ep.getTranslation("gui.anvil.tool"));
                    return stacks;
                }, (clickType, i) -> {
                    AnvilOpener opener = AnvilOpenerFactory.newOpener();
                    AtomicLong l = new AtomicLong(System.currentTimeMillis());
                    opener.open(evt.getPlayer(), evt.getClickedBlock().getLocation(), true, i == 2, cost -> {
                        if (l.get() + 50 < System.currentTimeMillis()) {
                            ep.sendTranslation("gui.anvil.cost", cost);
                            l.set(System.currentTimeMillis());
                        }
                    });
                }));
                return;
            }
            //Locks
            if (evt.getAction() == Action.RIGHT_CLICK_BLOCK && bs instanceof Chest && !evt.getPlayer().isSneaking()) {
                evt.setCancelled(true);
                Lock l = LockUtil.getLock((Chest) bs);
                if (l != null) {
                    if (!l.canAccess(p) && p.getBukkitPlayer().getGameMode() != GameMode.CREATIVE && p.getBukkitPlayer().getGameMode() != GameMode.SPECTATOR) {
                        l.sendDeny(p);
                        return;
                    }
                }
                evt.getPlayer().openInventory(((Chest) evt.getClickedBlock().getState()).getInventory());
            }
        }

        //Stats
        if (evt.getAction() == Action.LEFT_CLICK_AIR) {
            if (p.getSelectedClass() != null && p.getSelectedClass() != ClassEnum.NONE && p.getBukkitPlayer().getItemInHand() != null) {
                if (p.getSelectedClass().isWeaponAllowed(p.getBukkitPlayer().getItemInHand().getType()) && p.getBukkitPlayer().getItemInHand().getType() != Material.BOW) {
                    p.trackStatistic(StatType.CLICKS, p.getBukkitPlayer().getItemInHand().getType().getId(), v -> v + 1);
                }
            }
        }
    }
}
