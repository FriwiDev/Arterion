package me.friwi.arterion.plugin.jobs;

import me.friwi.arterion.plugin.ArterionPlugin;
import me.friwi.arterion.plugin.formula.ArterionFormula;
import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.util.language.api.Language;
import org.bukkit.Material;

public enum JobEnum {
    WOODWORKER(Material.IRON_AXE, Material.LOG, Material.LOG_2),
    FISHER(Material.FISHING_ROD),
    FARMER(Material.IRON_HOE, Material.POTATO, Material.CROPS, Material.CARROT, Material.NETHER_WARTS, Material.PUMPKIN, Material.MELON_BLOCK, Material.SUGAR_CANE_BLOCK),
    MINER(Material.IRON_PICKAXE, Material.STONE, Material.COAL_ORE, Material.GLOWING_REDSTONE_ORE, Material.LAPIS_ORE, Material.EMERALD_ORE, Material.IRON_ORE, Material.GOLD_ORE, Material.DIAMOND_ORE);


    private Material[] xpMaterials;
    private Material jobMaterial;

    private JobEnum(Material jobMaterial, Material... xpMaterials) {
        this.jobMaterial = jobMaterial;
        this.xpMaterials = xpMaterials;
    }

    public boolean isXpMaterial(Material material) {
        for (Material m : xpMaterials) if (m == material) return true;
        return false;
    }

    public String getName(Language lang) {
        return lang.getTranslation("job." + name().toLowerCase()).translate().getMessage();
    }

    public Material[] getXpMaterials() {
        return xpMaterials;
    }

    public Material getJobMaterial() {
        return jobMaterial;
    }

    public String getDescription(ArterionPlayer player) {
        double chance = 0;
        ArterionFormula f = ArterionPlugin.getInstance().getFormulaManager().JOB_DROP_CHANCE.get(this.name());
        if (f.isDeclared()) {
            chance = f.evaluateDouble(player.getJobLevel(this));
        }
        return player.getTranslation("job." + name().toLowerCase() + ".desc", chance);
    }
}
