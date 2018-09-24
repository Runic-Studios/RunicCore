package us.fortherealm.plugin.skills.caster.itemcaster;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import us.fortherealm.plugin.Main;
import us.fortherealm.plugin.skills.Skill;
import us.fortherealm.plugin.skills.caster.Caster;

import java.util.ArrayList;
import java.util.List;

public class ItemCaster extends Caster implements Listener {
	
	private List<Skill> primarySkills = new ArrayList<>();
	private List<Skill> secondarySkills = new ArrayList<>();
	
	private ItemStack item;
	
	public ItemCaster(ItemStack item, String name, ItemCaster.Type itemType, double cooldown,
	                  List<Skill> primarySkills, List<Skill> secondarySkills) {
		this(item, name, itemType, cooldown, primarySkills, secondarySkills,true);
	}
	
	public ItemCaster(ItemStack item, String name, ItemCaster.Type itemType, double cooldown,
	                  List<Skill> primarySkills, List<Skill> secondarySkills,
	                  boolean addToRegisteredItemCasterListener) {
		super(cooldown, name);
		
		if(item == null || name == null || itemType == null) {
			try {
				throw new NullPointerException();
			} catch (NullPointerException e) {
				e.printStackTrace();
				Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "ItemCaster could not be created due to null fields");
				return;
			}
		}
		
		if(primarySkills != null)
			this.primarySkills.addAll(primarySkills);
		
		if(secondarySkills != null)
			this.secondarySkills.addAll(secondarySkills);
		
		item.setItemMeta(generateItemMeta(item, name, itemType));
		this.item = item;
		
		if(addToRegisteredItemCasterListener)
			PlayerInteractWithRegisteredItemCasterListener.addRegisteredItemCaster(this);
	}
	
	public void executePrimarySkills(Player player) {
		for(Skill skill : primarySkills) {
			executeSkill(skill, player);
		}
	}
	
	public void executeSecondarySkills(Player player) {
		for(Skill skill : secondarySkills) {
			executeSkill(skill, player);
		}
	}
	
	public ItemStack getItem() {
		return item;
	}
	
	public List<Skill> getSecondarySkills() {
		return secondarySkills;
	}
	
	
	public List<Skill> getPrimarySkills() {
		return primarySkills;
	}
	
	public ItemMeta generateItemMeta(ItemStack item, String name, ItemCaster.Type itemType) {
		ItemMeta itemMeta = item.getItemMeta();
		itemMeta.setDisplayName(ChatColor.YELLOW + name);
		List<String> lore = new ArrayList<>();
		
		if(primarySkills.size() != 0) {
			
			lore.add(ChatColor.GRAY + "Primary:");
			
			for(Skill skill : primarySkills)
				lore.add(ChatColor.RED + skill.getName());
			
			lore.add("");
		}
		
		if(secondarySkills.size() != 0) {
			
			lore.add(ChatColor.GRAY + "Secondary:");
			
			for (Skill skill : secondarySkills)
				lore.add(ChatColor.DARK_RED + skill.getName());
			
			lore.add("");
			
		}
		
		lore.add(ChatColor.YELLOW + itemType.getName());
		itemMeta.setLore(lore);
		
		return itemMeta;
	}
	
	public enum Type {
		RUNE("Rune", 1),
		ARTIFACT("Artifact", 0);
		
		private String name;
		private int slot;
		
		Type(String name, int slot) {
			this.name = name;
			this.slot = slot;
		}
		
		@Override
		public String toString() {
			return getName();
		}
		
		public String getName() {
			return name;
		}
		
		public int getSlot() {
			return slot;
		}
		
	}
	
}
