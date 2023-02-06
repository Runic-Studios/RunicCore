package com.runicrealms.plugin.spellapi.spells.warrior;

import com.runicrealms.plugin.classes.CharacterClass;
import com.runicrealms.plugin.events.PhysicalDamageEvent;
import com.runicrealms.plugin.spellapi.spelltypes.HealingSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.spellapi.spellutil.particles.Cone;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import java.util.HashSet;
import java.util.UUID;

public class Leech extends Spell implements HealingSpell {

    private static final int BUFF_DURATION = 6;
    private static final int HEAL_AMT = 10;
    private static final int HEALING_PER_LEVEL = 1;
    private final HashSet<UUID> leechers = new HashSet<>();

    public Leech() {
        super("Leech",
                "For " + BUFF_DURATION + " seconds, your basic " +
                        "attacks restore✸ (" + HEAL_AMT + " + &f" + HEALING_PER_LEVEL +
                        "x&7 lvl) of your health!",
                ChatColor.WHITE, CharacterClass.WARRIOR, 20, 35);
    }

    @Override
    public void executeSpell(Player player, SpellItemType type) {

        // particles, sounds
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_WANDERING_TRADER_DRINK_POTION, 0.5f, 0.5f);
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_BREWING_STAND_BREW, 0.5f, 1.0f);
        Cone.coneEffect(player, Particle.REDSTONE, BUFF_DURATION, 0, 20L, Color.RED);

        // apply effect
        leechers.add(player.getUniqueId());

        // remove buff
        Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, () -> {
            player.getWorld().playSound(player.getLocation(), Sound.BLOCK_CONDUIT_DEACTIVATE, 0.5f, 1.0f);
            leechers.remove(player.getUniqueId());
        }, BUFF_DURATION * 20L);
    }

    @Override
    public int getHeal() {
        return HEAL_AMT;
    }

    @Override
    public double getHealingPerLevel() {
        return HEALING_PER_LEVEL;
    }

    /*
     * Activate on-hit effects
     */
    @EventHandler
    public void onSuccessfulHit(PhysicalDamageEvent event) {
        if (!leechers.contains(event.getPlayer().getUniqueId())) return;
        if (event.isCancelled()) return;
        Player player = event.getPlayer();
        healPlayer(player, player, HEAL_AMT, this);
    }
}

