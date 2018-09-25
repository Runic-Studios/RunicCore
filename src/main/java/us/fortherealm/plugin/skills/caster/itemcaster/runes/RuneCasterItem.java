package us.fortherealm.plugin.skills.caster.itemcaster.runes;

import org.bukkit.inventory.ItemStack;
import us.fortherealm.plugin.skills.Skill;
import us.fortherealm.plugin.skills.caster.itemcaster.ItemCaster;

import java.util.List;

public class RuneCasterItem extends ItemCaster {
	
	public RuneCasterItem(ItemStack item, String name, double cooldown, List<Skill> primarySkills, List<Skill> secondarySkills) {
		super(item, name, Type.RUNE, cooldown, primarySkills, secondarySkills);
	}
	
	public RuneCasterItem(ItemStack item, String name, double cooldown, List<Skill> primarySkills, List<Skill> secondarySkills, boolean addToRegisteredItemCasterListener) {
		super(item, name, Type.RUNE, cooldown, primarySkills, secondarySkills, addToRegisteredItemCasterListener);
	}
}
