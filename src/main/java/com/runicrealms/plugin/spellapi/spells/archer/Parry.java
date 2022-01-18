package com.runicrealms.plugin.spellapi.spells.archer;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.classes.ClassEnum;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.spellapi.spelltypes.WeaponDamageSpell;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.*;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

@SuppressWarnings("FieldCanBeLocal")
public class Parry extends Spell implements WeaponDamageSpell {

    private static final int DAMAGE = 4;
    private static final int DAMAGE_PER_LEVEL = 1;
    private static final double LAUNCH_PATH_MULT = 2;
    private final List<Arrow> parryArrows;
    private final HashSet<UUID> hasBeenHit;

    // constructor
    public Parry() {
        super("Parry",
                "You launch yourself backwards in the air, " +
                        "shooting a flurry of five arrows in front " +
                        "of you, each dealing (" + DAMAGE + " + &f" + DAMAGE_PER_LEVEL +
                        "x&7 lvl) weapon⚔ damage!",
                ChatColor.WHITE, ClassEnum.ARCHER, 8, 15);
        parryArrows = new ArrayList<>();
        hasBeenHit = new HashSet<>();
    }

    @Override
    public void executeSpell(Player pl, SpellItemType type) {

        // spell variables, vectors
        Vector look = pl.getLocation().getDirection();
        Vector launchPath = new Vector(-look.getX(), 1.0, -look.getZ()).normalize();

        // particles, sounds
        pl.getWorld().playSound(pl.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 0.5f, 2.0f);
        pl.getWorld().spawnParticle(Particle.REDSTONE, pl.getLocation(),
                25, 0, 0.5f, 0.5f, 0.5f, new Particle.DustOptions(Color.WHITE, 20));

        Vector vec = pl.getEyeLocation().getDirection().normalize().multiply(2);
        Vector left = rotateVectorAroundY(vec, -22.5);
        Vector leftMid = rotateVectorAroundY(vec, -11.25);
        Vector rightMid = rotateVectorAroundY(vec, 11.25);
        Vector right = rotateVectorAroundY(vec, 22.5);

        pl.setVelocity(launchPath.multiply(LAUNCH_PATH_MULT));
        startTask(pl, new Vector[]{vec, left, leftMid, rightMid, right});

        // protect player from fall damage
        new BukkitRunnable() {
            @Override
            public void run() {
                if (pl.isOnGround())
                    this.cancel();
                else
                    pl.setFallDistance(-8.0F);
            }
        }.runTaskTimerAsynchronously(RunicCore.getInstance(), 0, 1L);
    }

    private void startTask(Player pl, Vector[] vectors) {
        for (Vector vector : vectors) {
            Arrow parryArrow = pl.launchProjectile(Arrow.class);
            parryArrow.setVelocity(vector);
            parryArrow.setShooter(pl);
            parryArrows.add(parryArrow);
            new BukkitRunnable() {
                @Override
                public void run() {
                    Location arrowLoc = parryArrow.getLocation();
                    parryArrow.getWorld().spawnParticle(Particle.CRIT, arrowLoc,
                            10, 0, 0, 0, 0);
                    if (parryArrow.isDead() || parryArrow.isOnGround()) {
                        this.cancel();
                        pl.getWorld().playSound(pl.getLocation(), Sound.BLOCK_LAVA_POP, 0.5f, 2f);
                        pl.getWorld().playSound(pl.getLocation(), Sound.ENTITY_DRAGON_FIREBALL_EXPLODE, 0.5f, 2f);
                        parryArrow.getWorld().spawnParticle(Particle.CRIT, arrowLoc, 10, 0, 0, 0, 0);
                    }
                }
            }.runTaskTimer(RunicCore.getInstance(), 0, 1L);
        }
    }

    @EventHandler
    public void onParryArrowHit(EntityDamageByEntityEvent e) {

        // only listen for arrows
        if (!(e.getDamager() instanceof Arrow)) return;

        // listen for player fired arrow
        Arrow arrow = (Arrow) e.getDamager();
        if (!(arrow.getShooter() instanceof Player)) return;

        // deal magic damage if arrow in in the barrage hashmap
        if (!parryArrows.contains(arrow)) return;

        e.setCancelled(true);

        if (!(e.getEntity() instanceof LivingEntity)) return;
        Player pl = (Player) ((Arrow) e.getDamager()).getShooter();
        assert pl != null;
        LivingEntity le = (LivingEntity) e.getEntity();

        if (verifyEnemy(pl, le) && !hasBeenHit.contains(le.getUniqueId())) {
            Bukkit.getScheduler().scheduleAsyncDelayedTask(RunicCore.getInstance(), () -> hasBeenHit.remove(le.getUniqueId()), 20L);
            DamageUtil.damageEntityWeapon(DAMAGE, le, pl, false, true, this);
        }
    }

    @Override
    public double getDamagePerLevel() {
        return DAMAGE_PER_LEVEL;
    }
}
