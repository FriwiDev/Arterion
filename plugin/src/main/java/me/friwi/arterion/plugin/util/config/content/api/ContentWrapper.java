package me.friwi.arterion.plugin.util.config.content.api;

public abstract class ContentWrapper {
    public abstract Integer getInt(String key);

    public abstract Long getLong(String key);

    public abstract Boolean getBoolean(String key);

    public abstract Float getFloat(String key);

    public abstract Double getDouble(String key);

    public abstract String getString(String key);

    public abstract void setInt(String key, Integer value);

    public abstract void setLong(String key, Long value);

    public abstract void setBoolean(String key, Boolean value);

    public abstract void setFloat(String key, Float value);

    public abstract void setDouble(String key, Double value);

    public abstract void setString(String key, String value);


    public abstract Integer[] getInts(String key);

    public abstract Long[] getLongs(String key);

    public abstract Boolean[] getBooleans(String key);

    public abstract Float[] getFloats(String key);

    public abstract Double[] getDoubles(String key);

    public abstract String[] getStrings(String key);

    public abstract void setInts(String key, Integer[] value);

    public abstract void setLongs(String key, Long[] value);

    public abstract void setBooleans(String key, Boolean[] value);

    public abstract void setFloats(String key, Float[] value);

    public abstract void setDoubles(String key, Double[] value);

    public abstract void setStrings(String key, String[] value);
}
