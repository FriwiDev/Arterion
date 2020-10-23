package me.friwi.arterion.plugin.world.temporaryblock;

import java.util.LinkedList;
import java.util.List;

public class TemporaryBlockManager {
    private List<TemporaryBlockCompound> activeCompounds = new LinkedList<>();

    public TemporaryBlockCompound createCompound() {
        TemporaryBlockCompound comp = new TemporaryBlockCompound();
        activeCompounds.add(comp);
        return comp;
    }

    public void removeCompound(TemporaryBlockCompound compound) {
        activeCompounds.remove(compound);
    }

    public void rollbackAll() {
        for (TemporaryBlockCompound compound : activeCompounds) {
            compound.rollbackAll();
        }
        activeCompounds.clear();
    }
}
