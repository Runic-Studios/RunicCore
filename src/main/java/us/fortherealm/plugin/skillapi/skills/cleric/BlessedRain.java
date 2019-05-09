package us.fortherealm.plugin.skillapi.skills.cleric;

import org.bukkit.*;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import us.fortherealm.plugin.FTRCore;
import us.fortherealm.plugin.skillapi.skilltypes.Skill;
import us.fortherealm.plugin.skillapi.skilltypes.SkillItemType;
import us.fortherealm.plugin.skillapi.skilltypes.skillutil.HealUtil;
import us.fortherealm.plugin.utilities.DamageUtil;

@SuppressWarnings("FieldCanBeLocal")
public class BlessedRain extends Skill {

    private static final int HEALING_AMT = 5;
    private static final int DURATION = 6;
    private static final int PERIOD = 1;
    private static final float RADIUS = 3.5f;

    // constructor
    public BlessedRain() {
        super("Blessed Rain", "For " + DURATION + " seconds, you summon healing waters," +
                        "\nconjuring a ring of light magic which" +
                        "\nrestores " + HEALING_AMT + " health every " + PERIOD + " seconds to allies!",
                ChatColor.WHITE, 1, 5);
    }

    @Override
    public void executeSkill(Player pl, SkillItemType type) {

        Location loc = pl.getLocation();

        // begin effect
        BukkitRunnable rain = new BukkitRunnable() {
            @Override
            public void run() {
                spawnRing(pl, loc);
            }
        };
        rain.runTaskTimer(FTRCore.getInstance(), 0, PERIOD*20);

        // cancel effect
        new BukkitRunnable() {
            @Override
            public void run() {
                rain.cancel();
            }
        }.runTaskLater(FTRCore.getInstance(), DURATION*20);
    }

    private void spawnRing(Player pl, Location loc) {

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

        // heal people
        for (Entity entity : pl.getNearbyEntities(RADIUS, RADIUS, RADIUS)) {

            // skip the caster
            if(entity.equals(pl)) { continue; }

            // skip non-party members
            if (FTRCore.getPartyManager().getPlayerParty(pl) != null
                    && !FTRCore.getPartyManager().getPlayerParty(pl).hasMember(entity.getUniqueId())) { continue; }

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
