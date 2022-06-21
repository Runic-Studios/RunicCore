package com.runicrealms.plugin.database;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.*;
import com.runicrealms.plugin.RunicCore;
import org.bson.Document;
import org.bukkit.Bukkit;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class DatabaseManager {

    private MongoDatabase playersDB;
    private FindIterable<Document> player_data_last_30_days;
    private MongoCollection<Document> player_data;
    private MongoCollection<Document> guild_data;
    private MongoCollection<Document> shop_data;

    // TODO: DON'T read the whole collection. Instead, add the 'played in last 30 days' field, then read those into REDIS.
    // TODO: Then, figure out how to write to mongo from redis, and only do it on shutdown.
    // TODO: use the periodic timer to update redis
    // TODO: when player logs in, check redis first
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
//            for (Document d : playersDB.getCollection("player_data").find(dateFilter)) {
//                Bukkit.broadcastMessage(d.getString("player_uuid"));
//                Bukkit.broadcastMessage("document found");
//            }
            player_data = playersDB.getCollection("player_data");
            guild_data = playersDB.getCollection("guild_data");
            shop_data = playersDB.getCollection("shop_data");
        } catch (Exception e) {
            RunicCore.getInstance().getLogger().info("[ERROR]: Database connection failed!");
        }
        try {
            Bukkit.broadcastMessage(DATE_ONE_MONTH_AGO + " is one month ago date");
//            Bson dateFilter = Filters.lte("last_login", DATE_ONE_MONTH_AGO);
//            player_data_last_30_days = playersDB.getCollection("player_data").find(dateFilter);
//            player_data_last_30_days.forEach((Consumer<? super Document>) document -> Bukkit.broadcastMessage("IDK"));
//            MongoCursor<Document> iterator = player_data_last_30_days.iterator();
//            while (iterator.hasNext()) {
//                Document doc = iterator.next();
//                Bukkit.broadcastMessage(iterator.next() + " yep");
//            }
        } catch (Exception e) {
            Bukkit.broadcastMessage("idk");
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

    private static final String DATE_ONE_MONTH_AGO;

    static {
//        LocalDate pastMonth = LocalDate                       // Represent a date-only value, without time-of-day and without time zone.
//                .now(                           // Capture today's date as seen in the wall-clock time used by the people of a particular region (a time zone).
//                        ZoneId.of("-05:00")   // Specify the desired/expected zone. (EST)
//                )                               // Returns a `LocalDate` object.
//                .minus(                         // Subtract a span-of-time.
//                        Period.ofDays(30)         // Represent a span-of-time unattached to the timeline in terms of years-months-days.
//                );
        LocalDate localDate = LocalDate.now().minusDays(30); // TODO: static variable name
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd");
        DATE_ONE_MONTH_AGO = localDate.format(formatter);
    }

}
