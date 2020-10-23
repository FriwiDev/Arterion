package me.friwi.arterion.plugin.listener;

import me.friwi.arterion.plugin.ArterionPlugin;
import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.player.ArterionPlayerUtil;
import me.friwi.arterion.plugin.world.chunk.ArterionChunk;
import me.friwi.arterion.plugin.world.chunk.ArterionChunkUtil;
import me.friwi.arterion.plugin.world.item.CustomItem;
import me.friwi.arterion.plugin.world.item.CustomItemUtil;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBucketFillEvent;

public class PlayerBucketFillListener implements Listener {
    private ArterionPlugin plugin;

    public PlayerBucketFillListener(ArterionPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerBucketFill(PlayerBucketFillEvent evt) {
        Block b = evt.getBlockClicked().getRelative(evt.getBlockFace());
        ArterionChunk ec = ArterionChunkUtil.getNonNull(b.getChunk());
        ArterionPlayer ep = ArterionPlayerUtil.get(evt.getPlayer());

        if (ec.getRegion() != null && ep != null) {
            if (ec.getRegion().canPlayerBuild(ep)) {
                //Custom item usage
                CustomItem item = CustomItemUtil.getCustomItem(evt.getItemStack());
                if (!item.onPlace(ArterionPlayerUtil.get(evt.getPlayer()), b)) {
                    evt.setCancelled(true);
                    return;
                }
                return;
            }
        }
        evt.setCancelled(true);
        ep.sendTranslation("region.nobuild");
    }
}
