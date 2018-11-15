package us.fortherealm.plugin.skills.skilltypes;

import us.fortherealm.plugin.skills.Skill;

public class TargetingSkill<T> extends Skill {

	private boolean targetIsAlly;
	private T target;
	
	public TargetingSkill(String name, String description, int cooldown, boolean isTargetAnAlly) {
		super(name, description, cooldown);
		this.targetIsAlly = false;
	}
	
	public TargetingSkill(String name, String description, int cooldown, T target, boolean isTargetAnAlly) {
		super(name, description, cooldown);
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
	
	public void setTarget(T target) {
		this.target = target;
	}
	
}
