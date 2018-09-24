package us.fortherealm.plugin.skills.events;

import us.fortherealm.plugin.skills.Skill;

public class SkillImpactEvent extends SkillEvent {
	
	// Called during collision of skill
	
	public SkillImpactEvent(Skill skill) {
		super(skill);
	}

}
