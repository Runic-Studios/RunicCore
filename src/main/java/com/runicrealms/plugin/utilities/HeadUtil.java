package com.runicrealms.plugin.utilities;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;

public class HeadUtil {

    private static ItemStack getHead(String value) {
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        UUID hashAsId = new UUID(value.hashCode(), value.hashCode());
        return Bukkit.getUnsafe().modifyItemStack(skull, "{SkullOwner:{Id:\"" + hashAsId + "\",Properties:{textures:[{Value:\"" + value + "\"}]}}}");
    }

    private static String getHeadValue(String name) {
        try {
            String result = getURLContent("https://api.mojang.com/users/profiles/minecraft/" + name);
            Gson g = new Gson();
            JsonObject obj = g.fromJson(result, JsonObject.class);
            String uid = obj.get("id").toString().replace("\"","");
            String signature = getURLContent("https://sessionserver.mojang.com/session/minecraft/profile/" + uid);
            obj = g.fromJson(signature, JsonObject.class);
            String value = obj.getAsJsonArray("properties").get(0).getAsJsonObject().get("value").getAsString();
            String decoded = new String(Base64.getDecoder().decode(value));
            obj = g.fromJson(decoded,JsonObject.class);
            String skinURL = obj.getAsJsonObject("textures").getAsJsonObject("SKIN").get("url").getAsString();
            byte[] skinByte = ("{\"textures\":{\"SKIN\":{\"url\":\"" + skinURL + "\"}}}").getBytes();
            return new String(Base64.getEncoder().encode(skinByte));
        } catch (Exception ignored){ }
        return null;
    }

    private static String getURLContent(String urlStr) {
        URL url;
        BufferedReader stream = null;
        StringBuilder builder = new StringBuilder();
        try {
            url = new URL(urlStr);
            stream = new BufferedReader(new InputStreamReader(url.openStream(), StandardCharsets.UTF_8));
            String str;
            while ((str = stream.readLine()) != null) {
                builder.append(str);
            }
        } catch (Exception ignored) {} finally {
            try {
                if (stream!=null) {
                    stream.close();
                }
            } catch(IOException ignored) {}
        }
        return builder.toString();
    }

    /*
    Important:
    - This MUST be run async, or the server will hang until the head skin texture is received!
    - If you need to retrieve the head often (like for a GUI), cache it, because of the rate limit.
     */
    public static ItemStack getHead(Player player) {
        String value = getHeadValue(player.getName());
        value = value == null ? "" : value;
        return getHead(value);
    }

}
