package me.friwi.arterion.website.stats;

public class TraverseableListEntry {
    String link;
    int number;
    Object[] content;

    public TraverseableListEntry(String link, int number, Object... content) {
        this.link = link;
        this.number = number;
        this.content = content;
    }

    public String getLink() {
        return link;
    }

    public int getNumber() {
        return number;
    }

    public Object[] getContent() {
        return content;
    }
}
