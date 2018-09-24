package us.fortherealm.plugin.skills.skilltypes.targeting;

import org.bukkit.entity.Player;
import us.fortherealm.plugin.skills.Skill;

public class TargetingSkill<T> extends Skill {
	
	private T target;
	
	public TargetingSkill(Player player, String name, String description, double cooldown) {
		this(player, name, description, cooldown, null);
	}
	
	public TargetingSkill(Player player, String name, String description, double cooldown, T target) {
		super(player, name, description, cooldown);
		this.target = target;
	}
	
	public T getTarget() {
		return target;
	}
	
}
