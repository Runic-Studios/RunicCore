package com.runicrealms.plugin.spellapi.spells.archer;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.classes.ClassEnum;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.UUID;

@SuppressWarnings("FieldCanBeLocal")
public class Barrage extends Spell {

    private HashMap<Arrow, UUID> bArrows;
    private HashMap<UUID, UUID> hasBeenHit;
    private static final int DAMAGE = 25;

    private final int SUCCESSIVE_COOLDOWN = 1;

    public Barrage() {
        super("Barrage",
                "You launch a spread of five magical\n"
                        + "arrows that deal " + DAMAGE + " spellʔ damage!",
                ChatColor.WHITE, ClassEnum.ARCHER, 6, 15);
        this.bArrows = new HashMap<>();
        this.hasBeenHit = new HashMap<>();
    }

    // spell execute code
    @Override
    public void executeSpell(Player pl, SpellItemType type) {
        pl.getWorld().playSound(pl.getLocation(), Sound.ENTITY_ARROW_SHOOT, 0.5f, 1);
        pl.getWorld().playSound(pl.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 0.5f, 1);
        Vector middle = pl.getEyeLocation().getDirection().normalize().multiply(2);
        Vector left = rotateVectorAroundY(middle, -22.5);
        Vector leftMid = rotateVectorAroundY(middle, -11.25);
        Vector rightMid = rotateVectorAroundY(middle, 11.25);
        Vector right = rotateVectorAroundY(middle, 22.5);
        startTask(pl, new Vector[]{middle, left, leftMid, rightMid, right});
    }

    // vectors, particles
    private void startTask(Player player, Vector[] vectors) {
        for (Vector vector : vectors) {
            Arrow arrow = player.launchProjectile(Arrow.class);
            UUID uuid = player.getUniqueId();
            arrow.setVelocity(vector);
            arrow.setShooter(player);
            bArrows.put(arrow, uuid);
            new BukkitRunnable() {
                @Override
                public void run() {
                    Location arrowLoc = arrow.getLocation();
                    arrow.getWorld().spawnParticle(Particle.CRIT_MAGIC, arrowLoc, 5, 0, 0, 0, 0);
                    if (arrow.isDead() || arrow.isOnGround()) {
                        this.cancel();
                    }
                }
            }.runTaskTimer(RunicCore.getInstance(), 0, 1L);
        }
    }

    // deal bonus damage if arrow is a barrage arrow
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onArrowDamage(EntityDamageByEntityEvent e) {

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
        if (bArrows.containsKey(arrow)) {

            e.setCancelled(true);

            if (!(e.getEntity() instanceof LivingEntity)) return;
            Player pl = (Player) ((Arrow) e.getDamager()).getShooter();
            assert pl != null;
            UUID plID = pl.getUniqueId();
            LivingEntity le = (LivingEntity) e.getEntity();

            if (verifyEnemy(pl, le) && !hasBeenHit.containsKey(le.getUniqueId())) {

                DamageUtil.damageEntitySpell(DAMAGE, le, pl, false);
                e.getEntity().getWorld().spawnParticle(Particle.EXPLOSION_NORMAL, e.getEntity().getLocation(), 1, 0, 0, 0, 0);
                e.getEntity().getWorld().playSound(e.getEntity().getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 0.5f, 2.0f);

                hasBeenHit.put(le.getUniqueId(), plID);
                // remove concussive hit tracker
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        hasBeenHit.remove(le.getUniqueId());
                    }
                }.runTaskLater(RunicCore.getInstance(), (SUCCESSIVE_COOLDOWN * 20));
            }
        }
    }
}
