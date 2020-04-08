package com.runicrealms.plugin.database.util;

import com.runicrealms.runiccharacters.config.UserConfig;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.libs.org.apache.commons.io.output.ByteArrayOutputStream;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

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
     * @param userConfig from RunicCharacters
     */
    public static ItemStack[] loadInventory(UserConfig userConfig) {
//        ItemStack[] contents = new ItemStack[41];
//        try {
//            Player pl = userConfig.getPlayer();
//            Document playerFile = RunicCore.getDatabaseManager().getAPI().getPlayerFile("2343243243243");//pl.getUniqueId().toString()
//            Document playerCharacter = RunicCore.getDatabaseManager().getAPI().getCharacterAPI().getCharacter(playerFile, 1);
//            String serialized = playerCharacter.getString("inventory");
//            YamlConfiguration restoreInv = new YamlConfiguration();
//            restoreInv.loadFromString(serialized);
//            for (int i = 0; i < 41; i++) {
//                ItemStack restored = restoreInv.getItemStack(String.valueOf(i));
//                if (restored != null) {
//                    contents[i] = restored;
//                }
//            }
//            return contents;
//        } catch (Exception e) {
            return new ItemStack[41];
//        }
    }

    public static Location loadLocation(UserConfig userConfig) {
//        try {
//            Player pl = userConfig.getPlayer();
//            Document playerFile = RunicCore.getDatabaseManager().getAPI().getPlayerFile("2343243243243");//pl.getUniqueId().toString()
//            Document playerCharacter = RunicCore.getDatabaseManager().getAPI().getCharacterAPI().getCharacter(playerFile, 1);
//            String serialized = playerCharacter.getString("location");
//            YamlConfiguration restoreLoc = new YamlConfiguration();
//            restoreLoc.loadFromString(serialized);
//            return restoreLoc.getLocation("loc");
//        } catch (Exception e) {
//            e.printStackTrace();
//            Bukkit.broadcastMessage("something went wrong");
            return new Location(Bukkit.getWorld("Alterra"), -2317.5, 38.5, 1719.5);
//        }
    }

    /**
     * Converts a player's inventory to a format we can store in JSON objects
     */
    public static String serializedInventory(Inventory inv) {
        ItemStack[] contents = inv.getContents();
        YamlConfiguration invConfig = new YamlConfiguration();
        for (int i = 0; i < inv.getSize(); i++) {
            if (contents[i] == null) continue;
            ItemStack is = contents[i];
            invConfig.set(String.valueOf(i), is);
        }
        return invConfig.saveToString();
    }

    private static String serializeItemStack(ItemStack item) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);
            dataOutput.writeObject(item);
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
    public static String serializedLocation(Location loc) {
        YamlConfiguration locConfig = new YamlConfiguration();
        locConfig.set("loc", loc);
        return locConfig.saveToString();
    }
}
