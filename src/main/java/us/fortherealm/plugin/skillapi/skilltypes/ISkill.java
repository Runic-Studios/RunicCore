package us.fortherealm.plugin.skillapi.skilltypes;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemStack;

public interface ISkill {

    String getName(); // returns the skill name

    boolean isItem(ItemStack stack);

    ChatColor getColor();

    String getDescription(); // returns the skill description

    void execute(Player player, Action action, SkillItemType type); // casts the skill

    double getCooldown();

    Skill.ClickType getClickType();

}

