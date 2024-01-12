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
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import java.util.Map;

/**
 * Reworked Pyromancer ult passive
 *
 * @author BoBoBalloon, Skyfallin
 */
public class Wildfire extends Spell implements RadiusSpell, DurationSpell {
    private double radius;
    private double duration;
    private double maxTargets;

    public Wildfire() {
        super("Wildfire", CharacterClass.MAGE);
        this.setIsPassive(true);
        this.setDescription("Your &aFireball&7 now deals its damage to up to " +
                "3 enemies within " + this.radius + " blocks of impact! " +
                "For each enemy you hit with &aFireball&7, reduce the cooldown " +
                "of your &aMeteor &7spell by " + this.duration + "s!");
    }

    @Override
    protected void loadSpellSpecificData(Map<String, Object> spellData) {
        super.loadSpellSpecificData(spellData);
        Number maxTargets = (Number) spellData.getOrDefault("max-targets", 3);
        setMaxTargets(maxTargets.doubleValue());
    }

    public void setMaxTargets(double maxTargets) {
        this.maxTargets = maxTargets;
    }

    @EventHandler(priority = EventPriority.LOW)
    private void onMagicDamage(MagicDamageEvent event) {
        if (!this.hasPassive(event.getPlayer().getUniqueId(), this.getName()) || !(event.getSpell() instanceof Fireball)) {
            return;
        }

        Player player = event.getPlayer();
        LivingEntity victim = event.getVictim();

        int count = 1;
        for (Entity entity : victim.getWorld().getNearbyEntities(
                victim.getLocation(), this.radius, this.radius, this.radius, target -> isValidEnemy(player, target))) {
            if (entity.equals(victim)) continue;
            count++;
            if (count > maxTargets) break;
            DamageUtil.damageEntitySpell(event.getAmount(), (LivingEntity) entity, event.getPlayer(), this);
            RunicCore.getSpellAPI().reduceCooldown(event.getPlayer(), "Meteor", this.duration);
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

