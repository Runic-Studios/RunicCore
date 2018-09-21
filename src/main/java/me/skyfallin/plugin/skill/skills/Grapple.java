package me.skyfallin.plugin.skill.skills;

import me.skyfallin.plugin.skill.skilltypes.Skill;
import me.skyfallin.plugin.skill.skilltypes.SkillItemType;
import org.bukkit.*;
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

public class Grapple extends Skill {

    private HashMap<Arrow, UUID> hooks = new HashMap<>();
    private HashMap<UUID, Long> safefall = new HashMap<>();

    public Grapple() {
        super("Grapple", "fire a grappling hook", ChatColor.WHITE, ClickType.LEFT_CLICK_ONLY, 1);
    }

    @Override
    public void onLeftClick(Player player, SkillItemType type) {
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ARROW_SHOOT, 0.5f, 1);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_FIREWORK_LAUNCH, 0.5f, 1);
        startTask(player);
    }

    private void startTask(Player player) {
        Vector direction = player.getEyeLocation().getDirection().normalize().multiply(2);
        Arrow arrow = player.launchProjectile(Arrow.class);
        UUID uuid = player.getUniqueId();
        arrow.setVelocity(direction);
        arrow.setShooter(player);
        hooks.put(arrow, uuid);
        Location startLoc = arrow.getLocation();
        new BukkitRunnable() {
            @Override
            public void run() {
                Location arrowLoc = arrow.getLocation();
                player.getWorld().spawnParticle(Particle.CLOUD, arrowLoc, 5, 0, 0, 0, 0);
                if (arrow.isDead() && !arrow.isOnGround()) {
                    this.cancel();
                    player.sendMessage(ChatColor.RED + "Your line was blocked!");
                    player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5F, 1);
                }
                if (arrow.isOnGround()) {
                    this.cancel();
                    if (arrowLoc.distance(startLoc) > 30.0D) {
                        player.sendMessage(ChatColor.RED + "Your hook flew too far!");
                        player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5F, 1);
                    } else {
                        safefall.put(uuid, System.currentTimeMillis());
                        Location playerLoc = player.getLocation();
                        double x = (arrow.getLocation().getX() - playerLoc.getX());
                        double y = (arrow.getLocation().getY() - playerLoc.getY()) / 1.2;
                        double z = (arrow.getLocation().getZ() - playerLoc.getZ());
                        Vector velocity = new Vector(x, y, z).normalize().multiply(3.0);
                        player.setVelocity(velocity);
                        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_MAGMACUBE_JUMP, 0.5F, 2.0F);
                        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_TRIPWIRE_DETACH, 0.5F, 2.0F);
                        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ZOMBIE_BREAK_DOOR_WOOD, 0.5F, 2.0F);
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                safefall.remove(uuid);
                                player.sendMessage(ChatColor.GRAY + "You lost safefall!");
                            }
                        }.runTaskLater(plugin, 60L);
                    }
                }
            }
        }.runTaskTimer(plugin, 0, 1L);
    }

    @EventHandler
    public void onFallDamage(EntityDamageEvent e) {
        if (e.getEntity() instanceof Player && e.getCause().equals(EntityDamageEvent.DamageCause.FALL)) {
            UUID uuid = e.getEntity().getUniqueId();
            if (safefall.containsKey(uuid)) {
                e.setCancelled(true);
            } else if (!safefall.containsKey(uuid)) {
                // Do nothing
            }

        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onArrowDamage(EntityDamageByEntityEvent e) {
        if (e.getDamager() instanceof Arrow) {
            Arrow arrow = (Arrow) e.getDamager();
            if (hooks.containsKey(arrow)) {
                e.setCancelled(true);
            }
        }
    }
}
