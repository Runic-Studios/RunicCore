package com.runicrealms.plugin.spellapi.spells.mage;

import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.spellapi.spellutil.HealUtil;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import com.runicrealms.plugin.RunicCore;

import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

@SuppressWarnings("FieldCanBeLocal")
public class Sandstorm extends Spell {

    private static final int HEALING_AMT = 5;
    private static final int DURATION = 5;
    private static final int PERIOD = 1;
    private static final float RADIUS = 3f;

    // constructor
    public Sandstorm() {
        super("Sandstorm", "For " + DURATION + " seconds, you summon healing" +
                        "\nwaters, conjuring a ring of light magic" +
                        "\nwhich restores✦ " + HEALING_AMT + " health to allies" +
                        "\nwithin " + (int) RADIUS + " blocks every " + PERIOD + " second(s)!" +
                        "\n" + ChatColor.DARK_RED + "Gem Bonus: 50%",
                ChatColor.WHITE, 12, 20);
    }

    @Override
    public void executeSpell(Player pl, SpellItemType type) {

        Location loc = pl.getLocation();

        // begin effect
        final long startTime = System.currentTimeMillis();
        BukkitRunnable rain = new BukkitRunnable() {
            //int count = 1;
            @Override
            public void run() {

                long timePassed = System.currentTimeMillis() - startTime;
                if (timePassed > DURATION * 1000) {
                    this.cancel();
                } else {
                    //count += 1;
                    spawnRing(pl, loc);
                }
            }
        };
        rain.runTaskTimer(RunicCore.getInstance(), 0, 1L);
    }

    public int strands = 9;
    public int particles = 25;
    public float curve = 20.0F;
    public double rotation = 0.7853981633974483D;

    private void spawnRing(Player pl, Location loc) {

        //Location temp = new Location(pl.getWorld(), loc.getX(), loc.getY(), loc.getZ());

        pl.getWorld().playSound(pl.getLocation(), Sound.WEATHER_RAIN, 0.5F, 1.0F);

        //int particles = 50;
        float radius = RADIUS;

        // create circle
        for (int i = 1; i <= this.strands; i++) {
            for (int j = 1; j <= this.particles; j++) {
                float ratio = j / this.particles;
                double angle = this.curve * ratio * 2.0F * Math.PI / this.strands + (2*Math.PI) * i / this.strands + this.rotation;
                double x = Math.cos(angle) * ratio * radius;
                double z = Math.sin(angle) * ratio * radius;
                loc.add(x, 0.0D, z);
                pl.getWorld().spawnParticle(Particle.CRIT, loc, 1, 0, 0, 0, 0);
                loc.subtract(x, 0.0D, z);
            }
        }

//        int ranX = ThreadLocalRandom.current().nextInt(-((int) (RADIUS/2)), ((int) (RADIUS/2)) + 1);
//        int ranZ = ThreadLocalRandom.current().nextInt(-((int) (RADIUS/2)), ((int) (RADIUS/2)) + 1);
//
//        Location tempSplash = new Location(pl.getWorld(), loc.getX()+ranX, loc.getY(), loc.getZ()+ranZ);
//
//        pl.getWorld().spawnParticle(Particle.CRIT, tempSplash, 25, 0, 0, 0);
//        pl.getWorld().spawnParticle(Particle.SMOKE_LARGE, tempSplash, 25, 0, 0, 0);
//
//        // heal people
//        for (Entity entity : Objects.requireNonNull(loc.getWorld()).getNearbyEntities(loc, RADIUS, RADIUS, RADIUS)) {
//
//            // skip non-players
//            if (!(entity instanceof Player)) {
//                continue;
//            }
//
//            // heal party members and the caster
//            Player ally = (Player) entity;
//            HealUtil.healPlayer(HEALING_AMT, ally, pl, true, true, true);
//        }
    }
}
