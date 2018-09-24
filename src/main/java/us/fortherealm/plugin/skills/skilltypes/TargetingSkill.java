package us.fortherealm.plugin.skills.skilltypes;

import us.fortherealm.plugin.skills.Skill;

public class TargetingSkill<T> extends Skill {
	
	private T target;
	
	public TargetingSkill(String name, String description) {
		this(name, description, null);
	}
	
	public TargetingSkill(String name, String description, T target) {
		super(name, description);
		this.target = target;
	}
	
	public T getTarget() {
		return target;
	}
	
}
