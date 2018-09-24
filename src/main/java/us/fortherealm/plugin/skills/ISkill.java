package us.fortherealm.plugin.skills;

import org.bukkit.entity.Player;
import us.fortherealm.plugin.oldskills.skilltypes.SkillItemType;

public interface ISkill {
	
	String getName(); // returns the skills name
	
	String getDescription(); // returns the skills description
	
	void executeEntireSkill(Player player); // casts the skills
	
}
