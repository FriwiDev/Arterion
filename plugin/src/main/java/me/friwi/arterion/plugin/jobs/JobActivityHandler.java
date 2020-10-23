package me.friwi.arterion.plugin.jobs;

import me.friwi.arterion.plugin.ArterionPlugin;
import me.friwi.arterion.plugin.formula.ArterionFormula;
import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.ui.hotbar.HotbarJobItemEarnCard;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.inventory.ItemStack;

import java.security.SecureRandom;
import java.util.Random;

public class JobActivityHandler {
    private static final Random RANDOM = new SecureRandom();

    public static void onBreakBlock(Block block, ArterionPlayer player) {
        BlockState state = block.getState();
        /*if (state instanceof Crops) {
            if (!((Crops) state).getState().equals(CropState.RIPE)) return;
        } else*/
        if (block.getType() == Material.WHEAT || block.getType() == Material.POTATO || block.getType() == Material.CARROT) {
            if (block.getData() < 5) {
                return;
            }
        } else if (block.getType() == Material.NETHER_WARTS) {
            if (block.getData() < 2) {
                return;
            }
        } else {
            if (ArterionPlugin.getInstance().getUserPlacedBlockManager().isPlacedByUser(block)) return;
        }
        Material mat = block.getType();
        for (JobEnum job : JobEnum.values()) {
            if (job.isXpMaterial(mat)) {
                ArterionFormula formula = null;
                switch (job) {
                    case WOODWORKER:
                        formula = ArterionPlugin.getInstance().getFormulaManager().JOB_WOODWORKER_XP.get(mat.toString());
                        break;
                    case MINER:
                        formula = ArterionPlugin.getInstance().getFormulaManager().JOB_MINER_XP.get(mat.toString());
                        break;
                    case FARMER:
                        formula = ArterionPlugin.getInstance().getFormulaManager().JOB_FARMER_XP.get(mat.toString());
                        break;
                }
                if (formula != null && formula.isDeclared()) {
                    player.addJobXp(job, formula.evaluateInt(RANDOM.nextDouble()));
                }
                formula = ArterionPlugin.getInstance().getFormulaManager().JOB_DROP_CHANCE.get(job.name());
                if (formula.isDeclared()) {
                    if (RANDOM.nextDouble() <= formula.evaluateDouble(player.getJobLevel(job)) / 100d) {
                        ItemStack stack = null;
                        switch (job) {
                            case WOODWORKER:
                                stack = WoodworkerDrops.INSTANCE.getRandomDrop();
                                break;
                            case FARMER:
                                stack = FarmerDrops.INSTANCE.getRandomDrop();
                                break;
                            case MINER:
                                for (ItemStack s : block.getDrops(player.getBukkitPlayer().getItemInHand())) {
                                    stack = s;
                                    break;
                                }
                                stack.setAmount(1);
                                break;
                        }
                        block.getWorld().dropItem(block.getLocation().add(0.5, 0.2, 0.5), stack);
                        player.scheduleHotbarCard(new HotbarJobItemEarnCard(player, stack, job));
                    }
                }
            }
        }
    }

    public static void onCatchFish(ArterionPlayer player) {
        ArterionFormula formula = ArterionPlugin.getInstance().getFormulaManager().JOB_FISHER_XP;
        if (formula.isDeclared()) {
            player.addJobXp(JobEnum.FISHER, formula.evaluateInt(RANDOM.nextDouble()));
        }
        formula = ArterionPlugin.getInstance().getFormulaManager().JOB_DROP_CHANCE.get(JobEnum.FISHER.name());
        if (formula.isDeclared()) {
            if (RANDOM.nextDouble() <= formula.evaluateDouble(player.getJobLevel(JobEnum.FISHER)) / 100d) {
                ItemStack stack = FisherDrops.INSTANCE.getRandomDrop();
                player.getBukkitPlayer().getWorld().dropItem(player.getBukkitPlayer().getLocation(), stack);
                player.scheduleHotbarCard(new HotbarJobItemEarnCard(player, stack, JobEnum.FISHER));
            }
        }
    }
}
