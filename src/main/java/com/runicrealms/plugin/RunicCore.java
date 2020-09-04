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
import com.runicrealms.plugin.dungeons.WorldChangeListener;
import com.runicrealms.plugin.group.GroupChannel;
import com.runicrealms.plugin.group.GroupManager;
import com.runicrealms.plugin.healthbars.MobHealthBars;
import com.runicrealms.plugin.healthbars.MobHealthManager;
import com.runicrealms.plugin.item.BossTagger;
import com.runicrealms.plugin.item.HelmetListener;
import com.runicrealms.plugin.item.MobTagger;
import com.runicrealms.plugin.item.SoulboundListener;
import com.runicrealms.plugin.item.commands.CurrencyGive;
import com.runicrealms.plugin.item.commands.CurrencyPouch;
import com.runicrealms.plugin.item.commands.HearthstoneCMD;
import com.runicrealms.plugin.item.commands.ItemCMD;
import com.runicrealms.plugin.item.goldpouch.GoldPouchListener;
import com.runicrealms.plugin.item.hearthstone.HearthstoneListener;
import com.runicrealms.plugin.item.lootchests.LootChestListener;
import com.runicrealms.plugin.item.lootchests.LootChestManager;
import com.runicrealms.plugin.item.mounts.MountListener;
import com.runicrealms.plugin.item.scrapper.ItemScrapperCMD;
import com.runicrealms.plugin.item.scrapper.ScrapperListener;
import com.runicrealms.plugin.item.shops.ShopManager;
import com.runicrealms.plugin.listeners.*;
import com.runicrealms.plugin.npc.Build;
import com.runicrealms.plugin.npc.NPCBuilderSC;
import com.runicrealms.plugin.party.PartyChannel;
import com.runicrealms.plugin.party.PartyCommand;
import com.runicrealms.plugin.party.PartyDamageListener;
import com.runicrealms.plugin.party.PartyManager;
import com.runicrealms.plugin.player.*;
import com.runicrealms.plugin.player.cache.CacheManager;
import com.runicrealms.plugin.player.combat.CombatListener;
import com.runicrealms.plugin.player.combat.CombatManager;
import com.runicrealms.plugin.player.combat.ExpListener;
import com.runicrealms.plugin.player.combat.PlayerLevelListener;
import com.runicrealms.plugin.player.commands.*;
import com.runicrealms.plugin.player.gear.OffhandListener;
import com.runicrealms.plugin.player.mana.ManaListener;
import com.runicrealms.plugin.player.mana.RegenManager;
import com.runicrealms.plugin.player.outlaw.OutlawManager;
import com.runicrealms.plugin.player.outlaw.SetOutlawCMD;
import com.runicrealms.plugin.player.outlaw.SpeedListener;
import com.runicrealms.plugin.scoreboard.ScoreboardHandler;
import com.runicrealms.plugin.scoreboard.ScoreboardListener;
import com.runicrealms.plugin.shop.BoostCMD;
import com.runicrealms.plugin.spellapi.SpellManager;
import com.runicrealms.plugin.spellapi.SpellUseEvent;
import com.runicrealms.plugin.tablist.TabListManager;
import com.runicrealms.plugin.tutorial.TutorialCMD;
import com.runicrealms.plugin.utilities.ColorUtil;
import com.runicrealms.plugin.utilities.FilterUtil;
import com.runicrealms.plugin.utilities.PlaceholderAPI;
import com.runicrealms.runicrestart.api.RunicRestartApi;
import com.runicrealms.runicrestart.api.ServerShutdownEvent;
import net.minecraft.server.v1_15_R1.MinecraftServer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.Arrays;

public class RunicCore extends JavaPlugin implements Listener {

    // handlers
    private static RunicCore instance;
    private static CombatManager combatManager;
    private static LootChestManager lootChestManager;
    private static RegenManager regenManager;
    private static MobHealthManager mobHealthManager;
    private static PartyManager partyManager;
    private static ScoreboardHandler scoreboardHandler;
    private static SpellManager spellManager;
    private static TabListManager tabListManager;
    private static MobTagger mobTagger;
    private static BossTagger bossTagger;
    private static ShopManager shopManager;
    private static CacheManager cacheManager;
    private static OutlawManager outlawManager;
    private static ProtocolManager protocolManager;
    private static DatabaseManager databaseManager;
    private static PartyChannel partyChannel;
    private static GroupManager groupManager;
    private static GroupChannel groupChannel;
    private static PaperCommandManager commandManager;

    // getters for handlers
    public static RunicCore getInstance() { return instance; }
    public static CombatManager getCombatManager() { return combatManager; }
    public static RegenManager getRegenManager() { return regenManager; }
    public static LootChestManager getLootChestManager() { return lootChestManager; }
    public static PartyManager getPartyManager() { return partyManager; }
    public static ScoreboardHandler getScoreboardHandler() { return scoreboardHandler; }
    public static SpellManager getSpellManager() { return spellManager; }
    public static TabListManager getTabListManager() { return tabListManager; }
    public static MobTagger getMobTagger() { return mobTagger; }
    public static BossTagger getBossTagger() { return bossTagger; }
    public static ShopManager getShopManager() { return shopManager; }
    public static CacheManager getCacheManager() { return cacheManager; }
    public static OutlawManager getOutlawManager() { return outlawManager; }
    public static ProtocolManager getProtocolManager() { return protocolManager; }
    public static DatabaseManager getDatabaseManager() { return databaseManager; }
    public static PartyChannel getPartyChatChannel() { return partyChannel; }
    public static GroupManager getGroupManager() { return groupManager; }
    public static GroupChannel getGroupChatChannel() { return groupChannel; }
    public static PaperCommandManager getCommandManager() {
        return commandManager;
    }

