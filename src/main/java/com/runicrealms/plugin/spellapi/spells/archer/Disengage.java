package com.runicrealms.plugin.spellapi.spells.archer;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.classes.CharacterClass;
import com.runicrealms.plugin.events.GenericDamageEvent;
import com.runicrealms.plugin.spellapi.spelltypes.PhysicalDamageSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.*;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

public class Disengage extends Spell implements PhysicalDamageSpell {

    private static final int DAMAGE = 4;
    private static final int DAMAGE_PER_LEVEL = 1;
    private static final double LAUNCH_MULTIPLIER = 2;
    private final HashSet<Entity> casters;
    private final List<Arrow> parryArrows;
    private final HashSet<UUID> hasBeenHit;

    public Disengage() {
        super("Disengage",
                "You launch yourself backwards in the air, " +
                        "shooting a flurry of five arrows in front " +
                        "of you, each dealing (" + DAMAGE + " + &f" + DAMAGE_PER_LEVEL +
                        "x&7 lvl) physical⚔ damage!",
                ChatColor.WHITE, CharacterClass.ARCHER, 8, 15);
        casters = new HashSet<>();
        parryArrows = new ArrayList<>();
        hasBeenHit = new HashSet<>();
    }

    @Override
    public void executeSpell(Player player, SpellItemType type) {

        Vector look = player.getLocation().getDirection();
        Vector launchPath = new Vector(-look.getX(), 1.0, -look.getZ()).normalize();

        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 0.5f, 2.0f);
        player.getWorld().spawnParticle(Particle.REDSTONE, player.getLocation(),
                25, 0, 0.5f, 0.5f, 0.5f, new Particle.DustOptions(Color.WHITE, 20));

        Vector vec = player.getEyeLocation().getDirection().normalize().multiply(2);
        Vector left = rotateVectorAroundY(vec, -22.5);
        Vector leftMid = rotateVectorAroundY(vec, -11.25);
        Vector rightMid = rotateVectorAroundY(vec, 11.25);
        Vector right = rotateVectorAroundY(vec, 22.5);

        player.setVelocity(launchPath.multiply(LAUNCH_MULTIPLIER));
        startTask(player, new Vector[]{vec, left, leftMid, rightMid, right});
    }

    @Override
    public double getDamagePerLevel() {
        return DAMAGE_PER_LEVEL;
    }

    /**
     * Disable fall damage for players who are using Disengage
     */
    @EventHandler(priority = EventPriority.LOW)
    public void onFallDamage(GenericDamageEvent event) {
        if (!casters.contains(event.getVictim())) return;
        if (event.getCause() == GenericDamageEvent.DamageCauses.FALL_DAMAGE) {
            event.setCancelled(true);
            casters.remove(event.getVictim());
        }
    }

    @EventHandler
    public void onParryArrowHit(EntityDamageByEntityEvent event) {

        // only listen for arrows
        if (!(event.getDamager() instanceof Arrow)) return;

        // listen for player fired arrow
        Arrow arrow = (Arrow) event.getDamager();
        if (!(arrow.getShooter() instanceof Player)) return;

        // deal magic damage if arrow in in the barrage hashmap
        if (!parryArrows.contains(arrow)) return;

        event.setCancelled(true);

        if (!(event.getEntity() instanceof LivingEntity)) return;
        Player player = (Player) ((Arrow) event.getDamager()).getShooter();
        assert player != null;
        LivingEntity le = (LivingEntity) event.getEntity();

        if (isValidEnemy(player, le) && !hasBeenHit.contains(le.getUniqueId())) {
            Bukkit.getScheduler().scheduleAsyncDelayedTask(RunicCore.getInstance(),
                    () -> hasBeenHit.remove(le.getUniqueId()), 20L);
            DamageUtil.damageEntityPhysical(DAMAGE, le, player, false, true, this);
        }
    }

    private void startTask(Player player, Vector[] vectors) {
        for (Vector vector : vectors) {
            Arrow parryArrow = player.launchProjectile(Arrow.class);
            parryArrow.setVelocity(vector);
            parryArrow.setShooter(player);
            parryArrows.add(parryArrow);
            new BukkitRunnable() {
                @Override
                public void run() {
                    Location arrowLoc = parryArrow.getLocation();
                    parryArrow.getWorld().spawnParticle(Particle.CRIT, arrowLoc,
                            10, 0, 0, 0, 0);
                    if (parryArrow.isDead() || parryArrow.isOnGround()) {
                        this.cancel();
                        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_LAVA_POP, 0.5f, 2f);
                        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_DRAGON_FIREBALL_EXPLODE, 0.5f, 2f);
                        parryArrow.getWorld().spawnParticle(Particle.CRIT, arrowLoc, 10, 0, 0, 0, 0);
                    }
                }
            }.runTaskTimer(RunicCore.getInstance(), 0, 1L);
        }
    }
}
