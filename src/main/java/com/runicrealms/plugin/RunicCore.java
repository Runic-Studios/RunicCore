package com.runicrealms.plugin;

import co.aikar.commands.ConditionFailedException;
import co.aikar.commands.PaperCommandManager;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.runicrealms.RunicChat;
import com.runicrealms.plugin.character.CharacterManager;
import com.runicrealms.plugin.character.gui.CharacterGuiManager;
import com.runicrealms.plugin.command.*;
import com.runicrealms.plugin.command.subcommands.FastTravel;
import com.runicrealms.plugin.command.subcommands.set.SetClassCMD;
import com.runicrealms.plugin.command.supercommands.CurrencySC;
import com.runicrealms.plugin.command.supercommands.RunicGiveSC;
import com.runicrealms.plugin.command.supercommands.TravelSC;
import com.runicrealms.plugin.database.DatabaseManager;
import com.runicrealms.plugin.database.event.CacheSaveReason;
import com.runicrealms.plugin.donator.ThreeD;
import com.runicrealms.plugin.donator.ThreeDManager;
import com.runicrealms.plugin.group.GroupCommand;
import com.runicrealms.plugin.group.GroupManager;
import com.runicrealms.plugin.item.*;
import com.runicrealms.plugin.item.commands.CurrencyGive;
import com.runicrealms.plugin.item.commands.CurrencyPouch;
import com.runicrealms.plugin.item.commands.HearthstoneCMD;
import com.runicrealms.plugin.item.commands.ItemCMD;
import com.runicrealms.plugin.item.goldpouch.GoldPouchListener;
import com.runicrealms.plugin.item.hearthstone.HearthstoneListener;
import com.runicrealms.plugin.item.lootchests.LootChestListener;
import com.runicrealms.plugin.item.lootchests.LootChestManager;
import com.runicrealms.plugin.item.mounts.MountListener;
import com.runicrealms.plugin.item.shops.RunicItemShopManager;
import com.runicrealms.plugin.item.shops.RunicShopManager;
import com.runicrealms.plugin.listeners.*;
import com.runicrealms.plugin.npc.Build;
import com.runicrealms.plugin.npc.NPCBuilderSC;
import com.runicrealms.plugin.party.PartyChannel;
import com.runicrealms.plugin.party.PartyCommand;
import com.runicrealms.plugin.party.PartyDamageListener;
import com.runicrealms.plugin.party.PartyManager;
import com.runicrealms.plugin.player.PlayerHungerManager;
import com.runicrealms.plugin.player.cache.CacheManager;
import com.runicrealms.plugin.player.combat.CombatListener;
import com.runicrealms.plugin.player.combat.CombatManager;
import com.runicrealms.plugin.player.combat.ExpListener;
import com.runicrealms.plugin.player.combat.PlayerLevelListener;
import com.runicrealms.plugin.player.commands.*;
import com.runicrealms.plugin.player.gear.OffhandListener;
import com.runicrealms.plugin.player.listener.PlayerJoinListener;
import com.runicrealms.plugin.player.listener.PlayerMenuListener;
import com.runicrealms.plugin.player.listener.PlayerQuitListener;
import com.runicrealms.plugin.player.listener.PlayerRegenListener;
import com.runicrealms.plugin.player.mana.ManaListener;
import com.runicrealms.plugin.player.mana.RegenManager;
import com.runicrealms.plugin.player.stat.StatListener;
import com.runicrealms.plugin.player.stat.StatManager;
import com.runicrealms.plugin.scoreboard.ScoreboardHandler;
import com.runicrealms.plugin.scoreboard.ScoreboardListener;
import com.runicrealms.plugin.shop.*;
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
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.Arrays;

//import com.runicrealms.plugin.item.scrapper.ItemScrapperCMD;
//import com.runicrealms.plugin.item.scrapper.ScrapperListener;

public class RunicCore extends JavaPlugin implements Listener {

    // global variables
    private static final int BASE_OUTLAW_RATING = 1500;

    // handlers
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
    private static CacheManager cacheManager;
    private static ProtocolManager protocolManager;
    private static DatabaseManager databaseManager;
    private static PartyChannel partyChannel;
    private static GroupManager groupManager;
    private static PaperCommandManager commandManager;
    private static SkillTreeManager skillTreeManager;
    private static StatManager statManager;
    private static ThreeDManager threeDManager;
    private static RunicShopManager runicShopManager;

