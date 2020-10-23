package me.friwi.arterion.plugin.listener;

import me.friwi.arterion.plugin.ArterionPlugin;
import me.friwi.arterion.plugin.combat.Combat;
import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.player.ArterionPlayerUtil;
import me.friwi.arterion.plugin.stats.StatType;
import me.friwi.arterion.plugin.world.block.nonbtblocks.GuildBlockUtil;
import me.friwi.arterion.plugin.world.chunk.ArterionChunk;
import me.friwi.arterion.plugin.world.chunk.ArterionChunkUtil;
import me.friwi.arterion.plugin.world.item.CustomItem;
import me.friwi.arterion.plugin.world.item.CustomItemUtil;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

public class BlockPlaceListener implements Listener {
    private ArterionPlugin plugin;

    public BlockPlaceListener(ArterionPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent evt) {
        Block b = evt.getBlock();
        ArterionPlayer ep = ArterionPlayerUtil.get(evt.getPlayer());

        //Block nether ceiling exploit
        if (b.getWorld().getEnvironment() == World.Environment.NETHER && b.getY() >= 128) {
            evt.setCancelled(true);
            ep.sendTranslation("region.nobuild");
            return;
        }

        //Block slime blocks
        if (b.getType() == Material.SLIME_BLOCK) {
            evt.setCancelled(true);
            ep.sendTranslation("region.noslime");
            return;
        }

        //Custom item usage
        CustomItem item = CustomItemUtil.getCustomItem(evt.getItemInHand());
        if (!item.onPlaceWithoutChecks(ep, evt.getBlock())) {
            evt.setCancelled(true);
            return;
        }

        //Check for combat
        String combat = Combat.isPlayerInCombat(ep, true, false, false);
        if (combat != null) {
            evt.setCancelled(true);
            Combat.sendInCombatMessage(ep, combat);
            return;
        }

        ArterionChunk ec = ArterionChunkUtil.getNonNull(evt.getBlock().getChunk());

        if (ec.isTemporaryBlock(evt.getBlock())) {
            evt.setCancelled(true);
            ep.sendTranslation("region.nobuild");
            return;
        }

        if (ec.getRegion() != null && ep != null) {
            if (ec.getRegion().canPlayerBuild(ep)) {
                //Check further permissions
                if (!GuildBlockUtil.canBuildHere(ep, b)) {
                    evt.setCancelled(true);
                    ep.sendTranslation("region.nobuild");
                    return;
                }
                //Custom item usage
                if (!item.onPlace(ArterionPlayerUtil.get(evt.getPlayer()), evt.getBlock())) {
                    evt.setCancelled(true);
                    return;
                }

                //Stats
                b = evt.getBlockPlaced();
                ep.trackStatistic(StatType.PLACED_BLOCKS, BlockBreakListener.blockToStatData(b), v -> v + 1);
                return;
            }
        }
        evt.setCancelled(true);
        ep.sendTranslation("region.nobuild");
    }
}
