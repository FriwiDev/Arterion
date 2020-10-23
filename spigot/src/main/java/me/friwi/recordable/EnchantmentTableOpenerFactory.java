package me.friwi.recordable;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class EnchantmentTableOpenerFactory {
    private static Constructor<?> constructor;

    static {
        try {
            constructor = Class.forName("me.friwi.recordable.impl.EnchantmentTableOpenerImpl").getDeclaredConstructor();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static EnchantmentTableOpener newOpener(){
        try {
            return (EnchantmentTableOpener) constructor.newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }
}
