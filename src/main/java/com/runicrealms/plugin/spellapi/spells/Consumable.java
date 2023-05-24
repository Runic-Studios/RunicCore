package com.runicrealms.plugin.spellapi.spells;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.events.SpellCastEvent;
import com.runicrealms.plugin.common.CharacterClass;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.runicitems.item.event.RunicItemGenericTriggerEvent;
import com.runicrealms.runicitems.item.stats.RunicItemTag;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import java.util.HashSet;
import java.util.UUID;

/**
 * Used to handle potion cooldowns with a hotbar display
 */
public class Consumable extends Spell {

    private final HashSet<UUID> consumableUsers = new HashSet<>();

    public Consumable() {
        super("Consumable", CharacterClass.ANY);
    }

    @Override
    public void executeSpell(Player player, SpellItemType type) {
        consumableUsers.add(player.getUniqueId());
        Bukkit.getScheduler().runTaskLater(RunicCore.getInstance(),
                () -> consumableUsers.remove(player.getUniqueId()),
                (long) (this.getCooldown() * 20L));
    }

    /**
     * Handles cooldowns for consumables
     */
    @EventHandler(priority = EventPriority.HIGHEST) // last
    public void onConsumableUse(RunicItemGenericTriggerEvent event) {
        if (event.isCancelled()) return;
        if (event.getItem() == null) return;
        if (!event.getItem().getTags().contains(RunicItemTag.CONSUMABLE)) return;
        Bukkit.getPluginManager().callEvent(new SpellCastEvent(event.getPlayer(), this));
    }

    @EventHandler(priority = EventPriority.LOWEST) // first
    public void onFoodEat(RunicItemGenericTriggerEvent event) {
        if (!consumableUsers.contains(event.getPlayer().getUniqueId())) return;
        if (!event.getItem().getTags().contains(RunicItemTag.CONSUMABLE)) return;
        event.setCancelled(true);
        event.getPlayer().sendMessage(ChatColor.RED + "Use of consumables is on cooldown!");
    }
}

