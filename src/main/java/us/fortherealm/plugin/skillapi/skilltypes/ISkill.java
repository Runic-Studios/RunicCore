package us.fortherealm.plugin.skillapi.skilltypes;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemStack;

public interface ISkill {

    String getName(); // returns the skill name

    boolean isFound(ItemStack stack, String spellSlot); // searches for the given spell in the "primary" or "secondary"

    ChatColor getColor();

    String getDescription(); // returns the skill description

    void execute(Player player, SkillItemType type); // casts the skill

    double getCooldown();

    int getManaCost();
}

