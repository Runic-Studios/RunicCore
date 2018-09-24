package us.fortherealm.plugin;

import us.fortherealm.plugin.command.subcommands.party.*;
import us.fortherealm.plugin.command.subcommands.skills.Test;
import us.fortherealm.plugin.command.subcommands.skills.TestCaster;
import us.fortherealm.plugin.command.supercommands.PartySC;
import us.fortherealm.plugin.command.supercommands.SkillSC;
import us.fortherealm.plugin.healthbars.Healthbars;
import us.fortherealm.plugin.listeners.*;
import us.fortherealm.plugin.parties.PartyManager;
import us.fortherealm.plugin.oldskills.SkillUseEvent;
import us.fortherealm.plugin.oldskills.SkillManager;
import org.bukkit.plugin.java.JavaPlugin;
import us.fortherealm.plugin.skills.caster.CasterManager;
import us.fortherealm.plugin.skills.caster.itemcaster.PlayerInteractWithRegisteredItemCasterListener;

import java.util.Arrays;

public class Main extends JavaPlugin {

    private static Main instance;
    private static SkillManager skillManager;
    private static PartyManager partyManager;
    private static CasterManager casterManager;

    public void onEnable() {

        instance = this;
        skillManager = new SkillManager();
        partyManager = new PartyManager();
        casterManager = new CasterManager();

        getLogger().info(" §aFTRCore has been enabled.");

        this.registerEvents();
        this.loadConfig();
        
        this.registerCommands();
    }

    public static Main getInstance() { return instance; }

    public static SkillManager getSkillManager(){
        return skillManager;
    }

    public static PartyManager getPartyManager() { return partyManager; }

    public static CasterManager getCasterManager() {
        return casterManager;
    }
    
    public void onDisable() {
        getLogger().info(" has been disabled.");
        skillManager = null;
        partyManager = null;
        instance = null;
    }

    private void loadConfig() {
        getConfig().options().copyDefaults(true);
        saveConfig();
    }

    private void registerEvents() {
        getServer().getPluginManager().registerEvents(new SkillUseEvent(), this);
        getServer().getPluginManager().registerEvents(new FirstJoinEvent(), this);
        getServer().getPluginManager().registerEvents(new DeathEvent(), this);
        getServer().getPluginManager().registerEvents(new ScoreboardListener(), this);
        getServer().getPluginManager().registerEvents(new ScoreboardHealthListener(), this);
        getServer().getPluginManager().registerEvents(new SheepDyeEvent(), this);
        getServer().getPluginManager().registerEvents(new Healthbars(), this);
        getServer().getPluginManager().registerEvents(new ArtifactSlotEvent(), this);
        getServer().getPluginManager().registerEvents(new RuneSlotEvent(), this);
        getServer().getPluginManager().registerEvents(new HearthstoneEvent(), this);
        getServer().getPluginManager().registerEvents(new DurabilityEvent(), this);
        getServer().getPluginManager().registerEvents(new EquipArtifactEvent(), this);
        getServer().getPluginManager().registerEvents(new EquipRuneEvent(), this);
        getServer().getPluginManager().registerEvents(new HealthScaleListener(), this);
        getServer().getPluginManager().registerEvents(new FireBowEvent(), this);
        getServer().getPluginManager().registerEvents(new ResourcePackEvent(), this);
        getServer().getPluginManager().registerEvents(new LogoutEvent(), this);
        
        getServer().getPluginManager().registerEvents(new PlayerInteractWithRegisteredItemCasterListener(), this);
    }
    
    private void registerCommands() {
        registerSkillCommands();
        registerPartyCommands();
    }
    
    private void registerSkillCommands() {
        SkillSC skillSC = new SkillSC();
        getCommand("skill").setExecutor(skillSC);
        
        skillSC.addCommand(Arrays.asList("testSkill"), new Test(skillSC));
        skillSC.addCommand(Arrays.asList("test"), new TestCaster(skillSC));
    }
    
    private void registerPartyCommands() {
        
        PartySC partySC = new PartySC();
        getCommand("party").setExecutor(partySC);
        
        partySC.addCommand(Arrays.asList("create"), new Create(partySC));
        partySC.addCommand(Arrays.asList("disband", "end"), new Disband(partySC));
        partySC.addCommand(Arrays.asList("help"), new Help(partySC));
        partySC.addCommand(Arrays.asList("invite", "add"), new Invite(partySC));
        partySC.addCommand(Arrays.asList("join"), new Join(partySC));
        partySC.addCommand(Arrays.asList("kick"), new Kick(partySC));
        partySC.addCommand(Arrays.asList("leave", "exit"), new Leave(partySC));
   
    }
    
}
