package us.fortherealm.plugin.skills.caster.itemcaster;

import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import us.fortherealm.plugin.skills.Skill;
import us.fortherealm.plugin.skills.caster.Caster;

import java.util.ArrayList;
import java.util.List;

public class ItemCaster extends Caster implements Listener {
	
	private List<Skill> primarySkills = new ArrayList<>();
	private List<Skill> secondarySkills = new ArrayList<>();
	
	private ItemStack item;
	
	public ItemCaster() {
		this(true);
	}
	
	public ItemCaster(boolean addToRegisteredItemCasterListener) {
		this(null, null, addToRegisteredItemCasterListener);
	}
	
	public ItemCaster(List<Skill> primarySkills, List<Skill> secondarySkills, boolean addToRegisteredItemCasterListener) {
		if(primarySkills != null)
			this.primarySkills = primarySkills;
		
		if(secondarySkills != null)
			this.secondarySkills = secondarySkills;
		
		if(addToRegisteredItemCasterListener)
			PlayerInteractWithRegisteredItemCasterListener.addRegisteredItemCaster(this);
	}
	
	public List<Skill> getPrimarySkills() {
		return primarySkills;
	}
	
	public void setPrimarySkills(List<Skill> primarySkills) {
		this.primarySkills = primarySkills;
	}
	
	public void addPrimarySkill(Skill skill) {
		this.primarySkills.add(skill);
	}
	
	public void delPrimarySkill(Skill skill) {
		this.primarySkills.remove(skill);
	}
	
	public List<Skill> getSecondarySkills() {
		return secondarySkills;
	}
	
	public void setSecondarySkills(List<Skill> secondarySkills) {
		this.secondarySkills = secondarySkills;
	}
	
	public void addSecondarySkill(Skill skill) {
		this.secondarySkills.add(skill);
	}
	
	public void delSecondarySkill(Skill skill) {
		this.secondarySkills.remove(skill);
	}
	
	public ItemStack getItem() {
		return item;
	}
	
	public void setItem(ItemStack item) {
		this.item = item;
	}
	
	public static ItemStack generateItemStack() {
		
		return null;
	}
	
}
