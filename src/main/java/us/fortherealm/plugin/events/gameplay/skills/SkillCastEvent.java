package us.fortherealm.plugin.events.gameplay.skills;

import us.fortherealm.plugin.skills.Skill;

public class SkillCastEvent extends SkillEvent {
	
	// Called during the cast of a skill
	
	public SkillCastEvent(Skill skill) {
		super(skill);
	}
}
