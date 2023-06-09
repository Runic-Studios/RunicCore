package com.runicrealms.plugin.database;

import co.aikar.taskchain.TaskChain;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.api.CoreWriteOperation;
import com.runicrealms.plugin.api.PlayerDataAPI;
import com.runicrealms.plugin.common.CharacterClass;
import com.runicrealms.plugin.common.util.Pair;
import com.runicrealms.plugin.model.CorePlayerData;
import com.runicrealms.plugin.model.TitleData;
import com.runicrealms.plugin.rdb.CoreMongoConfiguration;
import com.runicrealms.plugin.rdb.RunicDatabase;
import com.runicrealms.plugin.rdb.api.CharacterAPI;
import com.runicrealms.plugin.rdb.api.DataAPI;
import com.runicrealms.plugin.rdb.api.WriteCallback;
import com.runicrealms.plugin.rdb.event.CharacterHasQuitEvent;
import com.runicrealms.plugin.rdb.event.CharacterLoadedEvent;
import com.runicrealms.plugin.rdb.event.CharacterQuitEvent;
import com.runicrealms.plugin.rdb.event.MongoSaveEvent;
import com.runicrealms.plugin.rdb.model.CharacterField;
import com.runicrealms.plugin.taskchain.TaskChainUtil;
import com.runicrealms.runicrestart.RunicRestart;
import org.bson.types.ObjectId;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import redis.clients.jedis.Jedis;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/**
 * The singleton database manager responsible for creating the connection to Mongo
 * into memory for lookup
 * <p>
 * As of 3/7/23, the DBManager no longer uses stateful architecture-- documents are no longer stored in memory
 *
 * @author Skyfallin
 */
public class DatabaseManager implements CharacterAPI, DataAPI, PlayerDataAPI, Listener, CoreWriteOperation {
    private static final int CHARACTER_SAVE_PERIOD = 30; // Seconds
    private final ConcurrentHashMap<UUID, Pair<Integer, CharacterClass>> loadedCharacterMap; // stores the current character the player is playing
    private final Map<UUID, CorePlayerData> corePlayerDataMap; // For caching session data in-memory
    private MongoClient mongoClient;
    private MongoDatabase mongoDatabase;
    private MongoTemplate mongoTemplate;

    public DatabaseManager() {
        Bukkit.getServer().getPluginManager().registerEvents(this, RunicCore.getInstance());
        loadedCharacterMap = new ConcurrentHashMap<>();
        corePlayerDataMap = new HashMap<>();
        /*
        Create a mongo client from Spring's config (Spring manages its lifecycle)
        Doesn't start until all other plugins are loaded
         */
        new BukkitRunnable() {
            @Override
            public void run() {
                if (RunicRestart.getAPI().getPluginsToLoad().size() > 1)
                    return; // Other plugins confirming startup (leave 1 for core)
                this.cancel();
                try {
                    ApplicationContext context = new AnnotationConfigApplicationContext(CoreMongoConfiguration.class);
                    mongoTemplate = context.getBean("pluginDataMongoTemplate", MongoTemplate.class);
                    mongoClient = context.getBean(MongoClient.class);
                    mongoDatabase = mongoClient.getDatabase(RunicDatabase.getDatabaseName());
                } catch (Exception e) {
                    e.printStackTrace();
                    RunicCore.getInstance().getLogger().log(Level.SEVERE, "Spring initialization failed!");
                } finally {
                    Bukkit.getLogger().info(ChatColor.GREEN + springString());
                    RunicRestart.getAPI().markPluginLoaded("core");
                    startLocationSaveTask(); // Save location periodically
                }
            }
        }.runTaskTimerAsynchronously(RunicCore.getInstance(), 0, 10L);
    }

    /**
     * Builds a new database document for the given player if it doesn't already exist when they join server/lobby
     *
     * @param uuid of player to lookup
     * @return a ProjectedData object
     */
    @Override
    public CorePlayerData getCorePlayerData(UUID uuid) {
        return corePlayerDataMap.get(uuid);
    }

    @Override
    public Map<UUID, CorePlayerData> getCorePlayerDataMap() {
        return corePlayerDataMap;
    }

    @Override
    public int getMaxCharacterSlot() {
        return 10;
    }

    @Override
    public MongoDatabase getMongoDatabase() {
        return mongoDatabase;
    }

    @Override
    public MongoTemplate getMongoTemplate() {
        return mongoTemplate;
    }

