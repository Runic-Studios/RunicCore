package us.fortherealm.plugin.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import us.fortherealm.plugin.oldskills.skilltypes.Skill;

public class PlayerCastSkillEvent extends Event implements Cancellable {
	
	private Skill skill;
	private Player player;
	
	
	@Override
	public HandlerList getHandlers() {
		return null;
	}
	
	@Override
	public boolean isCancelled() {
		return false;
	}
	
	@Override
	public void setCancelled(boolean b) {
	
	}
}
