package com.runicrealms.plugin.database;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.*;
import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.database.util.DatabaseUtil;
import org.bson.Document;
import org.bukkit.Bukkit;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.UUID;

/**
 *
 */
public class DatabaseManager {

    private MongoDatabase playersDB;
    private final HashMap<String, Document> playerDataLastMonth; // keyed by uuid
    private MongoCollection<Document> guild_data;
    private MongoCollection<Document> shop_data;

    /**
     *
     */
    public DatabaseManager() {

        playerDataLastMonth = new HashMap<>();

        // Connect to MongoDB database (Atlas)
        ConnectionString connString = new ConnectionString(
                "mongodb+srv://RunicCore:vggRBvA1MjNEw4pE@cluster0-mf2re.mongodb.net/test?retryWrites=true&w=majority"
        );
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(connString)
                .retryWrites(true)
                .build();

        // create a client
        try {
            MongoClient mongoClient = MongoClients.create(settings);
            playersDB = mongoClient.getDatabase(RunicCore.getInstance().getConfig().getString("database"));
            FindIterable<Document> player_data_last_30_days = playersDB.getCollection("player_data").find(DatabaseUtil.LAST_LOGIN_DATE_FILTER);
            for (Document document : player_data_last_30_days) {
                playerDataLastMonth.put(String.valueOf(document.get("player_uuid")), document);
            }
            Bukkit.getLogger().info(playerDataLastMonth.size() + " is the size of the map!");
            guild_data = playersDB.getCollection("guild_data");
            shop_data = playersDB.getCollection("shop_data");
        } catch (Exception e) {
            RunicCore.getInstance().getLogger().info("[ERROR]: Database connection failed!");
        }
    }

    /**
     * Adds a new mongo document (new players) and puts it into the lookup map in memory
     *
     * @param uuid of the player to add, string
     * @return the newly-added document
     */
    public Document addDocument(String uuid) {
        Document newDataFile = new Document("player_uuid", uuid).append("guild", "None").append("last_login", LocalDate.now());
        playersDB.getCollection("player_data").insertOne(newDataFile);
        playerDataLastMonth.put(uuid, newDataFile);
        return newDataFile;
    }

    /**
     * Adds a new mongo document (new players) and puts it into the lookup map in memory
     *
     * @param uuid of the player to add
     * @return the newly-added document
     */
    public Document addDocument(UUID uuid) {
        Document newDataFile = new Document("player_uuid", uuid.toString()).append("guild", "None").append("last_login", LocalDate.now());
        playersDB.getCollection("player_data").insertOne(newDataFile);
        playerDataLastMonth.put(uuid.toString(), newDataFile);
        return newDataFile;
    }

    public MongoDatabase getPlayersDB() {
        return playersDB;
    }

    public HashMap<String, Document> getPlayerDataLastMonth() {
        return playerDataLastMonth;
    }

    public MongoCollection<Document> getGuildData() {
        return guild_data;
    }

    public MongoCollection<Document> getShopData() {
        return shop_data;
    }
}
