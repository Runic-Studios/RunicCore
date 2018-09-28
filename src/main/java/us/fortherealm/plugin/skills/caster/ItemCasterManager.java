package us.fortherealm.plugin.skills.caster;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import us.fortherealm.plugin.skills.Skill;
import us.fortherealm.plugin.skills.caster.exception.ItemNotCasterItemException;
import us.fortherealm.plugin.skills.caster.exception.NameAlreadyBoundException;
import us.fortherealm.plugin.skills.skilltypes.defensive.Deliverance;
import us.fortherealm.plugin.skills.skilltypes.offensive.Fireball;

import java.util.*;

// This manager is NOT multi-thread friendly

public class ItemCasterManager {
	
	private Set<CasterStorage<ItemStack>> casterStorages = new HashSet<>();
	
	public ItemCasterManager() {
		createCasterStorages();
	}
	
	public final ItemMeta generateItemMeta(ItemStack item, Caster motherCaster) {
		ItemMeta itemMeta = item.getItemMeta();
		
		itemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
		
		itemMeta.setDisplayName(ChatColor.YELLOW + motherCaster.getName());
		List<String> lore = new ArrayList<>();
		
		if(motherCaster.getPrimarySkills().size() != 0) {
			
			lore.add(ChatColor.GRAY + "Primary:");
			
			for(Skill skill : motherCaster.getPrimarySkills())
				lore.add(ChatColor.RED + skill.getName());
			
			lore.add("");
		}
		
		if(motherCaster.getSecondarySkills().size() != 0) {
			
			lore.add(ChatColor.GRAY + "Secondary:");
			
			for (Skill skill : motherCaster.getSecondarySkills())
				lore.add(ChatColor.DARK_RED + skill.getName());
			
			lore.add("");
			
		}
		
		// Changing or removing this part will mega-fuck everything
		// DO NOT CHANGE THIS
		StringBuilder signature = new StringBuilder();
		for(char c : motherCaster.getSignature().toCharArray())
			signature.append(ChatColor.COLOR_CHAR + c);
		lore.add(signature.toString());
		// end no changes
		
		itemMeta.setLore(lore);
		
		return itemMeta;
	}
	
	public boolean isItemCasterItem(ItemStack item) {
		List<String> lore = item.getItemMeta().getLore();
		String signature = ChatColor.stripColor(item.getItemMeta().getLore().get(lore.size() - 1));
		for(CasterStorage casterStorage : casterStorages) {
			if(casterStorage.getMotherCaster().getSignature().equals(signature))
				return true;
		}
		return false;
	}
	
	public CasterStorage<ItemStack> getCasterStorage(ItemStack item) throws ItemNotCasterItemException {
		List<String> lore = item.getItemMeta().getLore();
		String signature = ChatColor.stripColor(item.getItemMeta().getLore().get(lore.size() - 1));
		for(CasterStorage casterStorage : casterStorages) {
			if(casterStorage.getMotherCaster().getSignature().equals(signature))
				return casterStorage;
		}
		throw new ItemNotCasterItemException();
	}
	
	public Set<CasterStorage<ItemStack>> getCasterStorages() {
		return casterStorages;
	}
	
	
	private void createCasterStorages() {
		try {
			casterStorages.add(
					new CasterStorage<>(
							new Caster(
									"test1",
									3.5,
									Arrays.asList(new Fireball()),
									Arrays.asList(new Deliverance())
							)
					)
			);
			
			casterStorages.add(
					new CasterStorage<>(
							new Caster(
									"test2",
									5,
									Arrays.asList(new Fireball()),
									null
							)
					)
			);
		} catch (NameAlreadyBoundException e) {
			e.printStackTrace();
			e.getLocalizedMessage();
		}
	}
	
}
