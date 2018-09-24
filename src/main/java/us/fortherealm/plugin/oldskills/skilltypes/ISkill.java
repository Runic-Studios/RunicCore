package us.fortherealm.plugin.oldskills.skilltypes;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemStack;

public interface ISkill {

    String getName(); // returns the skills name

    boolean isItem(ItemStack stack);

    ChatColor getColor();

    String getDescription(); // returns the skills description

    void execute(Player player, Action action, SkillItemType type); // casts the skills

    double getCooldown();

    Skill.ClickType getClickType();

}

