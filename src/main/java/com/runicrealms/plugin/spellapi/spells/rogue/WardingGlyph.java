package com.runicrealms.plugin.spellapi.spells.rogue;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.common.CharacterClass;
import com.runicrealms.plugin.spellapi.spelltypes.DurationSpell;
import com.runicrealms.plugin.spellapi.spelltypes.RadiusSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.spellapi.spellutil.particles.HorizontalCircleFrame;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.Map;

public class WardingGlyph extends Spell implements DurationSpell, RadiusSpell {
    private static final int POINTS = 5; // Number of points on the pentagram.
    private static final double RADIANS_PER_POINT = 2 * Math.PI / POINTS;
    private static final int PARTICLES_PER_LINE = 25; // Number of particles per line.
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
        new HorizontalCircleFrame((float) radius, false).playParticle(player, Particle.SOUL, center, 7.5f);
        drawPentagram(center, radius);
    }

    /**
     * Method for linear interpolation
     */
    private Vector linearInterpolation(Vector start, Vector end, double t) {
        return end.clone().subtract(start).multiply(t).add(start);
    }

    private void drawPentagram(Location location, double radius) {
        assert location != null;
        assert location.getWorld() != null;
        Vector center = location.toVector();

        for (int i = 0; i < POINTS; i++) {
            // Calculate the start and end points of each line.
            Vector start = calculatePoint(center, radius, i * RADIANS_PER_POINT);
            Vector end = calculatePoint(center, radius, ((i + 2) % POINTS) * RADIANS_PER_POINT);

            // Draw the line.
            for (int j = 0; j < PARTICLES_PER_LINE; j++) {
                Vector point = linearInterpolation(start, end, (double) j / (PARTICLES_PER_LINE - 1));
                location.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, point.toLocation(location.getWorld()), 0);
            }
        }
    }

    private Vector calculatePoint(Vector center, double radius, double angle) {
        return center.clone().add(new Vector(radius * Math.cos(angle), 0, radius * Math.sin(angle)));
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