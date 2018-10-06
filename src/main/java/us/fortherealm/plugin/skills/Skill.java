package us.fortherealm.plugin.skills;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import us.fortherealm.plugin.skills.events.SkillCastEvent;

import java.util.HashSet;
import java.util.Set;

public abstract class Skill implements ISkill {
	
	// ************* VERY IMPORTANT *************
	// When extending anything from the SkillAPI,
	// PLEASE read the tutorial at tinyurl.com/SkillsTut
	// ************* VERY IMPORTANT *************

	private static Set<Skill> activeSkills = new HashSet<>();

	private String name;
	private String description;
	private Player player;

	private SkillCastEvent skiilCastEvent;
	
	public Skill(String name, String description) {
		this.name = name;
		this.description = description;
	}
	
	@Override
	public final void executeEntireSkill(Player player) {
		SkillCastEvent event = new SkillCastEvent(this);
		this.skiilCastEvent = event;
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

	public SkillCastEvent getSkillCastEvent() {
		return skiilCastEvent;
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

	public synchronized static Set<Skill> getActiveSkills() {
		return activeSkills;
	}

	public synchronized static void addActiveSkill(Skill skill) {
		activeSkills.add(skill);
	}

	public synchronized static void delActiveSkill(Skill skill) {
		activeSkills.remove(skill);
	}

}
