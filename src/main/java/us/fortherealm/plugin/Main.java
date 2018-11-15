package us.fortherealm.plugin;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
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
import us.fortherealm.plugin.playerlist.PlayerListManager;
import us.fortherealm.plugin.scoreboard.ScoreboardHandler;
import us.fortherealm.plugin.scoreboard.ScoreboardListener;
//import us.fortherealm.plugin.skills.caster.itemstack.CooldownManager;
import us.fortherealm.plugin.skills.caster.itemstack.CasterItemStack;
import us.fortherealm.plugin.skills.caster.itemstack.PlayerInteractWithCasterItemStack;
import us.fortherealm.plugin.skills.listeners.impact.ImpactListenerObserver;

import java.util.Arrays;

public class Main extends JavaPlugin {

    private static Main instance;
    private static PartyManager partyManager;
    private static NameTagChanger nameTagChanger;
    //private static CooldownManager cooldownManager;

    public static Main getInstance() { return instance; }
    public static PartyManager getPartyManager() { return partyManager; }
    //public static CooldownManager getCooldownManager(){ return cooldownManager; }

    public void onEnable() {

        instance = this;
        partyManager = new PartyManager();

        getLogger().info(" §aFTRCore has been enabled.");

        this.registerEvents();
        this.loadConfig();
        
        this.registerCommands();

        CasterItemStack.startCooldownTask();
    }
    
    public void onDisable() {

        // reset player's game profiles from their changed names to their stored names
        for (Player online : Bukkit.getOnlinePlayers()) {

            // grab the player's stored name
            // convert it to a string
            Object storedName = this.getConfig().get(online.getUniqueId() + ".info.name");
            String nameToString = storedName.toString();

            // set the player's name back to their stored name
            nameTagChanger.changeNameGlobal(online, nameToString);
        }

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
        pm.registerEvents(new FireBowEvent(), this);
        pm.registerEvents(new ResourcePackListener(), this);
        pm.registerEvents(new PlayerQuitListener(), this);
        pm.registerEvents(new PartyDisconnect(), this);
        pm.registerEvents(new PlayerNameManager(), this);
        pm.registerEvents(new PartyDamageListener(), this);
        pm.registerEvents(new OutlawManager(), this);
        pm.registerEvents(new PlayerInteractWithCasterItemStack(), this);
        pm.registerEvents(new PlayerListManager(), this);
        pm.registerEvents(new ImpactListenerObserver(), this);
    }
    
    private void registerCommands() {
        registerSkillCommands();
        registerPartyCommands();

        // TODO: change super command to 'info', add 'artifact, rune, etc.' as sub commands
        //ArtifactSC artifactInfo = new ArtifactSC();
       // getCommand("artifactinfo").setExecutor(artifactInfo);
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
