package com.runicrealms.plugin.spellapi.spells.warrior;

import com.runicrealms.plugin.events.MobDamageEvent;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import io.lumine.xikage.mythicmobs.MythicMobs;
import org.bukkit.*;
import com.runicrealms.plugin.RunicCore;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

@SuppressWarnings("FieldCanBeLocal")
public class UnholyGround extends Spell {

    private static final int DURATION = 6;
    private static final double PERCENT = 75;
    private static final int PERIOD = 1;
    private static final float RADIUS = 5f;
    private List<LivingEntity> taunted = new ArrayList<>();
    private HashMap<UUID, Location> taunters = new HashMap<>();

    public UnholyGround() {
        super("Unholy Ground",
                "For " + DURATION + " seconds, you desecrate an area of" +
                        "\nland, conjuring a ring of unholy magic" +
                        "\nthat taunts enemy monsters within " + (int) RADIUS +
                        "\nblocks every " + PERIOD + " seconds(s)! While standing" +
                        "\non the unholy ground, you take " + (int) PERCENT + "%" +
                        "\nless damage from monsters!"
                        , ChatColor.WHITE,12, 15);
    }

    // spell execute code
    @Override
    public void executeSpell(Player pl, SpellItemType type) {

        pl.getWorld().playSound(pl.getLocation(), Sound.ENTITY_SKELETON_DEATH, 0.5F, 0.2F);
        Location loc = pl.getLocation();

        // save uuid and location
        taunters.put(pl.getUniqueId(), loc);

        // begin effect
        BukkitRunnable rain = new BukkitRunnable() {
            int count = 0;

            @Override
            public void run() {

                if (count >= DURATION) {
                    this.cancel();
                    taunted.clear();
                    taunters.remove(pl.getUniqueId());
                } else {

                    count += 1;
                    spawnRing(pl, loc, count);
                }
            }
        };
        rain.runTaskTimer(RunicCore.getInstance(), 0, PERIOD * 20);
    }


    private void spawnRing(Player pl, Location loc, int count) {

        pl.getWorld().playSound(pl.getLocation(), Sound.BLOCK_SLIME_BLOCK_BREAK, 0.5F, 0.2F);
        pl.getWorld().playSound(pl.getLocation(), Sound.ENTITY_SKELETON_DEATH, 0.5F, 2.0F);

        float radius = RADIUS;

        // create circle
        createCircle(pl, loc, radius);

        // create smaller circles
        createCircle(pl, loc, (radius-(count-1)));

        // taunt the baddies
        for (Entity en : Objects.requireNonNull(loc.getWorld()).getNearbyEntities(loc, RADIUS, RADIUS, RADIUS)) {

            if (!(en instanceof LivingEntity)) continue;

            if (taunted.contains(en)) {
                continue;
            }

            if (en instanceof Monster) {
                taunted.add((LivingEntity) en);
                ((Monster) en).setTarget(pl);
                MythicMobs.inst().getAPIHelper().taunt(en, pl);
                en.getWorld().playSound(en.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 0.5f, 0.2f);
                en.getWorld().spawnParticle
                        (Particle.VILLAGER_ANGRY, en.getLocation(), 3, 0.5F, 0.5F, 0.5F, 0);
            }
        }
    }

    private void createCircle(Player pl, Location loc, float radius) {
        int particles = 50;
        for (int i = 0; i < particles; i++) {
            double angle, x, z;
            angle = 2 * Math.PI * i / particles;
            x = Math.cos(angle) * radius;
            z = Math.sin(angle) * radius;
            loc.add(x, 0, z);
            pl.getWorld().spawnParticle(Particle.SLIME, loc, 1, 0, 0, 0, 0);//,new Particle.DustOptions(Color.GREEN, 1)
            pl.getWorld().spawnParticle(Particle.REDSTONE, loc, 1, 0, 0, 0, 0,
                    new Particle.DustOptions(Color.YELLOW, 1));
            loc.subtract(x, 0, z);
        }
    }

    @EventHandler
    public void onMonsterDamage(MobDamageEvent e) {

        if (!(e.getVictim() instanceof Player)) return;

        Player pl = (Player) e.getVictim();

        if (!taunters.containsKey(pl.getUniqueId())) return;

        Location loc = taunters.get(pl.getUniqueId());
        if (pl.getLocation().distance(loc) <= RADIUS) {
            double percent = (100-PERCENT) / 100;
            int amt = (int) (e.getAmount() * percent);
            e.setAmount(amt);
        }
    }
}

