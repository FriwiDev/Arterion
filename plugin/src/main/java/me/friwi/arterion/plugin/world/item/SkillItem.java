package me.friwi.arterion.plugin.world.item;

import de.tr7zw.changeme.nbtapi.NBTItem;
import me.friwi.arterion.plugin.combat.Combat;
import me.friwi.arterion.plugin.combat.skill.SkillSlotEnum;
import me.friwi.arterion.plugin.player.ArterionPlayer;
import me.friwi.arterion.plugin.ui.gui.NamedItemUtil;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Item;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class SkillItem extends CustomItem {
    private String skillName;
    private String description[];
    private SkillSlotEnum skillSlot;
    private Material diskMat;

    public SkillItem(ItemStack stack) {
        super(CustomItemType.SKILL, stack);
    }

    public SkillItem(String skillName, String mana, String cooldown, String[] description, SkillSlotEnum skillSlot, Material diskMat) {
        super(CustomItemType.SKILL);
        this.skillName = skillName;
        if (mana.length() == 0 || cooldown.length() == 0) {
            this.description = description;
        } else {
            this.description = new String[description.length + 2];
            this.description[0] = mana;
            this.description[1] = cooldown;
            for (int i = 0; i < description.length; i++) {
                this.description[i + 2] = description[i];
            }
        }
        this.skillSlot = skillSlot;
        this.diskMat = diskMat;
    }

    public String getSkillName() {
        return skillName;
    }

    public void setSkillName(String skillName) {
        this.skillName = skillName;
    }

    public SkillSlotEnum getSkillSlot() {
        return skillSlot;
    }

    public void setSkillSlot(SkillSlotEnum skillSlot) {
        this.skillSlot = skillSlot;
    }

    @Override
    protected void parseItem() {
        NBTItem nbti = new NBTItem(stack);
        if (nbti.hasKey("E_slot")) {
            this.skillSlot = SkillSlotEnum.values()[nbti.getInteger("E_slot")];
        }
        this.skillName = stack.getItemMeta().getDisplayName();
        List<String> l = stack.getItemMeta().getLore();
        this.description = l == null ? new String[0] : l.toArray(new String[l.size()]);
        this.diskMat = stack.getType();
    }

    @Override
    public ItemStack toItemStack() {
        ItemStack stack = new ItemStack(diskMat, 1);
        stack = NamedItemUtil.modify(stack, skillName, description);
        ItemMeta meta = stack.getItemMeta();
        meta.addItemFlags(ItemFlag.values());
        stack.setItemMeta(meta);
        stack = NBTItemUtil.setType(stack, this.getType());
        NBTItem nbti = new NBTItem(stack);
        nbti.setInteger("E_slot", this.skillSlot.ordinal());
        stack = nbti.getItem();
        stack = NBTItemUtil.setShouldDrop(stack, false);
        return stack;
    }

    @Override
    public boolean onPickup(ArterionPlayer player, Item item) {
        item.remove();
        return false;
    }

    @Override
    public boolean onDrop(ArterionPlayer player, Item item) {
        String combat = Combat.isPlayerInCombat(player);
        if (combat != null) {
            Combat.sendInCombatMessage(player, combat);
            item.remove();
            return false;
        }
        player.sendTranslation("skills.skilldisc.dropped");
        item.remove();
        return true;
    }

    @Override
    public boolean onPlaceWithoutChecks(ArterionPlayer arterionPlayer, Block block) {
        return false;
    }

    @Override
    public boolean onPlace(ArterionPlayer arterionPlayer, Block block) {
        return false;
    }

    @Override
    public boolean onInteract(ArterionPlayer player, Block block, BlockFace blockFace) {
        player.getSkillSlots().castSkill(this.skillSlot);
        return false;
    }

    @Override
    public boolean onSwitchInventory(ArterionPlayer arterionPlayer) {
        return false;
    }

    @Override
    public boolean onInventoryPickup(Inventory inventory) {
        return true;
    }
}
