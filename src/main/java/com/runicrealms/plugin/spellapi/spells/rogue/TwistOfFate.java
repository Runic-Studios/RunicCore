package com.runicrealms.plugin.spellapi.spells.rogue;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.classes.ClassEnum;
import com.runicrealms.plugin.spellapi.spelltypes.EffectEnum;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@SuppressWarnings("FieldCanBeLocal")
public class TwistOfFate extends Spell {

    private static final int DURATION = 5;
    private static final int MAX_DIST = 10;
    private static final int RADIUS = 5;
    private Firework firework;
    private final HashSet<UUID> invertedPlayers;

    // todo: finish
    public TwistOfFate() {
        super ("Twist of Fate",
                "You cast a wicked curse of inversion, " +
                        "striking all enemies within " + RADIUS +
                        " blocks! Enemies affected by the curse " +
                        "have their restoring✦ effects inverted " +
                        "for " + DURATION + "s, suffering damage " +
                        "instead! Against monsters, this ability will " +
                        "silence for the duration.",
                ChatColor.WHITE, ClassEnum.ROGUE, 1, 35); // cd: 40
        invertedPlayers = new HashSet<>();
    }

    @Override
    public void executeSpell(Player player, SpellItemType type) {
        // todo: wither death sound? ender dragon death sound?
        Location location = player.getLocation();
        player.getWorld().spigot().strikeLightningEffect(location, true);
        BukkitRunnable rain = new BukkitRunnable() {
            int count = 1;
            @Override
            public void run() {
                if (count > DURATION)
                    this.cancel();
                else {
                    count += 1;
                    spawnRing(player, location);
                }
            }
        };
        rain.runTaskTimer(RunicCore.getInstance(), 0, 20L);
    }

    /**
     *
     * @param pl
     * @param loc
     * @return
     */
    private Location spawnRing(Player pl, Location loc) {

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
                    new Particle.DustOptions(Color.TEAL, 1));
            pl.getWorld().spawnParticle(Particle.CRIT_MAGIC, loc, 5, 0, 0, 0, 0);
            loc.subtract(x, 0, z);
        }

        int ranX = ThreadLocalRandom.current().nextInt(-(RADIUS/2), RADIUS/2 + 1);
        int ranZ = ThreadLocalRandom.current().nextInt(-(RADIUS/2), RADIUS/2 + 1);

        Location tempSplash = new Location(pl.getWorld(), loc.getX()+ranX, loc.getY(), loc.getZ()+ranZ);

        pl.getWorld().spawnParticle(Particle.WATER_SPLASH, tempSplash, 25, 0, 0, 0); // todo: lightning
        pl.getWorld().playSound(tempSplash, Sound.ENTITY_LIGHTNING_BOLT_IMPACT, 0.25f, 1.0f);
        pl.getWorld().spigot().strikeLightningEffect(tempSplash, true);

        return loc;
    }

    /**
     *
     * @param player
     * @param ringLoc
     */
    private void invertPlayers(Player player, Location ringLoc) {
        for (Entity en : player.getWorld().getNearbyEntities(ringLoc, RADIUS, RADIUS, RADIUS)) {
            if (!verifyEnemy(player, en)) continue;
            if (!(en instanceof Player)) {
                addStatusEffect(en, EffectEnum.SILENCE, DURATION);
            } else {
                invertedPlayers.add(en.getUniqueId());
                // particles, sounds
            }
        }
    }
    // todo: heal event?
    // todo: custom regen event
}

