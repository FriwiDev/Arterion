package me.friwi.arterion.plugin.util.config.conversion.api;

import java.lang.reflect.ParameterizedType;

public abstract class TypeConverter<S, T> {
    protected Class<? extends S> one;
    protected Class<? extends T> two;

    public TypeConverter() {
        this(true);
    }

    @SuppressWarnings("unchecked")
    public TypeConverter(boolean init) {
        if (init) {
            try {
                one = (Class<? extends S>) ((ParameterizedType) this.getClass().getGenericSuperclass()).getActualTypeArguments()[0];
                two = (Class<? extends T>) ((ParameterizedType) this.getClass().getGenericSuperclass()).getActualTypeArguments()[1];
                /*for(Method method : this.getClass().getDeclaredMethods()){
                    if(method.getName().equals("convertOne")){
                        one = (Class<? extends S>) method.getReturnType();
                    }
                    if(method.getName().equals("convertTwo")){
                        two = (Class<? extends T>) method.getReturnType();
                    }
                }*/

            } catch (ClassCastException e) {
                throw new RuntimeException("Your type converter was most likely not correctly parameterized! Use correct generics!", e);
            }
        }
    }

    public abstract S convertOne(T value);

    public abstract T convertTwo(S value);

    public T convert(S value) {
        return convertTwo(value);
    }

    public Class<?> getOne() {
        return one;
    }

    public Class<?> getTwo() {
        return two;
    }

    @SuppressWarnings("unchecked")
    public TypeConverter<T, S> getInverse() {
        return TypeConverterAPI.getConverter(two, one);
    }
}
