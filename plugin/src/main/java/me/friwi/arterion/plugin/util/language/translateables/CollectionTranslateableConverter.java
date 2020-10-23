package me.friwi.arterion.plugin.util.language.translateables;

import me.friwi.arterion.plugin.util.config.conversion.api.TypeConverter;

import java.util.Collection;

public class CollectionTranslateableConverter extends TypeConverter<Collection, CollectionTranslateable> {

    @Override
    public Collection convertOne(CollectionTranslateable value) {
        return value.getCollection();
    }

    @Override
    public CollectionTranslateable convertTwo(Collection value) {
        return new CollectionTranslateable(value);
    }

}
