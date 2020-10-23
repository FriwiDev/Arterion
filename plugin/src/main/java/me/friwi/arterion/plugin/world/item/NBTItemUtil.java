package me.friwi.arterion.plugin.world.item;

import de.tr7zw.changeme.nbtapi.NBTItem;
import de.tr7zw.changeme.nbtapi.NbtApiException;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.List;
import java.util.UUID;

public class NBTItemUtil {
    public static CustomItemType getType(ItemStack itemStack) {
        try {
            NBTItem nbti = new NBTItem(itemStack);
            if (nbti.hasKey("E_type")) {
                try {
                    return CustomItemType.values()[nbti.getShort("E_type")];
                } catch (ArrayIndexOutOfBoundsException e) {
                    return CustomItemType.NONE;
                }
            }
            return CustomItemType.NONE;
        } catch (NbtApiException e) {
            return CustomItemType.NONE;
        }
    }

    public static ItemStack setType(ItemStack itemStack, CustomItemType customItemType) {
        NBTItem nbti = new NBTItem(itemStack);
        nbti.setShort("E_type", (short) customItemType.ordinal());
        return nbti.getItem();
    }

    public static ItemStack setShouldDrop(ItemStack stack, boolean shouldDrop) {
        try {
            NBTItem nbti = new NBTItem(stack);
            nbti.setBoolean("art_drop", shouldDrop);
            stack = nbti.getItem();
        } catch (NbtApiException e) {

        }
        return stack;
    }

    public static ItemStack setShouldStack(ItemStack stack, boolean shouldStack) {
        try {
            NBTItem nbti = new NBTItem(stack);
            if (shouldStack) nbti.removeKey("art_stackable");
            else nbti.setLong("art_stackable", UUID.randomUUID().getLeastSignificantBits());
            stack = nbti.getItem();
        } catch (NbtApiException e) {

        }
        return stack;
    }

    public static boolean getShouldDropItem(ItemStack stack) {
        try {
            NBTItem item = new NBTItem(stack);
            if (item.hasKey("art_drop")) return item.getBoolean("art_drop");
            return true;
        } catch (NbtApiException e) {
            return true;
        }
    }

    public static ItemStack addGlowNameLore(ItemStack item, boolean enchanted, String name, List<String> lore) {
        try {
            Class<?> ItemStack = getCraftClass("ItemStack");
            Class<?> NBTTagCompound = getCraftClass("NBTTagCompound");
            Class<?> NBTTagList = getCraftClass("NBTTagList");
            Class<?> CraftItemStack = getBukkitClass("inventory.CraftItemStack");
            Class<?> NBTTagString = getCraftClass("NBTTagString");
            Class<?> NBTBase = getCraftClass("NBTBase");

            Method asNMSCopy = CraftItemStack.getDeclaredMethod("asNMSCopy", org.bukkit.inventory.ItemStack.class);
            Method asCraftMirror = CraftItemStack.getDeclaredMethod("asCraftMirror", ItemStack);
            Method hasTag = ItemStack.getDeclaredMethod("hasTag");
            Method setTag = ItemStack.getDeclaredMethod("setTag", NBTTagCompound);
            Method getTag = ItemStack.getDeclaredMethod("getTag");
            Method set = NBTTagCompound.getDeclaredMethod("set", String.class, NBTBase);
            Method add = NBTTagList.getDeclaredMethod("add", NBTBase);

            asNMSCopy.setAccessible(true);
            asCraftMirror.setAccessible(true);
            hasTag.setAccessible(true);
            setTag.setAccessible(true);
            getTag.setAccessible(true);
            set.setAccessible(true);

            Constructor<?> NBTTagCompoundConstructor = NBTTagCompound.getConstructor();
            Constructor<?> NBTTagListConstructor = NBTTagList.getConstructor();
            Constructor<?> NBTTagStringConstructor = NBTTagString.getConstructor(String.class);

            NBTTagCompoundConstructor.setAccessible(true);
            NBTTagListConstructor.setAccessible(true);

            Object nmsStack = asNMSCopy.invoke(null, item);
            Object tag = null;

            if ((Boolean) hasTag.invoke(nmsStack))
                tag = getTag.invoke(nmsStack);
            else
                tag = NBTTagCompoundConstructor.newInstance();

            if (enchanted) {
                Object ench = NBTTagListConstructor.newInstance();
                set.invoke(tag, "ench", ench);
            }

            Object display = NBTTagCompoundConstructor.newInstance();
            set.invoke(display, "Name", NBTTagStringConstructor.newInstance(name));

            Object loreObj = NBTTagListConstructor.newInstance();
            for (String str : lore) {
                add.invoke(loreObj, NBTTagStringConstructor.newInstance(str));
            }
            set.invoke(display, "Lore", loreObj);

            set.invoke(tag, "display", display);

            setTag.invoke(nmsStack, tag);

            return (org.bukkit.inventory.ItemStack) asCraftMirror.invoke(null, nmsStack);
        } catch (Exception e) {
            e.printStackTrace();
            return item;
        }
    }

    public static Class<?> getCraftClass(String ClassName) {
        String name = Bukkit.getServer().getClass().getPackage().getName();
        String version = name.substring(name.lastIndexOf('.') + 1) + ".";
        String className = "net.minecraft.server." + version + ClassName;
        Class<?> c = null;
        try {
            c = Class.forName(className);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return c;
    }

    public static Class<?> getBukkitClass(String ClassPackageName) {
        String name = Bukkit.getServer().getClass().getPackage().getName();
        String version = name.substring(name.lastIndexOf('.') + 1) + ".";
        String className = "org.bukkit.craftbukkit." + version + ClassPackageName;
        Class<?> c = null;
        try {
            c = Class.forName(className);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return c;
    }
}
