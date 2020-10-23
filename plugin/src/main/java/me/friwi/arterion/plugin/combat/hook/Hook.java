package me.friwi.arterion.plugin.combat.hook;

import org.bukkit.entity.Entity;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Function;

public class Hook<S, T> {
    private Map<Binding<S>, Function<T, T>> listeners = new HashMap<>();

    public Binding<S> subscribe(S s, Function<T, T> func) {
        Binding<S> bind = new Binding<>(s);
        listeners.put(bind, func);
        return bind;
    }

    public boolean unsubscribe(Binding<S> binding) {
        return listeners.remove(binding) != null;
    }

    public boolean unsubscribeAll(S s) {
        boolean found = false;
        Iterator<Binding<S>> it = listeners.keySet().iterator();
        while (it.hasNext()) {
            if (it.next().getBoundObject().equals(s)) {
                it.remove();
                found = true;
            }
        }
        return found;
    }

    public T execute(S s, T value) {
        boolean allowNull = value == null;
        Iterator<Map.Entry<Binding<S>, Function<T, T>>> it = listeners.entrySet().iterator();
        while (it.hasNext() && (value != null || allowNull)) {
            Map.Entry<Binding<S>, Function<T, T>> entry = it.next();
            if (entry.getKey().getBoundObject().equals(s)) value = entry.getValue().apply(value);
            if (Entity.class.isAssignableFrom(entry.getKey().getClass())) {
                if (((Entity) entry.getKey()).isDead()) it.remove();
            }
        }
        return value;
    }
}
