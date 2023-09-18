package com.runicrealms.plugin.spellapi.spells.mage;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.common.CharacterClass;
import com.runicrealms.plugin.events.MagicDamageEvent;
import com.runicrealms.plugin.spellapi.spelltypes.DurationSpell;
import com.runicrealms.plugin.spellapi.spelltypes.RadiusSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

/**
 * Reworked Pyromancer ult passive
 *
 * @author BoBoBalloon
 */
public class Inferno extends Spell implements RadiusSpell, DurationSpell {
    private double radius;
    private double duration;

    public Inferno() {
        super("Inferno", CharacterClass.MAGE);
        this.setIsPassive(true);
        this.setDescription("Your &aFireball&7 now does its damage in a " + this.radius + " block AoE of the enemy it hits.\n" +
                "Everytime you hit at least one enemy with &aFireball&7, reduce the cooldown on your meteor by " + this.duration + "s.");
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    private void onMagicDamage(MagicDamageEvent event) {
        if (!this.hasPassive(event.getPlayer().getUniqueId(), this.getName()) || !(event.getSpell() instanceof Fireball)) {
            return;
        }

        RunicCore.getSpellAPI().reduceCooldown(event.getPlayer(), "Meteor", this.duration);

        for (Entity entity : event.getVictim().getNearbyEntities(this.radius, this.radius, this.radius)) {
            if (!(entity instanceof LivingEntity target) || !this.isValidEnemy(event.getPlayer(), target) || event.getVictim().equals(entity)) {
                return;
            }

            DamageUtil.damageEntitySpell(event.getAmount(), target, event.getPlayer(), this);
        }
    }

    @Override
    public double getRadius() {
        return this.radius;
    }

    @Override
    public void setRadius(double radius) {
        this.radius = radius;
    }

    @Override
    public double getDuration() {
        return this.duration;
    }

    @Override
    public void setDuration(double duration) {
        this.duration = duration;
    }
}

