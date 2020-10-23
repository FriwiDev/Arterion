package me.friwi.arterion.website.premium;

import org.apache.commons.io.IOUtils;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.net.URL;
import java.nio.charset.Charset;
import java.util.UUID;

public class MCUserFetcher {
    public static MCUser fetchMCUser(String name) {
        try {
            String url = "https://api.mojang.com/users/profiles/minecraft/" + name;
            String UUIDJson = IOUtils.toString(new URL(url), Charset.forName("UTF-8"));
            if (UUIDJson.isEmpty()) return null;
            JSONObject UUIDObject = (JSONObject) JSONValue.parseWithException(UUIDJson);
            String uuid = UUIDObject.get("id").toString();
            name = UUIDObject.get("name").toString();
            return new MCUser(name, fromTrimmed(uuid));
        } catch (Exception e) {
            return null;
        }
    }

    private static UUID fromTrimmed(String trimmedUUID) {
        if (trimmedUUID == null) return null;
        StringBuilder builder = new StringBuilder(trimmedUUID.trim());
        /* Backwards adding to avoid index adjustments */
        try {
            builder.insert(20, "-");
            builder.insert(16, "-");
            builder.insert(12, "-");
            builder.insert(8, "-");
        } catch (StringIndexOutOfBoundsException e) {
            throw new IllegalArgumentException();
        }

        return UUID.fromString(builder.toString());
    }
}
