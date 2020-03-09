package com.runicrealms.plugin.spellapi.spells.archer;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
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
public class ArrowBomb extends Spell {

    // globals
    private HashMap<Arrow, UUID> bombArrow;
    private static final int DAMAGE = 15;
    private static final int RADIUS = 4;

    // constructor
    public ArrowBomb() {
        super("Arrow Bomb",
                "You launch an enchanted arrow," +
                        "\ndealing " + DAMAGE + " spellʔ damage to all" +
                        "\nenemies within " + RADIUS + " blocks!",
                ChatColor.WHITE, 8, 25);
        this.bombArrow = new HashMap<>();
    }

    // spell execute code
    @Override
    public void executeSpell(Player pl, SpellItemType type) {
        pl.getWorld().playSound(pl.getLocation(), Sound.ENTITY_ARROW_SHOOT, 0.5f, 1);
        pl.getWorld().playSound(pl.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 0.5f, 1);
        Vector path = pl.getEyeLocation().getDirection().normalize().multiply(1.5);
        startTask(pl, path);
    }

    // vectors, particles
    private void startTask(Player player, Vector vector) {
        Arrow arrow = player.launchProjectile(Arrow.class);
        UUID uuid = player.getUniqueId();
        arrow.setVelocity(vector);
        arrow.setShooter(player);
        bombArrow.put(arrow, uuid);
        new BukkitRunnable() {
            @Override
            public void run() {
                Location arrowLoc = arrow.getLocation();
                arrowLoc.getWorld().spawnParticle(Particle.CRIT, arrowLoc,
                        10, 0, 0, 0, 0);
                if (arrow.isDead() || arrow.isOnGround()) {

                    this.cancel();
                    arrow.getWorld().playSound(arrow.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 0.5f, 1.0f);
                    arrowLoc.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, arrowLoc, 10, 0, 0, 0, 0);

                    // hit them baddies
                    for (Entity e : arrow.getWorld().getNearbyEntities(arrow.getLocation(), RADIUS, RADIUS, RADIUS)) {
                        tryToDamage(arrow, e);
                    }
                }
            }
        }.runTaskTimer(RunicCore.getInstance(), 0, 1L);
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

        // cancel the arrow damage event
        if (bombArrow.containsKey(arrow)) {
            e.setCancelled(true);
        }
    }

    private void tryToDamage(Arrow arrow, Entity victim) {

        if (!(victim instanceof LivingEntity)) return;
        Player pl = (Player) arrow.getShooter();
        assert pl != null;
        LivingEntity le = (LivingEntity) victim;

        if (verifyEnemy(pl, le)){
            DamageUtil.damageEntitySpell(DAMAGE, le, pl, false);
        }
    }
}
