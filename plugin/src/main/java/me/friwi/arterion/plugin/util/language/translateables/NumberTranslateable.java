package me.friwi.arterion.plugin.util.language.translateables;

import me.friwi.arterion.plugin.util.language.api.Language;
import me.friwi.arterion.plugin.util.language.api.Translateable;

import java.util.Locale;

public class NumberTranslateable extends Translateable {
    Number number;

    public NumberTranslateable(Number number) {
        this.number = number;
    }


    public static String getCaption(Number number) {
        return formatNumber(number);
    }

    public static String formatNumber(Number number) {
        if (number instanceof Long || number instanceof Integer) {
            return number.toString();
        } else {
            String num = String.format(Locale.ENGLISH, "%.2f", number.doubleValue());
            int point = num.indexOf('.');
            if (point != -1) {
                if (num.endsWith("00")) {
                    num = num.substring(0, point);
                } else if (num.endsWith("0")) {
                    num = num.substring(0, point + 2);
                }
            }
            return num;
        }
    }

    @Override
    public boolean isSingular(Language lang) {
        return Math.abs(number.doubleValue() - 1) < 0.001;
    }

    public Number getNumber() {
        return number;
    }

    @Override
    public String getCaption(Language lang, String outerColor, String innerColor) {
        return innerColor + getCaption(number);
    }
}
