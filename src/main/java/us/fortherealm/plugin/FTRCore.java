package us.fortherealm.plugin;

import org.bukkit.plugin.PluginManager;
import us.fortherealm.plugin.command.subcommands.CurrencyGive;
import us.fortherealm.plugin.command.subcommands.Skillpoint;
import us.fortherealm.plugin.command.supercommands.CurrencySC;
import us.fortherealm.plugin.command.supercommands.SkillpointSC;
import us.fortherealm.plugin.command.subcommands.set.SetClassCMD;
import us.fortherealm.plugin.healthbars.MobHealthBars;
import us.fortherealm.plugin.healthbars.MobHealthManager;
import us.fortherealm.plugin.item.HelmetListener;
import us.fortherealm.plugin.item.artifact.ArtifactListener;
import us.fortherealm.plugin.item.hearthstone.HearthstoneListener;
import us.fortherealm.plugin.npc.Build;
import us.fortherealm.plugin.npc.NPCBuilderSC;
import us.fortherealm.plugin.player.ExpListener;
import us.fortherealm.plugin.command.subcommands.party.*;
import us.fortherealm.plugin.command.supercommands.PartySC;
import us.fortherealm.plugin.player.CombatManager;
import us.fortherealm.plugin.healthbars.PlayerBars;
import us.fortherealm.plugin.listeners.*;
import us.fortherealm.plugin.nametags.PlayerNameManager;
import us.fortherealm.plugin.outlaw.OutlawManager;
import us.fortherealm.plugin.parties.PartyDamageListener;
import us.fortherealm.plugin.parties.PartyDisconnect;
import us.fortherealm.plugin.parties.PartyManager;
import org.bukkit.plugin.java.JavaPlugin;
import us.fortherealm.plugin.player.*;
import us.fortherealm.plugin.player.commands.SetLevelCMD;
import us.fortherealm.plugin.player.commands.Mana;
import us.fortherealm.plugin.player.commands.SetSC;
import us.fortherealm.plugin.professions.commands.ToolGive;
import us.fortherealm.plugin.professions.commands.ToolSC;
import us.fortherealm.plugin.professions.jeweler.SocketListener;
import us.fortherealm.plugin.professions.WorkstationListener;
import us.fortherealm.plugin.professions.ProfManager;
import us.fortherealm.plugin.professions.gathering.FarmingListener;
import us.fortherealm.plugin.professions.gathering.FishingListener;
import us.fortherealm.plugin.professions.gathering.MiningListener;
import us.fortherealm.plugin.professions.gathering.WCListener;
import us.fortherealm.plugin.item.rune.RuneListener;
import us.fortherealm.plugin.tablist.TabListManager;
import us.fortherealm.plugin.scoreboard.ScoreboardHandler;
import us.fortherealm.plugin.scoreboard.ScoreboardListener;
import us.fortherealm.plugin.skillapi.SkillManager;
import us.fortherealm.plugin.skillapi.SkillUseEvent;
import us.fortherealm.plugin.tutorial.ItemPickupListener;
import us.fortherealm.plugin.tutorial.commands.CaptainDefeated;
import us.fortherealm.plugin.tutorial.commands.TutorialSC;

import java.util.Arrays;

public class FTRCore extends JavaPlugin {

    // handlers
    private static FTRCore instance;
    private static CombatManager combatManager;
    private static ManaManager manaManager;
    private static MobHealthManager mobHealthManager;
    private static PartyManager partyManager;
    private static ProfManager profManager;
    private static ScoreboardHandler scoreboardHandler;
    private static SkillManager skillManager;
    private static TabListManager tabListManager;

    // getters for handlers
    public static FTRCore getInstance() { return instance; }
    public static CombatManager getCombatManager() { return combatManager; }
    public static ManaManager getManaManager() { return manaManager; }
    public static MobHealthManager getMobHealthManager() { return mobHealthManager; }
    public static PartyManager getPartyManager() { return partyManager; }
    public static ProfManager getProfManager() { return profManager; }
    public static ScoreboardHandler getScoreboardHandler() { return scoreboardHandler; }
    public static SkillManager getSkillManager() { return skillManager; }
    public static TabListManager getTabListManager() { return tabListManager; }

    public void onEnable() {

        // instantiate everything we need
        instance = this;
        combatManager = new CombatManager();
        manaManager = new ManaManager();
        mobHealthManager = new MobHealthManager();
        partyManager = new PartyManager();
        profManager = new ProfManager();
        scoreboardHandler = new ScoreboardHandler();
        skillManager = new SkillManager();
        tabListManager = new TabListManager(this);

        // enable message
        getLogger().info(" §aFTRCore has been enabled.");

        // register our events, config, commands
        this.registerEvents();
        this.loadConfig();
        this.registerCommands();

        // clean any stray healthbars
        mobHealthManager.fullClean();
    }
    
