package com.runicrealms.plugin.spellapi.spells.cleric;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.common.CharacterClass;
import com.runicrealms.plugin.spellapi.spelltypes.DurationSpell;
import com.runicrealms.plugin.spellapi.spelltypes.RadiusSpell;
import com.runicrealms.plugin.spellapi.spelltypes.RunicStatusEffect;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spellutil.particles.Circle;
import com.runicrealms.plugin.spellapi.spellutil.particles.HorizontalCircleFrame;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;

public class Accelerando extends Spell implements DurationSpell, RadiusSpell {
    private double duration;
    private double period;
    private double radius;

    public Accelerando() {
        super("Accelerando", CharacterClass.CLERIC);
        this.setIsPassive(true);
        this.setDescription("Your &aSacred Spring &7spell marks an " +
                "area on the floor for the next " + duration + "s. " +
                "Allies inside the field gain Speed III every " + period + "s!");
    }

    /**
     * @param player to receive speed
     */
    private void applySpeed(Player player) {
        // Begin sound effects
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 0.5F, 0.7F);
        // Add player effects
        addStatusEffect(player, RunicStatusEffect.SPEED_III, 1, false);
        player.getWorld().spawnParticle(Particle.REDSTONE, player.getLocation(),
                25, 0.5f, 0.5f, 0.5f, 0, new Particle.DustOptions(Color.WHITE, 20));
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
        Number duration = (Number) spellData.getOrDefault("duration", 0);
        setDuration(duration.doubleValue());
        Number period = (Number) spellData.getOrDefault("period", 0);
        setPeriod(period.doubleValue());
    }

    @Override
    public double getRadius() {
        return radius;
    }

    @Override
    public void setRadius(double radius) {
        this.radius = radius;
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

        new BukkitRunnable() {
            int count = 1;

            @Override
            public void run() {

                count++;
                if (count > duration)
                    this.cancel();

                Circle.createParticleCircle(player, location, (int) radius, Particle.NOTE, Color.LIME);
                Bukkit.getScheduler().runTaskAsynchronously(RunicCore.getInstance(),
                        () -> new HorizontalCircleFrame((float) radius, false).playParticle(player, Particle.NOTE, location, 20, Color.LIME));

                player.getWorld().playSound(location, Sound.BLOCK_CAMPFIRE_CRACKLE, 0.5f, 0.5f);

                for (Entity entity : player.getWorld().getNearbyEntities(location, radius, radius, radius, target -> isValidAlly(player, target))) {
                    applySpeed((Player) entity);
                }
            }
        }.runTaskTimer(RunicCore.getInstance(), 0, (long) period * 20L);
    }

    public void setPeriod(double period) {
        this.period = period;
    }
}
