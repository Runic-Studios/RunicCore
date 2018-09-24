package us.fortherealm.plugin.skills;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import us.fortherealm.plugin.Main;
import us.fortherealm.plugin.skills.events.SkillCastEvent;

public abstract class Skill implements ISkill {
	
	// ************* VERY IMPORTANT *************
	// When extending anything from the SkillAPI,
	// you MUST call skillImpactEvent right before
	// your skill is actually done andthen check if
	// skillImpactEvent resulted in the skill being
	// cancelled
	// ************* VERY IMPORTANT *************
	
	private Main plugin = Main.getInstance();
	
	private String name;
	private String description;
	private Player player;
	
	private double cooldown;
	private boolean doCooldown = true;
	
	public Skill(Player player, String name, String description, double cooldown) {
		this.player = player;
		this.name = name;
		this.description = description;
		this.cooldown = cooldown;
	}
	
	@Override
	public final void executeEntireSkill(Player player) {
		executeSkillSetup(player);
		executeSkill(player);
		executeSkillCleanUp(player);
	}
	
	public void executeSkillSetup(Player player) {
		SkillCastEvent event = new SkillCastEvent(this);
		Bukkit.getPluginManager().callEvent(event);
	}
	
	public abstract void executeSkill(Player player);
	
	public void executeSkillCleanUp(Player player) {}
	
	public Player getPlayer() {
		return player;
	}
	
	public String getName() {
		return this.name;
	}
	
	@Override
	public String getDescription() {
		return this.description;
	}
	
	@Override
	public double getCooldown() {
		return (doCooldown) ? this.cooldown : 0;
	}
	
}
