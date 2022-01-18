package com.runicrealms.plugin.spellapi.spells.archer;

import com.runicrealms.plugin.classes.ClassEnum;
import com.runicrealms.plugin.spellapi.spelltypes.EffectEnum;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.spellapi.spelltypes.WeaponDamageSpell;
import com.runicrealms.plugin.spellapi.spellutil.particles.EntityTrail;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.*;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

@SuppressWarnings("FieldCanBeLocal")
public class BindingShot extends Spell implements WeaponDamageSpell {

    private static final int DAMAGE = 25;
    private static final int DAMAGE_PER_LEVEL = 2;
    private static final int DURATION = 4;
    private static final int RADIUS = 3;
    private Arrow bindingArrow;

    public BindingShot() {
        super("Binding Shot",
                "You fire a cursed arrow which " +
                        "deals (" + DAMAGE + " + &f" + DAMAGE_PER_LEVEL
                        + "x&7 lvl) weapon⚔ damage to its " +
                        "target and creates a small rift at its " +
                        "location! For " + DURATION + "s, enemies " +
                        "within " + RADIUS + " blocks are pulled to " +
                        "the rift!",
                ChatColor.WHITE, ClassEnum.ARCHER, 16, 25);
    }

    @Override
    public void executeSpell(Player pl, SpellItemType type) {
        pl.getWorld().playSound(pl.getLocation(), Sound.ENTITY_ARROW_SHOOT, 0.5f, 0.5f);
        pl.getWorld().playSound(pl.getLocation(), Sound.ENTITY_WITCH_CELEBRATE, 2f, 0.5f);
        pl.getWorld().playSound(pl.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_SHOOT, 0.5f, 2f);
        bindingArrow = pl.launchProjectile(Arrow.class);
        Vector vec = pl.getEyeLocation().getDirection().normalize().multiply(2);
        bindingArrow.setVelocity(vec);
        bindingArrow.setShooter(pl);
        EntityTrail.entityTrail(bindingArrow, Color.PURPLE);
    }

    @EventHandler
    public void onBindingArrowHit(EntityDamageByEntityEvent e) {

        // only listen for arrows
        if (!e.getDamager().equals(this.bindingArrow)) return;
        e.setCancelled(true);
        Player pl = (Player) ((Arrow) e.getDamager()).getShooter();
        if (pl == null) return;
        LivingEntity livingEntity = (LivingEntity) e.getEntity();
        if (!verifyEnemy(pl, livingEntity)) return;

        // spell effect
        addStatusEffect(livingEntity, EffectEnum.SILENCE, DURATION);
        DamageUtil.damageEntityWeapon(DAMAGE, livingEntity, pl, false, true, this);

        livingEntity.getWorld().playSound(livingEntity.getLocation(), Sound.ENTITY_WITCH_DEATH, 0.5f, 0.5f);

        // spawn rift
        Location castLocation = pl.getLocation();
        while (castLocation.getBlock().getRelative(BlockFace.DOWN).getType() == Material.AIR)
            castLocation = castLocation.getBlock().getRelative(BlockFace.DOWN).getLocation();
        pl.getWorld().playSound(pl.getLocation(), Sound.BLOCK_PORTAL_TRAVEL, 0.5F, 2.0F);
        Location finalCastLocation = castLocation;
        new BukkitRunnable() {
            int count = 1;

            @Override
            public void run() {
                if (count > DURATION)
                    this.cancel();
                else {
                    count++;
                    spawnRift(pl, finalCastLocation);
                }
            }
        }.runTaskTimer(plugin, 0, 20L);
    }

    private void spawnRift(Player pl, Location castLocation) {

        pl.getWorld().playSound(pl.getLocation(), Sound.ENTITY_CAT_HISS, 0.5F, 0.5F);

        // create circle
        createCircle(pl, castLocation, RADIUS);

        // create smaller circles
        createCircle(pl, castLocation, (int) (RADIUS * 0.6));
        createCircle(pl, castLocation, (int) (RADIUS * 0.2));

        for (Entity en : pl.getWorld().getNearbyEntities(castLocation, RADIUS, RADIUS, RADIUS)) {
            if (!verifyEnemy(pl, en)) continue;
            LivingEntity victim = (LivingEntity) en;
            victim.teleport(castLocation);
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
            pl.getWorld().spawnParticle(Particle.SPELL_WITCH, loc, 1, 0, 0, 0, 0);
            pl.getWorld().spawnParticle(Particle.REDSTONE, loc, 1, 0, 0, 0, 0,
                    new Particle.DustOptions(Color.PURPLE, 1));
            loc.subtract(x, 0, z);
        }
    }

    @Override
    public double getDamagePerLevel() {
        return DAMAGE_PER_LEVEL;
    }
}
