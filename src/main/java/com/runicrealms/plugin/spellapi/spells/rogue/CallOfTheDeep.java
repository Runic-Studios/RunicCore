package com.runicrealms.plugin.spellapi.spells.rogue;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.classes.CharacterClass;
import com.runicrealms.plugin.events.PhysicalDamageEvent;
import com.runicrealms.plugin.spellapi.spelltypes.DurationSpell;
import com.runicrealms.plugin.spellapi.spelltypes.MagicDamageSpell;
import com.runicrealms.plugin.spellapi.spelltypes.RadiusSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spellutil.particles.HorizontalCircleFrame;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.scheduler.BukkitRunnable;

public class CallOfTheDeep extends Spell implements DurationSpell, MagicDamageSpell, RadiusSpell {
    private double damage;
    private double radius;
    private double damagePerLevel;
    private double duration;

    public CallOfTheDeep() {
        super("Call Of The Deep", CharacterClass.ROGUE);
        this.setIsPassive(true);
        this.setDescription("After pulling your target towards you, " +
                "your &aHarpoon &7spell now summons a whirlpool " +
                "at their feet for " + duration + "s! The whirlpool is " + radius + " " +
                "blocks wide, deals (" + damage + " + &f" + damagePerLevel + "x&7 lvl) " +
                "magicʔ damage to enemies inside each second and attempts to drag them " +
                "into the center.");
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

    @EventHandler(priority = EventPriority.HIGH) // runs last
    public void onPredatorHit(PhysicalDamageEvent event) {
        if (event.isCancelled()) return;
        if (!hasPassive(event.getPlayer().getUniqueId(), this.getName())) return;
        // Summon whirlpool a few ticks after harpoon is landed
        if (event.getSpell() != null && event.getSpell() instanceof Harpoon) {
            Bukkit.getScheduler().runTaskLater(RunicCore.getInstance(),
                    () -> summonWhirlPool(event.getPlayer(), event.getVictim()), 15L); // 0.75s
        }
    }

    /**
     * Creates the whirlpool effect
     *
     * @param caster    who cast the spell
     * @param recipient of the harpoon
     */
    private void summonWhirlPool(Player caster, LivingEntity recipient) {
        Spell spell = this;
        Location castLocation = recipient.getLocation();
        new BukkitRunnable() {
            int count = 1;

            @Override
            public void run() {

                count++;
                if (count > duration)
                    this.cancel();

                new HorizontalCircleFrame((float) radius, false).playParticle(caster, Particle.REDSTONE,
                        castLocation, Color.fromRGB(0, 64, 128));
                new HorizontalCircleFrame((float) (radius - 1), false).playParticle(caster, Particle.REDSTONE,
                        castLocation, Color.fromRGB(0, 89, 179));
                new HorizontalCircleFrame((float) (radius - 2), false).playParticle(caster, Particle.REDSTONE,
                        castLocation, Color.fromRGB(0, 102, 204));

                recipient.getWorld().playSound(castLocation,
                        Sound.ENTITY_PLAYER_SPLASH_HIGH_SPEED, 0.5f, 1.0f);

                for (Entity entity : recipient.getWorld().getNearbyEntities(castLocation, radius,
                        radius, radius, target -> isValidEnemy(caster, target))) {
                    DamageUtil.damageEntitySpell(damage, (LivingEntity) entity, caster, spell);
                    // summon to middle
                    entity.teleport(castLocation);
                }
            }
        }.runTaskTimer(RunicCore.getInstance(), 0, 20L);
    }

}

