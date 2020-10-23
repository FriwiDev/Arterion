package me.friwi.arterion.plugin.util.config;

import me.friwi.arterion.plugin.util.config.init.api.GetterValue;
import me.friwi.arterion.plugin.util.config.init.api.SetterValue;
import me.friwi.arterion.plugin.util.config.init.api.Value;

import java.math.BigInteger;

public class Sth {
    @Value
    public String id;

    public BigInteger s = BigInteger.ZERO;

    @GetterValue(name = "name")
    public BigInteger getName() {
        return s;
    }

    @SetterValue(name = "name")
    public void setName(BigInteger name) {
        this.s = name;
    }
}
