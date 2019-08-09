package com.runicrealms.plugin.spellapi.spelltypes;

import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public interface ISpell {

    String getName(); // returns the spell name

    boolean isFound(ItemStack stack, String spellSlot); // searches for the given spell in the "primary" or "secondary"

    ChatColor getColor();

    String getDescription(); // returns the spell description

    void execute(Player player, SpellItemType type); // casts the spell

    double getCooldown();

    int getManaCost();

    boolean testTarget(Player caster, Entity victim); // check tons of things, like if target entity is NPC, party member, and outlaw checks
}

