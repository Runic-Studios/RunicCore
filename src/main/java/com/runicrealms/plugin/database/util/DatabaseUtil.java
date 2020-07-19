package com.runicrealms.plugin.database.util;

import com.runicrealms.plugin.database.PlayerMongoDataSection;
import com.runicrealms.plugin.item.hearthstone.HearthstoneListener;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.libs.org.apache.commons.io.output.ByteArrayOutputStream;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.ByteArrayInputStream;
import java.io.EOFException;
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
            exception.printStackTrace();
        }
        return new ItemStack[41]; // That is bad! todo: if they load a blank inv then logout, they lose data. re-write this.
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
                } catch (EOFException exception) {
                    break;
                } catch (IOException exception) {
                    exception.printStackTrace();
                    break;
                }// This shouldn't happen!

            }
            dataInput.close();
            return contents;
        } catch (EOFException exception) {
            Bukkit.getServer().getLogger().info(ChatColor.RED + "EOFException encountered");
            exception.printStackTrace();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return new ItemStack[invSize]; // That is bad! todo: if they load a blank inv then logout, they lose data. re-write this.
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

    public static Location loadLocation(Player player, PlayerMongoDataSection mongoDataSection) {
        try {
            World world = Bukkit.getWorld(mongoDataSection.get(("loc.world"), String.class));
            double x = mongoDataSection.get("loc.x", double.class);
            double y = mongoDataSection.get("loc.y", double.class);
            double z = mongoDataSection.get("loc.z", double.class);
            float yaw = Float.parseFloat(String.valueOf(mongoDataSection.get("loc.yaw", int.class)));
            float pitch = Float.parseFloat(String.valueOf(mongoDataSection.get("loc.pitch", int.class)));
            return new Location(world, x, y, z, yaw, pitch);
        } catch (Exception e) {
            // return hearth location
            e.printStackTrace();
            return HearthstoneListener.getHearthstoneLocation(player);
        }
    }

    /**
     * This method saves the player's location as a document
     * @param mongoDataSection the character section of player data
     * @param location the location of the player cache (or hearthstone location)
     */
    public static void saveLocation(PlayerMongoDataSection mongoDataSection, Location location) {
        try {
            mongoDataSection.set("loc.world", location.getWorld().getName());
            mongoDataSection.set("loc.x", location.getX());
            mongoDataSection.set("loc.y", location.getY());
            mongoDataSection.set("loc.z", location.getZ());
            mongoDataSection.set("loc.yaw", (int) location.getYaw());
            mongoDataSection.set("loc.pitch", (int) location.getPitch());
        } catch (Exception e) {
            Bukkit.getLogger().info(ChatColor.RED + "Save location method encountered an exception!");
            e.printStackTrace();
        }
    }
}
