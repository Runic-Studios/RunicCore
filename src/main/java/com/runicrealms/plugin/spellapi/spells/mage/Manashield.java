package com.runicrealms.plugin.spellapi.spells.mage;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.api.event.ShieldBreakEvent;
import com.runicrealms.plugin.classes.CharacterClass;
import com.runicrealms.plugin.spellapi.spelltypes.*;
import com.runicrealms.plugin.spellapi.spellutil.particles.HorizontalCircleFrame;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import java.util.*;

public class Manashield extends Spell implements RadiusSpell, ShieldingSpell {
    private final Map<UUID, Set<UUID>> shieldedPlayersMap = new HashMap<>();
    private double manaRestored;
    private double maxAllies;
    private double radius;
    private double shield;
    private double shieldPerLevel;

    public Manashield() {
        super("Manashield", CharacterClass.MAGE);
        this.setDescription("You instantly shield yourself and " +
                "up to " + maxAllies + " allies within " + radius + " blocks for " +
                "(" + shield + " + &f" + shieldPerLevel +
                "x&7 lvl) health! Whenever a shield breaks " +
                "that was applied by this skill, " +
                "you regain " + manaRestored + " mana.");
    }

    @Override
    public void executeSpell(Player player, SpellItemType type) {
        shieldedPlayersMap.put(player.getUniqueId(), new HashSet<>() {{
            add(player.getUniqueId());
        }});
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1.0f, 2.0f);
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_PORTAL_TRAVEL, 1.0f, 1.0f);
        new HorizontalCircleFrame((float) radius, false).playParticle(player, Particle.REDSTONE, player.getEyeLocation(), Color.TEAL);
        shieldPlayer(player, player, shield, this);
        Set<UUID> alliesHit = shieldedPlayersMap.get(player.getUniqueId());
        for (Entity entity : player.getWorld().getNearbyEntities(player.getLocation(), radius, radius, radius, target -> isValidAlly(player, target))) {
            if (alliesHit.size() >= maxAllies) return;
            if (entity.equals(player)) continue;
            Player ally = (Player) entity;
            shieldPlayer(player, ally, shield, this);
            alliesHit.add(ally.getUniqueId());
        }
    }

    @Override
    public double getRadius() {
        return radius;
    }

    @Override
    public void setRadius(double radius) {
        this.radius = radius;
    }

    @Override
    public double getShield() {
        return shield;
    }

    @Override
    public void setShield(double shield) {
        this.shield = shield;
    }

    @Override
    public double getShieldingPerLevel() {
        return shieldPerLevel;
    }

    @Override
    public void loadShieldingData(Map<String, Object> spellData) {
        Number manaRestored = (Number) spellData.getOrDefault("mana-restored", 0);
        setManaRestored(manaRestored.doubleValue());
        Number maxAllies = (Number) spellData.getOrDefault("max-allies", 0);
        setMaxAllies(maxAllies.doubleValue());
        Number shield = (Number) spellData.getOrDefault("shield", 0);
        setShield(shield.doubleValue());
        Number shieldPerLevel = (Number) spellData.getOrDefault("shield-per-level", 0);
        setShieldPerLevel(shieldPerLevel.doubleValue());
    }

    @Override
    public void setShieldPerLevel(double shieldingPerLevel) {
        this.shieldPerLevel = shieldingPerLevel;
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
        if (shield == null) return; // Fixes a bug from race condition due to shield removal task
        for (UUID uuid : shieldedPlayersMap.keySet()) { // The casters who apply mana shields
            if (shieldedPlayersMap.get(uuid).stream().anyMatch(value -> shield.getSources().contains(value))) { // If a mana shield caster contributed to this shield
                Player player = Bukkit.getPlayer(uuid);
                if (player != null) {
                    player.getWorld().playSound(player.getLocation(), Sound.ENTITY_GENERIC_DRINK, 1.0f, 2.0f);
                    RunicCore.getRegenManager().addMana(player, (int) manaRestored);
                }
            }
        }

    }

    public void setManaRestored(double manaRestored) {
        this.manaRestored = manaRestored;
    }

    public void setMaxAllies(double maxAllies) {
        this.maxAllies = maxAllies;
    }

}

