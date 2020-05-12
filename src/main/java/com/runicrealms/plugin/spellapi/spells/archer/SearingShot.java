package com.runicrealms.plugin.spellapi.spells.archer;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.classes.ClassEnum;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.*;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
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
public class SearingShot extends Spell {

    private boolean doExplosion;
    private boolean spreadArrows;
    private static final int DAMAGE = 50;
    private static final int RADIUS = 3;
    private List<Arrow> searingArrows;
    private HashSet<UUID> hasBeenHit;

    public SearingShot() {
        super("Searing Shot",
                "You launch an enchanted, flaming arrow" +
                "\nwhich deals " + DAMAGE + " spellʔ damage on-hit!",
                ChatColor.WHITE, ClassEnum.ARCHER, 8, 25);
        searingArrows = new ArrayList<>();
        hasBeenHit = new HashSet<>();
        doExplosion = false;
        spreadArrows = false;
    }

    public SearingShot(boolean doExplosion, boolean spreadArrows, int cooldown) {
        super("Searing Shot",
                "You launch an enchanted, flaming arrow" +
                        "\nwhich deals " + DAMAGE + " spellʔ damage on-hit!",
                ChatColor.WHITE, ClassEnum.ARCHER, cooldown, 25);
        searingArrows = new ArrayList<>();
        hasBeenHit = new HashSet<>();
        this.doExplosion = doExplosion;
        this.spreadArrows = spreadArrows;
    }

    @Override
    public void executeSpell(Player pl, SpellItemType type) {

        pl.getWorld().playSound(pl.getLocation(), Sound.ENTITY_ARROW_SHOOT, 0.5f, 1f);
        pl.getWorld().playSound(pl.getLocation(), Sound.ITEM_FIRECHARGE_USE, 0.5f, 2f);

        Vector vec = pl.getEyeLocation().getDirection().normalize().multiply(2);
        Vector left = rotateVectorAroundY(vec, -22.5);
        Vector leftMid = rotateVectorAroundY(vec, -11.25);
        Vector rightMid = rotateVectorAroundY(vec, 11.25);
        Vector right = rotateVectorAroundY(vec, 22.5);

        if (spreadArrows) {
            startTask(pl, new Vector[]{vec, left, leftMid, rightMid, right});
        } else {
            startTask(pl, new Vector[]{vec});
        }
    }

    private void startTask(Player pl, Vector[] vectors) {
        for (Vector vector : vectors) {
            Arrow searing = pl.launchProjectile(Arrow.class);
            searing.setVelocity(vector);
            searing.setShooter(pl);
            searingArrows.add(searing);
            new BukkitRunnable() {
                @Override
                public void run() {
                    Location arrowLoc = searing.getLocation();
                    searing.getWorld().spawnParticle(Particle.FLAME, arrowLoc,
                            10, 0, 0, 0, 0);
                    if (searing.isDead() || searing.isOnGround()) {
                        this.cancel();
                        pl.getWorld().playSound(pl.getLocation(), Sound.BLOCK_LAVA_POP, 0.5f, 2f);
                        pl.getWorld().playSound(pl.getLocation(), Sound.ENTITY_DRAGON_FIREBALL_EXPLODE, 0.5f, 2f);
                        searing.getWorld().spawnParticle(Particle.LAVA, arrowLoc, 10, 0, 0, 0, 0);
                    }
                }
            }.runTaskTimer(RunicCore.getInstance(), 0, 1L);
        }
    }


    @EventHandler
    public void onSearingArrowHit(EntityDamageByEntityEvent e) {

        // only listen for arrows
        if (!(e.getDamager() instanceof Arrow)) {
            return;
        }

        // listen for player fired arrow
        Arrow arrow = (Arrow) e.getDamager();
        if (!(arrow.getShooter() instanceof Player)) {
            return;
        }

        // deal magic damage if arrow in in the barrage hashmap
        if (!searingArrows.contains(arrow)) return;

        e.setCancelled(true);

        if (!(e.getEntity() instanceof LivingEntity)) return;
        Player pl = (Player) ((Arrow) e.getDamager()).getShooter();
        assert pl != null;
        LivingEntity le = (LivingEntity) e.getEntity();

        if (verifyEnemy(pl, le) && !hasBeenHit.contains(le.getUniqueId())) {
            Bukkit.getScheduler().scheduleAsyncDelayedTask(RunicCore.getInstance(), () -> hasBeenHit.remove(le.getUniqueId()), 20L);
            DamageUtil.damageEntitySpell(DAMAGE, le, pl, 100);

            if (doExplosion) {

                // hit them baddies
                for (Entity en : arrow.getWorld().getNearbyEntities(arrow.getLocation(), RADIUS, RADIUS, RADIUS)) {
                    if (en.equals(le)) continue; // skip original target
                    if (verifyEnemy(pl, en) && !hasBeenHit.contains(en.getUniqueId())) {
                        hasBeenHit.add(en.getUniqueId()); // prevent multiple explosions on single target
                        Bukkit.getScheduler().scheduleAsyncDelayedTask(RunicCore.getInstance(), () -> hasBeenHit.remove(en.getUniqueId()), 20L);
                        en.getWorld().playSound(en.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 0.5f, 1.0f);
                        en.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, en.getLocation(), 10, 0, 0, 0, 0);
                        DamageUtil.damageEntitySpell(DAMAGE, ((LivingEntity) en), pl, 100);
                    }
                }
            }
        }
    }

    public static int getRadius() {
        return RADIUS;
    }
}
