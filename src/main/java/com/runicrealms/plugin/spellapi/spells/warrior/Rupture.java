package com.runicrealms.plugin.spellapi.spells.warrior;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.common.CharacterClass;
import com.runicrealms.plugin.events.PhysicalDamageEvent;
import com.runicrealms.plugin.events.SpellHealEvent;
import com.runicrealms.plugin.spellapi.spelltypes.DurationSpell;
import com.runicrealms.plugin.spellapi.spelltypes.PhysicalDamageSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class Rupture extends Spell implements DurationSpell, PhysicalDamageSpell {
    private final Map<UUID, Set<UUID>> bleedingEntitiesMap = new HashMap<>();
    private double damage;
    private double damagePerLevel;
    private double duration;
    private double percent;

    public Rupture() {
        super("Rupture", CharacterClass.MAGE);
        this.setIsPassive(true);
        this.setDescription("While your &aWhirlwind &7spell is active, " +
                "your basic attacks now cause your enemies to bleed, " +
                "dealing (" + damage + " + &f" + damagePerLevel +
                "x&7 lvl) physical⚔ damage every " +
                "second for " + duration + "s. During this time, the enemy receives " +
                (percent * 100) + "% less healing. " +
                "Enemies cannot be affected more than once per cast of &aWhirlwind.");
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPhysicalDamage(PhysicalDamageEvent event) {
        if (event.isCancelled()) return;
        if (!hasPassive(event.getPlayer().getUniqueId(), this.getName())) return;
        if (!Whirlwind.getUuidSet().contains(event.getPlayer().getUniqueId())) return;
        if (!event.isBasicAttack()) return; // Only listen for basic attacks
        if (event.getSpell() != null && event.getSpell() instanceof Rupture) return; // Insurance
        // Cleave!
        cleaveEffect(event.getPlayer(), event.getVictim());
    }

    @EventHandler
    public void onSpellHeal(SpellHealEvent event) {
        if (bleedingEntitiesMap.isEmpty()) return;
        bleedingEntitiesMap.forEach((uuidCaster, set) -> {
            if (set.contains(event.getEntity().getUniqueId())) {
                double reduction = event.getAmount() * percent;
                event.setAmount((int) (event.getAmount() - reduction));
            }
        });
    }

    @Override
    public void loadDurationData(Map<String, Object> spellData) {
        Number duration = (Number) spellData.getOrDefault("duration", 0);
        setDuration(duration.doubleValue());
        Number percent = (Number) spellData.getOrDefault("percent", 0);
        setPercent(percent.doubleValue());
    }

    public void setPercent(double percent) {
        this.percent = percent;
    }

    private void cleaveEffect(Player player, LivingEntity livingEntity) {
        bleedingEntitiesMap.computeIfAbsent(player.getUniqueId(), k -> new HashSet<>());
        if (bleedingEntitiesMap.get(player.getUniqueId()).contains(livingEntity.getUniqueId())) return;
        bleedingEntitiesMap.get(player.getUniqueId()).add(livingEntity.getUniqueId());
        Bukkit.getScheduler().runTaskLater(RunicCore.getInstance(),
                () -> bleedingEntitiesMap.remove(player.getUniqueId()), (long) duration * 20L);
        Spell spell = this;
        new BukkitRunnable() {
            int count = 0;

            @Override
            public void run() {
                if (count >= duration) {
                    this.cancel();
                } else {
                    count += 1;
                    livingEntity.getWorld().spawnParticle(Particle.CRIMSON_SPORE, livingEntity.getLocation(), 10, 0.5f, 0.5f, 0.5f, 0);
                    DamageUtil.damageEntityPhysical(damage, livingEntity, player, false, false, false, spell);
                }

            }
        }.runTaskTimer(RunicCore.getInstance(), 0, 20L);
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
    public double getPhysicalDamage() {
        return damage;
    }

    @Override
    public void setPhysicalDamage(double physicalDamage) {
        this.damage = physicalDamage;
    }

    @Override
    public double getPhysicalDamagePerLevel() {
        return damagePerLevel;
    }

    @Override
    public void setPhysicalDamagePerLevel(double physicalDamagePerLevel) {
        this.damagePerLevel = physicalDamagePerLevel;
    }

}

