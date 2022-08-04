package com.runicrealms.plugin;

import co.aikar.commands.ConditionFailedException;
import co.aikar.commands.PaperCommandManager;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.runicrealms.RunicChat;
import com.runicrealms.plugin.character.gui.CharacterGuiManager;
import com.runicrealms.plugin.commands.*;
import com.runicrealms.plugin.database.DatabaseManager;
import com.runicrealms.plugin.donator.ThreeD;
import com.runicrealms.plugin.donator.ThreeDManager;
import com.runicrealms.plugin.item.TeleportScrollListener;
import com.runicrealms.plugin.item.lootchests.LootChestListener;
import com.runicrealms.plugin.item.lootchests.LootChestManager;
import com.runicrealms.plugin.item.shops.RunicItemShopManager;
import com.runicrealms.plugin.item.shops.RunicShopManager;
import com.runicrealms.plugin.listeners.*;
import com.runicrealms.plugin.model.ModelListener;
import com.runicrealms.plugin.party.PartyChannel;
import com.runicrealms.plugin.party.PartyCommand;
import com.runicrealms.plugin.party.PartyDamageListener;
import com.runicrealms.plugin.party.PartyManager;
import com.runicrealms.plugin.player.CombatManager;
import com.runicrealms.plugin.player.PlayerHungerManager;
import com.runicrealms.plugin.player.RegenManager;
import com.runicrealms.plugin.player.listener.*;
import com.runicrealms.plugin.player.stat.StatListener;
import com.runicrealms.plugin.player.stat.StatManager;
import com.runicrealms.plugin.redis.RedisManager;
import com.runicrealms.plugin.scoreboard.ScoreboardHandler;
import com.runicrealms.plugin.scoreboard.ScoreboardListener;
import com.runicrealms.plugin.spellapi.ArtifactSpellListener;
import com.runicrealms.plugin.spellapi.SpellManager;
import com.runicrealms.plugin.spellapi.SpellScalingListener;
import com.runicrealms.plugin.spellapi.SpellUseListener;
import com.runicrealms.plugin.spellapi.skilltrees.SkillTreeManager;
import com.runicrealms.plugin.spellapi.skilltrees.cmd.ResetTreeCMD;
import com.runicrealms.plugin.spellapi.skilltrees.listener.*;
import com.runicrealms.plugin.tablist.TabListManager;
import com.runicrealms.plugin.utilities.ColorUtil;
import com.runicrealms.plugin.utilities.FilterUtil;
import com.runicrealms.plugin.utilities.PlaceholderAPI;
import com.runicrealms.runicrestart.api.RunicRestartApi;
import com.runicrealms.runicrestart.api.ServerShutdownEvent;
import net.minecraft.server.v1_16_R3.MinecraftServer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class RunicCore extends JavaPlugin implements Listener {

    private static final int BASE_OUTLAW_RATING = 1500;

    private static RunicCore instance;
    private static CombatManager combatManager;
    private static LootChestManager lootChestManager;
    private static RegenManager regenManager;
    private static PartyManager partyManager;
    private static ScoreboardHandler scoreboardHandler;
    private static SpellManager spellManager;
    private static TabListManager tabListManager;
    private static MobTagger mobTagger;
    private static BossTagger bossTagger;
    private static ProtocolManager protocolManager;
    private static DatabaseManager databaseManager;
    private static PartyChannel partyChannel;
    private static PaperCommandManager commandManager;
    private static SkillTreeManager skillTreeManager;
    private static StatManager statManager;
    private static ThreeDManager threeDManager;
    private static RunicShopManager runicShopManager;
    private static PlayerHungerManager playerHungerManager;
    private static RedisManager redisManager;

    // getters for handlers
    public static RunicCore getInstance() {
        return instance;
    }

    public static CombatManager getCombatManager() {
        return combatManager;
    }

    public static RegenManager getRegenManager() {
        return regenManager;
    }

    public static LootChestManager getLootChestManager() {
        return lootChestManager;
    }

    public static PartyManager getPartyManager() {
        return partyManager;
    }

    public static ScoreboardHandler getScoreboardHandler() {
        return scoreboardHandler;
    }

    public static SpellManager getSpellManager() {
        return spellManager;
    }

    public static TabListManager getTabListManager() {
        return tabListManager;
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

    public static DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public static PartyChannel getPartyChatChannel() {
        return partyChannel;
    }

    public static SkillTreeManager getSkillTreeManager() {
        return skillTreeManager;
    }

    public static PaperCommandManager getCommandManager() {
        return commandManager;
    }

    public static StatManager getStatManager() {
        return statManager;
    }

    public static ThreeDManager getThreeDManager() {
        return threeDManager;
    }

    public static RunicShopManager getRunicShopManager() {
        return runicShopManager;
    }

    public static PlayerHungerManager getPlayerHungerManager() {
        return playerHungerManager;
    }

    public static RedisManager getRedisManager() {
        return redisManager;
    }

    public static int getBaseOutlawRating() {
        return BASE_OUTLAW_RATING;
    }

    public void onEnable() {

        // Load config defaults
        this.loadConfig();

        // instantiate everything we need
        instance = this;
        combatManager = new CombatManager();
        lootChestManager = new LootChestManager();
        regenManager = new RegenManager();
        partyManager = new PartyManager();
        scoreboardHandler = new ScoreboardHandler();
        spellManager = new SpellManager();
        tabListManager = new TabListManager(this);
        mobTagger = new MobTagger();
        bossTagger = new BossTagger();
        protocolManager = ProtocolLibrary.getProtocolManager();
        databaseManager = new DatabaseManager();
        skillTreeManager = new SkillTreeManager();
        statManager = new StatManager();
        threeDManager = new ThreeDManager();
        runicShopManager = new RunicShopManager();
        playerHungerManager = new PlayerHungerManager();
        redisManager = new RedisManager();

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

        // motd
        String motd = ColorUtil.format("                    &d&lRUNIC REALMS&r" +
                "\n              &a&l2.0 - The Second Age!");
        MinecraftServer.getServer().setMotd(motd);
    }

    /*
    Prevent memory leaks
     */
    public void onDisable() {
        RunicCore.getDatabaseManager().getPlayersToSave().clear();
        combatManager = null;
        instance = null;
        lootChestManager = null;
        regenManager = null;
        partyManager = null;
        scoreboardHandler = null;
        spellManager = null;
        tabListManager = null;
        mobTagger = null;
        bossTagger = null;
        databaseManager = null;
        partyChannel = null;
        skillTreeManager = null;
        statManager = null;
        threeDManager = null;
        runicShopManager = null;
        playerHungerManager = null;
        redisManager = null;
    }

    @EventHandler(priority = EventPriority.LOWEST) // first
    public void onRunicShutdown(ServerShutdownEvent e) {
        getDatabaseManager().saveAllCharacters(); // saves SYNC CacheSaveReason.SERVER_SHUTDOWN
        // todo: call new mongo save event here
        /*
        Notify RunicRestart
         */
        getLogger().info(" §cRunicCore has been disabled.");
        RunicRestartApi.markPluginSaved("core");
    }

    private void loadConfig() {
        getConfig().options().copyDefaults(true);
        saveConfig();
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
        pm.registerEvents(new ManaListener(), this);
        pm.registerEvents(new PlayerLevelListener(), this);
        pm.registerEvents(new CraftingListener(), this);
        pm.registerEvents(new MobMechanicsListener(), this);
        pm.registerEvents(new CombatListener(), this);
        pm.registerEvents(new PlayerRegenListener(), this);
        pm.registerEvents(new PlayerMenuListener(), this);
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
        pm.registerEvents(new RunicExpListener(), this);
        pm.registerEvents(new RunicItemShopManager(), this);
        pm.registerEvents(new SpellVerifyListener(), this);
        pm.registerEvents(new SkillTreeGUIListener(), this);
        pm.registerEvents(new RuneGUIListener(), this);
        pm.registerEvents(new SubClassGUIListener(), this);
        pm.registerEvents(new SpellEditorGUIListener(), this);
        pm.registerEvents(new SpellGUIListener(), this);
        pm.registerEvents(new CreatureSpawnListener(), this);
        pm.registerEvents(new StatListener(), this);
        pm.registerEvents(new RuneListener(), this);
        pm.registerEvents(new TeleportScrollListener(), this);
        pm.registerEvents(new SpellScalingListener(), this);
        pm.registerEvents(new EnvironmentDamageListener(), this);
        pm.registerEvents(new GenericDamageListener(), this);
        pm.registerEvents(new SkillPointsListener(), this);
        pm.registerEvents(new MobCleanupListener(), this);
        pm.registerEvents(new DeathListener(), this);
        pm.registerEvents(partyManager, this);
        pm.registerEvents(new ArmorEquipListener(), this);
        pm.registerEvents(new EnderpearlListener(), this);
        pm.registerEvents(new ArtifactSpellListener(), this);
        pm.registerEvents(new StatsGUIListener(), this);
        pm.registerEvents(new ModelListener(), this);
        partyChannel = new PartyChannel();
        RunicChat.getRunicChatAPI().registerChatChannel(partyChannel);
    }

    private void registerACFCommands() {
        if (commandManager == null) {
            Bukkit.getLogger().info(ChatColor.DARK_RED + "ERROR: FAILED TO INITIALIZE ACF COMMANDS");
            return;
        }
        commandManager.registerCommand(new CheckExpCMD());
        commandManager.registerCommand(new ManaCMD());
        commandManager.registerCommand(new RunicGiveCMD());
        commandManager.registerCommand(new SetCMD());
        commandManager.registerCommand(new TravelCMD());
        commandManager.registerCommand(new VanishCMD());
        commandManager.registerCommand(new ResetTreeCMD());
        commandManager.registerCommand(new PartyCommand());
        commandManager.registerCommand(new RunicTeleportCMD());
        commandManager.registerCommand(new RunicBossCMD());
    }

    private void registerOldStyleCommands() {

        // boost
        getCommand("boost").setExecutor(new BoostCMD());
        // register 3d command
        getCommand("3d").setExecutor(new ThreeD());

        Bukkit.getPluginCommand("map").setExecutor(new MapLink());
        Bukkit.getPluginCommand("runicdamage").setExecutor(new RunicDamage());
        Bukkit.getPluginCommand("runicfirework").setExecutor(new FireworkCMD());
        Bukkit.getPluginCommand("spawn").setExecutor(new SpawnCMD());
        Bukkit.getPluginCommand("runicvote").setExecutor(new RunicVoteCMD());
    }
}
