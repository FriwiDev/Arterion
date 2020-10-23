package me.friwi.arterion.plugin.ui.toplist;

import me.friwi.arterion.plugin.ui.gui.CachedHead;
import me.friwi.arterion.plugin.ui.gui.HeadCacheUtil;
import org.bukkit.SkullType;
import org.bukkit.block.*;

import java.lang.reflect.Field;
import java.util.UUID;

public abstract class TopList {
    protected int length;
    private Block begin;
    private BlockFace direction;

    public TopList(int length, Block begin, BlockFace direction) {
        this.length = length;
        this.begin = begin;
        this.direction = direction;
    }

    public void setEntry(int i, CachedHead head, String[] text) {
        Block signBlock = begin.getRelative(direction, i);
        BlockState signState = signBlock.getState();
        if (signState instanceof Sign) {
            for (int j = 0; j < text.length; j++) {
                ((Sign) signState).setLine(j, text[j]);
            }
            signState.update();
        }
        Block headBlock = signBlock.getRelative(BlockFace.UP);
        BlockState headState = headBlock.getState();
        if (headState instanceof Skull) {
            try {
                ((Skull) headState).setSkullType(SkullType.PLAYER);
                Field profile = headState.getClass().getDeclaredField("profile");
                profile.setAccessible(true);
                profile.set(headState, head.getGameProfile());
                headState.update();
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    public void setEntry(int i, UUID head, String[] text) {
        HeadCacheUtil.supplyCachedHeadSync(heads -> {
            setEntry(i, heads[0], text);
        }, head);
    }

    public abstract void refreshTopList();
}
