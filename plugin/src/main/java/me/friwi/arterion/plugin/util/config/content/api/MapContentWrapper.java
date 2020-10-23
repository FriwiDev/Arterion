package me.friwi.arterion.plugin.util.config.content.api;

import java.util.Map;

public class MapContentWrapper extends ContentWrapper {
    Map<String, Object> map;

    public MapContentWrapper(Map<String, Object> map) {
        this.map = map;
    }

    @Override
    public Integer getInt(String key) {
        return (Integer) map.get(key);
    }

    @Override
    public Long getLong(String key) {
        return (Long) map.get(key);
    }

    @Override
    public Boolean getBoolean(String key) {
        return (Boolean) map.get(key);
    }

    @Override
    public Float getFloat(String key) {
        return (Float) map.get(key);
    }

    @Override
    public Double getDouble(String key) {
        return (Double) map.get(key);
    }

    @Override
    public String getString(String key) {
        return (String) map.get(key);
    }

    @Override
    public void setInt(String key, Integer value) {
        map.put(key, value);
    }

    @Override
    public void setLong(String key, Long value) {
        map.put(key, value);
    }

    @Override
    public void setBoolean(String key, Boolean value) {
        map.put(key, value);
    }

    @Override
    public void setFloat(String key, Float value) {
        map.put(key, value);
    }

    @Override
    public void setDouble(String key, Double value) {
        map.put(key, value);
    }

    @Override
    public void setString(String key, String value) {
        map.put(key, value);
    }

    @Override
    public Integer[] getInts(String key) {
        return (Integer[]) map.get(key);
    }

    @Override
    public Long[] getLongs(String key) {
        return (Long[]) map.get(key);
    }

    @Override
    public Boolean[] getBooleans(String key) {
        return (Boolean[]) map.get(key);
    }

    @Override
    public Float[] getFloats(String key) {
        return (Float[]) map.get(key);
    }

    @Override
    public Double[] getDoubles(String key) {
        return (Double[]) map.get(key);
    }

    @Override
    public String[] getStrings(String key) {
        return (String[]) map.get(key);
    }

    @Override
    public void setInts(String key, Integer[] value) {
        map.put(key, value);
    }

    @Override
    public void setLongs(String key, Long[] value) {
        map.put(key, value);
    }

    @Override
    public void setBooleans(String key, Boolean[] value) {
        map.put(key, value);
    }

    @Override
    public void setFloats(String key, Float[] value) {
        map.put(key, value);
    }

    @Override
    public void setDoubles(String key, Double[] value) {
        map.put(key, value);
    }

    @Override
    public void setStrings(String key, String[] value) {
        map.put(key, value);
    }

}
