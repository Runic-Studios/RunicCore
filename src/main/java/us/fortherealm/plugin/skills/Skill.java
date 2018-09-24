package us.fortherealm.plugin.skills;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import us.fortherealm.plugin.Main;

public abstract class Skill implements ISkill {
	
	private Main plugin = Main.getInstance();
	
	private String name;
	private String description;
	
	private double cooldown;
	private boolean doCooldown = true;
	
	public Skill(String name, String description, double cooldown) {
		this.name = name;
		this.description = description;
		this.cooldown = cooldown;
	}
	
	@Override
	public abstract void executeSkill(Player player);
	
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
