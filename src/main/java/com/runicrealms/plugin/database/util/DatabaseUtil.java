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

    /**
     * Loads inventory from JSON object into memory
     * @String encoded inventory data
     */
    public static ItemStack[] loadInventory(String encoded) {
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(encoded));
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

        }
        return new ItemStack[41]; // That is bad!
    }

    public static ItemStack[] loadInventory(String encoded, int invSize) {
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(encoded));
            BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
            ItemStack[] contents = new ItemStack[invSize];
            for (int i = 0; i < invSize; i++) {
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

        }
        return new ItemStack[invSize]; // That is bad!
    }

    public static Location loadLocation(String encoded) {
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(encoded));
            BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
            Location location = (Location) dataInput.readObject();
            dataInput.close();
            return location;
        } catch (Exception exception) {

        }
        return new Location(Bukkit.getWorld("Alterra"), -2317.5, 38.5, 1719.5); // That is bad!
    }

    public static String serializeInventory(Inventory inventory) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
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

    public static String serializeInventory(ItemStack[] contents) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);
            for (int i = 0; i < contents.length; i++) {
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
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
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
