package com.runicrealms.plugin.spellapi.spells.rogue;

import com.runicrealms.plugin.common.CharacterClass;
import com.runicrealms.plugin.events.PhysicalDamageEvent;
import com.runicrealms.plugin.runicitems.Stat;
import com.runicrealms.plugin.spellapi.spelltypes.AttributeSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

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
        event.setAmount((int) (event.getAmount() + this.percentAttribute(event.getPlayer())));
        event.getPlayer().getWorld().playSound(event.getPlayer().getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.25F, 0.25F);
        event.getVictim().getWorld().spawnParticle
                (Particle.VILLAGER_ANGRY, event.getVictim().getEyeLocation(), 5, 1.0F, 0, 0, 0); // 0.3F
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

