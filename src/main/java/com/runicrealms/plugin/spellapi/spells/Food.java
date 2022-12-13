package com.runicrealms.plugin.spellapi.spells;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.classes.ClassEnum;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.runicitems.item.event.RunicItemGenericTriggerEvent;
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
public class Food extends Spell {

    private final HashSet<UUID> foodEaters = new HashSet<>();

    public Food() {
        super("Food", "", ChatColor.WHITE, ClassEnum.ANY, 15, 0);
    }

    @Override
    public void executeSpell(Player player, SpellItemType type) {
        foodEaters.add(player.getUniqueId());
        Bukkit.getScheduler().runTaskLater(RunicCore.getInstance(),
                () -> foodEaters.remove(player.getUniqueId()),
                (long) (this.getCooldown() * 20L));
    }

    @EventHandler(priority = EventPriority.LOWEST) // first
    public void onFoodEat(RunicItemGenericTriggerEvent event) {
//        if (!foodEaters.contains(event.getPlayer().getUniqueId())) return;
//        if (!event.getItem().getTags().contains(RunicItemTag.FOOD_BUFF)) return;
//        event.setCancelled(true);
//        event.getPlayer().sendMessage(ChatColor.RED + "Use of food buffs is on cooldown!");
    }
}

