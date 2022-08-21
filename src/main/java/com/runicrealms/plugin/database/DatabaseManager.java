package com.runicrealms.plugin.database;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import com.runicrealms.plugin.CityLocation;
import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.character.api.CharacterQuitEvent;
import com.runicrealms.plugin.database.event.CacheSaveReason;
import com.runicrealms.plugin.database.event.MongoSaveEvent;
import com.runicrealms.plugin.database.util.DatabaseUtil;
import com.runicrealms.plugin.model.CharacterData;
import com.runicrealms.plugin.model.PlayerData;
import com.runicrealms.plugin.player.RegenManager;
import com.runicrealms.plugin.player.utilities.HealthUtils;
import com.runicrealms.plugin.redis.RedisManager;
import com.runicrealms.plugin.utilities.HearthstoneItemUtil;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The singleton database manager responsible for creating the connection to mongo and loading documents
 * into memory for lookup
 */
public class DatabaseManager implements Listener {

    private MongoDatabase playersDB;
    private MongoCollection<Document> guildDocuments;
    private MongoCollection<Document> shopDocuments;
    private final Map<UUID, Set<Integer>> playersToSave; // for redis-mongo saving
    private final HashMap<String, Document> playerDocumentMap; // keyed by uuid (as string) (stores last 30 days)
    private final ConcurrentHashMap<UUID, Integer> loadedCharacterMap;
    private final Map<UUID, PlayerData> playerDataMap;

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
                playerDocumentMap.put(String.valueOf(document.get("player_uuid")), document);
            }
            Bukkit.getLogger().info(playerDocumentMap.size() + " is the size of the map!");
            for (String uuid : playerDocumentMap.keySet()) {
                Bukkit.getLogger().info(uuid + " is uuid of document in map");
            }
            guildDocuments = playersDB.getCollection("guild_data");
            shopDocuments = playersDB.getCollection("shop_data");
        } catch (Exception e) {
            RunicCore.getInstance().getLogger().info("[ERROR]: Database connection failed!");
        }
    }

    /**
     * Call our custom character quit event
     */
    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        if (loadedCharacterMap.get(e.getPlayer().getUniqueId()) == null) return;
        CharacterQuitEvent characterQuitEvent = new CharacterQuitEvent(e.getPlayer(), loadedCharacterMap.get(e.getPlayer().getUniqueId()));
        Bukkit.getPluginManager().callEvent(characterQuitEvent);
    }

    /**
     * Remove reference to loaded players on logout
     */
    @EventHandler(priority = EventPriority.HIGHEST) // last
    public void onCharacterQuit(CharacterQuitEvent event) {
        loadedCharacterMap.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.HIGHEST) // last
    public void onDatabaseSave(MongoSaveEvent event) {
        Bukkit.broadcastMessage("SAVING EVENT SLOT IS " + event.getSlot());
        saveCharacter(event.getUuid(), event.getMongoData(), event.getSlot());
        event.getMongoData().save();
        event.getMongoDataSection().save();
        // TODO: mark the data as being saved here!
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
        mongoDataSection.set("currentHp", HealthUtils.getBaseHealth());
        mongoDataSection.set("maxMana", RegenManager.getBaseMana());
        mongoDataSection.set("storedHunger", 20);
        mongoDataSection.set("outlaw.enabled", false);
        mongoDataSection.set("outlaw.rating", RunicCore.getBaseOutlawRating());
        DatabaseUtil.saveLocation(playerMongoData.getCharacter(slot), CityLocation.getLocationFromItemStack(HearthstoneItemUtil.HEARTHSTONE_ITEMSTACK)); // tutorial
        playerMongoData.save();
        return new CharacterData(player.getUniqueId(), slot, playerMongoData);
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
    public CharacterData loadCharacterData(UUID uuid, Integer slot) {
        // Step 1: check if character data is cached in redis
        CharacterData characterData = RunicCore.getRedisManager().checkRedisForCharacterData(uuid, slot);
        if (characterData != null) return characterData;
        // Step 2: check mongo documents
        return new CharacterData(uuid, slot, new PlayerMongoData(uuid.toString()));
    }

    /**
     * Builds a new database document for the given player if it doesn't already exist when they join server/lobby
     *
     * @param player who joined
     * @return a PlayerData object
     */
    public PlayerData loadPlayerData(Player player) {
        // Step 1: check mongo documents loaded in memory (last 30 days)
        UUID uuid = player.getUniqueId();
        if (RunicCore.getDatabaseManager().getPlayerDocumentMap().containsKey(uuid.toString()))
            return new PlayerData(player, new PlayerMongoData(uuid.toString()));
        // Step 2: check entire mongo collection
        if (RunicCore.getDatabaseManager().getPlayersDB().getCollection("player_data").find
                (Filters.eq("player_uuid", uuid.toString())).limit(1).first() != null)
            return new PlayerData(player, new PlayerMongoData(uuid.toString()));
        // Step 3: if no data is found, we create some data, add it to mongo, then store a reference in memory
        RunicCore.getDatabaseManager().addNewDocument(uuid);
        return new PlayerData(player, new PlayerMongoData(uuid.toString()));
    }

    /**
     * Method to build a CharacterData object for the given uuid, then writes that data to mongo
     *
     * @param uuid            of the player to save
     * @param playerMongoData of the mongo save event
     * @param slot            of the character to save
     */
    public void saveCharacter(UUID uuid, PlayerMongoData playerMongoData, int slot) {
        JedisPool jedisPool = RunicCore.getRedisManager().getJedisPool();
        CharacterData characterData;
        try (Jedis jedis = jedisPool.getResource()) { // try-with-resources to close the connection for us
            jedis.auth(RedisManager.REDIS_PASSWORD);
            if (jedis.exists(uuid + ":character:" + slot)) {
                Bukkit.broadcastMessage(ChatColor.AQUA + "redis character data found, saving to mongo");
                Player player = Bukkit.getPlayer(uuid);
                if (player != null) {
                    RunicCore.getRedisManager().updateBaseCharacterInfo(player, slot); // ensure jedis is up-to-date if player is online
                }
                characterData = new CharacterData(uuid, slot, jedis); // build a data object
                characterData.writeCharacterDataToMongo(playerMongoData, slot);
            } else {
                // log error
            }
        }
    }

    /**
     * Saves all characters to mongo who have logged in to this server (on any characters they've played)
     */
    public void saveAllCharacters() {
        JedisPool jedisPool = RunicCore.getRedisManager().getJedisPool();
        try (Jedis jedis = jedisPool.getResource()) { // try-with-resources to close the connection for us
            jedis.auth(RedisManager.REDIS_PASSWORD);
            for (UUID uuid : playersToSave.keySet()) {
                for (int slot : playersToSave.get(uuid)) {
                    if (jedis.exists(uuid + ":character:" + slot)) {
                        Bukkit.broadcastMessage(ChatColor.AQUA + "redis character data found, saving slot " + slot + " to mongo on shutdown");
                        PlayerMongoData playerMongoData = new PlayerMongoData(uuid.toString());
                        playerMongoData.set("last_login", LocalDate.now());
                        PlayerMongoDataSection character = playerMongoData.getCharacter(slot);
                        MongoSaveEvent mongoSaveEvent = new MongoSaveEvent(slot, uuid, playerMongoData, character, CacheSaveReason.SERVER_SHUTDOWN);
                        Bukkit.getPluginManager().callEvent(mongoSaveEvent);
                    } else {
                        Bukkit.getLogger().info(ChatColor.RED + "[ERROR]: There was a problem saving all redis data on shutdown!");
                    }
                }
            }
        }
        RunicCore.getDatabaseManager().getPlayersToSave().clear();
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

    public MongoDatabase getPlayersDB() {
        return playersDB;
    }

    public MongoCollection<Document> getGuildData() {
        return guildDocuments;
    }

    public MongoCollection<Document> getShopData() {
        return shopDocuments;
    }

    public Map<UUID, Set<Integer>> getPlayersToSave() {
        return playersToSave;
    }

    public HashMap<String, Document> getPlayerDocumentMap() {
        return playerDocumentMap;
    }

    public ConcurrentHashMap.KeySetView<UUID, Integer> getLoadedCharacters() {
        return loadedCharacterMap.keySet();
    }

    public ConcurrentHashMap<UUID, Integer> getLoadedCharactersMap() {
        return loadedCharacterMap;
    }

    public Map<UUID, PlayerData> getPlayerDataMap() {
        return playerDataMap;
    }
}
