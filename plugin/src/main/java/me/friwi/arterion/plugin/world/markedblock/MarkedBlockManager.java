package me.friwi.arterion.plugin.world.markedblock;

import org.bukkit.block.Block;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public class MarkedBlockManager {
    private Map<UUID, Map<Integer, Map<Integer, MarkedBlockSection>>> sections = new HashMap<>();
    private String folderName;

    public MarkedBlockManager(String folderName) {
        this.folderName = folderName;
    }

    public boolean isBlockMarked(Block block) {
        return getMarkedBlockSection(block).getBlock(block);
    }

    public void setBlock(Block block, boolean marked) {
        getMarkedBlockSection(block).setBlock(block, marked);
    }

    private MarkedBlockSection getMarkedBlockSection(Block block) {
        Map<Integer, Map<Integer, MarkedBlockSection>> xzMap = sections.get(block.getWorld().getUID());
        if (xzMap == null) {
            xzMap = new HashMap<>();
            sections.put(block.getWorld().getUID(), xzMap);
        }
        Map<Integer, MarkedBlockSection> zMap = xzMap.get(block.getX() >> MarkedBlockSection.regionBitShift);
        if (zMap == null) {
            zMap = new HashMap<>();
            xzMap.put(block.getX() >> MarkedBlockSection.regionBitShift, zMap);
        }
        MarkedBlockSection ret = zMap.get(block.getZ() >> MarkedBlockSection.regionBitShift);
        if (ret == null) {
            ret = new MarkedBlockSection(block.getWorld(), block.getX() >> MarkedBlockSection.regionBitShift, block.getZ() >> MarkedBlockSection.regionBitShift, folderName);
            zMap.put(block.getZ() >> MarkedBlockSection.regionBitShift, ret);
            ret.load();
        }
        return ret;
    }

    public void performSave() {
        for (Map<Integer, Map<Integer, MarkedBlockSection>> xzMap : sections.values()) {
            for (Map<Integer, MarkedBlockSection> zMap : xzMap.values()) {
                for (MarkedBlockSection bs : zMap.values()) {
                    bs.save();
                }
            }
        }
    }

    public void saveOne() {
        MarkedBlockSection earliest = null;
        for (Map<Integer, Map<Integer, MarkedBlockSection>> xzMap : sections.values()) {
            for (Map<Integer, MarkedBlockSection> zMap : xzMap.values()) {
                for (MarkedBlockSection bs : zMap.values()) {
                    if (bs.isDirty() && (earliest == null || bs.getLastChanged() < earliest.getLastChanged())) {
                        earliest = bs;
                    }
                }
            }
        }
        if (earliest != null) earliest.save();
    }

    public void gc(long tolerance) {
        long deadline = System.currentTimeMillis() - tolerance;
        Iterator<Map<Integer, Map<Integer, MarkedBlockSection>>> xzMapIt = sections.values().iterator();
        while (xzMapIt.hasNext()) {
            Map<Integer, Map<Integer, MarkedBlockSection>> xzMap = xzMapIt.next();
            Iterator<Map<Integer, MarkedBlockSection>> zMapIt = xzMap.values().iterator();
            while (zMapIt.hasNext()) {
                Map<Integer, MarkedBlockSection> zMap = zMapIt.next();
                Iterator<MarkedBlockSection> it = zMap.values().iterator();
                while (it.hasNext()) {
                    MarkedBlockSection bs = it.next();
                    if (bs.getLastAccessed() < deadline) {
                        bs.save();
                        it.remove();
                    }
                }
                if (zMap.isEmpty()) zMapIt.remove();
            }
            if (xzMap.isEmpty()) xzMapIt.remove();
        }
    }
}
