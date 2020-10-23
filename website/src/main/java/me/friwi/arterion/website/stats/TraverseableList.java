package me.friwi.arterion.website.stats;

import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Spliterator;
import java.util.function.Consumer;

public class TraverseableList implements Iterable<TraverseableListEntry> {
    private List<TraverseableListEntry> entries = new LinkedList<>();
    private int index;

    public TraverseableList(int start) {
        this.index = start;
    }

    public void addEntry(String link, Object... content) {
        entries.add(new TraverseableListEntry(link, index, content));
        index++;
    }

    @NotNull
    @Override
    public Iterator<TraverseableListEntry> iterator() {
        return entries.iterator();
    }

    @Override
    public void forEach(Consumer<? super TraverseableListEntry> action) {
        entries.forEach(action);
    }

    @Override
    public Spliterator<TraverseableListEntry> spliterator() {
        return entries.spliterator();
    }

    public boolean isEmpty() {
        return entries.isEmpty();
    }
}
