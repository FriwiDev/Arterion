package me.friwi.arterion.plugin.ui.hotbar;

import org.apache.commons.lang.ObjectUtils;

public abstract class MergeableHotbarCard<T extends MergeableHotbarCard> extends HotbarCard {
    public MergeableHotbarCard(long duration) {
        super(duration);
    }

    public abstract void mergeWithCard(T card);

    public boolean canBeMerged(HotbarCard other) {
        if (other == null) return false;
        return ObjectUtils.equals(other.getClass(), getClass());
    }
}
