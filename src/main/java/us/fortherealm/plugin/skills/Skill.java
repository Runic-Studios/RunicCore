package us.fortherealm.plugin.skills;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import us.fortherealm.plugin.skills.events.SkillCastEvent;
import us.fortherealm.plugin.skills.listeners.SkillListener;
import us.fortherealm.plugin.skills.listeners.SkillListenerObserver;

public abstract class Skill implements ISkill {
	
	// ************* VERY IMPORTANT *************
	// When extending anything from the SkillAPI,
	// PLEASE read the tutorial at tinyurl.com/SkillsTut
	// ************* VERY IMPORTANT *************

	private static long nextUniqueId = 0;

	private long uniqueId;
	private String name;
	private String description;
	private Player player;

	private SkillCastEvent skiilCastEvent;
	
	public Skill(String name, String description) {
		this.name = name;
		this.description = description;
		this.uniqueId = nextUniqueId++;
	}
	
	@Override
	public final void executeEntireSkill(Player player) {
		SkillCastEvent event = new SkillCastEvent(this);
		this.skiilCastEvent = event;
		Bukkit.getPluginManager().callEvent(event);
		
		if(event.isCancelled())
			return;
		
		this.player = player;
		
		executeSkill();
		executeSkillCleanUp();
	}

	@Override
	public boolean equals(Object object) {
		if(!(object instanceof Skill))
			return false;
		return this.uniqueId == ((Skill) object).uniqueId;
	}
	
	protected void executeSkill() {}
	
	protected void executeSkillCleanUp() {
		if(this instanceof SkillListener)
			SkillListenerObserver.addActiveSkillListener((SkillListener) this);
	}

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

}