    @Override
    public CorePlayerData loadCorePlayerData(UUID uuid) {
        // Step 1: Check redis
        try (Jedis jedis = RunicDatabase.getAPI().getRedisAPI().getNewJedisResource()) {
            boolean dataInRedis = checkRedisForCoreData(uuid, jedis);
            if (dataInRedis) {
                //                Bukkit.getLogger().info("LOADING CORE DATA FROM REDIS");
                return new CorePlayerData(uuid, jedis);
            }
            // Step 2: Check the mongo database
            Query query = new Query();
            query.addCriteria(Criteria.where(CharacterField.PLAYER_UUID.getField()).is(uuid));
            MongoTemplate mongoTemplate = RunicDatabase.getAPI().getDataAPI().getMongoTemplate();
            List<CorePlayerData> results = mongoTemplate.find(query, CorePlayerData.class);
            if (results.size() > 0) {
                CorePlayerData result = results.get(0);
                result.writeToJedis(jedis);
                return result;
            }
            // Step 3: if no data is found, we create some data and save it to the collection
            CorePlayerData newData = new CorePlayerData
                    (
                            new ObjectId(),
                            uuid,
                            LocalDate.now(),
                            new HashMap<>(),
                            new HashMap<>(),
                            new HashMap<>(),
                            new TitleData()
                    );
            newData.addDocumentToMongo();
            newData.writeToJedis(jedis);
            return newData;
        }
    }

    public boolean checkRedisForCoreData(UUID uuid, Jedis jedis) {
        String database = RunicDatabase.getAPI().getDataAPI().getMongoDatabase().getName();
        return jedis.exists(database + ":" + uuid + ":hasCoreData");
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
        if (loadedCharacterMap.get(player.getUniqueId()) == null)
            return null;
        return loadedCharacterMap.get(player.getUniqueId()).second.getName();
    }

    @Override
    public String getPlayerClass(UUID uuid) {
        if (loadedCharacterMap.get(uuid) == null)
            return null;
        return loadedCharacterMap.get(uuid).second.getName();
    }

    @Override
    public String getPlayerClass(UUID uuid, int slot, Jedis jedis) {
        // If player class is not cached, player is offline
        String key = RunicDatabase.getAPI().getRedisAPI().getCharacterKey(uuid, slot);
        if (jedis.exists(key))
            return jedis.hmget(key, CharacterField.CLASS_TYPE.getField()).get(0);
        else
            return null;
    }

    @Override
    public CharacterClass getPlayerClassValue(UUID uuid) {
        String className = getPlayerClass(uuid);
        return CharacterClass.getFromName(className);
    }

    public MongoClient getMongoClient() {
        return mongoClient;
    }

    /**
     * IMPORTANT: performs all necessary data cleanup after player is loaded
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onCharacterLoaded(CharacterLoadedEvent event) {
        Player player = event.getPlayer();
        event.getCharacterSelectEvent().getBukkitTask().cancel();
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.0f);
        player.sendTitle(ChatColor.DARK_GREEN + "Load Complete!", ChatColor.GREEN + "Welcome " + player.getName(), 10, 100, 10);
        int slot = event.getCharacterSelectEvent().getSlot();
        Bukkit.getLogger().severe("PLAYER MARKED AS LOADED 5");
        loadedCharacterMap.put
                (
                        event.getPlayer().getUniqueId(),
                        Pair.pair(slot, ((CorePlayerData) event.getCharacterSelectEvent().getSessionDataMongo()).getCharacter(slot).getClassType())
                ); // Now we always know which character is playing
    }

    /**
     * Synchronously call an event when the player has finished saving their quit data.
     * Allows player to log back in
     */
    @EventHandler(priority = EventPriority.HIGHEST) // last thing that runs
    public void onCharacterQuitFinished(CharacterQuitEvent event) {
        Location location = event.getPlayer().getLocation();
        try (Jedis jedis = RunicDatabase.getAPI().getRedisAPI().getNewJedisResource()) {
            CorePlayerData corePlayerData = getCorePlayerData(event.getPlayer().getUniqueId());
            corePlayerData.setLastLoginDate(LocalDate.now());
            corePlayerData.getCharacter(event.getSlot()).setLocation(location);
            corePlayerData.getCharacter(event.getSlot()).setCurrentHp((int) event.getPlayer().getHealth());
            corePlayerData.writeToJedis(jedis);
            // Removed from in-game memory map
            loadedCharacterMap.remove(event.getPlayer().getUniqueId());
            // Inform plugins that logout is complete
            CharacterHasQuitEvent characterHasQuitEvent = new CharacterHasQuitEvent(event.getPlayer(), event);
            Bukkit.getScheduler().runTask(RunicCore.getInstance(),
                    () -> Bukkit.getPluginManager().callEvent(characterHasQuitEvent)); // Inform plugins that character has finished data save! SYNC
        }
    }

