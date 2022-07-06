package com.runicrealms.plugin.database;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import com.runicrealms.plugin.CityLocation;
import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.database.util.DatabaseUtil;
import com.runicrealms.plugin.model.CharacterData;
import com.runicrealms.plugin.player.utilities.HealthUtils;
import com.runicrealms.plugin.utilities.HearthstoneItemUtil;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.UUID;

/**
 * The singleton database manager responsible for creating the connection to mongo and loading documents
 * into memory for lookup
 */
public class DatabaseManager {

    private MongoDatabase playersDB;
    private final HashMap<String, Document> playerDataLastMonth; // keyed by uuid
    private MongoCollection<Document> guild_data;
    private MongoCollection<Document> shop_data;

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
            for (String uuid : playerDataLastMonth.keySet()) {
                Bukkit.getLogger().info(uuid + " is uuid of document in map");
            }
            guild_data = playersDB.getCollection("guild_data");
            shop_data = playersDB.getCollection("shop_data");
        } catch (Exception e) {
            RunicCore.getInstance().getLogger().info("[ERROR]: Database connection failed!");
        }
    }

    /**
     * WARNING: should only be called AFTER checking if the document is in the collection using 'isInCollection'
     *
     * @param uuid of the player to lookup
     * @return the document (if found, older than 30 days) or null
     */
    public Document retrieveDocumentFromCollection(UUID uuid) {
        Document document = playersDB.getCollection("player_data").find
                (Filters.eq("player_uuid", uuid.toString())).limit(1).first();
        if (document != null) {
            this.playerDataLastMonth.put(String.valueOf(uuid), document);
        }
        return document;
    }

    /**
     * Adds a new mongo document (new players) and puts it into the lookup map in memory
     *
     * @param uuid of the player to add, string
     * @return the newly-added document
     */
    public Document addNewDocument(String uuid) {
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
    public Document addNewDocument(UUID uuid) {
        Document newDataFile = new Document("player_uuid", uuid.toString()).append("guild", "None").append("last_login", LocalDate.now());
        playersDB.getCollection("player_data").insertOne(newDataFile);
        playerDataLastMonth.put(uuid.toString(), newDataFile);
        return newDataFile;
    }

    /**
     * Attempts to populate the document for a new character slot with default values
     *
     * @param player    who created a new character
     * @param className the name of the class
     * @param slot      the slot of the character
     */
    public CharacterData addNewCharacter(Player player, String className, Integer slot) {
        PlayerMongoData playerMongoData = new PlayerMongoData(player.getUniqueId().toString());
        MongoDataSection mongoDataSection = playerMongoData.getSection("character." + slot);
        mongoDataSection.set("class.name", className);
        mongoDataSection.set("class.level", 0);
        mongoDataSection.set("class.exp", 0);
        mongoDataSection.set("prof.name", "None");
        mongoDataSection.set("prof.level", 0);
        mongoDataSection.set("prof.exp", 0);
        mongoDataSection.set("currentHP", HealthUtils.getBaseHealth());
        mongoDataSection.set("maxMana", RunicCore.getRegenManager().getBaseMana());
        mongoDataSection.set("storedHunger", 20);
        mongoDataSection.set("outlaw.enabled", false);
        mongoDataSection.set("outlaw.rating", RunicCore.getBaseOutlawRating());
        DatabaseUtil.saveLocation(playerMongoData.getCharacter(slot), CityLocation.getLocationFromItemStack(HearthstoneItemUtil.HEARTHSTONE_ITEMSTACK)); // tutorial
        playerMongoData.save();
        return new CharacterData(player, slot, playerMongoData);
    }

    /**
     * Checks the entire player_data collection for the given document.
     * (For use if player has not been loaded into data structure for last 30 days).
     * I believe there is a way to optimize this by just returning the cursor using limit()
     *
     * @param uuid of the player to lookup
     * @return true if the player is in the collection
     */
    public boolean isInCollection(UUID uuid) {
        return playersDB.getCollection("player_data").find
                (Filters.eq("player_uuid", uuid.toString())).limit(1).first() != null;
    }

    /**
     * Attempts to populate the document for given character and slot with basic values
     *
     * @param player who created a new character
     * @param slot   the slot of the character
     */
    public CharacterData loadCharacterData(Player player, Integer slot) {

        // Step 1: check if character data is cached in redis
        RunicCore.getRedisManager().checkRedisForCharacterData(player, slot);
        // todo: get the return value and load it from that
//            return;

        // Step 2: check mongo documents loaded in memory (last 30 days)
        // new CharacterData(player, slot, new PlayerMongoData(player.getUniqueId().toString()
        // todo: ADD TO REDIS (do we need to do this? think it'll get added after the event?)
        // Step 3: check entire mongo collection
        // todo: ADD TO REDIS (do we need to do this? think it'll get added after the event?)
        return new CharacterData(player, slot, new PlayerMongoData(player.getUniqueId().toString()));
    }

    /**
     * Builds a new database document for the given player if it doesn't already exist when they join server/lobby
     *
     * @param player who joined
     */
    public void tryCreateNewPlayer(Player player) {
        UUID uuid = player.getUniqueId();
        // Step 1: check if player data is cached in redis
        if (RunicCore.getRedisManager().checkRedisForPlayerData(player)) return;
        // Step 2: check mongo documents loaded in memory (last 30 days)
        if (RunicCore.getDatabaseManager().getPlayerDataLastMonth().containsKey(uuid.toString())) return;
        // Step 3: check entire mongo collection
        if (RunicCore.getDatabaseManager().getPlayersDB().getCollection("player_data").find
                (Filters.eq("player_uuid", uuid.toString())).limit(1).first() != null)
            return;
        // Step 4: if no data is found, we create some data, add it to mongo, then store a reference in memory
        RunicCore.getDatabaseManager().addNewDocument(uuid);
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