    public void onDisable() {
        getLogger().info(" §cFTRCore has been disabled.");
        // let's prevent memory leaks, shall we?
        instance = null;
        combatManager = null;
        manaManager = null;
        mobHealthManager = null;
        partyManager = null;
        scoreboardHandler = null;
        skillManager = null;
        tabListManager = null;
    }

    private void loadConfig() {
        getConfig().options().copyDefaults(true);
        saveConfig();
    }

    private void registerEvents() {

        PluginManager pm = this.getServer().getPluginManager();

        pm.registerEvents(FTRCore.getScoreboardHandler(), this);
        pm.registerEvents(new ScoreboardListener(), this);
        pm.registerEvents(new ArtifactListener(), this);
        pm.registerEvents(new RuneListener(), this);
        pm.registerEvents(new HearthstoneListener(), this);
        pm.registerEvents(new DurabilityListener(), this);
        pm.registerEvents(new StaffListener(), this);
        pm.registerEvents(new BowListener(), this);
        pm.registerEvents(new DamageListener(), this);
        pm.registerEvents(new ResourcePackListener(), this);
        pm.registerEvents(new PlayerQuitListener(), this);
        pm.registerEvents(new PartyDisconnect(), this);
        pm.registerEvents(new PlayerNameManager(), this);
        pm.registerEvents(new PartyDamageListener(), this);
        pm.registerEvents(new OutlawManager(), this);
        pm.registerEvents(new ExpListener(), this);
        pm.registerEvents(new SkillUseEvent(), this);
        pm.registerEvents(new WeaponCDListener(), this);
        pm.registerEvents(new PlayerBars(), this);
        pm.registerEvents(new WorkstationListener(), this);
        pm.registerEvents(new ArmorTypeListener(), this);
        pm.registerEvents(new MiningListener(), this);
        pm.registerEvents(new FarmingListener(), this);
        pm.registerEvents(new WCListener(), this);
        pm.registerEvents(new FishingListener(), this);
        pm.registerEvents(new SocketListener(), this);
        pm.registerEvents(new PlayerJoinListener(), this);
        pm.registerEvents(new ManaListener(), this);
        pm.registerEvents(new PlayerLevelListener(), this);
        pm.registerEvents(new HelmetListener(), this);
        pm.registerEvents(new CraftingListener(), this);
        pm.registerEvents(new ItemPickupListener(), this);
        pm.registerEvents(new MobHealthBars(), this);
        pm.registerEvents(new CombatListener(), this);
        pm.registerEvents(new PlayerRegenListener(), this);
    }
    
    private void registerCommands() {

        // bigger commands get their own methods
        registerPartyCommands();
        registerSetCommands();

        // currency
        CurrencySC currencySC = new CurrencySC();
        getCommand("currency").setExecutor(currencySC);
        currencySC.addCommand(Arrays.asList("give"), new CurrencyGive(currencySC));

        // mana commands
        Mana mana = new Mana();
        getCommand("mana").setExecutor(mana);

        // gathertool commands
        ToolSC toolSC = new ToolSC();
        getCommand("gathertool").setExecutor(toolSC);
        toolSC.addCommand(Arrays.asList("give"), new ToolGive(toolSC));

        // npc build
        NPCBuilderSC builderSC = new NPCBuilderSC();
        getCommand("npcbuilder").setExecutor(builderSC);
        builderSC.addCommand(Arrays.asList("build"), new Build(builderSC));

        // skillpoint
        SkillpointSC skillpointSC = new SkillpointSC();
        getCommand("skillpoint").setExecutor(skillpointSC);
        skillpointSC.addCommand(Arrays.asList("give"), new Skillpoint(skillpointSC));

        // tutorial
        TutorialSC tutorialSC = new TutorialSC();
        getCommand("tutorial").setExecutor(tutorialSC);
        tutorialSC.addCommand(Arrays.asList("captaindefeated"), new CaptainDefeated(tutorialSC));
    }
    
    private void registerPartyCommands() {
        
        PartySC partySC = new PartySC();
        getCommand("party").setExecutor(partySC);
        
        partySC.addCommand(Arrays.asList("create"), new Create(partySC));
        partySC.addCommand(Arrays.asList("disband", "end"), new Disband(partySC));
        partySC.addCommand(Arrays.asList("help"), new Help(partySC));
        partySC.addCommand(Arrays.asList("invite", "add"), new Invite(partySC));
        partySC.addCommand(Arrays.asList("join", "accept"), new Join(partySC));
        partySC.addCommand(Arrays.asList("kick"), new Kick(partySC));
        partySC.addCommand(Arrays.asList("leave", "exit"), new Leave(partySC));

        partySC.addCommand(Arrays.asList("prof", "profession"), new Prof(partySC));
    }

    private void registerSetCommands() {

        SetSC setSC = new SetSC();
        getCommand("set").setExecutor(setSC);
        setSC.addCommand(Arrays.asList("class"), new SetClassCMD(setSC));
        setSC.addCommand(Arrays.asList("level"), new SetLevelCMD(setSC));
    }
}
