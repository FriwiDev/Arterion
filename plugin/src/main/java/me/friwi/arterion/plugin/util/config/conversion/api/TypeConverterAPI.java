package me.friwi.arterion.plugin.util.config.conversion.api;

import me.friwi.arterion.plugin.util.config.conversion.InstanceConverter;
import me.friwi.arterion.plugin.util.config.conversion.InvertedConverter;

public class TypeConverterAPI {
    private static TypeConverter[] converters;

    public static <T, S> T convertTo(S obj, Class<? extends T> to) {
        if (obj == null) return (T) null;
        Class<?> from = obj.getClass();
        if (to.isAssignableFrom(from)) return (T) obj;
        TypeConverter c = getConverter(from, to);
        if (c != null) return (T) c.convertTwo(obj);
        return null;
    }

    public static <T, S> T convertTo(S obj, TypeConverter<S, T> con) {
        if (obj == null) return (T) null;
        return con.convertTwo(obj);
    }

    public static <S, T> TypeConverter<S, T> getConverter(Class<? extends S> from, Class<? extends T>... to) {
        for (Class<?> x : to) if (x.isAssignableFrom(from)) return new InstanceConverter<S, T>();
        for (TypeConverter c : converters) {
            if (c.getOne() == from) {
                for (Class<?> x : to) {
                    if (c.getTwo() == x) {
                        //Found a converter
                        return c;
                    }
                }
            }
            if (c.getOne().isAssignableFrom(from)) {
                for (Class<?> x : to) {
                    if (x.isAssignableFrom(c.getTwo())) {
                        //Found a converter
                        return c;
                    }
                }
            }
        }
        return null;
    }

    public static void registerConverter(TypeConverter converter) {
        TypeConverter[] n = new TypeConverter[converters.length + 2];
        for (int i = 0; i < converters.length; i++) n[i] = converters[i];
        n[n.length - 2] = converter;
        n[n.length - 1] = new InvertedConverter(converter);
        converters = n;
    }

    public static void reset() {
        converters = new TypeConverter[0];
    }
}
