package com.runicrealms.plugin.spellapi.spells.warrior;

import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.spellapi.spellutil.HealUtil;
import com.runicrealms.plugin.spellapi.spellutil.particles.HorizCircleFrame;
import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.adapters.AbstractEntity;
import io.lumine.xikage.mythicmobs.mobs.MythicMob;
import io.lumine.xikage.mythicmobs.skills.SkillCaster;
import io.lumine.xikage.mythicmobs.skills.SkillMetadata;
import io.lumine.xikage.mythicmobs.skills.SkillTrigger;
import org.bukkit.*;
import com.runicrealms.plugin.RunicCore;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@SuppressWarnings("FieldCanBeLocal")
public class UnholyGround extends Spell {

    private static final int DURATION = 6;
    private static final int PERIOD = 1;
    private static final float RADIUS = 5f;
    private HashMap<UUID, Entity> taunted = new HashMap<>();

    public UnholyGround() {
        super("Unholy Ground",
                "For " + DURATION + " seconds, you desecrate an area of" +
                        "\nland, conjuring a ring of unholy magic" +
                        "\nwhich taunts enemy monsters within " + RADIUS +
                        "\nblocks every " + PERIOD + " seconds(s), compelling" +
                        "\nthem to attack you!"
                        , ChatColor.WHITE,1, 10);
    }

    // spell execute code
    @Override
    public void executeSpell(Player pl, SpellItemType type) {

        pl.getWorld().playSound(pl.getLocation(), Sound.ENTITY_SKELETON_DEATH, 0.5F, 0.2F);

        Location loc = pl.getLocation();

        // begin effect
        BukkitRunnable rain = new BukkitRunnable() {
            int count = 0;

            @Override
            public void run() {

                if (count >= DURATION) {
                    this.cancel();
                    taunted.clear();
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

            if (en instanceof Monster && !taunted.containsValue(en)) {
                taunted.put(pl.getUniqueId(), en);
                ((Monster) en).setTarget(pl);
                MythicMobs.inst().getAPIHelper().taunt(en, pl);
                en.getWorld().playSound(en.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 0.5f, 0.2f);
                en.getWorld().spawnParticle
                        (Particle.VILLAGER_ANGRY, en.getLocation(), 5, 0.5F, 0.5F, 0.5F, 0);
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
            pl.getWorld().spawnParticle(Particle.REDSTONE, loc, 5, 0, 0, 0, 0,
                    new Particle.DustOptions(Color.GREEN, 1));
            pl.getWorld().spawnParticle(Particle.REDSTONE, loc, 5, 0, 0, 0, 0,
                    new Particle.DustOptions(Color.BLACK, 1));
            loc.subtract(x, 0, z);
        }
    }
}

