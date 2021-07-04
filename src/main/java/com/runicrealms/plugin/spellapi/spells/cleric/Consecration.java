package com.runicrealms.plugin.spellapi.spells.cleric;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.classes.ClassEnum;
import com.runicrealms.plugin.spellapi.spelltypes.MagicDamageSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

@SuppressWarnings("FieldCanBeLocal")
public class Consecration extends Spell implements MagicDamageSpell {

    private static final int DAMAGE_AMT = 35;
    private static final int DAMAGE_PER_LEVEL = 2;
    private static final int DURATION = 8;
    private static final int RADIUS = 7;

    public Consecration() {
        super("Consecration",
                "You conjure a ring of holy magic on the ground " +
                        "for " + DURATION + "s, slowing enemies within " + RADIUS + " " +
                        "blocks and dealing (" + DAMAGE_AMT + " + &f" + DAMAGE_PER_LEVEL
                        + "x&7 lvl) spellʔ damage each second!",
                ChatColor.WHITE, ClassEnum.CLERIC, 15, 20);
    }

    @Override
    public void executeSpell(Player pl, SpellItemType type) {

        Location castLocation = pl.getLocation();
        Spell spell = this;

        new BukkitRunnable() {
            int count = 1;
            @Override
            public void run() {
                if (count > DURATION) {
                    this.cancel();
                } else {
                    count += 1;
                    createCircle(pl, castLocation);
                    pl.getWorld().playSound(castLocation, Sound.ENTITY_CAT_HISS, 0.5f, 0.1f);
                    for (Entity en : pl.getNearbyEntities(RADIUS, RADIUS, RADIUS)) {
                        if (!(verifyEnemy(pl, en))) continue;
                        LivingEntity victim = (LivingEntity) en;
                        victim.getWorld().playSound(victim.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5f, 2.0f);
                        victim.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 60, 2));
                        DamageUtil.damageEntitySpell(DAMAGE_AMT, victim, pl, spell);
                    }
                }
            }
        }.runTaskTimer(RunicCore.getInstance(), 0, 20L);
    }

    private void createCircle(Player pl, Location loc) {
        int particles = 50;
        for (int i = 0; i < particles; i++) {
            double angle, x, z;
            angle = 2 * Math.PI * i / particles;
            x = Math.cos(angle) * (float) RADIUS;
            z = Math.sin(angle) * (float) RADIUS;
            loc.add(x, 0, z);
            pl.getWorld().spawnParticle(Particle.SPELL_INSTANT, loc, 5, 0, 0, 0, 0);
            pl.getWorld().spawnParticle(Particle.REDSTONE, loc, 5, 0, 0, 0, 0, new Particle.DustOptions(Color.WHITE, 1));
            loc.subtract(x, 0, z);
        }
    }

    @Override
    public double getDamagePerLevel() {
        return DAMAGE_PER_LEVEL;
    }
}
