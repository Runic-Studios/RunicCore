package com.runicrealms.plugin.spellapi.spells.runic;

import com.runicrealms.plugin.events.SpellDamageEvent;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
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

import java.util.*;

import static java.lang.Math.cos;
import static java.lang.Math.sin;

@SuppressWarnings("FieldCanBeLocal")
public class ArcaneOrb extends Spell {

    // global variables
    private static final int DURATION = 7;
    private static final int MAX_DIST = 5;
    private static final double PERCENT = 25;
    private static final int RADIUS = 5;
    private HashMap<UUID, Location> buffed = new HashMap<>();

    // constructor
    public ArcaneOrb() {
        super("Arcane Orb",
                "You summon an orb of arcane magic!" +
                        "\nFor the next " + DURATION + " seconds, all ʔspell" +
                        "\ndamage you deal is increased by " + (int) PERCENT + "%" +
                        "\nif you stand within " + RADIUS + " blocks" +
                        "\nof the orb!", ChatColor.WHITE,15, 20);
    }

    // spell execute code
    @Override
    public void executeSpell(Player pl, SpellItemType type) {

        Location loc = pl.getTargetBlock(null, MAX_DIST).getLocation().add(0, 3, 0);
        Location circleLoc = loc.clone().subtract(0, 3, 0);
        buffed.put(pl.getUniqueId(), circleLoc);
        new BukkitRunnable() {
            int count = 1;
            @Override
            public void run() {
                if (count > DURATION) {
                    this.cancel();
                    buffed.remove(pl.getUniqueId());
                } else {
                    count += 1;
                    spawnSphere(loc);
                    createCircle(pl, circleLoc, RADIUS);
                }
            }
        }.runTaskTimerAsynchronously(RunicCore.getInstance(), 0, 20L);
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
                    new Particle.DustOptions(Color.FUCHSIA, 1));
            loc.subtract(x, 0, z);
        }
    }

    private void spawnSphere(Location loc) {

        for (double i = 0; i <= Math.PI; i += Math.PI / 12) {
            double radius = Math.sin(i);
            double y = Math.cos(i);
            for (double a = 0; a < Math.PI * 2; a+= Math.PI / 12) {
                double x = .9 * Math.cos(a) * radius;
                double z = .9 * Math.sin(a) * radius;
                loc.add(x, y, z);
                Objects.requireNonNull(loc.getWorld()).spawnParticle(Particle.SPELL_WITCH, loc, 1, 0, 0, 0, 0);
                loc.subtract(x, y, z);
            }
        }
    }

    @EventHandler
    public void onSpellDamage(SpellDamageEvent e) {

        Player pl = e.getPlayer();
        UUID id = pl.getUniqueId();
        if (buffed.get(id) == null) return;

        double dist = pl.getLocation().distance(buffed.get(id));

        if (dist <= RADIUS) {
            double percent = PERCENT / 100;
            int extraAmt = (int) (e.getAmount() * percent);
            e.setAmount(e.getAmount() + extraAmt);
        }
    }
}

