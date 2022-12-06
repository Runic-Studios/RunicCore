package com.runicrealms.plugin.database;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.WriteConcern;
import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import com.runicrealms.plugin.CityLocation;
import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.api.Pair;
import com.runicrealms.plugin.character.api.CharacterHasQuitEvent;
import com.runicrealms.plugin.character.api.CharacterLoadedEvent;
import com.runicrealms.plugin.character.api.CharacterQuitEvent;
import com.runicrealms.plugin.classes.ClassEnum;
import com.runicrealms.plugin.database.event.MongoSaveEvent;
import com.runicrealms.plugin.model.CharacterData;
import com.runicrealms.plugin.model.PlayerData;
import com.runicrealms.plugin.player.RegenManager;
import com.runicrealms.plugin.player.utilities.HealthUtils;
import com.runicrealms.plugin.utilities.HearthstoneItemUtil;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import redis.clients.jedis.Jedis;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * The singleton database manager responsible for creating the connection to mongo and loading documents
 * into memory for lookup
 */
public class DatabaseManager implements Listener {

    private final Map<UUID, ShutdownSaveWrapper> playersToSave; // for redis-mongo saving
    private final HashMap<String, Document> playerDocumentMap; // keyed by uuid (as string) (stores last 30 days)
    private final ConcurrentHashMap<UUID, Pair<Integer, ClassEnum>> loadedCharacterMap;
    private final Map<UUID, PlayerData> playerDataMap;
    private MongoClient mongoClient;
    private MongoDatabase playersDB;
    private MongoCollection<Document> guildDocuments;
    private MongoCollection<Document> shopDocuments;

    public DatabaseManager() {

        Bukkit.getServer().getPluginManager().registerEvents(this, RunicCore.getInstance());
        playersToSave = new HashMap<>();
        playerDocumentMap = new HashMap<>();
        loadedCharacterMap = new ConcurrentHashMap<>();
        playerDataMap = new HashMap<>();

        // Connect to MongoDB database (Atlas)
        ConnectionString connString = new ConnectionString(
                "mongodb+srv://RunicCore:vggRBvA1MjNEw4pE@cluster0-mf2re.mongodb.net/test?retryWrites=true&w=majority"
        );
        MongoClientSettings mongoClientSettings = MongoClientSettings.builder()
                .applyToSocketSettings(builder -> builder.connectTimeout(30, TimeUnit.SECONDS))
                .applyConnectionString(connString)
                .retryWrites(true)
                .writeConcern(WriteConcern.W2)
                .build();

        // create a client (keep it open / alive)
        try {
            mongoClient = MongoClients.create(mongoClientSettings);
            playersDB = mongoClient.getDatabase(RunicCore.getInstance().getConfig().getString("database"));
            FindIterable<Document> player_data_last_30_days = playersDB.getCollection("player_data").find(DatabaseHelper.LAST_LOGIN_DATE_FILTER);
            for (Document document : player_data_last_30_days) {
                playerDocumentMap.put(String.valueOf(document.get("player_uuid")), document);
            }
            guildDocuments = playersDB.getCollection("guild_data");
            shopDocuments = playersDB.getCollection("shop_data");
        } catch (Exception e) {
            RunicCore.getInstance().getLogger().info("[ERROR]: Database connection failed!");
        }
    }

    /**
     * Attempts to populate the document for a new character slot with default values
     *
     * @param player    who created a new character
     * @param className the name of the class
     * @param slot      the slot of the character
     */
    public CharacterData addNewCharacter(Player player, String className, Integer slot, Jedis jedis) {
        PlayerMongoData playerMongoData = new PlayerMongoData(player.getUniqueId().toString());
        MongoDataSection mongoDataSection = playerMongoData.getSection("character." + slot);
        mongoDataSection.set("class.name", className);
        mongoDataSection.set("class.level", 0);
        mongoDataSection.set("class.exp", 0);
        mongoDataSection.set("prof.name", "None");
        mongoDataSection.set("prof.level", 0);
        mongoDataSection.set("prof.exp", 0);
        mongoDataSection.set("currentHp", HealthUtils.getBaseHealth());
        mongoDataSection.set("maxMana", RegenManager.getBaseMana());
        mongoDataSection.set("storedHunger", 20);
        mongoDataSection.set("outlaw.enabled", false);
        mongoDataSection.set("outlaw.rating", RunicCore.getBaseOutlawRating());
        DatabaseHelper.saveLocation(playerMongoData.getCharacter(slot), CityLocation.getLocationFromItemStack(HearthstoneItemUtil.HEARTHSTONE_ITEMSTACK)); // tutorial
        playerMongoData.save();
        return new CharacterData(player.getUniqueId(), slot, playerMongoData, jedis);
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
        playerDocumentMap.put(uuid, newDataFile);
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
        playerDocumentMap.put(uuid.toString(), newDataFile);
        return newDataFile;
    }

    public MongoCollection<Document> getGuildDocuments() {
        return guildDocuments;
    }

    public ConcurrentHashMap.KeySetView<UUID, Pair<Integer, ClassEnum>> getLoadedCharacters() {
        return loadedCharacterMap.keySet();
    }

    public ConcurrentHashMap<UUID, Pair<Integer, ClassEnum>> getLoadedCharactersMap() {
        return loadedCharacterMap;
    }

    public MongoClient getMongoClient() {
        return mongoClient;
    }

    public Map<UUID, PlayerData> getPlayerDataMap() {
        return playerDataMap;
    }

    public HashMap<String, Document> getPlayerDocumentMap() {
        return playerDocumentMap;
    }

