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
public class Potion extends Spell {

    private final HashSet<UUID> potionDrinkers = new HashSet<>();

    public Potion() {
        super("Potion", "", ChatColor.WHITE, ClassEnum.ANY, 30, 0);
    }

    @Override
    public void executeSpell(Player player, SpellItemType type) {
        potionDrinkers.add(player.getUniqueId());
        Bukkit.getScheduler().runTaskLater(RunicCore.getInstance(),
                () -> potionDrinkers.remove(player.getUniqueId()),
                (long) (this.getCooldown() * 20L));
    }

    @EventHandler(priority = EventPriority.LOWEST) // first
    public void onPotionConsume(RunicItemGenericTriggerEvent event) {
        if (!potionDrinkers.contains(event.getPlayer().getUniqueId())) return;
        if (!event.getItem().getTemplateId().contains("potion")) return;
        event.setCancelled(true);
        event.getPlayer().sendMessage(ChatColor.RED + "Use of potions is on cooldown!");
    }
}

