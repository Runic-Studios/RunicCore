package us.fortherealm.plugin.skills.skilltypes;

import org.bukkit.entity.Player;
import us.fortherealm.plugin.skills.Skill;

public class NonTargetingSkill extends Skill {
	
	// This class primarily exists to be the other option for targeting skills
	
	public NonTargetingSkill(String name, String description) {
		super(name, description);
	}
	
}
