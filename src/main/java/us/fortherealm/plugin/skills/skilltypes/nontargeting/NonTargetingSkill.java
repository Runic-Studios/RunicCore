package us.fortherealm.plugin.skills.skilltypes.nontargeting;

import org.bukkit.entity.Player;
import us.fortherealm.plugin.skills.Skill;

public class NonTargetingSkill extends Skill {
	
	// This class primarily exists to be the other option for targeting skills
	
	public NonTargetingSkill(Player player, String name, String description, double cooldown) {
		super(player, name, description, cooldown);
	}
	
}