    // dungeon shops
    private static CaveShop caveShop;
    private static CavernShop cavernShop;
    private static KeepShop keepShop;
    private static LibraryShop libraryShop;
    private static CryptsShop cryptsShop;
    private static FortressShop fortressShop;

    //normal shops
    private static GeneralShop generalShop;

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

    public static CacheManager getCacheManager() {
        return cacheManager;
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

    public static GroupManager getGroupManager() {
        return groupManager;
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

    public static int getBaseOutlawRating() {
        return BASE_OUTLAW_RATING;
    }

    // getters for dungeon shops
    public static CaveShop getCaveShop() {
        return caveShop;
    }

    public static CavernShop getCavernShop() {
        return cavernShop;
    }

    public static KeepShop getKeepShop() {
        return keepShop;
    }

    public static LibraryShop getLibraryShop() {
        return libraryShop;
    }

    public static CryptsShop getCryptsShop() {
        return cryptsShop;
    }

    public static FortressShop getRaidVendor() {
        return fortressShop;
    }

    //getters for normal shops
    public static GeneralShop getGeneralShop() {
        return generalShop;
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
        cacheManager = new CacheManager();
        protocolManager = ProtocolLibrary.getProtocolManager();
        databaseManager = new DatabaseManager();
        groupManager = new GroupManager();
        skillTreeManager = new SkillTreeManager();
        statManager = new StatManager();
        threeDManager = new ThreeDManager();
        runicShopManager = new RunicShopManager();
        // dungeon shops
        caveShop = new CaveShop();
        cavernShop = new CavernShop();
        keepShop = new KeepShop();
        libraryShop = new LibraryShop();
        cryptsShop = new CryptsShop();
        fortressShop = new FortressShop();

        //normal shops
        generalShop = new GeneralShop();

        // ACF commands
        commandManager = new PaperCommandManager(this);
        commandManager.registerCommand(new PartyCommand());
        commandManager.registerCommand(new GroupCommand());
        commandManager.registerCommand(new ResetTreeCMD());
        commandManager.registerCommand(new VanishCommand());
        commandManager.getCommandConditions().addCondition("is-player", context -> {
            if (!(context.getIssuer().getIssuer() instanceof Player))
                throw new ConditionFailedException("This command cannot be run from console!");
        });
        commandManager.getCommandConditions().addCondition("is-op", context -> {
            if (!context.getIssuer().getIssuer().isOp())
                throw new ConditionFailedException("You must be an operator to run this command!");
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
        this.registerCommands();

        // register placeholder tags
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new PlaceholderAPI().register();
        }

        // motd
        String motd = ColorUtil.format("                    &d&lRUNIC REALMS&r" +
                "\n              &a&l1.9 - The Second Age!");
        MinecraftServer.getServer().setMotd(motd);
    }

    /*
    Prevent memory leaks
     */
    public void onDisable() {
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
        cacheManager = null;
        databaseManager = null;
        groupManager = null;
        partyChannel = null;
        skillTreeManager = null;
        statManager = null;
        threeDManager = null;
        runicShopManager = null;
        // dungeon shops
        caveShop = null;
        cavernShop = null;
        keepShop = null;
        libraryShop = null;
        cryptsShop = null;
        fortressShop = null;
    }

    @EventHandler
    public void onRunicShutdown(ServerShutdownEvent e) {
        /*
        Save current state of player data
         */
        getLogger().info(" §cRunicCore has been disabled.");
        getCacheManager().saveCaches(); // save player data
        //getCacheManager().saveQueuedFiles(false, false, CacheSaveReason.SERVER_SHUTDOWN); // saves SYNC
        getCacheManager().getCacheSavingTask().cancel(); // cancel cache saving queue
        getCacheManager().saveAllCachedPlayers(CacheSaveReason.SERVER_SHUTDOWN); // saves SYNC
        /*
        Notify RunicRestart
         */
        RunicRestartApi.markPluginSaved("core");
    }

    private void loadConfig() {
        getConfig().options().copyDefaults(true);
        saveConfig();
    }

    private void registerEvents() {

        PluginManager pm = this.getServer().getPluginManager();

        pm.registerEvents(RunicCore.getCacheManager(), this);
        pm.registerEvents(RunicCore.getScoreboardHandler(), this);
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
        pm.registerEvents(new WeaponCDListener(), this);
        pm.registerEvents(new ArmorTypeListener(), this);
        pm.registerEvents(new PlayerJoinListener(), this);
        pm.registerEvents(new ManaListener(), this);
        pm.registerEvents(new PlayerLevelListener(), this);
        pm.registerEvents(new HelmetListener(), this);
        pm.registerEvents(new CraftingListener(), this);
        pm.registerEvents(new MobMechanicsListener(), this);
        pm.registerEvents(new CombatListener(), this);
        pm.registerEvents(new PlayerRegenListener(), this);
        pm.registerEvents(new PlayerMenuListener(), this);
        pm.registerEvents(new BlockBreakListener(), this);
        pm.registerEvents(new MinLevelListener(), this);
        pm.registerEvents(new PlayerHungerManager(), this);
        pm.registerEvents(new KeyClickListener(), this);
        pm.registerEvents(new WorldChangeListener(), this);
        pm.registerEvents(new GoldPouchListener(), this);
        pm.registerEvents(new MountListener(), this);
        pm.registerEvents(new LootChestListener(), this);
        pm.registerEvents(new SoulboundListener(), this);
        pm.registerEvents(new HearthstoneListener(), this);
        //pm.registerEvents(new ScrapperListener(), this);
        pm.registerEvents(new OffhandListener(), this);
        pm.registerEvents(new CharacterManager(), this);
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
        pm.registerEvents(partyManager, this);
        CharacterGuiManager.initIcons();
        partyChannel = new PartyChannel();
        RunicChat.getRunicChatAPI().registerChatChannel(partyChannel);
    }

    // TODO: replace ALL commands w ACF
    private void registerCommands() {

        // bigger commands get their own methods
        registerSetCommands();

        // currency
        CurrencySC currencySC = new CurrencySC();
        getCommand("currency").setExecutor(currencySC);
        currencySC.addCommand(Arrays.asList("give"), new CurrencyGive(currencySC));
        currencySC.addCommand(Arrays.asList("pouch"), new CurrencyPouch(currencySC));
        // currencySC.addCommand(Arrays.asList("scrapper"), new ItemScrapperCMD(currencySC));

        // experience
        CheckExpCMD checkExpCMD = new CheckExpCMD();
        getCommand("experience").setExecutor(checkExpCMD);
        getCommand("exp").setExecutor(checkExpCMD);

        // mana commands
        Mana mana = new Mana();
        getCommand("mana").setExecutor(mana);

        // runic give commands
        RunicGiveSC giveItemSC = new RunicGiveSC();
        getCommand("runicgive").setExecutor(giveItemSC);
        giveItemSC.addCommand(Arrays.asList("experience", "exp"), new ClassExpCMD(giveItemSC));
        giveItemSC.addCommand(Arrays.asList("profexperience", "profexp"), new ProfExpCMD(giveItemSC));
        giveItemSC.addCommand(Arrays.asList("item"), new ItemCMD(giveItemSC));

        // npc build
        NPCBuilderSC builderSC = new NPCBuilderSC();
        getCommand("npcbuilder").setExecutor(builderSC);
        builderSC.addCommand(Arrays.asList("build"), new Build(builderSC));

        // travel
        TravelSC travelSC = new TravelSC();
        getCommand("travel").setExecutor(travelSC);
        travelSC.addCommand(Arrays.asList("fast"), new FastTravel(travelSC));

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

    private void registerSetCommands() {

        SetSC setSC = new SetSC();
        getCommand("set").setExecutor(setSC);
        setSC.addCommand(Arrays.asList("class"), new SetClassCMD(setSC));
        setSC.addCommand(Arrays.asList("hearthstone"), new HearthstoneCMD(setSC));
        setSC.addCommand(Arrays.asList("level"), new SetLevelCMD(setSC));
        setSC.addCommand(Arrays.asList("proflevel"), new SetProfLevelCMD(setSC));
    }
}
