package us.fortherealm.plugin;

import us.fortherealm.plugin.command.CommandListener;
import us.fortherealm.plugin.healthbars.Healthbars;
import us.fortherealm.plugin.listeners.HealthScaleListener;
import us.fortherealm.plugin.listeners.ScoreboardHealthListener;
import us.fortherealm.plugin.listeners.ScoreboardListener;
import us.fortherealm.plugin.skill.SkillUseEvent;
import us.fortherealm.plugin.skill.SkillManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Scoreboard;
import us.fortherealm.plugin.events.*;

public class Main extends JavaPlugin {

    private static Main instance;
    private static Scoreboard scoreboard;
    private static SkillManager skillManager;
    private static CommandListener commandListener;

    public void onEnable() {

        instance = this;
        skillManager = new SkillManager();
        commandListener = new CommandListener();

        scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();

        getLogger().info(" §aFTRCore has been enabled.");

        this.registerEvents();
        this.loadConfig();
    }

    public static Main getInstance() { return instance; }

    public static SkillManager getSkillManager(){
        return skillManager;
    }

    public static CommandListener getCommandListener() {
        return commandListener;
    }

    public static Scoreboard getScoreboard() { return scoreboard; }

    public void onDisable() {
        getLogger().info(" has been disabled.");
        skillManager = null;
        commandListener = null;
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
    }
}
