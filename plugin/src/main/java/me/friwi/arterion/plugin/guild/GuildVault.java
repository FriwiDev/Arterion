package me.friwi.arterion.plugin.guild;

import me.friwi.arterion.plugin.ArterionPlugin;
import me.friwi.arterion.plugin.player.ArterionPlayerUtil;
import me.friwi.arterion.plugin.util.language.api.LanguageAPI;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class GuildVault implements InventoryHolder {
    private Inventory inventory;
    private Guild guild;

    public GuildVault(Guild guild) throws IOException {
        this.guild = guild;
        this.inventory = Bukkit.createInventory(this, getCapacity(), getName());
        this.inventory.setContents(readInventory());
    }

    public String getName() {
        return LanguageAPI.translate("guild.vault.name");
    }

    public int getCapacity() {
        return guild.getVaultRows() * 9;
    }

    public void saveInventory(ItemStack[] stacks) throws IOException {
        File f = new File(ArterionPlugin.getInstance().getDataFolder().getAbsolutePath(), "guild_vaults" + File.separator + guild.getUuid().toString().replace("-", "") + "-vault.yml");
        if (!f.exists()) {
            f.getParentFile().mkdirs();
            f.createNewFile();
        }
        FileConfiguration c = YamlConfiguration.loadConfiguration(f);
        c.set("inventory.content", stacks);
        c.save(f);
    }

    public ItemStack[] readInventory() throws IOException {
        File f = new File(ArterionPlugin.getInstance().getDataFolder().getAbsolutePath(), "guild_vaults" + File.separator + guild.getUuid().toString().replace("-", "") + "-vault.yml");
        if (!f.exists()) return new ItemStack[getCapacity()];
        FileConfiguration c = YamlConfiguration.loadConfiguration(f);
        return ((List<ItemStack>) c.get("inventory.content")).toArray(new ItemStack[getCapacity()]);
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public void show(Player player) {
        ArterionPlayerUtil.get(player).closeGui();
        player.openInventory(getInventory());
    }

    public void hide(Player player) {
        if (player.getOpenInventory().equals(getInventory())) player.closeInventory();
    }

    public void hideAll() {
        for (HumanEntity ent : getInventory().getViewers()) ent.closeInventory();
    }

    public void save() {
        try {
            saveInventory(inventory.getContents());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void updateSize() {
        hideAll();
        ItemStack[] stacks = inventory.getContents();
        ItemStack[] newStacks = new ItemStack[getCapacity()];
        for (int i = 0; i < stacks.length && i < newStacks.length; i++) {
            newStacks[i] = stacks[i];
        }
        this.inventory = Bukkit.createInventory(this, getCapacity(), getName());
        this.inventory.setContents(newStacks);
        save();
    }

    public void drop(float part) {
        ItemStack[] stacks = inventory.getContents();
        for (int i = 0; i < stacks.length; i++) {
            if (stacks[i] != null) {
                int drop = (int) (stacks[i].getAmount() * part);
                ItemStack dropStack = null;
                if (stacks[i].getAmount() == 1) {
                    //Random if this whole stack drops
                    if ((1 - part) <= Math.random()) {
                        dropStack = stacks[i];
                        inventory.setItem(i, null);
                    }
                } else {
                    if (drop < 1) drop = 1;
                    dropStack = stacks[i].clone();
                    dropStack.setAmount(drop);
                    int remain = stacks[i].getAmount() - drop;
                    if (remain == 0) {
                        inventory.setItem(i, null);
                    } else {
                        stacks[i].setAmount(stacks[i].getAmount() - drop);
                        inventory.setItem(i, stacks[i]);
                    }
                }
                if (dropStack != null) {
                    guild.getHomeLocation().getWorld().dropItemNaturally(guild.getHomeLocation().add(0.5, 1, 0.5), dropStack);
                }
            }
        }
        save();
    }
}
