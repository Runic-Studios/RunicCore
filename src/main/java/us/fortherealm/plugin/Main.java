package us.fortherealm.plugin;

import org.bukkit.plugin.PluginManager;
import us.fortherealm.plugin.artifact.ArtifactListener;
import us.fortherealm.plugin.level.ExpListener;
import us.fortherealm.plugin.command.subcommands.party.*;
import us.fortherealm.plugin.command.supercommands.PartySC;
import us.fortherealm.plugin.healthbars.CombatManager;
import us.fortherealm.plugin.healthbars.PlayerBars;
import us.fortherealm.plugin.level.ClassLvListener;
import us.fortherealm.plugin.listeners.*;
import us.fortherealm.plugin.nametags.PlayerNameManager;
import us.fortherealm.plugin.outlaw.OutlawManager;
import us.fortherealm.plugin.parties.PartyDamageListener;
import us.fortherealm.plugin.parties.PartyDisconnect;
import us.fortherealm.plugin.parties.PartyManager;
import org.bukkit.plugin.java.JavaPlugin;
import us.fortherealm.plugin.professions.WorkstationListener;
import us.fortherealm.plugin.professions.ProfManager;
import us.fortherealm.plugin.professions.gathering.FarmingListener;
import us.fortherealm.plugin.professions.gathering.FishingListener;
import us.fortherealm.plugin.professions.gathering.MiningListener;
import us.fortherealm.plugin.professions.gathering.WCListener;
import us.fortherealm.plugin.rune.RuneListener;
import us.fortherealm.plugin.tablist.TabListManager;
import us.fortherealm.plugin.scoreboard.ScoreboardHandler;
import us.fortherealm.plugin.scoreboard.ScoreboardListener;
import us.fortherealm.plugin.skillapi.SkillManager;
import us.fortherealm.plugin.skillapi.SkillUseEvent;

import java.util.Arrays;

public class Main extends JavaPlugin {

    // handlers
    private static Main instance;
    private static CombatManager combatManager;
    private static PartyManager partyManager;
    private static ProfManager profManager;
    private static ScoreboardHandler scoreboardHandler;
    private static SkillManager skillManager;
    private static TabListManager tabListManager;

    // getters for handlers
    public static Main getInstance() { return instance; }
    public static CombatManager getCombatManager() { return combatManager; }
    public static PartyManager getPartyManager() { return partyManager; }
    public static ProfManager getProfManager() { return profManager; }
    public static ScoreboardHandler getScoreboardHandler() { return scoreboardHandler; }
    public static SkillManager getSkillManager() { return skillManager; }
    public static TabListManager getTabListManager() { return tabListManager; }

    public void onEnable() {

        // instantiate everything we need
        instance = this;
        combatManager = new CombatManager();
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
    }
    
    public void onDisable() {
        getLogger().info(" §cFTRCore has been enabled.");
        partyManager = null;
        instance = null;
    }

    private void loadConfig() {
        getConfig().options().copyDefaults(true);
        saveConfig();
    }

    private void registerEvents() {

        PluginManager pm = this.getServer().getPluginManager();

        pm.registerEvents(new PlayerJoinListener(), this);
        pm.registerEvents(Main.getScoreboardHandler(), this);
        pm.registerEvents(new ScoreboardListener(), this);
        pm.registerEvents(new ArtifactListener(), this);
        pm.registerEvents(new RuneListener(), this);
        pm.registerEvents(new HearthstoneListener(), this);
        pm.registerEvents(new DurabilityListener(), this);
        pm.registerEvents(new StavesListener(), this);
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
        pm.registerEvents(new WeaponListener(), this);
        pm.registerEvents(new PlayerBars(), this);
        pm.registerEvents(new WorkstationListener(), this);
        pm.registerEvents(new ArmorTypeListener(), this);
        pm.registerEvents(new ClassLvListener(), this);
        pm.registerEvents(new MiningListener(), this);
        pm.registerEvents(new FarmingListener(), this);
        pm.registerEvents(new WCListener(), this);
        pm.registerEvents(new FishingListener(), this);
    }
    
    private void registerCommands() {
        registerPartyCommands();
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
}
