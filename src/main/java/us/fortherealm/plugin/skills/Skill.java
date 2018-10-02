package us.fortherealm.plugin.skills;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import us.fortherealm.plugin.Main;
import us.fortherealm.plugin.skills.events.SkillCastEvent;

import java.util.ArrayList;
import java.util.List;

public abstract class Skill implements ISkill, Listener {
	
	// ************* VERY IMPORTANT *************
	// When extending anything from the SkillAPI,
	// you MUST call skillImpactEvent right before
	// your skill is actually executed andthen check if
	// skillImpactEvent resulted in the skill being
	// cancelled and if so stop the execution!!!
	// ************* VERY IMPORTANT *************
	
	private Main plugin = Main.getInstance();

	private static List<Skill> activeSkills = new ArrayList<>();

	private String name;
	private String description;
	private Player player;
	
	public Skill(String name, String description) {
		this.name = name;
		this.description = description;


		if(this instanceof Listener)
			Bukkit.getServer().getPluginManager().registerEvents((Listener) this, Main.getInstance());
	}
	
	@Override
	public final void executeEntireSkill(Player player) {
		SkillCastEvent event = new SkillCastEvent(this);
		Bukkit.getPluginManager().callEvent(event);
		
		if(event.isCancelled())
			return;
		
		this.player = player;

		this.activeSkills.add(this);
		
		executeSkill();
		executeSkillCleanUp();
	}

	@Override
	public boolean equals(Object object) {
		if(!(object instanceof Skill))
			return false;
		return this.getClass().equals(((Skill) object).getClass());
	}
	
	protected void executeSkill() {}
	
	protected void executeSkillCleanUp() {}
	
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

	public static List<Skill> getActiveSkills() {
		return activeSkills;
	}

	public static void addActiveSkill(Skill skill) {
		activeSkills.add(skill);
	}

	public static void delActiveSkill(Skill skill) {
		activeSkills.remove(skill);
	}

}
