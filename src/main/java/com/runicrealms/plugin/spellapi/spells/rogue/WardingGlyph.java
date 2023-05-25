package com.runicrealms.plugin.spellapi.spells.rogue;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.common.CharacterClass;
import com.runicrealms.plugin.spellapi.spelltypes.DurationSpell;
import com.runicrealms.plugin.spellapi.spelltypes.RadiusSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;
import java.util.Random;

public class WardingGlyph extends Spell implements DurationSpell, RadiusSpell {
    private double duration;
    private double durationSilence;
    private double radius;

    public WardingGlyph() {
        super("Warding Glyph", CharacterClass.ROGUE);
        this.setDescription("You place down an anti-magic glyph in a " +
                radius + " block radius for " + duration + "s. Allies inside the area are " +
                "immune to debuffs! &7&oBranded &7enemies who step " +
                "within the glyph are silenced for " + durationSilence + "s each second " +
                "they remain inside.");
    }

    public void setDurationSilence(double durationSilence) {
        this.durationSilence = durationSilence;
    }

    @Override
    public void loadDurationData(Map<String, Object> spellData) {
        Number durationGlyph = (Number) spellData.getOrDefault("duration-glyph", 0);
        Number durationSilence = (Number) spellData.getOrDefault("duration-silence", 0);
        setDuration(durationGlyph.doubleValue());
        setDurationSilence(durationSilence.doubleValue());
    }

    @Override
    public void executeSpell(Player player, SpellItemType type) {
        Location castLocation = player.getLocation();
        new BukkitRunnable() {
            int count = 1;

            @Override
            public void run() {

                count++;
                if (count > duration)
                    this.cancel();

                createRunicMarking(castLocation, player);
                player.getWorld().playSound(castLocation, Sound.BLOCK_CAMPFIRE_CRACKLE, 0.5f, 0.5f);

//                for (Entity entity : player.getWorld().getNearbyEntities(location, radius, radius, radius, target -> isValidAlly(player, target))) {
//                    applySpeed((Player) entity);
//                }
            }
        }.runTaskTimer(RunicCore.getInstance(), 0, 20L);
    }

    public void createRunicMarking(Location center, Player player) {
        Particle.DustOptions dustOptions = new Particle.DustOptions(Color.FUCHSIA, 1);
        double increment = Math.PI / 8;
        Random random = new Random();

        // Draw the circle.
        for (double angle = 0; angle <= 2 * Math.PI; angle += increment) {
            double x = radius * Math.cos(angle);
            double z = radius * Math.sin(angle);

            // Create a location for the particle.
            Location particleLocation = center.clone().add(x, 0, z);

            // Spawn the particle.
            player.spawnParticle(Particle.REDSTONE, particleLocation, 1, dustOptions);
        }

        // Add random particles inside the circle.
        for (int i = 0; i < radius * 10; i++) { // Adjust the multiplier for more or less particles.
            double angle = 2 * Math.PI * random.nextDouble(); // Random angle.
            double r = radius * Math.sqrt(random.nextDouble()); // Random distance from center.

            double x = r * Math.cos(angle);
            double z = r * Math.sin(angle);

            Location particleLocation = center.clone().add(x, 0, z);

            player.spawnParticle(Particle.REDSTONE, particleLocation, 1, dustOptions);
        }
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
    public double getRadius() {
        return radius;
    }

    @Override
    public void setRadius(double radius) {
        this.radius = radius;
    }

}