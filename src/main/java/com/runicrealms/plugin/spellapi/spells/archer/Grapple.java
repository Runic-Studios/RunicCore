package com.runicrealms.plugin.spellapi.spells.archer;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.classes.SubClassEnum;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.UUID;

public class Grapple extends Spell<SubClassEnum> {

    // globals
    private final HashMap<Arrow, UUID> hooks = new HashMap<>();
    private final HashMap<UUID, Long> safefall = new HashMap<>();
    private static final double HOOK_LENGTH = 30.0;

    // constructor
    public Grapple() {
        super("Grapple",
                "You fire a grappling hook which pulls" +
                        "\nyou to your target location, up to a max" +
                        "\nof " + (int) HOOK_LENGTH + " blocks!",
                ChatColor.WHITE, SubClassEnum.SCOUT, 15, 25);
    }

    // spell execute code
    @Override
    public void executeSpell(Player pl, SpellItemType type) {
        pl.getWorld().playSound(pl.getLocation(), Sound.ENTITY_ARROW_SHOOT, 0.5f, 1);
        pl.getWorld().playSound(pl.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 0.5f, 1);
        startTask(pl);
    }

    // handles the grappling hook
    private void startTask(Player pl) {
        Vector direction = pl.getEyeLocation().getDirection().normalize().multiply(2);
        Arrow arrow = pl.launchProjectile(Arrow.class);
        UUID uuid = pl.getUniqueId();
        arrow.setVelocity(direction);
        arrow.setShooter(pl);
        hooks.put(arrow, uuid);
        Location startLoc = arrow.getLocation();
        new BukkitRunnable() {
            @Override
            public void run() {
                Location arrowLoc = arrow.getLocation();
                pl.getWorld().spawnParticle(Particle.CLOUD, arrowLoc, 5, 0, 0, 0, 0);
                if (arrow.isDead() && !arrow.isOnGround()) {
                    this.cancel();
                    pl.sendMessage(ChatColor.RED + "Your line was blocked!");
                    pl.playSound(pl.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5F, 1);
                }
                if (arrow.isOnGround()) {
                    this.cancel();
                    if (arrowLoc.distance(startLoc) > HOOK_LENGTH) {
                        pl.sendMessage(ChatColor.RED + "Your hook flew too far!");
                        pl.playSound(pl.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5F, 1);
                    } else {
                        safefall.put(uuid, System.currentTimeMillis());
                        pl.playSound(pl.getLocation(), Sound.ENTITY_ZOMBIE_BREAK_WOODEN_DOOR, 0.5F, 1);
                        pl.teleport(pl.getLocation().add(0.0D, 0.5D, 0.0D));
                        final Vector v = getVectorForPoints(pl.getLocation(), arrowLoc);
                        pl.setVelocity(v);
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                safefall.remove(uuid);
                                pl.sendMessage(ChatColor.GRAY + "You lost safefall!");
                            }
                        }.runTaskLater(RunicCore.getInstance(), 40);
                    }
                }
            }
        }.runTaskTimer(RunicCore.getInstance(), 0, 1L);
    }

    // handles the vector
    private Vector getVectorForPoints(Location l1, Location l2) {
        double g = -0.08D;
        double d = l2.distance(l1);
        double vX = (1.0D + 0.07D * d) * (l2.getX() - l1.getX()) / d;
        double vY = (1.0D + 0.03D * d) * (l2.getY() - l1.getY()) / d - 0.5D * g * d;
        double vZ = (1.0D + 0.07D * d) * (l2.getZ() - l1.getZ()) / d;
        return new Vector(vX, vY, vZ);
    }

    // prevent fall damage if the user if grappling
    @EventHandler
    public void onFallDamage(EntityDamageEvent e) {
        if (e.getEntity() instanceof Player && e.getCause().equals(EntityDamageEvent.DamageCause.FALL)) {
            UUID uuid = e.getEntity().getUniqueId();
            if (this.safefall.containsKey(uuid)) {
                e.setCancelled(true);
            }
        }
    }

    // prevent the grappling hook from damaging players
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onArrowDamage(EntityDamageByEntityEvent e) {
        if (e.getDamager() instanceof Arrow) {
            Arrow arrow = (Arrow) e.getDamager();
            if (this.hooks.containsKey(arrow)) {
                e.setCancelled(true);
            }
        }
    }
}

