package us.fortherealm.plugin.skills;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import us.fortherealm.plugin.Main;
import us.fortherealm.plugin.skills.events.SkillCastEvent;

public abstract class Skill implements ISkill {
	
	// ************* VERY IMPORTANT *************
	// When extending anything from the SkillAPI,
	// you MUST call skillImpactEvent right before
	// your skill is actually executed andthen check if
	// skillImpactEvent resulted in the skill being
	// cancelled and if so stop the execution!!!
	// ************* VERY IMPORTANT *************
	
	private Main plugin = Main.getInstance();
	
	private String name;
	private String description;
	private Player player;
	
	
	
	public Skill(String name, String description) {
		this.name = name;
		this.description = description;
	}
	
	@Override
	public final void executeEntireSkill(Player player) {
		executeSkillSetup(player);
		executeSkill(player);
		executeSkillCleanUp(player);
	}
	
	public final void executeSkillSetup(Player player) {
		SkillCastEvent event = new SkillCastEvent(this);
		Bukkit.getPluginManager().callEvent(event);
		this.player = player;
	}
	
	public void executeSkill(Player player) {}
	
	public void executeSkillCleanUp(Player player) {}
	
	public Player getPlayer() {
		return player;
	}
	
	public String getName() {
		return this.name;
	}
	
	protected Plugin getPlugin() {
		return plugin;
	}
	
	@Override
	public String getDescription() {
		return this.description;
	}
	
}
