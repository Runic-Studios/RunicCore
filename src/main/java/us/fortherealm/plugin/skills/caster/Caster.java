package us.fortherealm.plugin.skills.caster;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import us.fortherealm.plugin.Main;
import us.fortherealm.plugin.skills.Skill;
import us.fortherealm.plugin.skills.caster.exception.NameAlreadyBoundException;

import java.util.*;


// For now the Caster class is final. I will decide if I want to keep it this way
public final class Caster {
	
	// Names can not be duplicated because they are used in the Caster signature which must be unique
	private static List<String> registeredNames = new ArrayList<>();
	
	// Rather than creating many Bukkit Runnable cooldowns, I will use one and run all cooldown tasks inside the thread
	private static Map<Caster, Player> castersOnCooldown = new HashMap<>();
	private static boolean isTaskRunning;
	
	// Caster specific information
	private final String name;
	private final double cooldown;
	private long lastCast;
	
	private List<Skill> primarySkills = new ArrayList<>();
	private List<Skill> secondarySkills = new ArrayList<>();
	
	// The Caster constructor is protected because I don't want people accidentally instantiating it
	// People must use the ItemCasterManager instead
	protected Caster(Caster caster) {
		this.name = caster.getName();
		this.cooldown = caster.getCooldown();
		this.primarySkills = caster.getPrimarySkills();
		this.secondarySkills = caster.getSecondarySkills();
	}
	
	protected Caster(final String name, double cooldown,
	                       List<Skill> primarySkills, List<Skill> secondarySkills) throws NameAlreadyBoundException {
		if(registeredNames.contains(name.toLowerCase()))
			throw new NameAlreadyBoundException();
		registeredNames.add(name.toLowerCase());
		
		this.name = name;
		this.cooldown = cooldown;
		
		if(primarySkills != null)
			this.primarySkills.addAll(primarySkills);
		
		if(secondarySkills != null)
			this.secondarySkills.addAll(secondarySkills);
	}
	
	// For now these methods will be protected. If I see a reason to change this I will.
	protected void executeSkill(Skill skill, Player player) {
		if(this.isOnCooldown())
			return;
		
		this.lastCast = System.currentTimeMillis();
		
		displayCooldown(player);
		
		skill.executeEntireSkill(player);
	}
	
	protected void executePrimarySkills(Player player) {
		for(Skill skill : primarySkills) {
			executeSkill(skill, player);
		}
		
	}
	
	protected void executeSecondarySkills(Player player) {
		for(Skill skill : secondarySkills) {
			executeSkill(skill, player);
		}
	}
	
	// This is the cooldown thread
	private void displayCooldown(Player player) {
		synchronized (castersOnCooldown) {
			castersOnCooldown.put(this, player);
		}
		
		if(isTaskRunning)
			return;
		
		isTaskRunning = true;
		
		new BukkitRunnable() {
			
			@Override
			public void run() {
				
				Set<Caster> oldCastersOnCooldown = new HashSet<>(); // Avoids threading issues
				
				synchronized(castersOnCooldown) {
					for(Caster caster : castersOnCooldown.keySet()) {
						if(!caster.isOnCooldown()) {
							oldCastersOnCooldown.add(caster);
							continue;
						}
						
						castersOnCooldown.get(caster).spigot().sendMessage(
								ChatMessageType.ACTION_BAR,
								new TextComponent( ChatColor.YELLOW +
										caster.getName() + " is on cooldown for " +
												caster.getCurrentCooldown()/1000 + " more seconds"
								)
						);
					}
				}
				
				synchronized (castersOnCooldown) {
					for(Caster casterItem : oldCastersOnCooldown) {
						castersOnCooldown.remove(casterItem);
					}
					oldCastersOnCooldown.clear();
					
					if(castersOnCooldown.isEmpty()) {
						this.cancel();
						return;
					}
				}
				
			}
		}.runTaskTimer(Main.getInstance(), 0, 10);
		
		isTaskRunning = false;
	}
	
	// Changing this would mega-fuck the entire skill API
	public final String getSignature() {
		return "Caster." + name;
	}
	
	private boolean isOnCooldown() {
		return getCurrentCooldown() < cooldown * 1000;
	}
	
	private long getCurrentCooldown() {
		return System.currentTimeMillis() - lastCast;
	}
	
	public double getTotalCooldown() {
		return cooldown;
	}
	
	public long getLastCast() {
		return lastCast;
	}
	
	public void setLastCast(long lastCast) {
		this.lastCast = lastCast;
	}
	
	public String getName() {
		return name;
	}
	
	public double getCooldown() {
		return cooldown;
	}
	
	public List<Skill> getPrimarySkills() {
		return primarySkills;
	}
	
	public List<Skill> getSecondarySkills() {
		return secondarySkills;
	}
	
}
