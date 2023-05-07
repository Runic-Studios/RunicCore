package com.runicrealms.plugin.spellapi.spells.cleric;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.classes.CharacterClass;
import com.runicrealms.plugin.spellapi.spelltypes.DurationSpell;
import com.runicrealms.plugin.spellapi.spelltypes.HealingSpell;
import com.runicrealms.plugin.spellapi.spelltypes.RadiusSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spellutil.particles.Circle;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;

public class Lightwell extends Spell implements DurationSpell, HealingSpell, RadiusSpell {
    private double duration;
    private double healAmt;
    private double radius;
    private double healingPerLevel;

    public Lightwell() {
        super("Lightwell", CharacterClass.CLERIC);
        this.setIsPassive(true);
        this.setDescription("Your Sacred Spring now leaves behind a pool of light for " + duration + "s, " +
                "healing✦ all allies for (" + healAmt + " + &f" + healingPerLevel +
                "x&7 lvl) per second while they stand inside it.");
    }

    @Override
    public double getDuration() {
        return duration;
    }

    @Override
    public void setDuration(double duration) {
        this.duration = (int) duration;
    }

    @Override
    public void loadDurationData(Map<String, Object> spellData) {
        Number duration = (Number) spellData.getOrDefault("duration", 0);
        setDuration(duration.doubleValue());
    }

    @Override
    public double getHeal() {
        return healAmt;
    }

    @Override
    public void setHeal(double heal) {
        this.healAmt = heal;
    }

    @Override
    public double getHealingPerLevel() {
        return healingPerLevel;
    }

    @Override
    public void setHealingPerLevel(double healingPerLevel) {
        this.healingPerLevel = healingPerLevel;
    }

    @Override
    public double getRadius() {
        return radius;
    }

    @Override
    public void setRadius(double radius) {
        this.radius = (int) radius;
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPotionBreak(PotionSplashEvent event) {
        if (event.isCancelled()) return;
        if (!(SacredSpring.getThrownPotionSet().contains(event.getPotion())
                || DefiledFont.getThrownPotionSet().contains(event.getPotion())))
            return;
        if (!(event.getPotion().getShooter() instanceof Player player)) return;
        if (!hasPassive(player.getUniqueId(), this.getName())) return;

        Location location = event.getPotion().getLocation();

        Spell spell = this;
        new BukkitRunnable() {
            int count = 1;

            @Override
            public void run() {

                count++;
                if (count > duration)
                    this.cancel();

                Circle.createParticleCircle(player, location, (int) radius, Particle.SPELL_INSTANT, Color.WHITE);
                player.getWorld().playSound(location, Sound.BLOCK_CAMPFIRE_CRACKLE, 0.5f, 0.5f);
                player.getWorld().playSound(location, Sound.ENTITY_BLAZE_SHOOT, 0.5f, 0.5f);
                player.getWorld().spawnParticle(Particle.FIREWORKS_SPARK, location, 25, 0.75f, 0.75f, 0.75f, 0);

                for (Entity entity : player.getWorld().getNearbyEntities(location, radius, radius, radius)) {
                    if (isValidAlly(player, entity) && !DefiledFont.getThrownPotionSet().contains(event.getPotion())) { // Defiled Font cannot heal
                        healPlayer(player, (Player) entity, healAmt, spell);
                    } else if (isValidEnemy(player, entity)) {
                        entity.getWorld().spawnParticle(Particle.REDSTONE, ((LivingEntity) entity).getEyeLocation(), 5, 0.5f, 0.5f, 0.5f,
                                new Particle.DustOptions(Color.BLACK, 1));
                    }
                }


            }
        }.runTaskTimer(RunicCore.getInstance(), 0, 20L);

    }
}

