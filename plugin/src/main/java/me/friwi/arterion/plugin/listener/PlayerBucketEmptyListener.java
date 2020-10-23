package me.friwi.arterion.plugin.listener;

import me.friwi.arterion.plugin.ArterionPlugin;
import me.friwi.arterion.plugin.combat.Combat;
import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.player.ArterionPlayerUtil;
import me.friwi.arterion.plugin.world.block.nonbtblocks.GuildBlockUtil;
import me.friwi.arterion.plugin.world.chunk.ArterionChunk;
import me.friwi.arterion.plugin.world.chunk.ArterionChunkUtil;
import me.friwi.arterion.plugin.world.item.CustomItem;
import me.friwi.arterion.plugin.world.item.CustomItemUtil;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBucketEmptyEvent;

public class PlayerBucketEmptyListener implements Listener {
    private ArterionPlugin plugin;

    public PlayerBucketEmptyListener(ArterionPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerBucketEmpty(PlayerBucketEmptyEvent evt) {
        Block b = evt.getBlockClicked().getRelative(evt.getBlockFace());
        ArterionChunk ec = ArterionChunkUtil.getNonNull(b.getChunk());
        ArterionPlayer ep = ArterionPlayerUtil.get(evt.getPlayer());

        //Block nether ceiling exploit
        if (b.getWorld().getEnvironment() == World.Environment.NETHER && b.getY() >= 128) {
            evt.setCancelled(true);
            ep.sendTranslation("region.nobuild");
            return;
        }

        //Check for combat
        String combat = Combat.isPlayerInCombat(ep, true, false, false);
        if (combat != null) {
            evt.setCancelled(true);
            Combat.sendInCombatMessage(ep, combat);
            return;
        }

        if (ec.getRegion() != null && ep != null) {
            if (ec.getRegion().canPlayerBuild(ep)) {
                //Check further permissions
                if (!GuildBlockUtil.canBuildHere(ep, b)) {
                    evt.setCancelled(true);
                    return;
                }
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
