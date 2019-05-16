package com.runicrealms.plugin;

import com.runicrealms.plugin.command.subcommands.CurrencyGive;
import com.runicrealms.plugin.command.subcommands.Skillpoint;
import com.runicrealms.plugin.command.subcommands.party.*;
import com.runicrealms.plugin.command.subcommands.set.SetClassCMD;
import com.runicrealms.plugin.command.supercommands.CurrencySC;
import com.runicrealms.plugin.command.supercommands.PartySC;
import com.runicrealms.plugin.command.supercommands.SkillpointSC;
import com.runicrealms.plugin.healthbars.MobHealthBars;
import com.runicrealms.plugin.healthbars.MobHealthManager;
import com.runicrealms.plugin.healthbars.PlayerBars;
import com.runicrealms.plugin.item.HelmetListener;
import com.runicrealms.plugin.item.artifact.ArtifactListener;
import com.runicrealms.plugin.item.hearthstone.HearthstoneListener;
import com.runicrealms.plugin.item.rune.RuneListener;
import com.runicrealms.plugin.listeners.*;
import com.runicrealms.plugin.nametags.PlayerNameManager;
import com.runicrealms.plugin.npc.Build;
import com.runicrealms.plugin.npc.NPCBuilderSC;
import com.runicrealms.plugin.outlaw.OutlawManager;
import com.runicrealms.plugin.parties.PartyDamageListener;
import com.runicrealms.plugin.parties.PartyDisconnect;
import com.runicrealms.plugin.parties.PartyManager;
import com.runicrealms.plugin.player.*;
import com.runicrealms.plugin.player.commands.Mana;
import com.runicrealms.plugin.player.commands.SetLevelCMD;
import com.runicrealms.plugin.player.commands.SetSC;
import com.runicrealms.plugin.professions.ProfManager;
import com.runicrealms.plugin.professions.WorkstationListener;
import com.runicrealms.plugin.professions.commands.ToolGive;
import com.runicrealms.plugin.professions.commands.ToolSC;
import com.runicrealms.plugin.professions.gathering.FarmingListener;
import com.runicrealms.plugin.professions.gathering.FishingListener;
import com.runicrealms.plugin.professions.gathering.MiningListener;
import com.runicrealms.plugin.professions.gathering.WCListener;
import com.runicrealms.plugin.professions.jeweler.SocketListener;
import com.runicrealms.plugin.scoreboard.ScoreboardHandler;
import com.runicrealms.plugin.scoreboard.ScoreboardListener;
import com.runicrealms.plugin.skillapi.SkillManager;
import com.runicrealms.plugin.skillapi.SkillUseEvent;
import com.runicrealms.plugin.tablist.TabListManager;
import com.runicrealms.plugin.tutorial.commands.CaptainDefeated;
import com.runicrealms.plugin.tutorial.commands.TutorialSC;
import org.bukkit.plugin.PluginManager;
import com.runicrealms.plugin.player.ExpListener;
import com.runicrealms.plugin.player.CombatManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;

public class RunicCore extends JavaPlugin {

    // handlers
    private static RunicCore instance;
    private static CombatManager combatManager;
    private static ManaManager manaManager;
    private static MobHealthManager mobHealthManager;
    private static PartyManager partyManager;
    private static ProfManager profManager;
    private static ScoreboardHandler scoreboardHandler;
    private static SkillManager skillManager;
    private static TabListManager tabListManager;

    // getters for handlers
    public static RunicCore getInstance() { return instance; }
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

        pm.registerEvents(RunicCore.getScoreboardHandler(), this);
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
