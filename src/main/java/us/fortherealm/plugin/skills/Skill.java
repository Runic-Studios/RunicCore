package us.fortherealm.plugin.skills;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import us.fortherealm.plugin.Main;
import us.fortherealm.plugin.skills.events.SkillCastEvent;

public abstract class Skill implements ISkill {
	
	// If you decide to change execute command:
	//  1) remember to call super.executeCommand()
	//  2) remember to call SkillImpactEvent before the skill has cast
	//          and check if the skill was cancelled before casting the skill
	
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
	public void executeSkill(Player player) {
		SkillCastEvent event = new SkillCastEvent(this);
		Bukkit.getPluginManager().callEvent(event);
	}
	
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
