package com.runicrealms.plugin.spellapi.spells;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.common.CharacterClass;
import com.runicrealms.plugin.runicitems.item.event.RunicItemGenericTriggerEvent;
import com.runicrealms.plugin.runicitems.item.stats.RunicItemTag;
import com.runicrealms.plugin.spellapi.event.SpellCastEvent;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
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
        super("Potion", CharacterClass.ANY);
        this.setDisplayCastMessage(false);
    }

    @Override
    public void executeSpell(Player player, SpellItemType type) {
        potionDrinkers.add(player.getUniqueId());
        Bukkit.getScheduler().runTaskLater(RunicCore.getInstance(),
                () -> potionDrinkers.remove(player.getUniqueId()),
                (long) (this.getCooldown() * 20L));
    }

    /**
     * Handles cooldowns for potions
     */
    @EventHandler(priority = EventPriority.HIGHEST) // last
    public void onConsumableUse(RunicItemGenericTriggerEvent event) {
        if (event.isCancelled()) return;
        if (event.getItem() == null) return;
        if (!event.getItem().getTags().contains(RunicItemTag.POTION)) return;
        Bukkit.getPluginManager().callEvent(new SpellCastEvent(event.getPlayer(), this));
    }

    @EventHandler(priority = EventPriority.LOWEST) // first
    public void onPotionConsume(RunicItemGenericTriggerEvent event) {
        if (!potionDrinkers.contains(event.getPlayer().getUniqueId())) return;
        if (!event.getItem().getTags().contains(RunicItemTag.POTION)) return;
        event.setCancelled(true);
        event.getPlayer().sendMessage(ChatColor.RED + "Use of potions is on cooldown!");
    }
}

