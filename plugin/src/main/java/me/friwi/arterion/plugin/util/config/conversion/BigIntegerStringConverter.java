package me.friwi.arterion.plugin.util.config.conversion;

import me.friwi.arterion.plugin.util.config.conversion.api.TypeConverter;

import java.math.BigInteger;

public class BigIntegerStringConverter extends TypeConverter<BigInteger, String> {

    @Override
    public BigInteger convertOne(String value) {
        return new BigInteger(value);
    }

    @Override
    public String convertTwo(BigInteger value) {
        if (value == null) return null;
        return value.toString();
    }

}
