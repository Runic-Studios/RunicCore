package us.fortherealm.plugin;

import org.bukkit.plugin.PluginManager;
import us.fortherealm.plugin.command.subcommands.party.*;
import us.fortherealm.plugin.command.subcommands.skills.Test;
import us.fortherealm.plugin.command.supercommands.PartySC;
import us.fortherealm.plugin.command.supercommands.SkillSC;
import us.fortherealm.plugin.healthbars.Healthbars;
import us.fortherealm.plugin.listeners.*;
import us.fortherealm.plugin.parties.PartyManager;
import org.bukkit.plugin.java.JavaPlugin;
import us.fortherealm.plugin.scoreboard.ScoreboardHealthListener;
import us.fortherealm.plugin.scoreboard.ScoreboardListener;
import us.fortherealm.plugin.skills.caster.itemstack.PlayerInteractWithCasterItemStack;
import us.fortherealm.plugin.skills.listeners.SkillListenerObserver;

import java.util.Arrays;

public class Main extends JavaPlugin {

    private static Main instance;
    private static PartyManager partyManager;

    public void onEnable() {

        instance = this;
        partyManager = new PartyManager();

        getLogger().info(" §aFTRCore has been enabled.");

        this.registerEvents();
        this.loadConfig();
        
        this.registerCommands();
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

        pm.registerEvents(new FirstJoinEvent(), this);
        pm.registerEvents(new DeathEvent(), this);
        pm.registerEvents(new ScoreboardListener(), this);
        pm.registerEvents(new ScoreboardHealthListener(), this);
        pm.registerEvents(new SheepDyeEvent(), this);
        pm.registerEvents(new Healthbars(), this);
        pm.registerEvents(new ArtifactSlotEvent(), this);
        pm.registerEvents(new RuneSlotEvent(), this);
        pm.registerEvents(new HearthstoneEvent(), this);
        pm.registerEvents(new DurabilityEvent(), this);
        pm.registerEvents(new EquipArtifactEvent(), this);
        pm.registerEvents(new EquipRuneEvent(), this);
        pm.registerEvents(new HealthScaleListener(), this);
        pm.registerEvents(new FireBowEvent(), this);
        pm.registerEvents(new ResourcePackEvent(), this);
        pm.registerEvents(new LogoutEvent(), this);

        pm.registerEvents(new PlayerInteractWithCasterItemStack(), this);
        pm.registerEvents(new SkillListenerObserver(), this);
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
        partySC.addCommand(Arrays.asList("join"), new Join(partySC));
        partySC.addCommand(Arrays.asList("kick"), new Kick(partySC));
        partySC.addCommand(Arrays.asList("leave", "exit"), new Leave(partySC));
   
    }

    public static Main getInstance() { return instance; }

    public static PartyManager getPartyManager() { return partyManager; }
    
}
