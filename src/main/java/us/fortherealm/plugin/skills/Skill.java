package us.fortherealm.plugin.skills;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import us.fortherealm.plugin.Main;
import us.fortherealm.plugin.events.gameplay.skills.SkillCastEvent;
import us.fortherealm.plugin.skills.listeners.impact.ImpactListener;
import us.fortherealm.plugin.skills.listeners.impact.ImpactListenerObserver;

public abstract class Skill implements ISkill {
	
	// ************* VERY IMPORTANT *************
	// When extending anything from the SkillAPI,
	// PLEASE read the tutorial at tinyurl.com/SkillsTut
	// ************* VERY IMPORTANT *************

	protected static final double REFRESH_RATE = 0.1;

	private static long nextUniqueId = 0;

	private long uniqueId;
	private String name;
	private String description;
	private Player player;
	private String playerName;
	private int cooldown;

	private SkillCastEvent skiilCastEvent;

	public Skill(String name, String description, int cooldown) {//double cooldown
		this.name = name;
		this.description = description;
		this.uniqueId = nextUniqueId++;
		this.cooldown = cooldown;
	}
	
	@Override
	public final void executeEntireSkill(Player player) {
		SkillCastEvent event = new SkillCastEvent(this);
		this.skiilCastEvent = event;
		Bukkit.getPluginManager().callEvent(event);
		
		if(event.isCancelled())
			return;
		
		this.player = player;
		this.playerName = Main.getInstance().getConfig().get(player.getUniqueId() + ".info.name").toString();
		
		executeSkill();
		executeSkillCleanUp();
	}

	@Override
	public boolean equals(Object object) {
		if(!(object instanceof Skill))
			return false;
		return this.uniqueId == ((Skill) object).uniqueId;
	}

	protected void executeSkill() {}
	
	private void executeSkillCleanUp() {
		if(this instanceof ImpactListener)
			ImpactListenerObserver.addActiveSkillListener((ImpactListener) this);
	}

	public SkillCastEvent getSkillCastEvent() {
		return skiilCastEvent;
	}

	public Player getPlayer() {
		return player;
	}

	public String getPlayerName() { return this.playerName; }
	
	public String getName() {
		return this.name;
	}
	
	@Override
	public String getDescription() {
		return this.description;
	}

    //@Override
    public int getCooldown() {
        return this.cooldown;
    }
}
