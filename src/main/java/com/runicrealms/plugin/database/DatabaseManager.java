package com.runicrealms.plugin.database;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

public class DatabaseManager {

    private MongoDatabase playersDB;
    private MongoCollection<Document> player_data;
    private MongoCollection<Document> guild_data;

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
        MongoClient mongoClient = MongoClients.create(settings);
        playersDB = mongoClient.getDatabase("players");
        player_data = playersDB.getCollection("player_data");
        guild_data = playersDB.getCollection("guild_data");
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

}
