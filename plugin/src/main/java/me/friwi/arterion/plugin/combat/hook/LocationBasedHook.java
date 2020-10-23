package me.friwi.arterion.plugin.combat.hook;

import me.friwi.arterion.plugin.world.chunk.ArterionChunkUtil;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

import java.util.Iterator;
import java.util.Map;
import java.util.function.Function;

public class LocationBasedHook<S extends Location, T> extends Hook<S, T> {
    private int index;

    public LocationBasedHook(int index) {
        this.index = index;
    }

    @Override
    public Binding<S> subscribe(S s, Function<T, T> func) {
        Binding<S> bind = new Binding<>(s);
        getListeners(s).put(bind, func);
        return bind;
    }

    @Override
    public boolean unsubscribe(Binding<S> binding) {
        return getListeners(binding.getBoundObject()).remove(binding) != null;
    }

    @Override
    public boolean unsubscribeAll(S s) {
        boolean found = false;
        Iterator<Binding<S>> it = getListeners(s).keySet().iterator();
        while (it.hasNext()) {
            if (it.next().getBoundObject().equals(s)) {
                it.remove();
                found = true;
            }
        }
        return found;
    }

    @Override
    public T execute(S s, T value) {
        boolean allowNull = value == null;
        Iterator<Map.Entry<Binding<S>, Function<T, T>>> it = getListeners(s).entrySet().iterator();
        while (it.hasNext() && (value != null || allowNull)) {
            Map.Entry<Binding<S>, Function<T, T>> entry = it.next();
            if (entry.getKey().getBoundObject().equals(s)) value = entry.getValue().apply(value);
            if (Entity.class.isAssignableFrom(entry.getKey().getClass())) {
                if (((Entity) entry.getKey()).isDead()) it.remove();
            }
        }
        return value;
    }

    public Map getListeners(Location loc) {
        return ArterionChunkUtil.getNonNull(loc.getChunk()).getListeners(index);
    }
}
