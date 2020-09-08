package com.runicrealms.plugin.spellapi.spells.cleric;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.classes.ClassEnum;
import com.runicrealms.plugin.item.GearScanner;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.spellapi.spellutil.HealUtil;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

@SuppressWarnings("FieldCanBeLocal")
public class BlessedRain extends Spell {

    private final boolean blessedFire;
    private final boolean restoreMana;
    private static final int HEALING_AMT = 15;
    private static final int DURATION = 5;
    private static final int PERIOD = 1;
    private static final float RADIUS = 5f;
    private static final double GEM_BOOST = 50;
    private static final double MANA_PERCENT = 35;

    // constructor
    public BlessedRain() {
        super("Blessed Rain",
                "For " + DURATION + " seconds, you summon healing" +
                        "\nwaters, conjuring a ring of light magic" +
                        "\nwhich restores✦ " + HEALING_AMT + " health to allies" +
                        "\nwithin " + (int) RADIUS + " blocks every " + PERIOD + " second(s)!" +
                        "\n" + ChatColor.DARK_RED + "Gem Bonus: " + (int) GEM_BOOST + "%",
                ChatColor.WHITE, ClassEnum.CLERIC, 15, 25);
        this.blessedFire = false;
        this.restoreMana = false;
    }

    /**
     * Used for GUILD and RAID tier sets
     * @param blessedFire whether to use the alternative skill of the tier set
     * @param restoreMana whether to restore mana for RAID tier set
     */
    public BlessedRain(boolean blessedFire, boolean restoreMana) {
        super("Blessed Rain",
                "For " + DURATION + " seconds, you summon healing" +
                        "\nwaters, conjuring a ring of light magic" +
                        "\nwhich restores✦ " + HEALING_AMT + " health to allies" +
                        "\nwithin " + (int) RADIUS + " blocks every " + PERIOD + " second(s)!" +
                        "\n" + ChatColor.DARK_RED + "Gem Bonus: " + (int) GEM_BOOST + "%",
                ChatColor.WHITE, ClassEnum.CLERIC, 15, 25);
        this.blessedFire = blessedFire;
        this.restoreMana = restoreMana;
    }

    @Override
    public void executeSpell(Player pl, SpellItemType type) {

        Location loc = pl.getLocation();
        //Location splashLoc = pl.getLocation();

        // begin effect
        BukkitRunnable rain = new BukkitRunnable() {
            int count = 1;
            @Override
            public void run() {
                if (count > DURATION)
                    this.cancel();
                else {
                    count += 1;
                    spawnRing(pl, loc);
                }
            }
        };
        rain.runTaskTimer(RunicCore.getInstance(), 0, PERIOD*20);
    }

    private void spawnRing(Player pl, Location loc) {

        if (blessedFire)
            pl.getWorld().playSound(pl.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5F, 1.0F);
        else
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
            if (blessedFire) {
                pl.getWorld().spawnParticle(Particle.FLAME, loc, 5, 0, 0, 0, 0);
                pl.getWorld().spawnParticle(Particle.FLAME, loc, 5, 0, 0, 0, 0);
            } else {
                pl.getWorld().spawnParticle(Particle.REDSTONE, loc, 5, 0, 0, 0, 0,
                        new Particle.DustOptions(Color.AQUA, 1));
                pl.getWorld().spawnParticle(Particle.REDSTONE, loc, 5, 0, 0, 0, 0,
                        new Particle.DustOptions(Color.WHITE, 1));
            }
            loc.subtract(x, 0, z);
        }

        int ranX = ThreadLocalRandom.current().nextInt(-((int) (RADIUS/2)), ((int) (RADIUS/2)) + 1);
        int ranZ = ThreadLocalRandom.current().nextInt(-((int) (RADIUS/2)), ((int) (RADIUS/2)) + 1);

        Location tempSplash = new Location(pl.getWorld(), loc.getX()+ranX, loc.getY(), loc.getZ()+ranZ);

        if (blessedFire) {
            pl.getWorld().spawnParticle(Particle.LAVA, tempSplash, 5, 0, 0, 0);
        } else {
            pl.getWorld().spawnParticle(Particle.WATER_SPLASH, tempSplash, 25, 0, 0, 0);
            pl.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, tempSplash, 25, 0, 0, 0);
        }

        // heal people
        for (Entity entity : Objects.requireNonNull(loc.getWorld()).getNearbyEntities(loc, RADIUS, RADIUS, RADIUS)) {

            if (blessedFire) {
                if (verifyEnemy(pl, entity)) {
                    pl.getWorld().spawnParticle(Particle.FLAME, entity.getLocation(), 5, 0, 0, 0);
                    DamageUtil.damageEntitySpell(HEALING_AMT, (LivingEntity) entity, pl, 50);
                }

            } else {

                // skip non-players
                if (!(entity instanceof Player)) continue;

                // heal party members and the caster
                Player ally = (Player) entity;
                if (verifyAlly(pl, ally)) {
                    HealUtil.healPlayer(HEALING_AMT, ally, pl, true, true, false);
                    if (restoreMana)
                        RunicCore.getRegenManager().addMana(ally, (int) ((HEALING_AMT + GearScanner.getHealingBoost(pl)) * (MANA_PERCENT / 100)), false);
                }
            }
        }
    }

    public static double getManaPercent() {
        return MANA_PERCENT;
    }
}
