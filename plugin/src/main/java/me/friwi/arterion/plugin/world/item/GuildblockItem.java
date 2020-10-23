package me.friwi.arterion.plugin.world.item;

import com.google.common.collect.ImmutableList;
import de.tr7zw.changeme.nbtapi.NBTItem;
import me.friwi.arterion.plugin.ArterionPlugin;
import me.friwi.arterion.plugin.guild.Guild;
import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.util.database.entity.DatabaseGuild;
import me.friwi.arterion.plugin.util.language.api.LanguageAPI;
import me.friwi.arterion.plugin.util.scheduler.InternalTask;
import me.friwi.arterion.plugin.world.block.nonbtblocks.GuildBlock;
import me.friwi.arterion.plugin.world.chunk.ArterionChunk;
import me.friwi.arterion.plugin.world.chunk.ArterionChunkUtil;
import me.friwi.arterion.plugin.world.region.GuildRegion;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Item;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.UUID;

public class GuildblockItem extends CustomItem {
    private Guild guild;

    public GuildblockItem(ItemStack stack) {
        super(CustomItemType.GUILDBLOCK, stack);
    }

    public GuildblockItem(Guild guild) {
        super(CustomItemType.GUILDBLOCK);
        this.guild = guild;
    }

    @Override
    protected void parseItem() {
        NBTItem item = new NBTItem(this.stack);
        guild = ArterionPlugin.getInstance().getGuildManager().getGuildByUUID(UUID.fromString(item.getString("E_uuid")));
    }

    @Override
    public ItemStack toItemStack() {
        ItemStack stack = new ItemStack(Material.ENDER_PORTAL_FRAME, 1);
        ItemMeta meta = stack.getItemMeta();
        meta.setDisplayName(LanguageAPI.getLanguage(LanguageAPI.DEFAULT_LANGUAGE).getTranslation("item.guildblock.name").translate().getMessage());
        meta.setLore(ImmutableList.copyOf(LanguageAPI.getLanguage(LanguageAPI.DEFAULT_LANGUAGE).getTranslation("item.guildblock.lore").translate(guild.getName()).getMessage().split("\n")));
        stack.setItemMeta(meta);
        NBTItem item = new NBTItem(stack);
        item.setString("E_uuid", guild.getUuid().toString());
        stack = NBTItemUtil.setType(item.getItem(), this.getType());
        stack = NBTItemUtil.setShouldDrop(stack, false);
        return stack;
    }

    @Override
    public boolean onPickup(ArterionPlayer player, Item item) {
        return true;
    }

    @Override
    public boolean onDrop(ArterionPlayer player, Item item) {
        if (this.guild == null || this.guild.getDeleted() != DatabaseGuild.NOT_DELETED)
            return true; //Guild got disbanded while holding the block
        return false;
    }

    @Override
    public boolean onPlaceWithoutChecks(ArterionPlayer arterionPlayer, Block block) {
        return true;
    }

