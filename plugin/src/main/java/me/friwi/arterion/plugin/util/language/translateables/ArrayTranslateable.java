package me.friwi.arterion.plugin.util.language.translateables;

import me.friwi.arterion.plugin.util.config.conversion.api.TypeConverterAPI;
import me.friwi.arterion.plugin.util.language.api.Language;
import me.friwi.arterion.plugin.util.language.api.Translateable;

import java.util.List;

public class ArrayTranslateable extends Translateable {
    Object[] array;
    Translateable[] content;
    Integer[] index;

    public ArrayTranslateable(Object[] array) {
        this.array = array;
        this.content = new Translateable[array.length];
        this.index = new Integer[array.length];
    }


    @Override
    public String getCaption(Language lang, String outerColor, String innerColor) {
        StringBuilder builder = new StringBuilder();
        if (content.length == 0) {
            builder.append(innerColor + "-");
        } else {
            int l = content.length;
            int b = 0;
            for (int i = 0; i < array.length; i++) {
                content[i] = TypeConverterAPI.convertTo(array[i], Translateable.class);
                index[i] = b;
                String add = content[i].getCaptionInternal(lang, outerColor, innerColor) + outerColor + (i == l - 2 ? (" " + lang.AND.translate().getMessage() + " ") : (i < l - 2 ? ", " : ""));
                builder.append(add);
                b += add.length();
            }
        }
        return builder.toString();
    }

    @Override
    public boolean isSingular(Language lang) {
        return content.length == 1;
    }

    @Override
    public void addIndexes(List<Integer[]> index, int offs) {
        for (int i = 0; i < content.length; i++) {
            content[i].addIndexes(index, offs + this.index[i]);
        }
    }

    @Override
    public void addTranslateables(List<Translateable> trans) {
        for (int i = 0; i < content.length; i++) {
            trans.add(content[i]);
        }
    }


    public Object[] getArray() {
        return array;
    }
}
