package us.fortherealm.plugin;

import org.bukkit.plugin.PluginManager;
import us.fortherealm.plugin.classes.ExpListener;
import us.fortherealm.plugin.command.subcommands.party.*;
import us.fortherealm.plugin.command.subcommands.skills.Test;
//import us.fortherealm.plugin.command.supercommands.ArtifactSC;
import us.fortherealm.plugin.command.supercommands.PartySC;
import us.fortherealm.plugin.command.supercommands.SkillSC;
import us.fortherealm.plugin.healthbars.Healthbars;
import us.fortherealm.plugin.listeners.*;
import us.fortherealm.plugin.nametags.NameTagChanger;
import us.fortherealm.plugin.nametags.PlayerNameManager;
import us.fortherealm.plugin.outlaw.OutlawManager;
import us.fortherealm.plugin.parties.PartyDamageListener;
import us.fortherealm.plugin.parties.PartyDisconnect;
import us.fortherealm.plugin.parties.PartyManager;
import org.bukkit.plugin.java.JavaPlugin;
import us.fortherealm.plugin.playerlist.TabListManager;
import us.fortherealm.plugin.scoreboard.ScoreboardHandler;
import us.fortherealm.plugin.scoreboard.ScoreboardListener;
import us.fortherealm.plugin.skills.caster.itemstack.CasterItemStack;
import us.fortherealm.plugin.skills.caster.itemstack.PlayerInteractWithCasterItemStack;
import us.fortherealm.plugin.skills.listeners.impact.ImpactListenerObserver;
import us.fortherealm.plugin.skills.skilltypes.archer.defensive.ParryListener;

import java.util.Arrays;

public class Main extends JavaPlugin {

    // wrappers
    private static Main instance;
    private static PartyManager partyManager;
    private static NameTagChanger nameTagChanger;
    private static TabListManager tabListManager;

    // getters for wrappers
    public static Main getInstance() { return instance; }
    public static PartyManager getPartyManager() { return partyManager; }
    public static TabListManager getTabListManager() { return tabListManager; }
    //public static CooldownManager getCooldownManager(){ return cooldownManager; }

    public void onEnable() {

        instance = this;
        partyManager = new PartyManager();
        tabListManager = new TabListManager(this);

        getLogger().info(" §aFTRCore has been enabled.");

        this.registerEvents();
        this.loadConfig();
        
        this.registerCommands();

        CasterItemStack.startCooldownTask();
    }
    
    public void onDisable() {
        getLogger().info(" has been disabled.");
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
        pm.registerEvents(new DeathListener(), this);
        pm.registerEvents(new ScoreboardHandler(), this);
        pm.registerEvents(new ScoreboardListener(), this);
        pm.registerEvents(new Healthbars(), this);
        pm.registerEvents(new ArtifactListener(), this);
        pm.registerEvents(new RuneListener(), this);
        pm.registerEvents(new HearthstoneListener(), this);
        pm.registerEvents(new DurabilityListener(), this);
        pm.registerEvents(new SneakListener(), this);
        pm.registerEvents(new HealthScaleListener(), this);
        pm.registerEvents(new BowListener(), this);
        pm.registerEvents(new ResourcePackListener(), this);
        pm.registerEvents(new PlayerQuitListener(), this);
        pm.registerEvents(new PartyDisconnect(), this);
        pm.registerEvents(new PlayerNameManager(), this);
        pm.registerEvents(new PartyDamageListener(), this);
        pm.registerEvents(new OutlawManager(), this);
        pm.registerEvents(new PlayerInteractWithCasterItemStack(), this);
        //pm.registerEvents(new PlayerListManager(), this);
        pm.registerEvents(new ImpactListenerObserver(), this);
        pm.registerEvents(new ExpListener(), this);
        pm.registerEvents(new ParryListener(), this);
    }
    
    private void registerCommands() {
        registerSkillCommands();
        registerPartyCommands();
    }
    
    private void registerSkillCommands() {
        SkillSC skillSC = new SkillSC();
        getCommand("skill").setExecutor(skillSC);
        
        skillSC.addCommand(Arrays.asList("test"), new Test(skillSC));
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
   
    }
}
