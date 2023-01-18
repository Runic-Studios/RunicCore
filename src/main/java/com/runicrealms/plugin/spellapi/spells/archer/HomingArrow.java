package com.runicrealms.plugin.spellapi.spells.archer;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.api.event.RunicBowEvent;
import com.runicrealms.plugin.classes.CharacterClass;
import com.runicrealms.plugin.events.SpellCastEvent;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import org.bukkit.*;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Set;

public class HomingArrow extends Spell {

    private static final int DURATION = 10; // seconds
    private static final int MAX_DIST = 25;
    private static final double RAY_SIZE = 3.5D;
    private final Set<ProjectileSource> homingPlayers;

    public HomingArrow() {
        super("Homing Arrow",
                "Your first basic attack after casting &aGrapple &7or &aDecoy " +
                        "&7becomes a homing arrow, tracking the first enemy " +
                        "within " + RAY_SIZE + " blocks of your scope and following " +
                        "them until it finds its target, or is blocked.",
                ChatColor.WHITE, CharacterClass.ARCHER, 0, 0);
        this.setIsPassive(true);
        homingPlayers = new HashSet<>();
    }

    Vector generateNewVelocity(AbstractArrow arrow, LivingEntity target) {
        return target.getEyeLocation().subtract(arrow.getLocation()).toVector().normalize().multiply(arrow.getVelocity().length());
    }

    /**
     * Enhances the given arrow to make it track a target (determined by the race trace result)
     *
     * @param player who fired the arrow
     * @param arrow  from the bow event (basic attack)
     */
    private void makeHomingArrow(Player player, Arrow arrow) {
        RayTraceResult rayTraceResult = player.getWorld().rayTraceEntities
                (
                        player.getLocation(),
                        player.getLocation().getDirection(),
                        MAX_DIST,
                        RAY_SIZE,
                        entity -> isValidEnemy(player, entity)
                );
        if (rayTraceResult == null) {
            player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5f, 1.0f);
            player.sendMessage(ChatColor.RED + "A valid target could not be found!");
        } else if (rayTraceResult.getHitEntity() != null) {
            LivingEntity livingEntity = (LivingEntity) rayTraceResult.getHitEntity();
            // Update the arrow vector
            new BukkitRunnable() {
                @Override
                public void run() {
                    Location arrowLoc = arrow.getLocation();
                    player.getWorld().spawnParticle(Particle.FLAME, arrowLoc, 5, 0, 0, 0, 0);
                    arrow.setVelocity(generateNewVelocity(arrow, livingEntity));
                    if (arrow.isDead() || arrow.isOnGround()) {
                        this.cancel();
                    }
                }
            }.runTaskTimer(RunicCore.getInstance(), 0, 1L);
        }

        Bukkit.getScheduler().runTaskLaterAsynchronously(RunicCore.getInstance(), arrow::remove, DURATION * 20L);
    }

    @EventHandler(priority = EventPriority.NORMAL) // early
    public void onHomingArrow(RunicBowEvent event) {
        if (!homingPlayers.contains(event.getPlayer())) return;
        if (event.isCancelled()) return;
        Player player = event.getPlayer();
        assert player != null;
        if (!hasPassive(player.getUniqueId(), this.getName())) return;
        event.setCancelled(true);
        homingPlayers.remove(player);
        makeHomingArrow(player, event.getArrow());
    }

    @EventHandler
    public void onSpellCast(SpellCastEvent event) {
        if (!hasPassive(event.getCaster().getUniqueId(), this.getName())) return;
        if (!(event.getSpell() instanceof Grapple || event.getSpell() instanceof Decoy)) return;
        homingPlayers.add(event.getCaster());
    }

}

