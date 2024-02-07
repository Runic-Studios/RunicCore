package com.runicrealms.plugin.spellapi.spells.rogue;

import com.runicrealms.plugin.common.CharacterClass;
import com.runicrealms.plugin.events.PhysicalDamageEvent;
import com.runicrealms.plugin.runicitems.Stat;
import com.runicrealms.plugin.spellapi.spelltypes.AttributeSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.util.Vector;

public class Backstab extends Spell implements AttributeSpell {
    private double baseValue;
    private double multiplier;
    private String statName;

    public Backstab() {
        super("Backstab", CharacterClass.ROGUE);
        this.setIsPassive(true);
        Stat stat = Stat.getFromName(statName);
        String prefix = stat == null ? "" : stat.getPrefix();
        this.setDescription("When attacking enemies from behind, " +
                "your basic attacks deal " +
                "(" + baseValue + " + " + multiplier + "x " + prefix + ")% " +
                "extra physical⚔ damage!");
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true) // runs last
    public void onBackstab(PhysicalDamageEvent event) {
        if (!event.isBasicAttack()) return;
        if (!hasPassive(event.getPlayer().getUniqueId(), this.getName())) return;
        if (!isBehind(event.getPlayer(), event.getVictim())) return;
        event.setAmount((int) (event.getAmount() + this.percentAttribute(event.getPlayer())));
        event.getPlayer().getWorld().playSound(event.getPlayer().getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.25F, 0.25F);
        event.getVictim().getWorld().spawnParticle
                (Particle.VILLAGER_ANGRY, event.getVictim().getEyeLocation(), 5, 1.0F, 0, 0, 0); // 0.3F
    }

    /**
     * @return true if the observer is behind the target
     */
    private boolean isBehind(LivingEntity observer, LivingEntity target) {
        // Get the location vectors of both entities
        Vector observerLoc = observer.getLocation().toVector();
        Vector targetLoc = target.getLocation().toVector();

        // Get the direction vector of the observer
        Vector direction = observer.getLocation().getDirection();

        // Calculate the vector from the observer to the target
        Vector toTarget = targetLoc.subtract(observerLoc);

        // Normalize the vectors to unit vectors
        direction.normalize();
        toTarget.normalize();

        // Calculate the dot product between the observer's direction vector and the vector to the target
        double dot = direction.dot(toTarget);

        // If the dot product is negative, the target is behind the observer
        return dot < 0;
    }

    @Override
    public double getBaseValue() {
        return baseValue;
    }

    @Override
    public void setBaseValue(double baseValue) {
        this.baseValue = baseValue;
    }

    @Override
    public double getMultiplier() {
        return multiplier;
    }

    @Override
    public void setMultiplier(double multiplier) {
        this.multiplier = multiplier;
    }

    @Override
    public String getStatName() {
        return statName;
    }

    @Override
    public void setStatName(String statName) {
        this.statName = statName;
    }
}

