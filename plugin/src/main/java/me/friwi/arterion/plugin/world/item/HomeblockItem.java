package me.friwi.arterion.plugin.world.item;

import com.google.common.collect.ImmutableList;
import me.friwi.arterion.plugin.ArterionPlugin;
import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.util.language.api.LanguageAPI;
import me.friwi.arterion.plugin.world.block.nonbtblocks.HomeBlock;
import me.friwi.arterion.plugin.world.chunk.ArterionChunk;
import me.friwi.arterion.plugin.world.chunk.ArterionChunkUtil;
import me.friwi.arterion.plugin.world.region.PlayerClaimRegion;
import me.friwi.arterion.plugin.world.region.WildernessRegion;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Item;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class HomeblockItem extends CustomItem {
    public HomeblockItem(ItemStack stack) {
        super(CustomItemType.HOMEBLOCK, stack);
    }

    public HomeblockItem() {
        super(CustomItemType.HOMEBLOCK);
    }

    @Override
    protected void parseItem() {

    }

    @Override
    public ItemStack toItemStack() {
        ItemStack stack = new ItemStack(Material.ENDER_PORTAL_FRAME, 1);
        ItemMeta meta = stack.getItemMeta();
        meta.setDisplayName(LanguageAPI.getLanguage(LanguageAPI.DEFAULT_LANGUAGE).getTranslation("item.homeblock.name").translate().getMessage());
        meta.setLore(ImmutableList.copyOf(LanguageAPI.getLanguage(LanguageAPI.DEFAULT_LANGUAGE).getTranslation("item.homeblock.lore").translate().getMessage().split("\n")));
        stack.setItemMeta(meta);
        stack = NBTItemUtil.setShouldDrop(stack, false);
        return NBTItemUtil.setType(stack, this.getType());
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
        return true;
    }

    @Override
    public boolean onPlace(ArterionPlayer arterionPlayer, Block block) {
        System.out.println("On place 1");
        if (arterionPlayer.getGuild() != null) {
            arterionPlayer.sendTranslation("guild.alreadyinguild");
            return false;
        }
        //Do not allow multiple homeblocks
        ArterionChunk chunk = ArterionChunkUtil.getNonNull(block.getChunk());
        if (arterionPlayer.getHomeLocation() != null) {
            arterionPlayer.sendTranslation("homeblock.twice");
            return false;
        }
        System.out.println("On place 2");
        //Check own chunk
        boolean validRange = block.getWorld().equals(Bukkit.getWorlds().get(0))
                && (Math.abs(block.getX()) >= ArterionPlugin.getInstance().getFormulaManager().PLAYER_HOMEBLOCK_MIN.evaluateInt(arterionPlayer)
                || Math.abs(block.getZ()) >= ArterionPlugin.getInstance().getFormulaManager().PLAYER_HOMEBLOCK_MIN.evaluateInt(arterionPlayer))
                && Math.abs(block.getX()) <= ArterionPlugin.getInstance().getFormulaManager().PLAYER_HOMEBLOCK_MAX.evaluateInt(arterionPlayer)
                && Math.abs(block.getZ()) <= ArterionPlugin.getInstance().getFormulaManager().PLAYER_HOMEBLOCK_MAX.evaluateInt(arterionPlayer)
                && block.getY() >= ArterionPlugin.getInstance().getFormulaManager().PLAYER_HOMEBLOCK_MINY.evaluateInt(arterionPlayer)
                && block.getY() <= ArterionPlugin.getInstance().getFormulaManager().PLAYER_HOMEBLOCK_MAXY.evaluateInt(arterionPlayer);
        if (chunk.getRegion().equals(ArterionPlugin.getInstance().getRegionManager().getWilderness()) && validRange) {
            System.out.println("On place 3");
            //Check surrounding chunks
            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    ArterionChunk check = ArterionChunkUtil.getNonNull(block.getWorld().getChunkAt((block.getX() >> 4) + (x), (block.getZ() >> 4) + (z)));
                    if (!(check.getRegion() instanceof WildernessRegion)) {
                        arterionPlayer.sendTranslation("homeblock.otherregion");
                        return false;
                    }
                }
            }
            System.out.println("On place 4");
            //Perform home placing
            HomeBlock homeBlock = new HomeBlock(block.getLocation(), arterionPlayer.getBukkitPlayer().getUniqueId());
            homeBlock.applyToBlock(block);
            ArterionPlugin.getInstance().getSpecialBlockManager().add(homeBlock);
            ArterionPlugin.getInstance().getRegionManager().registerRegion(new PlayerClaimRegion(arterionPlayer.getName(), arterionPlayer.getBukkitPlayer().getUniqueId(), block.getWorld(), block.getChunk().getX(), block.getChunk().getZ()));
            //Perform database operations
            arterionPlayer.setHomeLocationAndRoommate(true, block.getLocation().clone(), null, success -> {
                arterionPlayer.sendTranslation(success ? "homeblock.success" : "homeblock.error");
            });
            return true;
        } else {
            if (block.getWorld().equals(Bukkit.getWorlds().get(0)) && validRange) {
                arterionPlayer.sendTranslation("homeblock.otherregion");
            } else {
                arterionPlayer.sendTranslation("homeblock.notinrange",
                        ArterionPlugin.getInstance().getFormulaManager().PLAYER_HOMEBLOCK_MIN.evaluateInt(arterionPlayer),
                        ArterionPlugin.getInstance().getFormulaManager().PLAYER_HOMEBLOCK_MAX.evaluateInt(arterionPlayer),
                        ArterionPlugin.getInstance().getFormulaManager().PLAYER_HOMEBLOCK_MINY.evaluateInt(arterionPlayer),
                        ArterionPlugin.getInstance().getFormulaManager().PLAYER_HOMEBLOCK_MAXY.evaluateInt(arterionPlayer));
            }
        }
        return false;
    }

    @Override
    public boolean onInteract(ArterionPlayer player, Block block, BlockFace blockFace) {
        return true;
    }

    @Override
    public boolean onSwitchInventory(ArterionPlayer arterionPlayer) {
        return true;
    }

    @Override
    public boolean onInventoryPickup(Inventory inventory) {
        return true;
    }
}
