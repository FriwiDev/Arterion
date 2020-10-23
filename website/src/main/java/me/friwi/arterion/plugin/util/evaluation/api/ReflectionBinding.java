package me.friwi.arterion.plugin.util.evaluation.api;

public class ReflectionBinding {
    String name;
    Class<?> type;

    public ReflectionBinding(String name, Class<?> type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public Class<?> getType() {
        return type;
    }

    @Override
    public String toString() {
        return "ReflectionBinding{" +
                "name='" + name + '\'' +
                ", type=" + type +
                '}';
    }
}
