package us.fortherealm.plugin.skills.caster.itemcaster.runes;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import us.fortherealm.plugin.skills.Skill;
import us.fortherealm.plugin.skills.skilltypes.defensive.DefensiveSkillTest;
import us.fortherealm.plugin.skills.skilltypes.offensive.OffensiveSkillTest;

import java.util.Arrays;
import java.util.List;

public class RuneCasterTester extends RuneCaster {
	public RuneCasterTester() {
		super(new ItemStack(Material.SLIME_BALL), "test caster", 1,
				Arrays.asList(new OffensiveSkillTest()), Arrays.asList(new DefensiveSkillTest()));
	}
}
