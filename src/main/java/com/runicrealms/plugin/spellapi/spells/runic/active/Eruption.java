package com.runicrealms.plugin.spellapi.spells.runic.active;

import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import com.runicrealms.plugin.RunicCore;

import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

@SuppressWarnings("FieldCanBeLocal")
public class Eruption extends Spell {

    private static final int DAMAGE_AMT = 5;
    private static final int DURATION = 5;
    private static final int PERIOD = 1;
    private static final float RADIUS = 3f;

    public Eruption() {
        super("Eruption",
                "For " + DURATION + " seconds, you summon burning" +
                        "\nfire, conjuring a ring of flames" +
                        "\nwhich deals " + DAMAGE_AMT + " spellʔ damage to enemies" +
                        "\nwithin " + (int) RADIUS + " blocks every " + PERIOD + " second(s)!" +
                        "\n" + ChatColor.DARK_RED + "Gem Bonus: 50%",
                ChatColor.WHITE, 12, 20);
    }

    @Override
    public void executeSpell(Player pl, SpellItemType type) {

        Location loc = pl.getLocation();

        // begin effect
        BukkitRunnable fire = new BukkitRunnable() {
            int count = 1;
            @Override
            public void run() {

                if (count > DURATION) {
                    this.cancel();
                } else {

                    count += 1;
                    spawnRing(pl, loc);
                }
            }
        };
        fire.runTaskTimer(RunicCore.getInstance(), 0, PERIOD*20);
    }

    private void spawnRing(Player pl, Location loc) {

        pl.getWorld().playSound(pl.getLocation(), Sound.BLOCK_LAVA_POP, 0.25f, 2f);
        pl.getWorld().playSound(pl.getLocation(), Sound.ENTITY_DRAGON_FIREBALL_EXPLODE, 0.25f, 2f);

        int particles = 50;
        float radius = RADIUS;

        // create circle
        for (int i = 0; i < particles; i++) {
            double angle, x, z;
            angle = 2 * Math.PI * i / particles;
            x = Math.cos(angle) * radius;
            z = Math.sin(angle) * radius;
            loc.add(x, 0, z);
            pl.getWorld().spawnParticle(Particle.FLAME, loc, 5, 0, 0, 0, 0);
            loc.subtract(x, 0, z);
        }

        int ranX = ThreadLocalRandom.current().nextInt(-((int) (RADIUS/2)), ((int) (RADIUS/2)) + 1);
        int ranZ = ThreadLocalRandom.current().nextInt(-((int) (RADIUS/2)), ((int) (RADIUS/2)) + 1);

        Location tempSplash = new Location(pl.getWorld(), loc.getX()+ranX, loc.getY(), loc.getZ()+ranZ);
        pl.getWorld().spawnParticle(Particle.LAVA, tempSplash, 25, 0, 0, 0);

        for (Entity en : Objects.requireNonNull(loc.getWorld()).getNearbyEntities(loc, RADIUS, RADIUS, RADIUS)) {
            if (verifyEnemy(pl, en)) {
                LivingEntity le = (LivingEntity) en;
                DamageUtil.damageEntitySpell(DAMAGE_AMT, le, pl, true);
            }
        }
    }
}