    /**
     * Saves all character-related core data (class, professions, etc.) on server shutdown
     * for EACH alt the player has used during the runtime of this server.
     * Works even if the player is now entirely offline
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onDatabaseSave(MongoSaveEvent event) {
        // Cancel the task timer
        RunicCore.getMongoTask().getTask().cancel();
        // Manually save all sync
        RunicCore.getMongoTask().saveAllToMongo(() -> {
        });
        // Wait to mark core complete until all other plugins are complete
        new BukkitRunnable() {
            @Override
            public void run() {
                if (event.getPluginsToSave().size() > 1)
                    return; // Other plugins confirming startup (leave 1 for core)
                this.cancel();
                event.markPluginSaved("core");
            }
        }.runTaskTimerAsynchronously(RunicCore.getInstance(), 0, 10L);

    }

    /**
     * Call our custom character quit event, sync or async depending on context
     */
    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        if (loadedCharacterMap.get(event.getPlayer().getUniqueId()) == null) return;
        CorePlayerData corePlayerData = getCorePlayerData(event.getPlayer().getUniqueId());
        boolean isAsync = !RunicRestart.getAPI().isShuttingDown();
        if (isAsync) {
            Bukkit.getScheduler().runTaskAsynchronously(RunicCore.getInstance(),
                    () -> Bukkit.getPluginManager().callEvent(new CharacterQuitEvent
                            (
                                    event.getPlayer(),
                                    loadedCharacterMap.get(event.getPlayer().getUniqueId()).first,
                                    corePlayerData.getCharacter(loadedCharacterMap.get(event.getPlayer().getUniqueId()).first),
                                    true
                            )));
        } else {
            Bukkit.getPluginManager().callEvent(new CharacterQuitEvent
                    (
                            event.getPlayer(),
                            loadedCharacterMap.get(event.getPlayer().getUniqueId()).first,
                            corePlayerData.getCharacter(loadedCharacterMap.get(event.getPlayer().getUniqueId()).first),
                            false
                    ));
        }
    }

    /**
     * Uses ASCII art to display a pretty message on spring initialization
     * <a href="https://patorjk.com/software/taag/#p=display&f=Big&t=Spring%20Data%20MongoDB">ASCII generator</a>
     *
     * @return a string for console to confirm server startup
     */
    private String springString() {
        return """

                   _____            _               _____        _          __  __                         _____  ____ \s
                  / ____|          (_)             |  __ \\      | |        |  \\/  |                       |  __ \\|  _ \\\s
                 | (___  _ __  _ __ _ _ __   __ _  | |  | | __ _| |_ __ _  | \\  / | ___  _ __   __ _  ___ | |  | | |_) |
                  \\___ \\| '_ \\| '__| | '_ \\ / _` | | |  | |/ _` | __/ _` | | |\\/| |/ _ \\| '_ \\ / _` |/ _ \\| |  | |  _ <\s
                  ____) | |_) | |  | | | | | (_| | | |__| | (_| | || (_| | | |  | | (_) | | | | (_| | (_) | |__| | |_) |
                 |_____/| .__/|_|  |_|_| |_|\\__, | |_____/ \\__,_|\\__\\__,_| |_|  |_|\\___/|_| |_|\\__, |\\___/|_____/|____/\s
                        | |                  __/ |                                              __/ |                  \s
                        |_|                 |___/                                              |___/                   \s
                """;
    }

    /**
     * Periodic task to save player location
     */
    private void startLocationSaveTask() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(RunicCore.getInstance(), () -> {
            for (UUID uuid : loadedCharacterMap.keySet()) {
//                if (PlayerJoinListener.LOADING_PLAYERS.contains(uuid)) continue; // Not yet teleported
                Player player = Bukkit.getPlayer(uuid);
                if (player == null) continue; // Player not online
                int slot = RunicDatabase.getAPI().getCharacterAPI().getCharacterSlot(uuid);
                Location location = player.getLocation();
                try (Jedis jedis = RunicDatabase.getAPI().getRedisAPI().getNewJedisResource()) {
                    CorePlayerData corePlayerData = getCorePlayerData(uuid);
                    corePlayerData.getCharacter(slot).setLocation(location);
                    corePlayerData.getCharacter(slot).setCurrentHp((int) player.getHealth());
                    corePlayerData.writeToJedis(jedis);
                }
            }
        }, 0, CHARACTER_SAVE_PERIOD * 20L);
    }

    @Override
    public <T> void updateCorePlayerData(UUID uuid, int slot, String fieldName, T newValue, MongoTemplate mongoTemplate, WriteCallback callback) {
        TaskChain<?> chain = RunicCore.newChain();
        chain
                .asyncFirst(() -> {
                    // Define a query to find the CorePlayerData for this player
                    Query query = new Query();
                    query.addCriteria(Criteria.where(CharacterField.PLAYER_UUID.getField()).is(uuid));

                    // Define an update to set the specific field
                    Update update = new Update();
                    update.set(fieldName + "." + slot, newValue);

                    // Execute the update operation
                    return mongoTemplate.updateFirst(query, update, CorePlayerData.class);
                })
                .abortIfNull(TaskChainUtil.CONSOLE_LOG, null, "RunicCore failed to save " + fieldName + "!")
                .syncLast(updateResult -> callback.onWriteComplete())
                .execute();
    }
}
