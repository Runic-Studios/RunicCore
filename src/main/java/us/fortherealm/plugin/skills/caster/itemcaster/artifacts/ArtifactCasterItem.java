package us.fortherealm.plugin.skills.caster.itemcaster.artifacts;

import org.bukkit.inventory.ItemStack;
import us.fortherealm.plugin.skills.Skill;
import us.fortherealm.plugin.skills.caster.itemcaster.ItemCaster;

import java.util.List;

public class ArtifactCasterItem extends ItemCaster {
	
	public ArtifactCasterItem(ItemStack item, String name, double cooldown, List<Skill> primarySkills, List<Skill> secondarySkills) {
		super(item, name, Type.RUNE, cooldown, primarySkills, secondarySkills);
	}
	
	public ArtifactCasterItem(ItemStack item, String name, double cooldown, List<Skill> primarySkills, List<Skill> secondarySkills, boolean addToRegisteredItemCasterListener) {
		super(item, name, Type.RUNE, cooldown, primarySkills, secondarySkills, addToRegisteredItemCasterListener);
	}
}
