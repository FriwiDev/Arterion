package me.friwi.arterion.plugin.util.config.init.api;

import me.friwi.arterion.plugin.util.config.conversion.api.TypeConverter;
import me.friwi.arterion.plugin.util.config.conversion.api.TypeConverterAPI;
import me.friwi.arterion.plugin.util.config.init.reflection.*;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.LinkedList;

public class ReflectionSkeletonBuilder {
    public static final Class<?>[] available = new Class<?>[]{
            Boolean.class, Double.class, Float.class, Integer.class, Long.class, String.class,
            Boolean[].class, Double[].class, Float[].class, Integer[].class, Long[].class, String[].class,
    };

    public static ReflectionSkeleton buildSkeleton(Class<?> clazz) {
        LinkedList<ReflectedField> fields = new LinkedList<>();
        LinkedList<ReflectedMethod> getters = new LinkedList<>();
        LinkedList<ReflectedMethod> setters = new LinkedList<>();

        for (Field f : clazz.getDeclaredFields()) {
            if (f.isAnnotationPresent(Value.class)) {
                Value v = f.getAnnotation(Value.class);
                String name = v.name().isEmpty() ? f.getName() : v.name();
                String d = v.fallback().equalsIgnoreCase("null") ? null : v.fallback();
                boolean required = v.required();
                if (f.getType() == Boolean.class) {
                    fields.add(new BooleanField(f, name, d, required));
                } else if (f.getType() == Double.class) {
                    fields.add(new DoubleField(f, name, d, required));
                } else if (f.getType() == Float.class) {
                    fields.add(new FloatField(f, name, d, required));
                } else if (f.getType() == Integer.class) {
                    fields.add(new IntField(f, name, d, required));
                } else if (f.getType() == Long.class) {
                    fields.add(new LongField(f, name, d, required));
                } else if (f.getType() == String.class) {
                    fields.add(new StringField(f, name, d, required));
                } else if (f.getType() == Boolean[].class) {
                    fields.add(new BooleansField(f, name, d, required));
                } else if (f.getType() == Double[].class) {
                    fields.add(new DoublesField(f, name, d, required));
                } else if (f.getType() == Float[].class) {
                    fields.add(new FloatsField(f, name, d, required));
                } else if (f.getType() == Integer[].class) {
                    fields.add(new IntsField(f, name, d, required));
                } else if (f.getType() == Long[].class) {
                    fields.add(new LongsField(f, name, d, required));
                } else if (f.getType() == String[].class) {
                    fields.add(new StringsField(f, name, d, required));
                } else {
                    TypeConverter to = TypeConverterAPI.getConverter(f.getType(), available);
                    if (to == null) {
                        throw new IllegalArgumentException("Field " + f.getName() + " has an invalid type!");
                    }
                    TypeConverter back = to.getInverse();
                    Class<?> replacement = to.getTwo();
                    if (replacement == Boolean.class) {
                        fields.add(new TypeConverterField(new BooleanField(f, name, d, required), to, back));
                    } else if (replacement == Double.class) {
                        fields.add(new TypeConverterField(new DoubleField(f, name, d, required), to, back));
                    } else if (replacement == Float.class) {
                        fields.add(new TypeConverterField(new FloatField(f, name, d, required), to, back));
                    } else if (replacement == Integer.class) {
                        fields.add(new TypeConverterField(new IntField(f, name, d, required), to, back));
                    } else if (replacement == Long.class) {
                        fields.add(new TypeConverterField(new LongField(f, name, d, required), to, back));
                    } else if (replacement == String.class) {
                        fields.add(new TypeConverterField(new StringField(f, name, d, required), to, back));
                    } else if (replacement == Boolean[].class) {
                        fields.add(new TypeConverterField(new BooleansField(f, name, d, required), to, back));
                    } else if (replacement == Double[].class) {
                        fields.add(new TypeConverterField(new DoublesField(f, name, d, required), to, back));
                    } else if (replacement == Float[].class) {
                        fields.add(new TypeConverterField(new FloatsField(f, name, d, required), to, back));
                    } else if (replacement == Integer[].class) {
                        fields.add(new TypeConverterField(new IntsField(f, name, d, required), to, back));
                    } else if (replacement == Long[].class) {
                        fields.add(new TypeConverterField(new LongsField(f, name, d, required), to, back));
                    } else if (replacement == String[].class) {
                        fields.add(new TypeConverterField(new StringsField(f, name, d, required), to, back));
                    }
                }


            }
        }

        for (Method m : clazz.getDeclaredMethods()) {
            if (m.isAnnotationPresent(GetterValue.class)) {
                GetterValue v = m.getAnnotation(GetterValue.class);
                String name = v.name().isEmpty() ? m.getName() : v.name();
                String d = null;
                boolean required = false;
                Class<?> type = m.getReturnType();
                if (type == Boolean.class) {
                    getters.add(new BooleanMethod(m, name, d, required));
                } else if (type == Double.class) {
                    getters.add(new DoubleMethod(m, name, d, required));
                } else if (type == Float.class) {
                    getters.add(new FloatMethod(m, name, d, required));
                } else if (type == Integer.class) {
                    getters.add(new IntMethod(m, name, d, required));
                } else if (type == Long.class) {
                    getters.add(new LongMethod(m, name, d, required));
                } else if (type == String.class) {
                    getters.add(new StringMethod(m, name, d, required));
                } else if (type == Boolean[].class) {
                    getters.add(new BooleansMethod(m, name, d, required));
                } else if (type == Double[].class) {
                    getters.add(new DoublesMethod(m, name, d, required));
                } else if (type == Float[].class) {
                    getters.add(new FloatsMethod(m, name, d, required));
                } else if (type == Integer[].class) {
                    getters.add(new IntsMethod(m, name, d, required));
                } else if (type == Long[].class) {
                    getters.add(new LongsMethod(m, name, d, required));
                } else if (type == String[].class) {
                    getters.add(new StringsMethod(m, name, d, required));
                } else {
                    TypeConverter to = TypeConverterAPI.getConverter(type, available);
                    if (to == null) {
                        throw new IllegalArgumentException("Method " + m.getName() + " has an invalid type!");
                    }
                    TypeConverter back = to.getInverse();
                    type = to.getTwo();
                    if (type == Boolean.class) {
                        getters.add(new TypeConverterMethod(new BooleanMethod(m, name, d, required), to, back));
                    } else if (type == Double.class) {
                        getters.add(new TypeConverterMethod(new DoubleMethod(m, name, d, required), to, back));
                    } else if (type == Float.class) {
                        getters.add(new TypeConverterMethod(new FloatMethod(m, name, d, required), to, back));
                    } else if (type == Integer.class) {
                        getters.add(new TypeConverterMethod(new IntMethod(m, name, d, required), to, back));
                    } else if (type == Long.class) {
                        getters.add(new TypeConverterMethod(new LongMethod(m, name, d, required), to, back));
                    } else if (type == String.class) {
                        getters.add(new TypeConverterMethod(new StringMethod(m, name, d, required), to, back));
                    } else if (type == Boolean[].class) {
                        getters.add(new TypeConverterMethod(new BooleansMethod(m, name, d, required), to, back));
                    } else if (type == Double[].class) {
                        getters.add(new TypeConverterMethod(new DoublesMethod(m, name, d, required), to, back));
                    } else if (type == Float[].class) {
                        getters.add(new TypeConverterMethod(new FloatsMethod(m, name, d, required), to, back));
                    } else if (type == Integer[].class) {
                        getters.add(new TypeConverterMethod(new IntsMethod(m, name, d, required), to, back));
                    } else if (type == Long[].class) {
                        getters.add(new TypeConverterMethod(new LongsMethod(m, name, d, required), to, back));
                    } else if (type == String[].class) {
                        getters.add(new TypeConverterMethod(new StringsMethod(m, name, d, required), to, back));
                    }
                }
            }
            if (m.isAnnotationPresent(SetterValue.class)) {
                SetterValue v = m.getAnnotation(SetterValue.class);
                String name = v.name().isEmpty() ? m.getName() : v.name();
                String d = v.fallback().equalsIgnoreCase("null") ? null : v.fallback();
                boolean required = v.required();
                if (m.getParameterTypes().length != 1) {
                    throw new IllegalArgumentException("Method " + m.getName() + " has an invalid amount of arguments. Should be 1");
                }
                Class<?> type = m.getParameterTypes()[0];
                if (type == Boolean.class) {
                    setters.add(new BooleanMethod(m, name, d, required));
                } else if (type == Double.class) {
                    setters.add(new DoubleMethod(m, name, d, required));
                } else if (type == Float.class) {
                    setters.add(new FloatMethod(m, name, d, required));
                } else if (type == Integer.class) {
                    setters.add(new IntMethod(m, name, d, required));
                } else if (type == Long.class) {
                    setters.add(new LongMethod(m, name, d, required));
                } else if (type == String.class) {
                    setters.add(new StringMethod(m, name, d, required));
                } else if (type == Boolean[].class) {
                    setters.add(new BooleansMethod(m, name, d, required));
                } else if (type == Double[].class) {
                    setters.add(new DoublesMethod(m, name, d, required));
                } else if (type == Float[].class) {
                    setters.add(new FloatsMethod(m, name, d, required));
                } else if (type == Integer[].class) {
                    setters.add(new IntsMethod(m, name, d, required));
                } else if (type == Long[].class) {
                    setters.add(new LongsMethod(m, name, d, required));
                } else if (type == String[].class) {
                    setters.add(new StringsMethod(m, name, d, required));
                } else {
                    TypeConverter to = TypeConverterAPI.getConverter(type, available);
                    if (to == null) {
                        throw new IllegalArgumentException("Method " + m.getName() + " has an invalid type!");
                    }
                    TypeConverter back = to.getInverse();
                    type = to.getTwo();
                    if (type == Boolean.class) {
                        setters.add(new TypeConverterMethod(new BooleanMethod(m, name, d, required), to, back));
                    } else if (type == Double.class) {
                        setters.add(new TypeConverterMethod(new DoubleMethod(m, name, d, required), to, back));
                    } else if (type == Float.class) {
                        setters.add(new TypeConverterMethod(new FloatMethod(m, name, d, required), to, back));
                    } else if (type == Integer.class) {
                        setters.add(new TypeConverterMethod(new IntMethod(m, name, d, required), to, back));
                    } else if (type == Long.class) {
                        setters.add(new TypeConverterMethod(new LongMethod(m, name, d, required), to, back));
                    } else if (type == String.class) {
                        setters.add(new TypeConverterMethod(new StringMethod(m, name, d, required), to, back));
                    } else if (type == Boolean[].class) {
                        setters.add(new TypeConverterMethod(new BooleansMethod(m, name, d, required), to, back));
                    } else if (type == Double[].class) {
                        setters.add(new TypeConverterMethod(new DoublesMethod(m, name, d, required), to, back));
                    } else if (type == Float[].class) {
                        setters.add(new TypeConverterMethod(new FloatsMethod(m, name, d, required), to, back));
                    } else if (type == Integer[].class) {
                        setters.add(new TypeConverterMethod(new IntsMethod(m, name, d, required), to, back));
                    } else if (type == Long[].class) {
                        setters.add(new TypeConverterMethod(new LongsMethod(m, name, d, required), to, back));
                    } else if (type == String[].class) {
                        setters.add(new TypeConverterMethod(new StringsMethod(m, name, d, required), to, back));
                    }
                }
            }
        }

        ReflectedField[] fs = new ReflectedField[fields.size()];
        fields.toArray(fs);
        ReflectedMethod[] gs = new ReflectedMethod[getters.size()];
        getters.toArray(gs);
        ReflectedMethod[] ss = new ReflectedMethod[setters.size()];
        setters.toArray(ss);
        return new ReflectionSkeleton(fs, gs, ss);
    }
}