    @Override
    public boolean onPlace(ArterionPlayer arterionPlayer, Block block) {
        //Do not allow multiple guildblocks
        ArterionChunk chunk = ArterionChunkUtil.getNonNull(block.getChunk());
        if (arterionPlayer.getHomeLocation() != null) {
            arterionPlayer.sendTranslation("guildblock.youstillhavehome");
            return false;
        }
        //Check own chunk
        boolean validRange = block.getWorld().equals(Bukkit.getWorlds().get(0))
                && (Math.abs(block.getX()) >= ArterionPlugin.getInstance().getFormulaManager().GUILD_GUILDBLOCK_MIN.evaluateInt(guild)
                || Math.abs(block.getZ()) >= ArterionPlugin.getInstance().getFormulaManager().GUILD_GUILDBLOCK_MIN.evaluateInt(guild))
                && Math.abs(block.getX()) <= ArterionPlugin.getInstance().getFormulaManager().GUILD_GUILDBLOCK_MAX.evaluateInt(guild)
                && Math.abs(block.getZ()) <= ArterionPlugin.getInstance().getFormulaManager().GUILD_GUILDBLOCK_MAX.evaluateInt(guild)
                && block.getY() >= ArterionPlugin.getInstance().getFormulaManager().GUILD_GUILDBLOCK_MINY.evaluateInt(guild)
                && block.getY() <= ArterionPlugin.getInstance().getFormulaManager().GUILD_GUILDBLOCK_MAXY.evaluateInt(guild);
        if (chunk.getRegion().equals(ArterionPlugin.getInstance().getRegionManager().getWilderness()) && validRange) {
            //Check for other guilds nearby
            int distance = ArterionPlugin.getInstance().getFormulaManager().GUILD_GUILDBLOCK_DISTANCE.evaluateInt(guild) * 16;
            for (Guild g : ArterionPlugin.getInstance().getGuildManager().getGuilds()) {
                if (g.getHomeLocation() == null || g.equals(guild)) continue;
                if (Math.abs(g.getHomeLocation().getX() - block.getX()) < distance && Math.abs(g.getHomeLocation().getZ() - block.getZ()) < distance) {
                    arterionPlayer.sendTranslation("guildblock.otherguild");
                    return false;
                }
            }
            //Check for block space
            int x_z = ArterionPlugin.getInstance().getFormulaManager().GUILD_GUILDBLOCK_FREE_SPACE_X_Z.evaluateInt(guild);
            int y = ArterionPlugin.getInstance().getFormulaManager().GUILD_GUILDBLOCK_FREE_SPACE_Y.evaluateInt(guild);
            for (int i = -x_z; i <= x_z; i++) {
                for (int j = -x_z; j <= x_z; j++) {
                    for (int k = 0; k <= y; k++) {
                        if (i == 0 && j == 0 && k == 0) continue;
                        if (block.getRelative(i, k, j).getType() != Material.AIR) {
                            arterionPlayer.sendTranslation("guildblock.nospace", x_z, y);
                            return false;
                        }
                    }
                }
            }
            //Perform home placing
            Guild g = arterionPlayer.getGuild();
            if (g == null) return true; //Player is glitching
            GuildBlock guildblock = new GuildBlock(block.getLocation(), g);
            guildblock.setNoInteract(true);
            ArterionPlugin.getInstance().getSpecialBlockManager().add(guildblock);
            guildblock.applyToBlock(block);
            g.setHomeLocation(block.getLocation(), success -> {
                ArterionPlugin.getInstance().getSchedulers().getMainScheduler().executeInSpigotCircle(new InternalTask() {
                    @Override
                    public void run() {
                        int dist = g.getRegionDistance();
                        GuildRegion region = new GuildRegion(g, g.getHomeLocation().getWorld(), (g.getHomeLocation().getBlockX() >> 4) - dist, (g.getHomeLocation().getBlockX() >> 4) + dist, (g.getHomeLocation().getBlockZ() >> 4) - dist, (g.getHomeLocation().getBlockZ() >> 4) + dist);
                        arterionPlayer.sendTranslation("guildblock.inprogress");
                        g.setRegion(region);
                        ArterionPlugin.getInstance().getRegionManager().registerRegionParallel(region, GuildRegion.REGISTER_BATCH_SIZE, () -> {
                            guildblock.setNoInteract(false);
                            arterionPlayer.sendTranslation(success ? "guildblock.success" : "guildblock.error");
                        });
                        guild.setLastReclaim(System.currentTimeMillis(), succ -> {
                        });
                    }
                });
            });
            return true;
        } else {
            if (block.getWorld().equals(Bukkit.getWorlds().get(0)) && validRange) {
                arterionPlayer.sendTranslation("guildblock.otherregion");
            } else {
                arterionPlayer.sendTranslation("guildblock.notinrange",
                        ArterionPlugin.getInstance().getFormulaManager().GUILD_GUILDBLOCK_MIN.evaluateInt(guild),
                        ArterionPlugin.getInstance().getFormulaManager().GUILD_GUILDBLOCK_MAX.evaluateInt(guild),
                        ArterionPlugin.getInstance().getFormulaManager().GUILD_GUILDBLOCK_MINY.evaluateInt(guild),
                        ArterionPlugin.getInstance().getFormulaManager().GUILD_GUILDBLOCK_MAXY.evaluateInt(guild));
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
        return false;
    }

    @Override
    public boolean onInventoryPickup(Inventory inventory) {
        return false;
    }
}
