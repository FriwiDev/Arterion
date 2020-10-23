package me.friwi.arterion.plugin.util.language.api;

public class TranslatedMessage {
    String msg;
    int[][] index;
    Translateable[] translateables;

    public TranslatedMessage(String message, int[][] index, Translateable[] translateables) {
        this.msg = message;
        this.index = index;
        this.translateables = translateables;
    }

    public int[] getStartAndEnd(int index) {
        return this.index[index];
    }

    public String getMessage() {
        return msg;
    }

    public Translateable getTranslateable(int index) {
        return translateables[index];
    }

    public int getIndexSize() {
        return index.length;
    }
}
