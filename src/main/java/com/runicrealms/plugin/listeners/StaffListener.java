package com.runicrealms.plugin.listeners;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.api.RunicCoreAPI;
import com.runicrealms.plugin.events.EnemyVerifyEvent;
import com.runicrealms.plugin.utilities.DamageUtil;
import com.runicrealms.runicitems.RunicItemsAPI;
import com.runicrealms.runicitems.item.RunicItemWeapon;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Handles the mage basic attack
 *
 * @author Skyfallin
 */
public class StaffListener implements Listener {

    private static final int STAFF_COOLDOWN = 15; // ticks (0.75s)
    private static final int MAX_DIST = 7; // was 8
    private static final int DURATION = 6;
    private static final int BEAM_SPEED = 3;
    private static final int SUCCESSIVE_COOLDOWN = 2; // seconds
    private static final double BEAM_HITBOX_SIZE = 1.5;
    private final HashMap<UUID, List<UUID>> hasBeenHit = new HashMap<>();
    private final HashMap<UUID, HashSet<UUID>> affectedPlayers = new HashMap<>();

    @EventHandler
    public void onStaffAttack(PlayerInteractEvent e) {

        // check for null
        if (e.getItem() == null) return;

        // retrieve the weapon type
        ItemStack artifact = e.getItem();
        Material artifactType = artifact.getType();
        double cooldown = e.getPlayer().getCooldown(artifact.getType());

        // only listen for items that can be artifact weapons
        if (artifactType == Material.AIR) return;
        if (e.getHand() != EquipmentSlot.HAND) return;

        // IGNORE NON-STAFF ITEMS
        if (artifactType != Material.WOODEN_HOE) return;

        Player player = e.getPlayer();

        // only listen for left clicks
        if (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) return;

        // only apply cooldown if it's not already active
        if (cooldown != 0) return;

        // cancel the event, run custom mechanics
        e.setCancelled(true);

        // check for mage
        String className = RunicCoreAPI.getPlayerClass(player);
        if (className == null) return;
        if (!className.equals("Mage")) return;
        if (RunicCoreAPI.isCasting(player)) return;

        staffAttack(player, artifact);
        // set the cooldown
        player.setCooldown(artifact.getType(), STAFF_COOLDOWN);
    }

    /**
     * Function to begin staff attack
     *
     * @param player    who initiated attack
     * @param itemStack to be passed down to damage function
     */
    private void staffAttack(Player player, ItemStack itemStack) {
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_GHAST_SHOOT, 0.4f, 2.0F);
        Vector vector = player.getEyeLocation().getDirection().normalize().multiply(BEAM_SPEED);
        startTask(player, vector, itemStack);
    }

    /**
     * @param player
     * @param vector
     * @param itemStack
     */
    private void startTask(Player player, Vector vector, ItemStack itemStack) {
        new BukkitRunnable() {
            final Location location = player.getEyeLocation();
            final Location startLoc = player.getLocation();

            @Override
            public void run() {
                location.add(vector);
                if (location.getBlock().getType().isSolid() || location.distance(startLoc) >= MAX_DIST) {
                    this.cancel();
                    player.getWorld().spawnParticle(Particle.CRIT_MAGIC, location, 15, 0.5f, 0.5f, 0.5f, 0);
                }
                player.getWorld().spawnParticle(Particle.REDSTONE, location, 10, 0, 0, 0, 0, new Particle.DustOptions(Color.AQUA, 1));
                player.getWorld().spawnParticle(Particle.CRIT_MAGIC, location, 10, 0.1f, 0.1f, 0.1f, 0);
                checkForEnemies(player, location, itemStack);
            }
        }.runTaskTimer(RunicCore.getInstance(), 0L, 1L);
    }

    /**
     * @param caster
     * @param location
     */
    private void checkForEnemies(Player caster, Location location, ItemStack itemStack) {
        HashSet<UUID> enemies = new HashSet<>();
        affectedPlayers.put(caster.getUniqueId(), enemies);
        for (Entity entity : caster.getWorld().getNearbyEntities(location, BEAM_HITBOX_SIZE, BEAM_HITBOX_SIZE, BEAM_HITBOX_SIZE)) {
            EnemyVerifyEvent e = new EnemyVerifyEvent(caster, entity);
            Bukkit.getServer().getPluginManager().callEvent(e);
            if (e.isCancelled()) continue;
            LivingEntity livingEntity = (LivingEntity) entity;
            // a bunch of fancy checks to make sure one entity can't be spam damaged by the same effect
            // multiple times
            if (hasBeenHit.containsKey(livingEntity.getUniqueId())) {
                List<UUID> uuids = hasBeenHit.get(livingEntity.getUniqueId());
                if (uuids.contains(caster.getUniqueId())) {
                    break;
                } else {
                    uuids.add(caster.getUniqueId());
                    hasBeenHit.put(livingEntity.getUniqueId(), uuids);
                }
            } else {
                List<UUID> uuids = new ArrayList<>();
                uuids.add(caster.getUniqueId());
                hasBeenHit.put(livingEntity.getUniqueId(), uuids);
            }

            // can't be hit by the same player's beam for SUCCESSIVE_COOLDOWN secs
            new BukkitRunnable() {
                @Override
                public void run() {
                    List<UUID> uuids = hasBeenHit.get(livingEntity.getUniqueId());
                    uuids.remove(caster.getUniqueId());
                    hasBeenHit.put(livingEntity.getUniqueId(), uuids);
                }
            }.runTaskLater(RunicCore.getInstance(), (SUCCESSIVE_COOLDOWN * 20));


            damageStaff(caster, livingEntity, itemStack);
            caster.playSound(caster.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1);
            affectedPlayers.get(caster.getUniqueId()).add(entity.getUniqueId());
            Bukkit.getScheduler().runTaskLaterAsynchronously(RunicCore.getInstance(), () -> affectedPlayers.remove(caster.getUniqueId()), DURATION * 20L);
            break; // stop the beam if it hits an enemy
        }
    }

    /**
     * Create a WeaponDamageEvent for our staff attack, verify that the player can use the weapon
     *
     * @param player    who summoned staff attack
     * @param victim    to be damaged
     * @param itemStack held item to read damage values from
     */
    private void damageStaff(Player player, LivingEntity victim, ItemStack itemStack) {

        int minDamage;
        int maxDamage;
        int reqLv;

        try {
            RunicItemWeapon runicItemWeapon = (RunicItemWeapon) RunicItemsAPI.getRunicItemFromItemStack(itemStack);
            minDamage = runicItemWeapon.getWeaponDamage().getMin();
            maxDamage = runicItemWeapon.getWeaponDamage().getMax();
            reqLv = runicItemWeapon.getLevel();
        } catch (Exception ex) {
            minDamage = 1;
            maxDamage = 1;
            reqLv = 1;
        }

        if (reqLv > player.getLevel()) {
            player.playSound(player.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 0.5f, 1.0f);
            player.sendMessage(ChatColor.RED + "Your level is too low to wield this!");
            return;
        }

        // apply attack effects, random damage amount
        if (maxDamage != 0) {
            int randomNum = ThreadLocalRandom.current().nextInt(minDamage, maxDamage + 1);
            DamageUtil.damageEntityWeapon(randomNum, victim, player, true, true);
        } else {
            DamageUtil.damageEntityWeapon(maxDamage, victim, player, true, true);
        }

        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_HURT, 0.5f, 1);
    }
}
