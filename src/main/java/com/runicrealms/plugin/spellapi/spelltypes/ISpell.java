package com.runicrealms.plugin.spellapi.spelltypes;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public interface ISpell {

    String getName(); // returns the skill name

    boolean isFound(ItemStack stack, String spellSlot); // searches for the given spell in the "primary" or "secondary"

    ChatColor getColor();

    String getDescription(); // returns the skill description

    void execute(Player player, SpellItemType type); // casts the skill

    double getCooldown();

    int getManaCost();
}

