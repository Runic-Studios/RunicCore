package com.runicrealms.plugin.spellapi.spells.rogue;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.common.CharacterClass;
import com.runicrealms.plugin.events.PhysicalDamageEvent;
import com.runicrealms.plugin.spellapi.event.SpellCastEvent;
import com.runicrealms.plugin.spellapi.event.SpellHealEvent;
import com.runicrealms.plugin.spellapi.spelltypes.DurationSpell;
import com.runicrealms.plugin.spellapi.spelltypes.MagicDamageSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class Castigate extends Spell implements DurationSpell, MagicDamageSpell {
    private final Set<UUID> weakenedHealers = new HashSet<>();
    private final Map<UUID, BukkitTask> buffedMap = new HashMap<>();
    private double damage;
    private double damagePerLevel;
    private double durationToHit;
    private double healingReductionDuration;
    private double numberOfTicks;
    private double percent;

    public Castigate() {
        super("Castigate", CharacterClass.ROGUE);
        this.setIsPassive(true);
        this.setDescription("After casting a spell, your next basic attack " +
                "within " + durationToHit + "s now burns the target " +
                "for (" + damage + " + &f" + damagePerLevel
                + "x&7 lvl) magicʔ damage " +
                "per second for " + numberOfTicks + "s. For " + healingReductionDuration + "s, " +
                "the target receives " + (percent * 100) + "% less healing " +
                "from all sources!");
    }

    public double getPercent() {
        return percent;
    }

    public void setPercent(double percent) {
        this.percent = percent;
    }

    public void setNumberOfTicks(double numberOfTicks) {
        this.numberOfTicks = numberOfTicks;
    }

    public void setDurationToHit(double durationToHit) {
        this.durationToHit = durationToHit;
    }

    @Override
    public void loadDurationData(Map<String, Object> spellData) {
        Number durationToHit = (Number) spellData.getOrDefault("duration-to-hit", 0);
        setDurationToHit(durationToHit.doubleValue());
        Number healingReductionDuration = (Number) spellData.getOrDefault("healing-reduction-duration", 0);
        setHealingReductionDuration(healingReductionDuration.doubleValue());
        Number numberOfTicks = (Number) spellData.getOrDefault("number-of-ticks", 0);
        setNumberOfTicks(numberOfTicks.doubleValue());
        Number percent = (Number) spellData.getOrDefault("percent", 0);
        setPercent(percent.doubleValue());
    }

    public void setHealingReductionDuration(double healingReductionDuration) {
        this.healingReductionDuration = healingReductionDuration;
    }

    @Override
    public double getDuration() {
        return durationToHit;
    }

    @Override
    public void setDuration(double duration) {
        this.durationToHit = duration;
    }

    @Override
    public double getMagicDamage() {
        return damage;
    }

    @Override
    public void setMagicDamage(double magicDamage) {
        this.damage = magicDamage;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onHeal(SpellHealEvent event) {
        if (event.isCancelled()) return;
        if (weakenedHealers.isEmpty()) return;
        if (!weakenedHealers.contains(event.getPlayer().getUniqueId())) return;
        double reduction = event.getAmount() * percent;
        event.setAmount((int) (event.getAmount() - reduction));
    }

    @EventHandler
    public void onSpellCast(SpellCastEvent event) {
        if (event.isCancelled()) return;
        if (!hasPassive(event.getCaster().getUniqueId(), this.getName())) return;
        if (buffedMap.containsKey(event.getCaster().getUniqueId()))
            buffedMap.get(event.getCaster().getUniqueId()).cancel(); // Cancel removal task
        buffedMap.put(event.getCaster().getUniqueId(), Bukkit.getScheduler().runTaskLater(RunicCore.getInstance(),
                () -> buffedMap.remove(event.getCaster().getUniqueId()), (long) durationToHit * 20L));
    }

    @EventHandler
    public void onBasicAttack(PhysicalDamageEvent event) {
        if (event.isCancelled()) return;
        if (!event.isBasicAttack()) return;
        if (!hasPassive(event.getPlayer().getUniqueId(), this.getName())) return;
        if (!buffedMap.containsKey(event.getPlayer().getUniqueId())) return;
        applyCastigation(event.getPlayer(), event.getVictim());
    }

    private void applyCastigation(Player caster, LivingEntity victim) {
        weakenedHealers.add(victim.getUniqueId());
        Bukkit.getScheduler().runTaskLater(RunicCore.getInstance(), () -> weakenedHealers.remove(victim.getUniqueId()), (long) healingReductionDuration * 20L);
        Spell spell = this;
        new BukkitRunnable() {
            double count = 1;

            @Override
            public void run() {
                if (count > numberOfTicks) {
                    this.cancel();
                } else {
                    count += 1;
                    victim.getWorld().spawnParticle(Particle.SOUL, victim.getLocation(), 5, 0.5f, 0.5f, 0.5f, 0);
                    victim.getWorld().playSound(victim.getLocation(), Sound.ENTITY_WITCH_HURT, 0.25f, 0.5f);
                    DamageUtil.damageEntitySpell(damage, victim, caster, spell);
                }

            }
        }.runTaskTimer(RunicCore.getInstance(), 0, 20L);
    }

    @Override
    public double getMagicDamagePerLevel() {
        return damagePerLevel;
    }

    @Override
    public void setMagicDamagePerLevel(double magicDamagePerLevel) {
        this.damagePerLevel = magicDamagePerLevel;
    }
}
