package com.runicrealms.plugin.spellapi.spells.cleric;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.classes.CharacterClass;
import com.runicrealms.plugin.spellapi.spelltypes.HealingSpell;
import com.runicrealms.plugin.spellapi.spelltypes.MagicDamageSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.spellapi.spellutil.HealUtil;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.Objects;

@SuppressWarnings("FieldCanBeLocal")
public class HolyNova extends Spell implements MagicDamageSpell, HealingSpell {

    private static final int DAMAGE_AMT = 5;
    private static final double DAMAGE_PER_LEVEL = 0.5;
    private static final int DURATION = 5;
    private static final int HEAL_AMT = 8;
    private static final double HEALING_PER_LEVEL = 0.75;
    private static final float RADIUS = 5f;
    private static final double KNOCKBACK_MULTIPLIER = -1.25;

    public HolyNova() {
        super("Holy Nova",
                "For " + DURATION + "s, you pulse with holy " +
                        "power, conjuring rings of light " +
                        "that deal (" + DAMAGE_AMT + " + &f" + DAMAGE_PER_LEVEL
                        + "x&7 lvl) magicʔ damage to enemies " +
                        "and push them back! The rings restore✸ " +
                        "(" + HEAL_AMT + " + &f" + HEALING_PER_LEVEL +
                        "x&7 lvl) health to allies!",
                ChatColor.WHITE, CharacterClass.CLERIC, 12, 25);
    }

    @Override
    public void executeSpell(Player player, SpellItemType type) {
        Spell spell = this;
        BukkitRunnable nova = new BukkitRunnable() {
            @Override
            public void run() {
                Location loc = player.getLocation();

                Bukkit.getScheduler().runTaskAsynchronously(RunicCore.getInstance(), () -> spawnRing(player));
                for (Entity entity : Objects.requireNonNull(loc.getWorld()).getNearbyEntities(loc, RADIUS, RADIUS, RADIUS)) {

                    if (!(entity instanceof LivingEntity)) continue;
                    LivingEntity livingEntity = (LivingEntity) entity;

                    // Executes the damage aspect of spell
                    if (isValidEnemy(player, entity)) {
                        DamageUtil.damageEntitySpell(DAMAGE_AMT, livingEntity, player, spell);
                        Vector force = player.getLocation().toVector().subtract(entity.getLocation().toVector()).normalize().multiply(KNOCKBACK_MULTIPLIER);
                        entity.setVelocity(force);
                    }

                    // heal party members
                    if (entity instanceof Player && isValidAlly(player, entity))
                        HealUtil.healPlayer(HEAL_AMT, ((Player) livingEntity), player, false, spell);
                }
            }
        };
        nova.runTaskTimer(RunicCore.getInstance(), 0, 20);
        Bukkit.getScheduler().runTaskLaterAsynchronously(RunicCore.getInstance(), nova::cancel, DURATION * 20L);
    }

    @Override
    public double getDamagePerLevel() {
        return DAMAGE_PER_LEVEL;
    }

    @Override
    public int getHeal() {
        return HEAL_AMT;
    }

    @Override
    public double getHealingPerLevel() {
        return HEALING_PER_LEVEL;
    }

    /**
     * Creates a ring of particles around the given location, spawned in the player's world, with the given radius
     *
     * @param player who summoned the particles
     */
    private void spawnRing(Player player) {

        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HARP, 0.5F, 1.0F);

        Location location1 = player.getEyeLocation();
        int particles = 50;
        float radius = RADIUS;

        for (int i = 0; i < particles; i++) {
            double angle, x, z;
            angle = 2 * Math.PI * i / particles;
            x = Math.cos(angle) * radius;
            z = Math.sin(angle) * radius;
            location1.add(x, 0, z);
            player.getWorld().spawnParticle(Particle.FIREWORKS_SPARK, location1, 1, 0, 0, 0, 0);
            location1.subtract(x, 0, z);
        }
    }
}
