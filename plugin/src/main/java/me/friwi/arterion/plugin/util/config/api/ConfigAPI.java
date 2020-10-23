package me.friwi.arterion.plugin.util.config.api;

import me.friwi.arterion.plugin.util.config.ConfigFile;
import me.friwi.arterion.plugin.util.config.conversion.*;
import me.friwi.arterion.plugin.util.config.conversion.api.TypeConverterAPI;
import me.friwi.arterion.plugin.util.language.translateables.*;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class ConfigAPI {
    public static final String basePath = "plugins/ArterionPlugin/";

    public static void init() {
        //Register all internal converters
        TypeConverterAPI.reset();
        TypeConverterAPI.registerConverter(new BooleanStringConverter());
        TypeConverterAPI.registerConverter(new DoubleStringConverter());
        TypeConverterAPI.registerConverter(new FloatStringConverter());
        TypeConverterAPI.registerConverter(new IntStringConverter());
        TypeConverterAPI.registerConverter(new LongStringConverter());
        TypeConverterAPI.registerConverter(new BigIntegerStringConverter());
        TypeConverterAPI.registerConverter(new LocationStringConverter());

        TypeConverterAPI.registerConverter(new CollectionTranslateableConverter());
        TypeConverterAPI.registerConverter(new ArrayTranslateableConverter());
        TypeConverterAPI.registerConverter(new NumberTranslateableConverter());
        TypeConverterAPI.registerConverter(new StringTranslateableConverter());

        TypeConverterAPI.registerConverter(new ArterionPlayerStringTranslateableConverter());
        TypeConverterAPI.registerConverter(new GuildStringTranslateableConverter());
    }

    public static <T> T readConfig(T obj) {
        if (obj.getClass().isAnnotationPresent(Configureable.class)) {
            Configureable c = obj.getClass().getAnnotation(Configureable.class);
            if (c.location().isEmpty()) {
                throw new IllegalArgumentException("No location specified for config!");
            }
            return readConfig(obj, c.location());
        } else {
            throw new IllegalArgumentException("Target object is not configureable!");
        }
    }

    public static <T> void writeConfig(T obj) {
        if (obj.getClass().isAnnotationPresent(Configureable.class)) {
            Configureable c = obj.getClass().getAnnotation(Configureable.class);
            if (c.location().isEmpty()) {
                throw new IllegalArgumentException("No location specified for config!");
            }
            writeConfig(obj, c.location());
        } else {
            throw new IllegalArgumentException("Target object is not configureable!");
        }
    }

    public static <T> T readConfig(T obj, String location) {
        return readConfig(obj, new File(basePath + location));
    }

    public static <T> void writeConfig(T obj, String location) {
        writeConfig(obj, new File(basePath + location));
    }

    public static <T> T readConfig(T obj, File file) {
        ConfigFile f = new ConfigFile(file);
        f.read();
        fillConfig(obj, f.getMap());
        return obj;
    }

    public static <T> T readConfig(T obj, InputStream in) {
        ConfigFile f = new ConfigFile(in);
        f.read();
        fillConfig(obj, f.getMap());
        return obj;
    }

    public static <T> void writeConfig(T obj, File file) {
        ConfigFile f = new ConfigFile(file);
        f.setMap(fetchConfig(obj));
        f.write();
    }

    protected static <T> T fillConfig(T obj, Map<String, String> map) {
        if (obj.getClass().isAnnotationPresent(Configureable.class)) {
            try {
                Class<?> clasz = obj.getClass();
                while (!clasz.equals(Object.class)) {
                    for (Field f : obj.getClass().getDeclaredFields()) {
                        if (f.isAnnotationPresent(ConfigValue.class)) {
                            ConfigValue v = f.getAnnotation(ConfigValue.class);
                            String name = v.name().isEmpty() ? f.getName() : v.name();
                            String value = map.get(name);
                            if (value == null) {
                                if (!v.fallback().isEmpty()) {
                                    value = v.fallback();
                                } else if (v.required()) {
                                    throw new RuntimeException("Value " + name + " is not in config!");
                                }
                            }
                            f.setAccessible(true);
                            f.set(obj, TypeConverterAPI.convertTo(value, f.getType()));
                            map.remove(name);
                        }
                    }
                    for (Field f : obj.getClass().getDeclaredFields()) {
                        if (f.isAnnotationPresent(ConfigHashmap.class)) {
                            f.setAccessible(true);
                            f.set(obj, map);
                        }
                    }
                    clasz = clasz.getSuperclass();
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                throw new RuntimeException("Something went wrong while using reflection");
            }
        } else {
            throw new IllegalArgumentException("Target object is not configureable!");
        }
        return obj;
    }

    @SuppressWarnings("unchecked")
    protected static <T> Map<String, String> fetchConfig(T obj) {
        HashMap<String, String> ret = new HashMap<String, String>();
        if (obj.getClass().isAnnotationPresent(Configureable.class)) {
            try {
                for (Field f : obj.getClass().getDeclaredFields()) {
                    if (f.isAnnotationPresent(ConfigHashmap.class)) {
                        f.setAccessible(true);
                        Map<String, String> map = (Map<String, String>) f.get(obj);
                        for (Map.Entry<String, String> entry : map.entrySet()) {
                            ret.put(entry.getKey(), entry.getValue());
                        }
                    }
                }
                for (Field f : obj.getClass().getDeclaredFields()) {
                    if (f.isAnnotationPresent(ConfigValue.class)) {
                        f.setAccessible(true);
                        ConfigValue v = f.getAnnotation(ConfigValue.class);
                        String name = v.name().isEmpty() ? f.getName() : v.name();
                        String value = TypeConverterAPI.convertTo(f.get(obj), String.class);
                        if (value == null) value = "null";
                        ret.put(name, value);
                    }
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                throw new RuntimeException("Something went wrong while using reflection");
            }
        } else {
            throw new IllegalArgumentException("Target object is not configureable!");
        }
        return ret;
    }
}
