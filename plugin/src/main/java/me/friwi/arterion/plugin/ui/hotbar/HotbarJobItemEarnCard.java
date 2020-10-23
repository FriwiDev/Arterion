package me.friwi.arterion.plugin.ui.hotbar;

import me.friwi.arterion.plugin.jobs.JobEnum;
import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.util.language.translateables.NumberTranslateable;
import me.friwi.arterion.plugin.world.item.CustomItem;
import me.friwi.arterion.plugin.world.item.CustomItemUtil;
import me.friwi.arterion.plugin.world.item.GoldItem;
import org.bukkit.inventory.ItemStack;

public class HotbarJobItemEarnCard extends HotbarCard {
    private ArterionPlayer player;
    private String itemName;
    private String itemAmount;
    private JobEnum job;

    public HotbarJobItemEarnCard(ArterionPlayer player, ItemStack stack, JobEnum job) {
        super(1500);
        this.player = player;
        this.job = job;
        CustomItem item = CustomItemUtil.getCustomItem(stack);
        if (item instanceof GoldItem) {
            this.itemName = player.getTranslation("money");
            this.itemAmount = NumberTranslateable.formatNumber(((GoldItem) item).getAmount() / 100d);
        } else {
            this.itemName = player.getLanguage().translateObject(stack);
            this.itemAmount = stack.getAmount() + "x";
        }
    }

    @Override
    public String getMessage() {
        return player.getTranslation("hotbar.jobitemgain", job.getName(player.getLanguage()), itemName, itemAmount);
    }
}
