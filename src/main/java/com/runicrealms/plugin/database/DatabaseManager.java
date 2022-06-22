package com.runicrealms.plugin.database;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.*;
import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.database.util.DatabaseUtil;
import org.bson.Document;
import org.bukkit.Bukkit;

import java.util.ArrayList;

public class DatabaseManager {

    private MongoDatabase playersDB;
    private FindIterable<Document> player_data_last_30_days;
    private MongoCollection<Document> player_data;
    private MongoCollection<Document> guild_data;
    private MongoCollection<Document> shop_data;

    // TODO: DON'T read the whole collection. Instead, add the 'played in last 30 days' field, then read those into Redis on startup. (store by player uuid)
    // TODO: Then, figure out how to write to mongo from redis, and only do it on shutdown.
    // TODO: use the periodic timer to update redis
    // TODO: when player logs in, check redis first, then make a read operation to mongo if redis comes up empty.
    public DatabaseManager() {

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
            player_data = playersDB.getCollection("player_data");
            player_data_last_30_days = playersDB.getCollection("player_data").find(DatabaseUtil.LAST_LOGIN_DATE_FILTER);
            ArrayList<Document> testArray = new ArrayList<>();
            player_data_last_30_days.into(testArray);
            Bukkit.getLogger().info(testArray.size() + " is the size!");
            guild_data = playersDB.getCollection("guild_data");
            shop_data = playersDB.getCollection("shop_data");
        } catch (Exception e) {
            RunicCore.getInstance().getLogger().info("[ERROR]: Database connection failed!");
        }
    }

    public MongoDatabase getPlayersDB() {
        return playersDB;
    }

    public MongoCollection<Document> getPlayerData() {
        return player_data;
    }

    public MongoCollection<Document> getGuildData() {
        return guild_data;
    }

    public MongoCollection<Document> getShopData() {
        return shop_data;
    }
}
