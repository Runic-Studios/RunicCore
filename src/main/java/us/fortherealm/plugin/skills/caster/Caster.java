package us.fortherealm.plugin.skills.caster;

import org.bukkit.entity.Player;
import us.fortherealm.plugin.skills.Skill;

public interface Caster {
	
	void executeSkill(Skill skill, Player player);
	
}
