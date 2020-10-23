package me.friwi.arterion.plugin.listener;

import me.friwi.arterion.plugin.ArterionPlugin;
import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.player.ArterionPlayerUtil;
import me.friwi.arterion.plugin.world.chunk.ArterionChunkUtil;
import me.friwi.arterion.plugin.world.region.Region;
import me.friwi.arterion.plugin.world.villager.CustomVillagerUtil;
import me.friwi.arterion.plugin.world.villager.VillagerType;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;

public class PlayerInteractEntityListener implements Listener {
    private ArterionPlugin plugin;

    public PlayerInteractEntityListener(ArterionPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent evt) {
        ArterionPlayer player = ArterionPlayerUtil.get(evt.getPlayer());

        //Region protection
        if (evt.getRightClicked() instanceof ArmorStand || evt.getRightClicked() instanceof ItemFrame) {
            Region region = ArterionChunkUtil.getNonNull(evt.getRightClicked().getLocation().getChunk()).getRegion();
            if (!region.canPlayerBuild(player) && !region.isModifyEntities()) {
                evt.setCancelled(true);
                player.sendTranslation("region.nobuild");
                return;
            }
        }

        //Block combat villager interacting
        if (plugin.getCombatLoggingHandler().isCombatLoggingVillager(evt.getRightClicked())) {
            evt.setCancelled(true);
            return;
        }

        //Villager stuff
        VillagerType villagerType = CustomVillagerUtil.getVillagerType(evt.getRightClicked());
        if (villagerType != null) {
            evt.setCancelled(true);
            villagerType.getHandler().handleInteract(ArterionPlayerUtil.get(evt.getPlayer()));
        } else if (evt.getRightClicked() instanceof Villager && evt.getRightClicked().isCustomNameVisible() && evt.getRightClicked().getCustomName().contains("[")) {
            evt.setCancelled(true);
            return;
        }
    }
}
