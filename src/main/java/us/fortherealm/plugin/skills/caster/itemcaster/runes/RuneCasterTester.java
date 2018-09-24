package us.fortherealm.plugin.skills.caster.itemcaster.runes;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import us.fortherealm.plugin.skills.Skill;
import us.fortherealm.plugin.skills.skilltypes.defensive.Deliverance;
import us.fortherealm.plugin.skills.skilltypes.offensive.Fireball;

import java.util.Arrays;

public class RuneCasterTester extends RuneCaster {
	public RuneCasterTester() {
		super(new ItemStack(Material.SLIME_BALL), "test caster", 5,
				Arrays.asList(new Fireball()), Arrays.asList(new Deliverance()));
	}
}
