package me.friwi.arterion.plugin.util.config;

import me.friwi.arterion.plugin.formula.ArterionFormula;
import me.friwi.arterion.plugin.util.config.api.ConfigAPI;
import me.friwi.arterion.plugin.util.config.content.api.ContentWrapper;
import me.friwi.arterion.plugin.util.config.content.api.MapContentWrapper;
import me.friwi.arterion.plugin.util.config.conversion.api.TypeConverterAPI;
import me.friwi.arterion.plugin.util.config.init.api.ReflectionSkeleton;
import me.friwi.arterion.plugin.util.config.init.api.ReflectionSkeletonBuilder;
import me.friwi.arterion.plugin.util.evaluation.api.Evaluation;
import me.friwi.arterion.plugin.util.evaluation.api.EvaluationBuilder;
import me.friwi.arterion.plugin.util.evaluation.api.ReflectionBinding;
import me.friwi.arterion.plugin.util.language.api.LanguageAPI;
import me.friwi.arterion.plugin.util.language.api.TranslatedMessage;
import me.friwi.arterion.plugin.util.language.api.Translation;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

public class ConfigAndLanguageTest {
    public static void main(String[] args) throws IllegalAccessException, InvocationTargetException {
        //Register all internal converters
        ConfigAPI.init();

        //Fire up language system
        LanguageAPI.reloadAllLanguages();

        Translation t = LanguageAPI.getLanguage("de_DE").getTranslation("test");
        TranslatedMessage m = t.translate(1);
        TranslatedMessage m1 = t.translate(1);
        System.out.println(m.getMessage());
        System.out.println(m1.getMessage());


        Evaluation eval = EvaluationBuilder.fromString("(4*(((((2^sth)))))))", new ReflectionBinding("sth", int.class));

        ReflectionSkeleton skel = ReflectionSkeletonBuilder.buildSkeleton(Sth.class);
        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put("name", "335577");
        map.put("id", "Fritz");

        String id = "34";
        Long i = null;
        for (int d = 0; d < 100; d++) {
            i = TypeConverterAPI.convertTo(id, Long.class);
        }
        long start = System.nanoTime();
        for (int d = 0; d < 10000; d++) {
            i = TypeConverterAPI.convertTo(id, Long.class);
        }
        System.out.println("10000 type conversions took " + (System.nanoTime() - start) + " ns");

        String e = eval.evaluate(new String[]{"sth"}, new Object[]{2}).floatValue() + "";
        System.out.println(eval + " = " + e);

        ContentWrapper wrapper = new MapContentWrapper(map);
        Sth sth = new Sth();
        skel.apply(sth, wrapper);
        System.out.println(sth.id + " " + sth.getName());

        System.out.println(i);

        eval = EvaluationBuilder.fromString("random", new ReflectionBinding("bowcharge", Double.class), new ReflectionBinding("random", Double.class));

        System.out.println(eval.evaluate(new String[]{"bowcharge", "random"}, new Double[]{Double.valueOf(1), Double.valueOf(2)}));
        ArterionFormula f = new ArterionFormula("lul", new ReflectionBinding("bowcharge", Double.class), new ReflectionBinding("random", Double.class));
        f.setFormula("random");
        System.out.println(f.evaluateDouble(1, 2));
    }
}
