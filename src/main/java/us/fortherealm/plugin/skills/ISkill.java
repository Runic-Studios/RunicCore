package us.fortherealm.plugin.skills;

import org.bukkit.entity.Player;

public interface ISkill {
	
	String getName(); // returns the oldskills name
	
	String getDescription(); // returns the oldskills description

	//double getCooldown(); // returns the cooldown
	
	void executeEntireSkill(Player player); // casts the oldskills
	
}