    public void onEnable() {
        // Load config defaults
        this.loadConfig();

        // instantiate everything we need
        instance = this;
        combatManager = new CombatManager();
        lootChestManager = new LootChestManager();
        regenManager = new RegenManager();
        mobHealthManager = new MobHealthManager();
        partyManager = new PartyManager();
        scoreboardHandler = new ScoreboardHandler();
        spellManager = new SpellManager();
        tabListManager = new TabListManager(this);
        mobTagger = new MobTagger();
        bossTagger = new BossTagger();
        shopManager = new ShopManager();
        cacheManager = new CacheManager();
        outlawManager = new OutlawManager();
        protocolManager = ProtocolLibrary.getProtocolManager();
        databaseManager = new DatabaseManager();
        groupManager = new GroupManager();
        commandManager = new PaperCommandManager(this);
        commandManager.registerCommand(new PartyCommand());
        //commandManager.registerCommand(new GroupCommand());
        commandManager.getCommandConditions().addCondition("is-player", context -> {
            if (!(context.getIssuer().getIssuer() instanceof Player)) throw new ConditionFailedException("This command cannot be run from console!");
        });
        commandManager.getCommandConditions().addCondition("is-op", context -> {
            if (!context.getIssuer().getIssuer().isOp()) throw new ConditionFailedException("You must be an operator to run this command!");
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

        // clean any stray armor stands, evokers
        mobHealthManager.insurancePolicy();
        mobHealthManager.fullClean();

        // motd
        String motd = ColorUtil.format("                    &d&lRUNIC REALMS&r" +
                "\n            &f&l1.8 - The Dungeons Patch!");
        MinecraftServer.getServer().setMotd(motd);
    }

    public void onDisable() {
        /*
        let's prevent memory leaks, shall we?
         */
        combatManager = null;
        instance = null;
        lootChestManager = null;
        regenManager = null;
        mobHealthManager = null;
        partyManager = null;
        scoreboardHandler = null;
        spellManager = null;
        tabListManager = null;
        mobTagger = null;
        bossTagger = null;
        shopManager = null;
        cacheManager = null;
        outlawManager = null;
        databaseManager = null;
        groupManager = null;
        partyChannel = null;
    }

    @EventHandler
    public void onRunicShutdown(ServerShutdownEvent e) {
        /*
        Save current state of player data
         */
        getLogger().info(" §cRunicCore has been disabled.");
        getCacheManager().saveCaches(); // save player data
        getCacheManager().saveQueuedFiles(false, false); // saves SYNC
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
        pm.registerEvents(new SpellUseEvent(), this);
        pm.registerEvents(new WeaponCDListener(), this);
        pm.registerEvents(new ArmorTypeListener(), this);
        pm.registerEvents(new PlayerJoinListener(), this);
        pm.registerEvents(new ManaListener(), this);
        pm.registerEvents(new PlayerLevelListener(), this);
        pm.registerEvents(new HelmetListener(), this);
        pm.registerEvents(new CraftingListener(), this);
        pm.registerEvents(new MobHealthBars(), this);
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
        pm.registerEvents(new ScrapperListener(), this);
        pm.registerEvents(new MobBurnListener(), this);
        pm.registerEvents(new OffhandListener(), this);
        pm.registerEvents(new SpeedListener(), this);
        pm.registerEvents(new CharacterManager(), this);
        pm.registerEvents(new CharacterGuiManager(), this);
        pm.registerEvents(new GroupManager(), this);
        pm.registerEvents(new SwapHandsListener(), this);
        pm.registerEvents(new EnvironmentDMGListener(), this);
        pm.registerEvents(new RunicExpListener(), this);
        pm.registerEvents(partyManager, this);
        pm.registerEvents(groupManager, this);
        groupManager.registerGuiEvents();
        CharacterGuiManager.initIcons();
        partyChannel = new PartyChannel();
        RunicChat.getRunicChatAPI().registerChatChannel(partyChannel);
        groupChannel = new GroupChannel();
        RunicChat.getRunicChatAPI().registerChatChannel(groupChannel);
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
        currencySC.addCommand(Arrays.asList("scrapper"), new ItemScrapperCMD(currencySC));

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

        Bukkit.getPluginCommand("map").setExecutor(new MapLink());
        Bukkit.getPluginCommand("runicdamage").setExecutor(new RunicDamage());
        Bukkit.getPluginCommand("runicfirework").setExecutor(new FireworkCMD());
        Bukkit.getPluginCommand("outlaw").setExecutor(new SetOutlawCMD());
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
        setSC.addCommand(Arrays.asList("artifact"), new TutorialCMD(setSC));
    }
}
