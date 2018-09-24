package us.fortherealm.plugin.skills.caster;

import org.bukkit.entity.Player;

public class Caster {
	
	private Player player;
	private String name;
	private double cooldown;
	private boolean doCooldown = true;
	
	public Caster(double cooldown, String name) {
		this.cooldown = cooldown;
		this.name = name;
	}
	
	public double getCooldown() {
		return (doCooldown) ? this.cooldown : 0;
	}
	
	public void setCooldown(double cooldown) {
		this.cooldown = cooldown;
	}
	
	public boolean shouldDoCooldown() {
		return doCooldown;
	}
	
	public void setDoCooldown(boolean doCooldown) {
		this.doCooldown = doCooldown;
	}
	
	public String getName() {
		return name;
	}
	
	public void setPlayer(Player player) {
		this.player = player;
	}
	
	public Player getPlayer() {
		return player;
	}
	
}