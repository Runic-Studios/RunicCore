package com.runicrealms.plugin.spellapi.spells.cleric;

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
public class BlessedRain extends Spell {

    private static final int HEALING_AMT = 5;
    private static final int DURATION = 6;
    private static final int PERIOD = 1;
    private static final float RADIUS = 5f;

    // constructor
    public BlessedRain() {
        super("Blessed Rain", "For " + DURATION + " seconds, you summon healing waters," +
                        "\nconjuring a ring of light magic which restores" +
                        "\n" + HEALING_AMT + " health every " + PERIOD + " second(s) to party members!",
                ChatColor.WHITE, 1, 5);
    }

    @Override
    public void executeSpell(Player pl, SpellItemType type) {

        Location loc = pl.getLocation();
        //Location splashLoc = pl.getLocation();

        // begin effect
        BukkitRunnable rain = new BukkitRunnable() {
            @Override
            public void run() {
                spawnRing(pl, loc);
            }
        };
        rain.runTaskTimer(RunicCore.getInstance(), 0, PERIOD*20);

        // cancel effect
        new BukkitRunnable() {
            @Override
            public void run() {
                rain.cancel();
            }
        }.runTaskLater(RunicCore.getInstance(), DURATION*20);
    }

    private void spawnRing(Player pl, Location loc) {

        //Location temp = new Location(pl.getWorld(), loc.getX(), loc.getY(), loc.getZ());

        pl.getWorld().playSound(pl.getLocation(), Sound.WEATHER_RAIN, 0.5F, 1.0F);

        int particles = 50;
        float radius = RADIUS;

        // create circle
        for (int i = 0; i < particles; i++) {
            double angle, x, z;
            angle = 2 * Math.PI * i / particles;
            x = Math.cos(angle) * radius;
            z = Math.sin(angle) * radius;
            loc.add(x, 0, z);
            pl.getWorld().spawnParticle(Particle.REDSTONE, loc, 5, 0, 0, 0, 0,
                    new Particle.DustOptions(Color.AQUA, 1));
            pl.getWorld().spawnParticle(Particle.REDSTONE, loc, 5, 0, 0, 0, 0,
                    new Particle.DustOptions(Color.BLUE, 1));
            loc.subtract(x, 0, z);
        }

        int ranX = ThreadLocalRandom.current().nextInt(-((int) (RADIUS/2)), ((int) (RADIUS/2)) + 1);
        int ranZ = ThreadLocalRandom.current().nextInt(-((int) (RADIUS/2)), ((int) (RADIUS/2)) + 1);

        Location tempSplash = new Location(pl.getWorld(), loc.getX()+ranX, loc.getY(), loc.getZ()+ranZ);

        pl.getWorld().spawnParticle(Particle.WATER_SPLASH, tempSplash, 25, 0, 0, 0);
        pl.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, tempSplash, 25, 0, 0, 0);

        // heal people
        for (Entity entity : Objects.requireNonNull(loc.getWorld()).getNearbyEntities(loc, RADIUS, RADIUS, RADIUS)) {

            // skip the caster
            if(entity.equals(pl)) { continue; }

            // skip non-party members
            if (RunicCore.getPartyManager().getPlayerParty(pl) != null
                    && !RunicCore.getPartyManager().getPlayerParty(pl).hasMember(entity.getUniqueId())) { continue; }

            // skip non-players
            if (!(entity instanceof Player)) {
                continue;
            }

            // heal allies
            Player ally = (Player) entity;
            HealUtil.healPlayer(HEALING_AMT, ally, pl);
        }
    }
}
