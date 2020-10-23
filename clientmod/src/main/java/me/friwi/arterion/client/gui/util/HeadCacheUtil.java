package me.friwi.arterion.client.gui.util;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.commons.lang3.StringUtils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;

public class HeadCacheUtil {
	public static final UUID QUESTION_MARK = UUID.fromString("606e2ff0-ed77-4842-9d6c-e1d3321c7838");
	public static final UUID ARROW_LEFT = UUID.fromString("a68f0b64-8d14-4000-a95f-4b9ba14f8df9");
	public static final UUID ARROW_RIGHT = UUID.fromString("50c8510b-5ea0-4d60-be9a-7d542d6cd156");
	public static final UUID ARROW_UP = UUID.fromString("fef039ef-e6cd-4987-9c84-26a3e6134277");
	public static final UUID ARROW_DOWN = UUID.fromString("68f59b9b-5b0b-4b05-a9f2-e1d1405aa348");

	private static Map<UUID, CachedHead> headMap = new TreeMap<>();
	private static ConcurrentLinkedQueue<Runnable> work = new ConcurrentLinkedQueue();
	private static Object lock = new Object();

	public static ItemStack[] supplyItemStack(UUID... requestedHeads) {
		CachedHead[] cachedHeads = supplyCachedHeads(requestedHeads);
		ItemStack[] stacks = new ItemStack[requestedHeads.length];
		for (int i = 0; i < stacks.length; i++) {
			stacks[i] = cachedHeads[i]==null?new ItemStack(Items.skull, 1, 0):cachedHeads[i].getHead();
		}
		return stacks;
	}

	public static CachedHead[] supplyCachedHeads(UUID... requestedHeads) {
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
			return result;
		}
		work.add(()->{
				for (int i = 0; i < requestedHeads.length; i++) {
					if (result[i] == null) {
						synchronized (headMap) {
							result[i] = headMap.get(requestedHeads[i]);
						}
						if (result[i] == null) {
							result[i] = createCache(requestedHeads[i]);
							synchronized (headMap) {
								headMap.put(requestedHeads[i], result[i]);
							}
						}
					}
				}
		});
		synchronized(lock) {
			lock.notifyAll();
		}
		return result;
	}

	private static synchronized CachedHead createCache(UUID u) {
		ItemStack item = new ItemStack(Items.skull, 1, (short) 3);
		NBTTagCompound compound = item.getTagCompound();
		if(compound==null) {
			compound = new NBTTagCompound();
			item.setTagCompound(compound);
		}

		String texture;
		String signature;
		String name;
		String uuid;
		uuid = StringUtils.replace(u.toString(), "-", "");
		URL url = null;

		try {
			url = new URL("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid + "?unsigned=false");
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return null;
		}

		try {
			InputStreamReader reader = new InputStreamReader(url.openStream());
			JsonObject json = new JsonParser().parse(reader).getAsJsonObject();
			JsonObject properties = json.get("properties").getAsJsonArray().get(0).getAsJsonObject();
			texture = properties.get("value").getAsString();
			signature = properties.get("signature").getAsString();
			name = json.get("name").getAsString();
		} catch (IOException e) {
			return null;
		} catch (IllegalStateException e) {
			return null;
		}

		GameProfile profile = new GameProfile(u, name);
		 profile.getProperties().put("textures", new Property("textures", texture,
		 signature));
		
		 compound.setTag("SkullOwner", NBTUtil.writeGameProfile(new NBTTagCompound(), profile));
	
		return new CachedHead(u, name, texture, signature, item);
	}

	public static int countMissingHeads(UUID[] requiredHeads) {
		int c = 0;
		for (UUID u : requiredHeads) {
			if (!headMap.containsKey(u))
				c++;
		}
		return c;
	}
	
	public static void startWorkerThread() {
		new Thread() {
			public void run() {
				setName("Head-Retrieve-Thread");
				while(true) {
					if(work.isEmpty()) {
						synchronized(lock) {
							try {
								lock.wait();
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
					} else {
						Runnable run = work.poll();
						if(run!=null)run.run();
					}
				}
			}
		}.start();
	}
}
