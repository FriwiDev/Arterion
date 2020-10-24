package me.friwi.arterion.plugin.combat.hook;

public class Binding<T> {
    private T t;

    public Binding(T t) {
        this.t = t;
    }

    public T getBoundObject() {
        return t;
    }
}
