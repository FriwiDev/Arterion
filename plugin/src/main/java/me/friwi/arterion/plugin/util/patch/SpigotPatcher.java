package me.friwi.arterion.plugin.util.patch;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.Modifier;
import org.bukkit.Bukkit;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class SpigotPatcher {
    public static Field playerAccess;
    public static Field chunkAccess;
    private static boolean injected = false;

    public static void inject() {
        if (injected) return;
        injected = true;
        try {
            System.out.println("Patching internal plugin fields...");
            playerAccess = injectInternField(Bukkit.getServer().getClass().getPackage().getName() + ".entity.CraftPlayer");
            chunkAccess = injectInternField(Bukkit.getServer().getClass().getPackage().getName() + ".CraftChunk");
            System.out.println("Patching completed");
        } catch (Exception e) {
            throw new RuntimeException("Failed to patch spigot", e);
        }
    }

    private static Field injectInternField(String classname) throws Exception {
        CtClass point = ClassPool.getDefault().get(classname);
        CtClass type = ClassPool.getDefault().get(Object.class.getCanonicalName());
        CtField f = new CtField(type, "intern", point);
        f.setModifiers(Modifier.PUBLIC);
        point.addField(f);
        byte[] b = point.toBytecode();
        Method defineClass = ClassLoader.class.getDeclaredMethod("defineClass", String.class, byte[].class, int.class, int.class);
        defineClass.setAccessible(true);
        defineClass.invoke(Bukkit.class.getClassLoader(), classname, b, 0, b.length);
        return Class.forName(classname).getDeclaredField("intern");
    }
}
