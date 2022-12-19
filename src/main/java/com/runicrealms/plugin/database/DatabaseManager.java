package com.runicrealms.plugin.database;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.WriteConcern;
import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.api.CharacterAPI;
import com.runicrealms.plugin.api.DataAPI;
import com.runicrealms.plugin.api.Pair;
import com.runicrealms.plugin.character.api.CharacterHasQuitEvent;
import com.runicrealms.plugin.character.api.CharacterLoadedEvent;
import com.runicrealms.plugin.character.api.CharacterQuitEvent;
import com.runicrealms.plugin.classes.CharacterClass;
import com.runicrealms.plugin.database.event.MongoSaveEvent;
import com.runicrealms.plugin.model.CharacterData;
import com.runicrealms.plugin.model.CharacterField;
import com.runicrealms.plugin.model.PlayerData;
import com.runicrealms.runicrestart.RunicRestart;
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
public class DatabaseManager implements CharacterAPI, DataAPI, Listener {

    private final Map<UUID, ShutdownSaveWrapper> playersToSave; // for redis-mongo saving
    private final HashMap<String, Document> playerDocumentMap; // keyed by uuid (as string) (stores last 30 days)
    private final ConcurrentHashMap<UUID, Pair<Integer, CharacterClass>> loadedCharacterMap;
    private final Map<UUID, PlayerData> playerDataMap;
    private MongoClient mongoClient;
    private MongoDatabase mongoDatabase;
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
            mongoDatabase = mongoClient.getDatabase(RunicCore.getInstance().getConfig().getString("database"));
            FindIterable<Document> player_data_last_30_days = mongoDatabase.getCollection("player_data").find(DatabaseHelper.LAST_LOGIN_DATE_FILTER);
            for (Document document : player_data_last_30_days) {
                playerDocumentMap.put(String.valueOf(document.get("player_uuid")), document);
            }
            guildDocuments = mongoDatabase.getCollection("guild_data");
            shopDocuments = mongoDatabase.getCollection("shop_data");
        } catch (Exception e) {
            RunicCore.getInstance().getLogger().info("[ERROR]: Database connection failed!");
        }
    }

    @Override
    public Document addNewDocument(String uuid) {
        Document newDataFile = new Document("player_uuid", uuid).append("guild", "None").append("last_login", LocalDate.now());
        mongoDatabase.getCollection("player_data").insertOne(newDataFile);
        playerDocumentMap.put(uuid, newDataFile);
        return newDataFile;
    }

    @Override
    public CharacterData checkRedisForCharacterData(UUID uuid, Integer slot, Jedis jedis) {
        String key = uuid + ":character:" + slot;
        if (jedis.exists(key)) {
            jedis.expire(key, RunicCore.getRedisAPI().getExpireTime());
            return new CharacterData(uuid, slot, jedis);
        }
        return null;
    }

    @Override
    public MongoCollection<Document> getGuildDocuments() {
        return guildDocuments;
    }

    @Override
    public MongoDatabase getMongoDatabase() {
        return mongoDatabase;
    }

    @Override
    public PlayerData getPlayerData(UUID uuid) {
        return playerDataMap.get(uuid);
    }

    @Override
    public Map<UUID, PlayerData> getPlayerDataMap() {
        return playerDataMap;
    }

    @Override
    public HashMap<String, Document> getPlayerDocumentMap() {
        return playerDocumentMap;
    }

    @Override
    public Map<UUID, ShutdownSaveWrapper> getPlayersToSave() {
        return playersToSave;
    }

    @Override
    public boolean isInCollection(String uuid) {
        return mongoDatabase.getCollection("player_data").find
                (Filters.eq("player_uuid", uuid)).limit(1).first() != null;
    }

    /**
     * Builds a new database document for the given player if it doesn't already exist when they join server/lobby
     *
     * @param player who joined
     * @return a PlayerData object
     */
    @Override
    public PlayerData loadPlayerData(Player player, Jedis jedis) {
        // Step 1: check mongo documents loaded in memory (last 30 days)
        UUID uuid = player.getUniqueId();
        if (playerDocumentMap.containsKey(uuid.toString()))
            return new PlayerData(player, new PlayerMongoData(uuid.toString()), jedis);
        // Step 2: check entire mongo collection
        if (mongoDatabase.getCollection("player_data").find
                (Filters.eq("player_uuid", uuid.toString())).limit(1).first() != null)
            return new PlayerData(player, new PlayerMongoData(uuid.toString()), jedis);
        // Step 3: if no data is found, we create some data, add it to mongo, then store a reference in memory
        this.addNewDocument(uuid.toString());
        return new PlayerData(player, new PlayerMongoData(uuid.toString()), jedis);
    }

    @Override
    public Document retrieveDocumentFromCollection(String uuid) {
        Document document = mongoDatabase.getCollection("player_data").find(Filters.eq("player_uuid", uuid)).limit(1).first();
        if (document != null) {
            this.playerDocumentMap.put(uuid, document);
        }
        return document;
    }

    @Override
    public int getCharacterSlot(UUID uuid) {
        Pair<Integer, CharacterClass> slotAndClass = loadedCharacterMap.get(uuid);
        if (slotAndClass != null) {
            return loadedCharacterMap.get(uuid).first;
        }
        return -1;
    }

    @Override
    public ConcurrentHashMap.KeySetView<UUID, Pair<Integer, CharacterClass>> getLoadedCharacters() {
        return loadedCharacterMap.keySet();
    }

    @Override
    public String getPlayerClass(Player player) {
        return loadedCharacterMap.get(player.getUniqueId()).second.getName();
    }

    @Override
    public String getPlayerClass(UUID uuid) {
        return loadedCharacterMap.get(uuid).second.getName();
    }

    @Override
    public String getPlayerClass(UUID uuid, int slot, Jedis jedis) {
        // If player class is not cached, player is offline
        String key = RunicCore.getRedisAPI().getCharacterKey(uuid, slot);
        if (jedis.exists(key))
            return jedis.hmget(key, CharacterField.CLASS_TYPE.getField()).get(0);
        else
            return null;
    }

    public ConcurrentHashMap<UUID, Pair<Integer, CharacterClass>> getLoadedCharactersMap() {
        return loadedCharacterMap;
    }

    public MongoClient getMongoClient() {
        return mongoClient;
    }

    public MongoCollection<Document> getShopData() {
        return shopDocuments;
    }

    /**
     * IMPORTANT: performs all necessary data cleanup after player is loaded
     */
    @EventHandler
    public void onCharacterLoaded(CharacterLoadedEvent event) {
        event.getCharacterSelectEvent().close(); // close all jedis resources
        playerDataMap.remove(event.getPlayer().getUniqueId()); // remove intermediary data objects
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
     * Call our custom character quit event, sync or async depending on context
     */
    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        if (loadedCharacterMap.get(event.getPlayer().getUniqueId()) == null) return;
        boolean isAsync = !RunicRestart.getAPI().isShuttingDown();
        if (isAsync) {
            Bukkit.getScheduler().runTaskAsynchronously(RunicCore.getInstance(),
                    () -> Bukkit.getPluginManager().callEvent(new CharacterQuitEvent
                            (
                                    event.getPlayer(),
                                    loadedCharacterMap.get(event.getPlayer().getUniqueId()).first,
                                    true
                            )));
        } else {
            Bukkit.getScheduler().runTask(RunicCore.getInstance(),
                    () -> Bukkit.getPluginManager().callEvent(new CharacterQuitEvent
                            (
                                    event.getPlayer(),
                                    loadedCharacterMap.get(event.getPlayer().getUniqueId()).first,
                                    false
                            )));
        }
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
                RunicCore.getRedisAPI().updateBaseCharacterInfo(player, slot, jedis); // ensure jedis is up-to-date if player is online
            }
            characterData = new CharacterData(uuid, slot, jedis); // build a data object
            characterData.writeCharacterDataToMongo(playerMongoData, slot);
        } else {
            Bukkit.getLogger().warning("ERROR: There was an error saving character data!");
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
