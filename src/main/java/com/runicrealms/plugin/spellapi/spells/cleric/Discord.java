package com.runicrealms.plugin.spellapi.spells.cleric;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.classes.ClassEnum;
import com.runicrealms.plugin.spellapi.spelltypes.EffectEnum;
import com.runicrealms.plugin.spellapi.spelltypes.MagicDamageSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.spellapi.spellutil.VectorUtil;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import static java.lang.Math.cos;
import static java.lang.Math.sin;

public class Discord extends Spell implements MagicDamageSpell {

    private static final int BUBBLE_DURATION = 6;
    private static final int BUBBLE_SIZE = 5;
    private static final double UPDATES_PER_SECOND = 10;
    private static final int DELAY = 2;
    private static final int DAMAGE_AMT = 20;
    private static final double DAMAGE_PER_LEVEL = 2.5;
    private static final int DURATION = 3;
    private static final int RADIUS = 8;

    public Discord() {
        super("Discord",
                "You prime yourself with a chaotic magic! After " + DELAY + "s, " +
                        "enemies within " + RADIUS + " blocks are stunned for " +
                        DURATION + "s and suffer (" + DAMAGE_AMT + " + &f" + DAMAGE_PER_LEVEL
                        + "x&7 lvl) spellʔ damage!",
                ChatColor.WHITE, ClassEnum.CLERIC, 20, 20);
    }

    @Override
    public void executeSpell(Player player, SpellItemType type) {
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_TNT_PRIMED, 0.5f, 1.0f);
        // todo: particle orb that gets smaller each tick, explosion whether it hits somebody or not
        particleTask(player);
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> discord(player), DELAY * 20L);
    }

    private void particleTask(Player player) {
        final long startTime = System.currentTimeMillis();
        new BukkitRunnable() {
            double phi = 0;

            @Override
            public void run() {

                // create visual bubble
                phi += Math.PI / 10;
                Location loc = player.getLocation();
                for (double theta = 0; theta <= 2 * Math.PI; theta += Math.PI / 40) {
                    double x = BUBBLE_SIZE * cos(theta) * sin(phi);
                    double y = BUBBLE_SIZE * cos(phi) + 1.5;
                    double z = BUBBLE_SIZE * sin(theta) * sin(phi);
                    loc.add(x, y, z);
                    player.getWorld().spawnParticle(Particle.NOTE, loc, 1, 0, 0, 0, 0);
                    loc.subtract(x, y, z);
                }

                // Spell duration, allow cancel by sneaking
                long timePassed = System.currentTimeMillis() - startTime;
                if (timePassed > BUBBLE_DURATION * 1000 || player.isSneaking()) {
                    this.cancel();
//                    for (BukkitTask bukkitTask : coneTasks) {
//                        bukkitTask.cancel();
//                    }
//                    coneTasks.clear();
//                    fateCasters.clear();
                    return;
                }

                // More effect noises
                player.getWorld().playSound(player.getLocation(), Sound.ENTITY_CAT_HISS, 0.01F, 0.5F);
            }
        }.runTaskTimer(RunicCore.getInstance(), 0, (int) (20 / UPDATES_PER_SECOND));
    }

    private void discord(Player player) {
        for (Entity en : player.getNearbyEntities(RADIUS, RADIUS, RADIUS)) {
            if (!(verifyEnemy(player, en))) continue;
            causeDiscord(player, (LivingEntity) en);
            addStatusEffect(en, EffectEnum.STUN, DURATION);
            DamageUtil.damageEntitySpell(DAMAGE_AMT, (LivingEntity) en, player, this);
        }
    }

    private void causeDiscord(Player caster, LivingEntity victim) {
        caster.getWorld().playSound(caster.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_IMPACT, 0.5f, 2.0f);
        caster.getWorld().playSound(caster.getLocation(), Sound.BLOCK_NOTE_BLOCK_HARP, 0.5F, 1.0F);
        caster.getWorld().playSound(caster.getLocation(), Sound.BLOCK_NOTE_BLOCK_HARP, 0.5F, 0.6F);
        caster.getWorld().playSound(caster.getLocation(), Sound.BLOCK_NOTE_BLOCK_HARP, 0.5F, 0.2F);
        VectorUtil.drawLine(caster, Particle.CRIT_MAGIC, Color.WHITE, caster.getEyeLocation(), victim.getEyeLocation(), 1);
    }

    @Override
    public double getDamagePerLevel() {
        return DAMAGE_PER_LEVEL;
    }
}
