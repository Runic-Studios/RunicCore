package us.fortherealm.plugin.skills.skilltypes;

import us.fortherealm.plugin.skills.Skill;
import us.fortherealm.plugin.skills.SkillRegistry;

public class TargetingSkill<T> extends Skill {
	
	private boolean targetIsAlly;
	private T target;
	
	public TargetingSkill(String name, String description) {
		this(name, description, false);
	}
	
	public TargetingSkill(String name, String description, boolean isTargetAnAlly) {
		this(name, description, null, false);
	}
	
	public TargetingSkill(String name, String description, T target, boolean isTargetAnAlly) {
		super(name, description);
		this.target = target;
	}
	
	public boolean targetIsAlly() {
		return targetIsAlly;
	}
	
	public void setTargetIsAlly(boolean targetIsAlly) {
		this.targetIsAlly = targetIsAlly;
	}
	
	public T getTarget() {
		return target;
	}
	
	protected void setTarget(T target) {
		this.setTarget(target);
	}
	
}
