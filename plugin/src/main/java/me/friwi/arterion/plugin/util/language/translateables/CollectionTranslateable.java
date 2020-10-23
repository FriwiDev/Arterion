package me.friwi.arterion.plugin.util.language.translateables;

import me.friwi.arterion.plugin.util.config.conversion.api.TypeConverterAPI;
import me.friwi.arterion.plugin.util.language.api.Language;
import me.friwi.arterion.plugin.util.language.api.Translateable;

import java.util.Collection;
import java.util.List;

public class CollectionTranslateable extends Translateable {
    Collection coll;
    Translateable[] content;
    Integer[] index;
    int i = 0;
    int b = 0;

    public CollectionTranslateable(Collection coll) {
        this.coll = coll;
        this.content = new Translateable[coll.size()];
        this.index = new Integer[coll.size()];
    }


    @Override
    public String getCaption(Language lang, String outerColor, String innerColor) {
        StringBuilder builder = new StringBuilder();
        if (content.length == 0) {
            builder.append(innerColor + "-");
        } else {
            int l = content.length;
            i = 0;
            b = 0;
            coll.iterator().forEachRemaining((obj) -> {
                content[i] = TypeConverterAPI.convertTo(obj, Translateable.class);
                index[i] = b;
                String add = content[i].getCaptionInternal(lang, outerColor, innerColor) + outerColor + (i == l - 2 ? (" " + lang.AND.translate().getMessage() + " ") : (i < l - 2 ? ", " : ""));
                builder.append(add);
                i++;
                b += add.length();
            });
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


    public Collection getCollection() {
        return coll;
    }
}
