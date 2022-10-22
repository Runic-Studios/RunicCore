package com.runicrealms.plugin.spellapi.spells.rogue;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.classes.ClassEnum;
import com.runicrealms.plugin.spellapi.spelltypes.EffectEnum;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.spellapi.spelltypes.WeaponDamageSpell;
import com.runicrealms.plugin.utilities.DamageUtil;
import com.runicrealms.plugin.utilities.FloatingItemUtil;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.UUID;

@SuppressWarnings("FieldCanBeLocal")
public class Cannonfire extends Spell implements WeaponDamageSpell {

    private static final int DAMAGE_AMT = 35;
    private static final int DURATION = 2;
    private static final int PELLET_SPEED = 1;
    private static final int TOTAL_PELLETS = 5;
    private static final double DAMAGE_PER_LEVEL = 1.75;
    private static final double KNOCKBACK_MULT = -2.75;
    private static final Material MATERIAL = Material.FIREWORK_STAR;
    private final HashMap<UUID, UUID> hasBeenHit;

    public Cannonfire() {
        super("Cannonfire",
                "You fire a flurry of " + TOTAL_PELLETS + " shrapnel fragments! " +
                        "On hit, each fragment will deal " +
                        "(" + DAMAGE_AMT + " + &f" + DAMAGE_PER_LEVEL + "x&7 lvl) weapon⚔ damage, " +
                        "silences the target for " + DURATION + "s, and launches them back!",
                ChatColor.WHITE, ClassEnum.ROGUE, 7, 15);
        this.hasBeenHit = new HashMap<>();
    }

    @Override
    public void executeSpell(Player player, SpellItemType type) {
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_EGG_THROW, 0.5f, 0.5f);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ZOMBIE_BREAK_WOODEN_DOOR, 0.5f, 2.0f);
        player.swingMainHand();
        final Vector vector = player.getEyeLocation().add(0, 1, 0).getDirection().normalize().multiply(PELLET_SPEED);
        Location left = player.getEyeLocation().clone().add(1, 0, 0);
        Location middle = player.getEyeLocation();
        Location right = player.getEyeLocation().clone().add(-1, 0, 0);
        Location bottomLeft = player.getEyeLocation().clone().add(-0.5, -1, 0);
        Location bottomRight = player.getEyeLocation().clone().add(0.5, -1, 0);
        firePellets(player, new Location[]{left, middle, right, bottomLeft, bottomRight}, vector);
    }

    private void firePellets(Player player, Location[] pelletLocations, Vector vector) {
        for (Location location : pelletLocations) {
            Entity pellet = FloatingItemUtil.spawnFloatingItem(location, MATERIAL, 50, vector, 0);
            Spell spell = this;
            new BukkitRunnable() {
                @Override
                public void run() {

                    if (pellet.isOnGround() || pellet.isDead()) {
                        if (pellet.isOnGround()) {
                            pellet.remove();
                        }
                        this.cancel();
                        return;
                    }

                    Location loc = pellet.getLocation();
                    pellet.getWorld().spawnParticle(Particle.CRIT, pellet.getLocation(), 1, 0, 0, 0, 0);

                    for (Entity entity : pellet.getWorld().getNearbyEntities(loc, 1.5, 1.5, 1.5)) {
                        if (verifyEnemy(player, entity)) {
                            if (hasBeenHit.get(player.getUniqueId()) == entity.getUniqueId()) continue;
                            explode(entity, player, spell, pellet);
                        }
                    }
                }
            }.runTaskTimer(RunicCore.getInstance(), 0, 1L);

            Bukkit.getScheduler().runTaskLaterAsynchronously(RunicCore.getInstance(), hasBeenHit::clear, DURATION * 20L);
        }
    }

    private void explode(Entity victim, Player shooter, Spell spell, Entity pellet) {
        hasBeenHit.put(shooter.getUniqueId(), victim.getUniqueId()); // prevent concussive hits
        pellet.remove();
        victim.getWorld().playSound(victim.getLocation(), Sound.ENTITY_DRAGON_FIREBALL_EXPLODE, 0.5f, 1.0f);
        victim.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, victim.getLocation(), 1, 0, 0, 0, 0);
        DamageUtil.damageEntityWeapon(DAMAGE_AMT, (LivingEntity) victim, shooter, false, false, spell);
        addStatusEffect(victim, EffectEnum.SILENCE, DURATION);
        Vector force = shooter.getLocation().toVector().subtract(victim.getLocation().toVector()).normalize().multiply(KNOCKBACK_MULT);
        victim.setVelocity(force);
    }

    @Override
    public double getDamagePerLevel() {
        return DAMAGE_PER_LEVEL;
    }
}
