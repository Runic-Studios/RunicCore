package us.fortherealm.plugin.skills.caster.itemcaster.artifacts;

import org.bukkit.inventory.ItemStack;
import us.fortherealm.plugin.skills.Skill;
import us.fortherealm.plugin.skills.caster.itemcaster.ItemCaster;

import java.util.List;

public class ArtifactCaster extends ItemCaster {
	
	public ArtifactCaster(ItemStack item, String name, double cooldown, List<Skill> primarySkills, List<Skill> secondarySkills) {
		super(item, name, Type.RUNE, cooldown, primarySkills, secondarySkills);
	}
	
	public ArtifactCaster(ItemStack item, String name, double cooldown, List<Skill> primarySkills, List<Skill> secondarySkills, boolean addToRegisteredItemCasterListener) {
		super(item, name, Type.RUNE, cooldown, primarySkills, secondarySkills, addToRegisteredItemCasterListener);
	}
}
