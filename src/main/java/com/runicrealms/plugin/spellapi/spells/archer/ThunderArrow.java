package com.runicrealms.plugin.spellapi.spells.archer;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.classes.CharacterClass;
import com.runicrealms.plugin.spellapi.spelltypes.MagicDamageSpell;
import com.runicrealms.plugin.spellapi.spelltypes.RadiusSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.spellapi.spellutil.particles.EntityTrail;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class ThunderArrow extends Spell implements MagicDamageSpell, RadiusSpell {
    private final List<Arrow> powerShots;
    private double damage;
    private double damagePerLevel;
    private double radius;

    public ThunderArrow() {
        super("Thunder Arrow", CharacterClass.ARCHER);
        powerShots = new ArrayList<>();
        this.setDescription("You launch an enchanted arrow that " +
                "deals (" + damage + " + &f" + damagePerLevel
                + "x&7 lvl)" + " magicʔ damage on-hit to " +
                "all enemies within " + radius + " blocks!");
    }

    @Override
    public void executeSpell(Player player, SpellItemType type) {
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ARROW_SHOOT, 0.5f, 1f);
        player.getWorld().playSound(player.getLocation(), Sound.ITEM_FIRECHARGE_USE, 0.5f, 2f);
        Vector vec = player.getEyeLocation().getDirection().normalize().multiply(2);
        startTask(player, vec);
    }

    @Override
    public double getMagicDamage() {
        return damage;
    }

    @Override
    public void setMagicDamage(double magicDamage) {
        this.damage = magicDamage;
    }

    @Override
    public double getMagicDamagePerLevel() {
        return damagePerLevel;
    }

    @Override
    public void setMagicDamagePerLevel(double magicDamagePerLevel) {
        this.damagePerLevel = magicDamagePerLevel;
    }

    @Override
    public double getRadius() {
        return radius;
    }

    @Override
    public void setRadius(double radius) {
        this.radius = radius;
    }

    @EventHandler
    public void onSearingArrowHit(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Arrow arrow)) return;
        if (!(arrow.getShooter() instanceof Player)) return;
        if (!powerShots.contains(arrow)) return;
        event.setCancelled(true);
    }

    private void startTask(Player player, Vector vector) {
        Arrow powerShot = player.launchProjectile(Arrow.class);
        powerShot.setVelocity(vector);
        powerShot.setShooter(player);
        powerShots.add(powerShot);
        Spell spell = this;
        EntityTrail.entityTrail(powerShot, Color.fromRGB(0, 71, 72));
        new BukkitRunnable() {
            @Override
            public void run() {
                Location arrowLoc = powerShot.getLocation();
                if (powerShot.isDead() || powerShot.isOnGround()) {
                    this.cancel();
                    player.getWorld().playSound(player.getLocation(), Sound.BLOCK_LAVA_POP, 0.5f, 2f);
                    player.getWorld().playSound(player.getLocation(), Sound.ENTITY_DRAGON_FIREBALL_EXPLODE, 0.5f, 2f);
                    powerShot.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, arrowLoc, 10, 0, 0, 0, 0);
                    powerShot.getWorld().spawnParticle(Particle.CRIT_MAGIC, arrowLoc, 25, 0.5f, 0.5f, 0.5f, 0);
                    for (Entity entity : player.getWorld().getNearbyEntities(arrowLoc, radius, radius, radius)) {
                        if (!isValidEnemy(player, entity)) continue;
                        DamageUtil.damageEntitySpell(damage, (LivingEntity) entity, player, spell);
                    }
                }
            }
        }.runTaskTimer(RunicCore.getInstance(), 0, 1L);
    }
}
