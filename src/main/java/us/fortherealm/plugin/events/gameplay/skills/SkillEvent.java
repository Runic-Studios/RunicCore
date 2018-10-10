package us.fortherealm.plugin.events.gameplay.skills;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import us.fortherealm.plugin.events.RealmEvent;
import us.fortherealm.plugin.skills.Skill;


public class SkillEvent extends RealmEvent{

	private Skill skill;
	
	public SkillEvent(Skill skill) {
		this.skill = skill;
	}
}
