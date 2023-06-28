package com.runicrealms.plugin;

import co.aikar.commands.ConditionFailedException;
import co.aikar.commands.PaperCommandManager;
import co.aikar.taskchain.BukkitTaskChainFactory;
import co.aikar.taskchain.TaskChain;
import co.aikar.taskchain.TaskChainFactory;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.runicrealms.RunicChat;
import com.runicrealms.plugin.api.*;
import com.runicrealms.plugin.character.gui.CharacterGuiManager;
import com.runicrealms.plugin.commands.admin.*;
import com.runicrealms.plugin.commands.player.*;
import com.runicrealms.plugin.common.RunicCommon;
import com.runicrealms.plugin.config.ConfigManager;
import com.runicrealms.plugin.converter.ConverterHandler;
import com.runicrealms.plugin.database.DatabaseManager;
import com.runicrealms.plugin.donor.DonorCommand;
import com.runicrealms.plugin.donor.boost.BoostManager;
import com.runicrealms.plugin.donor.boost.api.BoostAPI;
import com.runicrealms.plugin.donor.boost.command.AddBoostCommand;
import com.runicrealms.plugin.donor.boost.command.BoostCommand;
import com.runicrealms.plugin.donor.boost.ui.BoostConfirmUIListener;
import com.runicrealms.plugin.donor.boost.ui.BoostsUIListener;
import com.runicrealms.plugin.donor.ui.DonorPerksUIListener;
import com.runicrealms.plugin.donor.ui.DonorUIListener;
import com.runicrealms.plugin.fieldboss.FieldBossCommand;
import com.runicrealms.plugin.fieldboss.FieldBossManager;
import com.runicrealms.plugin.item.artifact.ArtifactOnCastListener;
import com.runicrealms.plugin.item.artifact.ArtifactOnHitListener;
import com.runicrealms.plugin.item.artifact.ArtifactOnKillListener;
import com.runicrealms.plugin.item.lootchests.*;
import com.runicrealms.plugin.item.shops.RunicItemShopManager;
import com.runicrealms.plugin.item.shops.RunicShopManager;
import com.runicrealms.plugin.listeners.*;
import com.runicrealms.plugin.loot.LootChestCreator;
import com.runicrealms.plugin.loot.LootManager;
import com.runicrealms.plugin.luckperms.LuckPermsManager;
import com.runicrealms.plugin.model.MongoTask;
import com.runicrealms.plugin.model.SettingsManager;
import com.runicrealms.plugin.model.TitleManager;
import com.runicrealms.plugin.modtools.AFKListener;
import com.runicrealms.plugin.modtools.TempbanListener;
import com.runicrealms.plugin.modtools.VanishManager;
import com.runicrealms.plugin.party.*;
import com.runicrealms.plugin.player.CombatManager;
import com.runicrealms.plugin.player.PlayerHungerManager;
import com.runicrealms.plugin.player.RegenManager;
import com.runicrealms.plugin.player.death.GravestoneManager;
import com.runicrealms.plugin.player.listener.*;
import com.runicrealms.plugin.player.stat.StatListener;
import com.runicrealms.plugin.player.stat.StatManager;
import com.runicrealms.plugin.player.ui.ProfileUIListener;
import com.runicrealms.plugin.player.ui.SettingsUIListener;
import com.runicrealms.plugin.player.ui.StatsGUIListener;
import com.runicrealms.plugin.playerqueue.PlayerQueueManager;
import com.runicrealms.plugin.rdb.RunicDatabase;
import com.runicrealms.plugin.rdb.RunicDatabaseAPI;
import com.runicrealms.plugin.rdb.api.CharacterAPI;
import com.runicrealms.plugin.rdb.api.ConverterAPI;
import com.runicrealms.plugin.rdb.api.DataAPI;
import com.runicrealms.plugin.rdb.api.RedisAPI;
import com.runicrealms.plugin.rdb.event.MongoSaveEvent;
import com.runicrealms.plugin.redis.RedisManager;
import com.runicrealms.plugin.region.RegionEventListener;
import com.runicrealms.plugin.scoreboard.ScoreboardHandler;
import com.runicrealms.plugin.scoreboard.ScoreboardListener;
import com.runicrealms.plugin.sound.ambient.AmbientSoundHandler;
import com.runicrealms.plugin.sound.music.MusicListener;
import com.runicrealms.plugin.spellapi.*;
import com.runicrealms.plugin.spellapi.skilltrees.SkillTreeManager;
import com.runicrealms.plugin.spellapi.skilltrees.listener.*;
import com.runicrealms.plugin.tablist.TabListManager;
import com.runicrealms.plugin.utilities.FilterUtil;
import com.runicrealms.plugin.utilities.NametagHandler;
import com.runicrealms.plugin.utilities.PlaceholderAPI;
import com.runicrealms.plugin.utilities.RegionHelper;
import com.runicrealms.runicrestart.event.PreShutdownEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class RunicCore extends JavaPlugin implements Listener {

    private static final int BASE_OUTLAW_RATING = 1500;

    private static RunicCore instance;
    private static TaskChainFactory taskChainFactory;
    private static CombatAPI combatAPI;
    private static LootChestManager lootChestManager;
    private static RegenManager regenManager;
    private static PartyAPI partyAPI;
    private static ScoreboardAPI scoreboardAPI;
    private static SpellAPI spellAPI;
    private static TabAPI tabAPI;
    private static LootTableAPI lootTableAPI;
    private static MobTagger mobTagger;
    private static BossTagger bossTagger;
    private static ProtocolManager protocolManager;
    private static RegionAPI regionAPI;
    private static PartyChannel partyChannel;
    private static PaperCommandManager commandManager;
    private static SkillTreeAPI skillTreeAPI;
    private static StatAPI statAPI;
    private static RunicShopManager runicShopManager;
    private static PlayerHungerManager playerHungerManager;
    private static TitleAPI titleAPI;
    private static SettingsManager settingsManager;
    private static ShopAPI shopAPI;
    private static MongoTask mongoTask;
    private static StatusEffectAPI statusEffectAPI;
    private static GravestoneManager gravestoneManager;
    private static PlayerDataAPI playerDataAPI;
    private static AmbientSoundHandler ambientSoundHandler;
    private static BoostAPI boostAPI;
    private static VanishAPI vanishAPI;
    private static CoreWriteOperation coreWriteOperation;
    private static FieldBossAPI fieldBossAPI;
    private static LootAPI lootAPI;

    // getters for handlers
    public static RunicCore getInstance() {
        return instance;
    }

    public static CombatAPI getCombatAPI() {
        return combatAPI;
    }

    public static RegenManager getRegenManager() {
        return regenManager;
    }

    public static LootChestManager getLootChestManager() {
        return lootChestManager;
    }

    public static PartyAPI getPartyAPI() {
        return partyAPI;
    }

    public static ScoreboardAPI getScoreboardAPI() {
        return scoreboardAPI;
    }

    public static SpellAPI getSpellAPI() {
        return spellAPI;
    }

    public static TabAPI getTabAPI() {
        return tabAPI;
    }

    public static LootTableAPI getLootTableAPI() {
        return lootTableAPI;
    }

    public static MobTagger getMobTagger() {
        return mobTagger;
    }

    public static BossTagger getBossTagger() {
        return bossTagger;
    }

    public static ProtocolManager getProtocolManager() {
        return protocolManager;
    }

    public static ShopAPI getShopAPI() {
        return shopAPI;
    }

    public static StatusEffectAPI getStatusEffectAPI() {
        return statusEffectAPI;
    }

    public static RegionAPI getRegionAPI() {
        return regionAPI;
    }

    public static PartyChannel getPartyChatChannel() {
        return partyChannel;
    }

    public static SkillTreeAPI getSkillTreeAPI() {
        return skillTreeAPI;
    }

    public static PaperCommandManager getCommandManager() {
        return commandManager;
    }

    public static StatAPI getStatAPI() {
        return statAPI;
    }

    public static RunicShopManager getRunicShopManager() {
        return runicShopManager;
    }

    public static PlayerHungerManager getPlayerHungerManager() {
        return playerHungerManager;
    }

    public static TitleAPI getTitleAPI() {
        return titleAPI;
    }

    public static GravestoneManager getGravestoneManager() {
        return gravestoneManager;
    }

    public static SettingsManager getSettingsManager() {
        return settingsManager;
    }

    public static MongoTask getMongoTask() {
        return mongoTask;
    }

    public static int getBaseOutlawRating() {
        return BASE_OUTLAW_RATING;
    }

    public static TaskChainFactory getTaskChainFactory() {
        return taskChainFactory;
    }

    public static PlayerDataAPI getPlayerDataAPI() {
        return playerDataAPI;
    }

    public static AmbientSoundHandler getAmbientSoundHandler() {
        return ambientSoundHandler;
    }

    public static BoostAPI getBoostAPI() {
        return boostAPI;
    }

    public static VanishAPI getVanishAPI() {
        return vanishAPI;
    }

    public static FieldBossAPI getFieldBossAPI() {
        return fieldBossAPI;
    }

    public static LootAPI getLootAPI() {
        return lootAPI;
    }

    /**
     * @return a TaskChain for thread context switching
     */
    public static <T> TaskChain<T> newChain() {
        return taskChainFactory.newChain();
    }

    public static CoreWriteOperation getCoreWriteOperation() {
        return coreWriteOperation;
    }

    private void loadConfig() {
        getConfig().options().copyDefaults(true);
        saveConfig();
    }

    /*
    Prevent memory leaks
     */
    @Override
    public void onDisable() {
        combatAPI = null;
        instance = null;
        lootChestManager = null;
        regenManager = null;
        partyAPI = null;
        scoreboardAPI = null;
        spellAPI = null;
        tabAPI = null;
        lootTableAPI = null;
        mobTagger = null;
        bossTagger = null;
        regionAPI = null;
        partyChannel = null;
        skillTreeAPI = null;
        statAPI = null;
        runicShopManager = null;
        playerHungerManager = null;
        titleAPI = null;
        settingsManager = null;
        shopAPI = null;
        mongoTask = null;
        statusEffectAPI = null;
        taskChainFactory = null;
        gravestoneManager = null;
        playerDataAPI = null;
        ambientSoundHandler = null;
        boostAPI = null;
        vanishAPI = null;
        coreWriteOperation = null;
        fieldBossAPI = null;
        lootAPI = null;
    }

    @Override
    public void onEnable() {

        // Load config defaults
        this.loadConfig();

        // Important
        instance = this;

        // Set database stuff first
        RunicCommon.registerConfigAPI(new ConfigManager());
        RunicCommon.registerLuckPermsAPI(new LuckPermsManager());

        ConverterAPI converterAPI = new ConverterHandler();
        RedisAPI redisAPI = new RedisManager();
        DatabaseManager databaseManager = new DatabaseManager();
        coreWriteOperation = databaseManager;
        // Implement interface logic from RDB
        RunicDatabase.getInstance().setAPIImplementation(new RunicDatabaseAPI() {
            @Override
            public CharacterAPI getCharacterAPI() {
                return databaseManager;
            }

            @Override
            public ConverterAPI getConverterAPI() {
                return converterAPI;
            }

            @Override
            public DataAPI getDataAPI() {
                return databaseManager;
            }

            @Override
            public RedisAPI getRedisAPI() {
                return redisAPI;
            }
        });
        playerDataAPI = databaseManager;

        // instantiate everything we need
        taskChainFactory = BukkitTaskChainFactory.create(this);
        combatAPI = new CombatManager();
        lootChestManager = new LootChestManager();
        regenManager = new RegenManager();
        partyAPI = new PartyManager();
        scoreboardAPI = new ScoreboardHandler();
        spellAPI = new SpellManager();
        tabAPI = new TabListManager(this);
        lootTableAPI = new LootTableManager();
        regionAPI = new RegionHelper();
        mobTagger = new MobTagger();
        bossTagger = new BossTagger();
        protocolManager = ProtocolLibrary.getProtocolManager();
        skillTreeAPI = new SkillTreeManager();
        statAPI = new StatManager();
        runicShopManager = new RunicShopManager();
        playerHungerManager = new PlayerHungerManager();
        titleAPI = new TitleManager();
        settingsManager = new SettingsManager();
        shopAPI = new RunicItemShopManager();
        mongoTask = new MongoTask();
        statusEffectAPI = new StatusEffectManager();
        gravestoneManager = new GravestoneManager();
        ambientSoundHandler = new AmbientSoundHandler();
        boostAPI = new BoostManager();
        vanishAPI = new VanishManager();
        fieldBossAPI = new FieldBossManager();
        lootAPI = new LootManager();
        new DaylightCycleListener();
        new NpcListener();
        new ArtifactOnCastListener();
        new ArtifactOnHitListener();
        new ArtifactOnKillListener();
        new PlayerQueueManager();


        // ACF commands
        commandManager = new PaperCommandManager(this);
        registerACFCommands();
        commandManager.getCommandConditions().addCondition("is-console-or-op", context -> {
            if (!(context.getIssuer().getIssuer() instanceof ConsoleCommandSender) && !context.getIssuer().getIssuer().isOp()) // ops can execute console commands
                throw new ConditionFailedException("Only the console may run this command!");
        });
        commandManager.getCommandConditions().addCondition("is-op", context -> {
            if (!context.getIssuer().getIssuer().isOp())
                throw new ConditionFailedException("You must be an operator to run this command!");
        });
        commandManager.getCommandConditions().addCondition("is-player", context -> {
            if (!(context.getIssuer().getIssuer() instanceof Player))
                throw new ConditionFailedException("This command cannot be run from console!");
        });

        Bukkit.getPluginManager().registerEvents(this, this);

        // enable message
        getLogger().info(" §aRunicCore has been enabled.");

        // save filter txt
        this.saveResource("swearWords.txt", false);

        // load filter
        FilterUtil.loadFromFile(new File(this.getDataFolder(), "swearWords.txt"));

        // register our events, config, commands
        this.registerEvents();

        // register custom yml files
        this.saveResource("item_prefixes.yml", true);

        // register commands
        this.registerOldStyleCommands();

        // register placeholder tags
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new PlaceholderAPI().register();
        }
    }

    @EventHandler
    public void onPreShutdownEvent(PreShutdownEvent event) {
        MongoSaveEvent mongoSaveEvent = new MongoSaveEvent(event);
        Bukkit.getScheduler().runTaskAsynchronously(this, () -> Bukkit.getPluginManager().callEvent(mongoSaveEvent));
    }

    private void registerACFCommands() {
        if (commandManager == null) {
            Bukkit.getLogger().info(ChatColor.DARK_RED + "ERROR: FAILED TO INITIALIZE ACF COMMANDS");
            return;
        }
        commandManager.registerCommand(new ManaCMD());
        commandManager.registerCommand(new CooldownCMD());
        commandManager.registerCommand(new RunicGiveCMD());
        commandManager.registerCommand(new SetCMD());
        commandManager.registerCommand(new TravelCMD());
        commandManager.registerCommand(new VanishCMD());
        commandManager.registerCommand(new ResetTreeCMD());
        commandManager.registerCommand(new PartyCommand());
        commandManager.registerCommand(new RunicTeleportCMD());
        commandManager.registerCommand(new RunicBossCMD());
        commandManager.registerCommand(new HelpCMD());
        commandManager.registerCommand(new SpeedCMD());
        commandManager.registerCommand(new GameModeCMD());
        commandManager.registerCommand(new ArmorStandCMD());
        commandManager.registerCommand(new ExpCMD());
        commandManager.registerCommand(new RollCMD());
        commandManager.registerCommand(new AddBoostCommand());
        commandManager.registerCommand(new DonorCommand());
        commandManager.registerCommand(new BoostCommand());
        commandManager.registerCommand(new WhoIsCMD());
        commandManager.registerCommand(new TempbanCMD());
        commandManager.registerCommand(new TempunbanCMD());
        commandManager.registerCommand(new TPHereCMD());
        commandManager.registerCommand(new DiscordCMD());
        commandManager.registerCommand(new FieldBossCommand());
        commandManager.registerCommand(new LootChestCreator());

        partyChannel = new PartyChannel();
        RunicChat.getRunicChatAPI().registerChatChannel(partyChannel);
        commandManager.registerCommand(new PartyChannelCommand(partyChannel));
    }

    private void registerEvents() {

        PluginManager pm = this.getServer().getPluginManager();

        pm.registerEvents(RunicCore.getMobTagger(), this);
        pm.registerEvents(RunicCore.getBossTagger(), this);
        pm.registerEvents(new ScoreboardListener(), this);
        pm.registerEvents(new DurabilityListener(), this);
        pm.registerEvents(new StaffListener(), this);
        pm.registerEvents(new BowListener(), this);
        pm.registerEvents(new DamageListener(), this);
        pm.registerEvents(new ResourcePackListener(), this);
        pm.registerEvents(new PlayerQuitListener(), this);
        pm.registerEvents(new PartyDamageListener(), this);
        pm.registerEvents(new ExpListener(), this);
        pm.registerEvents(new SpellUseListener(), this);
        pm.registerEvents(new ArmorTypeListener(), this);
        pm.registerEvents(new PlayerJoinListener(), this);
        pm.registerEvents(new MusicListener(), this);
        pm.registerEvents(new ManaListener(), this);
        pm.registerEvents(new PlayerLevelListener(), this);
        pm.registerEvents(new CraftingListener(), this);
        pm.registerEvents(new MobMechanicsListener(), this);
        pm.registerEvents(new CombatListener(), this);
        pm.registerEvents(new PlayerRegenListener(), this);
        pm.registerEvents(new PlayerMenuListener(), this);
        pm.registerEvents(new BedEnterListener(), this);
        pm.registerEvents(new BlockBreakListener(), this);
        pm.registerEvents(new BlockInteractListener(), this);
        pm.registerEvents(new BlockPlaceListener(), this);
        pm.registerEvents(new MinLevelListener(), this);
        pm.registerEvents(new KeyClickListener(), this);
        pm.registerEvents(new WorldChangeListener(), this);
        pm.registerEvents(new LootChestListener(), this);
        pm.registerEvents(new HearthstoneListener(), this);
        pm.registerEvents(new OffhandListener(), this);
        pm.registerEvents(new CharacterGuiManager(), this);
        pm.registerEvents(new SwapHandsListener(), this);
        pm.registerEvents(new HerbFallDamageListener(), this);
        pm.registerEvents(new HorseFeedListener(), this);
        pm.registerEvents(new NametagHandler(), this);
        pm.registerEvents(new RunicExpListener(), this);
        pm.registerEvents(new EnemyVerifyListener(), this);
        pm.registerEvents(new AllyVerifyListener(), this);
        pm.registerEvents(new SkillTreeGUIListener(), this);
        pm.registerEvents(new RuneGUIListener(), this);
        pm.registerEvents(new SubClassGUIListener(), this);
        pm.registerEvents(new SpellEditorGUIListener(), this);
        pm.registerEvents(new SpellGUIListener(), this);
        pm.registerEvents(new CreatureSpawnListener(), this);
        pm.registerEvents(new StatListener(), this);
        pm.registerEvents(new RuneListener(), this);
        pm.registerEvents(new SpellScalingListener(), this);
        pm.registerEvents(new EnvironmentDamageListener(), this);
        pm.registerEvents(new GenericDamageListener(), this);
        pm.registerEvents(new SkillPointsListener(), this);
        pm.registerEvents(new MobCleanupListener(), this);
        pm.registerEvents(new InventoryClickListener(), this);
        pm.registerEvents(new RegionEventListener(), this);
        pm.registerEvents(new DeathListener(), this);
        pm.registerEvents(new NoJockeysListener(), this);
        pm.registerEvents(new ArmorEquipListener(), this);
        pm.registerEvents(new CampfireListener(), this);
        pm.registerEvents(new EnderpearlListener(), this);
        pm.registerEvents(new ArtifactSpellListener(), this);
        pm.registerEvents(new StatsGUIListener(), this);
        pm.registerEvents(new ServerListPingListener(), this);
        pm.registerEvents(new BossChestListener(), this);
        pm.registerEvents(new ExpBoostListener(), this);
        pm.registerEvents(new ShieldListener(), this);
        pm.registerEvents(new PreCommandListener(), this);
        pm.registerEvents(new BasicAttackListener(), this);
        pm.registerEvents(new SettingsUIListener(), this);
        pm.registerEvents(new SheepShearListener(), this);
        pm.registerEvents(new WeaponSkinListener(), this);
        pm.registerEvents(new DonorUIListener(), this);
        pm.registerEvents(new BoostsUIListener(), this);
        pm.registerEvents(new BoostConfirmUIListener(), this);
        pm.registerEvents(new ProfileUIListener(), this);
        pm.registerEvents(new DonorPerksUIListener(), this);
        pm.registerEvents(new TempbanListener(), this);
        pm.registerEvents(new AFKListener(), this);
    }

    private void registerOldStyleCommands() {
        Bukkit.getPluginCommand("map").setExecutor(new MapLink());
        Bukkit.getPluginCommand("runicdamage").setExecutor(new RunicDamage());
        Bukkit.getPluginCommand("runicfirework").setExecutor(new FireworkCMD());
        Bukkit.getPluginCommand("spawn").setExecutor(new SpawnCMD());
        Bukkit.getPluginCommand("runicvote").setExecutor(new RunicVoteCMD());
    }
}
