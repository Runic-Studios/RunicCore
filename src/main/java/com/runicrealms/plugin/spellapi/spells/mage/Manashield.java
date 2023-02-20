package com.runicrealms.plugin.spellapi.spells.mage;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.api.event.ShieldBreakEvent;
import com.runicrealms.plugin.classes.CharacterClass;
import com.runicrealms.plugin.spellapi.spelltypes.Shield;
import com.runicrealms.plugin.spellapi.spelltypes.ShieldingSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.spellapi.spellutil.particles.HorizontalCircleFrame;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import java.util.*;

public class Manashield extends Spell implements ShieldingSpell {
    private static final int MANA_AMOUNT = 20;
    private static final int MAX_ALLIES = 3;
    private static final int RADIUS = 3;
    private static final int SHIELD = 140;
    private static final double SHIELD_PER_LEVEL = 1.0;
    private final Map<UUID, Set<UUID>> shieldedPlayersMap = new HashMap<>();

    public Manashield() {
        super("Manashield",
                "You instantly shield yourself and " +
                        "up to " + MAX_ALLIES + " allies within " + RADIUS + " blocks for " +
                        "(" + SHIELD + " + &f" + SHIELD_PER_LEVEL +
                        "x&7 lvl) health! Whenever a shield breaks " +
                        "that was applied by this skill, " +
                        "you regain " + MANA_AMOUNT + " mana.",
                ChatColor.WHITE, CharacterClass.MAGE, 40, 50);
    }

    @Override
    public void executeSpell(Player player, SpellItemType type) {
        shieldedPlayersMap.put(player.getUniqueId(), new HashSet<UUID>() {{
            add(player.getUniqueId());
        }});
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1.0f, 2.0f);
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_PORTAL_TRAVEL, 1.0f, 1.0f);
        new HorizontalCircleFrame(RADIUS, false).playParticle(player, Particle.REDSTONE, player.getEyeLocation(), Color.TEAL);
        shieldPlayer(player, player, SHIELD, this);
        Set<UUID> alliesHit = shieldedPlayersMap.get(player.getUniqueId());
        for (Entity entity : player.getWorld().getNearbyEntities(player.getLocation(), RADIUS, RADIUS, RADIUS, target -> isValidEnemy(player, target))) {
            if (alliesHit.size() >= MAX_ALLIES) return;
            if (entity.equals(player)) continue;
            Player ally = (Player) entity;
            shieldPlayer(player, ally, SHIELD, this);
            alliesHit.add(ally.getUniqueId());
        }
    }

    @Override
    public int getShield() {
        return SHIELD;
    }

    @Override
    public double getShieldingPerLevel() {
        return SHIELD_PER_LEVEL;
    }

    /**
     * Whenever a shield breaks, grabs the list of sources who contributed to that shield.
     * Loops through every current mana shield caster, and if that caster is a source of the shield, returns X mana
     * to the caster
     */
    @EventHandler(priority = EventPriority.LOW)
    public void onShieldBreak(ShieldBreakEvent event) {
        if (event.isCancelled()) return;
        Shield shield = event.getShield();
        for (UUID uuid : shieldedPlayersMap.keySet()) { // The casters who apply mana shields
            if (shieldedPlayersMap.get(uuid).stream().anyMatch(value -> shield.getSources().contains(value))) { // If a mana shield caster contributed to this shield
                Player player = Bukkit.getPlayer(uuid);
                if (player != null) {
                    player.getWorld().playSound(player.getLocation(), Sound.ENTITY_GENERIC_DRINK, 1.0f, 2.0f);
                    RunicCore.getRegenManager().addMana(player, MANA_AMOUNT);
                }
            }
        }

    }

}

