package com.runicrealms.plugin.database.util;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.libs.org.apache.commons.io.output.ByteArrayOutputStream;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.ByteArrayInputStream;
import java.io.IOException;

public class DatabaseUtil {

    //        // example player
//        Document newDataFile = new Document("player_uuid", "2343243243243")
//                .append("bank", new Document("inv", "some inv"))
//                .append("guild", "Sister_Slayers")
//                .append("trade_market", "some trade market")
//                .append("character", new Document("1", new Document("quests", "quest_data"))
//                        .append("2", "test 2"));
//
//        player_data.insertOne(newDataFile);

    /**
     * Loads inventory from JSON object into memory
     * @String encoded inventory data
     */
    public static ItemStack[] loadInventory(String encoded) {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(encoded));
        try {
            BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
            ItemStack[] contents = new ItemStack[41];
            for (int i = 0; i < 41; i++) {
                try {
                    int next = dataInput.readInt();
                    if (next != -1) {
                        contents[next] = (ItemStack) dataInput.readObject();
                    } else {
                        break;
                    }
                } catch (IOException exception) {
                    break;
                }// This shouldn't happen!

            }
            dataInput.close();
            return contents;
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return new ItemStack[41]; // That is bad!
    }

    public static Location loadLocation(String encoded) {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(encoded));
        try {
            BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
            Location location = (Location) dataInput.readObject();
            dataInput.close();
            return location;
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return new Location(Bukkit.getWorld("Alterra"), -2317.5, 38.5, 1719.5); // That is bad!
    }

    public static String serializeInventory(Inventory inventory) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);
            ItemStack[] contents = inventory.getContents();
            for (int i = 0; i < 41; i++) {
                if (contents[i] != null) {
                    dataOutput.writeInt(i);
                    dataOutput.writeObject(contents[i]);
                }
            }
            dataOutput.close();
            return Base64Coder.encodeLines(outputStream.toByteArray());
        } catch (IOException exception) {
            exception.printStackTrace();
        }
        return null;
    }

    /**
     * Converts a player's location to a format we can store in JSON objects
     */
    public static String serializeLocation(Location location) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);
            dataOutput.writeObject(location);
            dataOutput.close();
            return Base64Coder.encodeLines(outputStream.toByteArray());
        } catch (IOException exception) {
            exception.printStackTrace();
        }
        return null;
    }
}
