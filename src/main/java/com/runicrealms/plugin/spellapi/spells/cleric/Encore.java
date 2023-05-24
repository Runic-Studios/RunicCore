package com.runicrealms.plugin.spellapi.spells.cleric;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.events.PhysicalDamageEvent;
import com.runicrealms.plugin.common.CharacterClass;
import com.runicrealms.plugin.spellapi.spelltypes.DurationSpell;
import com.runicrealms.plugin.spellapi.spelltypes.MagicDamageSpell;
import com.runicrealms.plugin.spellapi.spelltypes.RadiusSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class Encore extends Spell implements DurationSpell, MagicDamageSpell, RadiusSpell {
    private final Set<UUID> encoreCooldowns = new HashSet<>();
    private double cooldown;
    private double damage;
    private double duration;
    private double radius;
    private double damagePerLevel;

    public Encore() {
        super("Encore", CharacterClass.CLERIC);
        this.setIsPassive(true);
        this.setDescription("Every " + cooldown + "s, your next basic attack " +
                "deals an extra (" + damage + " + &f" + (int) damagePerLevel
                + "x&7 lvl) magicʔ damage! " +
                "It also reduces the active spell cooldowns of all allies " +
                "within " + radius + " blocks by " + duration + "s!");
    }

    @Override
    public double getCooldown() {
        return cooldown;
    }

    public void setCooldown(double cooldown) {
        this.cooldown = cooldown;
    }

    public double getDamage() {
        return damage;
    }

    public void setDamage(double damage) {
        this.damage = damage;
    }

    public double getDamagePerLevel() {
        return damagePerLevel;
    }

    public void setDamagePerLevel(double damagePerLevel) {
        this.damagePerLevel = damagePerLevel;
    }

    @Override
    public double getDuration() {
        return duration;
    }

    @Override
    public void setDuration(double duration) {
        this.duration = duration;
    }

    @Override
    public void loadDurationData(Map<String, Object> spellData) {
        Number cooldown = (Number) spellData.getOrDefault("cooldown", 0);
        setCooldown(cooldown.doubleValue());
        Number cooldownReduction = (Number) spellData.getOrDefault("cooldown-reduction", 0);
        setDuration(cooldownReduction.doubleValue());
    }

    public Set<UUID> getEncoreCooldowns() {
        return encoreCooldowns;
    }

    @Override
    public double getMagicDamage() {
        return damage;
    }

    @Override
    public void setMagicDamage(double magicDamage) {
        this.damage = magicDamage;
    }

    @Override
    public double getMagicDamagePerLevel() {
        return damagePerLevel;
    }

    @Override
    public void setMagicDamagePerLevel(double magicDamagePerLevel) {
        this.damagePerLevel = magicDamagePerLevel;
    }

    @Override
    public double getRadius() {
        return radius;
    }

    @Override
    public void setRadius(double radius) {
        this.radius = radius;
    }

    @EventHandler
    public void onWeaponHit(PhysicalDamageEvent event) {
        if (event.isCancelled()) return;
        if (!event.isBasicAttack()) return;
        if (!hasPassive(event.getPlayer().getUniqueId(), this.getName())) return;
        if (encoreCooldowns.contains(event.getPlayer().getUniqueId())) return;
        Player player = event.getPlayer();
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_TWINKLE, 0.25F, 1.0F);
        event.getVictim().getWorld().spawnParticle
                (Particle.NOTE, event.getVictim().getLocation().add(0, 1.5, 0),
                        5, 1.0F, 0, 0, 0);
        DamageUtil.damageEntitySpell(damage, event.getVictim(), player, this);
        for (Entity entity : player.getWorld().getNearbyEntities(player.getLocation(), radius, radius, radius, target -> isValidAlly(player, target))) {
            Player ally = (Player) entity;
            ConcurrentHashMap.KeySetView<Spell, Long> spellsOnCD = RunicCore.getSpellAPI().getSpellsOnCooldown(ally.getUniqueId());
            if (spellsOnCD == null) continue;
            for (Spell spell : spellsOnCD) {
                RunicCore.getSpellAPI().reduceCooldown(ally, spell, duration);
            }
        }
        encoreCooldowns.add(player.getUniqueId());
        Bukkit.getScheduler().runTaskLaterAsynchronously(RunicCore.getInstance(), () -> encoreCooldowns.remove(player.getUniqueId()), (long) cooldown * 20L);
    }

}