    public MongoDatabase getPlayersDB() {
        return playersDB;
    }

    public Map<UUID, ShutdownSaveWrapper> getPlayersToSave() {
        return playersToSave;
    }

    public MongoCollection<Document> getShopData() {
        return shopDocuments;
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
     * Creates a CharacterData object. Tries to build it from session storage (Redis) first,
     * then falls back to Mongo
     *
     * @param uuid of player who is attempting to load their data
     * @param slot the slot of the character
     */
    public CharacterData loadCharacterData(UUID uuid, Integer slot, Jedis jedis) {
        // Step 1: check if character data is cached in redis
        CharacterData characterData = RunicCore.getRedisManager().checkRedisForCharacterData(uuid, slot, jedis);
        if (characterData != null) return characterData;
        // Step 2: check mongo documents
        return new CharacterData(uuid, slot, new PlayerMongoData(uuid.toString()), jedis);
    }

    /**
     * Builds a new database document for the given player if it doesn't already exist when they join server/lobby
     *
     * @param player who joined
     * @return a PlayerData object
     */
    public PlayerData loadPlayerData(Player player, Jedis jedis) {
        // Step 1: check mongo documents loaded in memory (last 30 days)
        UUID uuid = player.getUniqueId();
        if (RunicCore.getDatabaseManager().getPlayerDocumentMap().containsKey(uuid.toString()))
            return new PlayerData(player, new PlayerMongoData(uuid.toString()), jedis);
        // Step 2: check entire mongo collection
        if (RunicCore.getDatabaseManager().getPlayersDB().getCollection("player_data").find
                (Filters.eq("player_uuid", uuid.toString())).limit(1).first() != null)
            return new PlayerData(player, new PlayerMongoData(uuid.toString()), jedis);
        // Step 3: if no data is found, we create some data, add it to mongo, then store a reference in memory
        RunicCore.getDatabaseManager().addNewDocument(uuid);
        return new PlayerData(player, new PlayerMongoData(uuid.toString()), jedis);
    }

    /**
     * IMPORTANT: performs all necessary data cleanup after player is loaded
     */
    @EventHandler
    public void onCharacterLoaded(CharacterLoadedEvent event) {
        event.getCharacterSelectEvent().close(); // close all jedis resources
        RunicCore.getDatabaseManager().getPlayerDataMap().remove(event.getPlayer().getUniqueId()); // remove intermediary data objects
    }

    /**
     * Synchronously call an event when the player has finished saving their quit data.
     * Allows player to log back in
     */
    @EventHandler(priority = EventPriority.HIGHEST) // last thing that runs
    public void onCharacterQuitFinished(CharacterQuitEvent event) {
        loadedCharacterMap.remove(event.getPlayer().getUniqueId());
        Bukkit.getScheduler().runTaskLater(RunicCore.getInstance(),
                () -> {
                    CharacterHasQuitEvent characterHasQuitEvent = new CharacterHasQuitEvent(event.getPlayer(), event);
                    Bukkit.getPluginManager().callEvent(characterHasQuitEvent); // inform plugins that character has finished data save!
                }, 1L);
    }

    /**
     * Saves all character-related core data (class, professions, etc.) on server shutdown
     * for EACH alt the player has used during the runtime of this server.
     * Works even if the player is now entirely offline
     */
    @EventHandler
    public void onDatabaseSave(MongoSaveEvent event) {
        for (UUID uuid : event.getPlayersToSave().keySet()) {
            for (int characterSlot : event.getPlayersToSave().get(uuid).getCharactersToSave()) {
                PlayerMongoData playerMongoData = event.getPlayersToSave().get(uuid).getPlayerMongoData();
                playerMongoData.set("last_login", LocalDate.now());
                saveCharacter(uuid, playerMongoData, characterSlot, event.getJedis());
            }
        }
    }

    /**
     * Call our custom character quit event
     */
    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        if (loadedCharacterMap.get(event.getPlayer().getUniqueId()) == null) return;
        CharacterQuitEvent characterQuitEvent = new CharacterQuitEvent
                (
                        event.getPlayer(),
                        loadedCharacterMap.get(event.getPlayer().getUniqueId()).first
                );
        Bukkit.getPluginManager().callEvent(characterQuitEvent);
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
            this.playerDocumentMap.put(String.valueOf(uuid), document);
        }
        return document;
    }

    /**
     * Method to build a CharacterData object for the given uuid, then writes that data to mongo
     *
     * @param uuid            of the player to save
     * @param playerMongoData of the mongo save event
     * @param slot            of the character to save
     * @param jedis           the jedis resource
     */
    public void saveCharacter(UUID uuid, PlayerMongoData playerMongoData, int slot, Jedis jedis) {
        CharacterData characterData;
        if (jedis.exists(uuid + ":character:" + slot)) {
            // Bukkit.broadcastMessage(ChatColor.AQUA + "redis character data found, saving to mongo");
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                RunicCore.getRedisManager().updateBaseCharacterInfo(player, slot, jedis); // ensure jedis is up-to-date if player is online
            }
            characterData = new CharacterData(uuid, slot, jedis); // build a data object
            characterData.writeCharacterDataToMongo(playerMongoData, slot);
        } else {
            // log error
        }
    }

    /**
     * Keeps the in-memory document consistent with the mongo disk.
     * Updates a single player
     *
     * @param uuid     of the player to update
     * @param document the updated document to replace the old
     */
    public void updateDocument(UUID uuid, Document document) {
        playerDocumentMap.put(uuid.toString(), document);
    }
}
