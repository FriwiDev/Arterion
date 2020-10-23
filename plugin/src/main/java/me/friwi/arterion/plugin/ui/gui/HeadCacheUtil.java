package me.friwi.arterion.plugin.ui.gui;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.friwi.arterion.plugin.ArterionPlugin;
import me.friwi.arterion.plugin.util.scheduler.InternalTask;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.function.Consumer;

public class HeadCacheUtil {
    public static final UUID QUESTION_MARK = UUID.fromString("606e2ff0-ed77-4842-9d6c-e1d3321c7838");
    public static final UUID ARROW_LEFT = UUID.fromString("a68f0b64-8d14-4000-a95f-4b9ba14f8df9");
    public static final UUID ARROW_RIGHT = UUID.fromString("50c8510b-5ea0-4d60-be9a-7d542d6cd156");
    public static final UUID ARROW_UP = UUID.fromString("fef039ef-e6cd-4987-9c84-26a3e6134277");
    public static final UUID ARROW_DOWN = UUID.fromString("68f59b9b-5b0b-4b05-a9f2-e1d1405aa348");

    private static Map<UUID, CachedHead> headMap = new TreeMap<>();

    private static Class<?> gameProfileClass, propertyClass;
    private static Constructor<?> gameProfileConstructor, propertyConstructor;
    private static Field profileField;
    private static Method getPropertiesMethod;
    private static Method getPropertiesPutMethod;

    static {
        try {
            gameProfileClass = Class.forName("com.mojang.authlib.GameProfile");
            propertyClass = Class.forName("com.mojang.authlib.properties.Property");
            gameProfileConstructor = gameProfileClass.getDeclaredConstructor(UUID.class, String.class);
            propertyConstructor = propertyClass.getDeclaredConstructor(String.class, String.class, String.class);
            getPropertiesMethod = gameProfileClass.getDeclaredMethod("getProperties");
            getPropertiesPutMethod = getPropertiesMethod.getReturnType().getSuperclass().getDeclaredMethod("put", Object.class, Object.class);
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    public static void supplyItemStack(Consumer<ItemStack[]> consumer, UUID... requestedHeads) {
        supplyCachedHead(cachedHeads -> {
            ItemStack[] stacks = new ItemStack[cachedHeads.length];
            for (int i = 0; i < stacks.length; i++) {
                stacks[i] = cachedHeads[i].getHead();
            }
            consumer.accept(stacks);
        }, requestedHeads);
    }

    public static void supplyCachedHeadSync(Consumer<CachedHead[]> consumer, UUID... requestedHeads) {
        supplyCachedHead(heads -> {
            ArterionPlugin.getInstance().getSchedulers().getMainScheduler().executeInSpigotCircle(new InternalTask() {
                @Override
                public void run() {
                    consumer.accept(heads);
                }
            });
        }, requestedHeads);
    }

    public static void supplyCachedHead(Consumer<CachedHead[]> consumer, UUID... requestedHeads) {
        CachedHead[] result = new CachedHead[requestedHeads.length];
        boolean missing = false;
        synchronized (headMap) {
            for (int i = 0; i < requestedHeads.length; i++) {
                result[i] = headMap.get(requestedHeads[i]);
                if (result[i] == null) {
                    missing = true;
                }
            }
        }
        if (!missing) {
            consumer.accept(result);
            return;
        }
        ArterionPlugin.getInstance().getSchedulers().getNetworkScheduler().executeInMyCircle(new InternalTask() {
            @Override
            public void run() {
                for (int i = 0; i < requestedHeads.length; i++) {
                    if (result[i] == null) {
                        synchronized (headMap) {
                            result[i] = headMap.get(requestedHeads[i]);
                        }
                        if (result[i] == null) {
                            result[i] = createCache(requestedHeads[i]);
                            if (result[i] == null)
                                result[i] = new CachedHead(requestedHeads[i], "ERR", null, null, null, null);
                            synchronized (headMap) {
                                headMap.put(requestedHeads[i], result[i]);
                            }
                        }
                    }
                }
                consumer.accept(result);
            }
        });
    }

    private static synchronized CachedHead createCache(UUID u) {
        ItemStack item = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
        SkullMeta meta = (SkullMeta) item.getItemMeta();

        String texture;
        String signature;
        String name;
        String uuid;
        uuid = StringUtils.replace(u.toString(), "-", "");
        URL url = null;

        try {
            url = new URL("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid +
                    "?unsigned=false");
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }

        try {
            InputStreamReader reader = new InputStreamReader(url.openStream());
            JsonObject json = new JsonParser().parse(reader).getAsJsonObject();
            JsonObject properties = json.get("properties")
                    .getAsJsonArray().get(0).getAsJsonObject();
            texture = properties.get("value").getAsString();
            signature = properties.get("signature").getAsString();
            name = json.get("name").getAsString();
        } catch (IOException e) {
            return null;
        } catch (IllegalStateException e) {
            return null;
        }

        try {
            Object profile = gameProfileConstructor.newInstance(UUID.randomUUID(), name);
            //profile.getProperties().put("textures", new Property("textures", texture, signature));
            getPropertiesPutMethod.invoke(getPropertiesMethod.invoke(profile), "textures", propertyConstructor.newInstance("textures", texture, signature));
            if (profileField == null) {
                profileField = meta.getClass().getDeclaredField("profile");
                profileField.setAccessible(true);
            }
            profileField.set(meta, profile);
            item.setItemMeta(meta);
            return new CachedHead(u, name, texture, signature, item, profile);
        } catch (NoSuchFieldException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void updatePlayerName(UUID uniqueId, String name) {
        CachedHead head = headMap.get(uniqueId);
        if (head != null) head.setName(name);
    }

    public static int countMissingHeads(UUID[] requiredHeads) {
        int c = 0;
        for (UUID u : requiredHeads) {
            if (!headMap.containsKey(u)) c++;
        }
        return c;
    }
}
