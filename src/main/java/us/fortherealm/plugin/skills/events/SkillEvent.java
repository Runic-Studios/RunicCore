package us.fortherealm.plugin.skills.events;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import us.fortherealm.plugin.skills.Skill;


public class SkillEvent extends Event implements Cancellable {
	
	private static final HandlerList handlers = new HandlerList();
	
	private Skill skill;
	private boolean isCancelled;
	
	public SkillEvent(Skill skill) {
		this.skill = skill;
	}
	
	@Override
	public boolean isCancelled() {
		return isCancelled;
	}
	
	@Override
	public void setCancelled(boolean cancel) {
		this.isCancelled = cancel;
	}
	
	public Skill getSkill() {
		return this.skill;
	}
	
	@Override
	public HandlerList getHandlers() {
		return handlers;
	}
	
	public static HandlerList getHandlerList() {
		return handlers;
	}
	
}
