package com.runicrealms.plugin.listeners;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.attributes.AttributeUtil;
import com.runicrealms.plugin.player.outlaw.OutlawManager;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.*;
import org.bukkit.entity.ArmorStand;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class StaffListener implements Listener {

    // globals
    private static double BEAM_WIDTH = 2.0;
    private static int RADIUS = 5;
    private static double RANGE = 6.0;
    private static final int SPEED_MULT = 3;
    private HashMap<UUID, List<UUID>> hasBeenHit = new HashMap<>();

    // creates the mage ranged auto-attack
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

        Player pl = e.getPlayer();

        // only listen for left clicks
        if (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) return;

        // only apply cooldown if its not already active
        if (cooldown != 0) return;

         // cancel the event, run custom mechanics
         e.setCancelled(true);

        // check for mage
        String className = RunicCore.getCacheManager().getPlayerCache(pl.getUniqueId()).getClassName();
        if (className == null) return;
        if (!className.equals("Mage")) {
            return;
        }

        staffAttack(artifact, pl);
        // set the cooldown
        pl.setCooldown(artifact.getType(), 20);
    }

    private void staffAttack(ItemStack artifact, Player pl) {

        // retrieve the weapon damage, cooldown
        int minDamage = (int) AttributeUtil.getCustomDouble(artifact, "custom.minDamage");
        int maxDamage = (int) AttributeUtil.getCustomDouble(artifact, "custom.maxDamage");
        int reqLv = (int) AttributeUtil.getCustomDouble(artifact, "required.level");

        if (reqLv > RunicCore.getCacheManager().getPlayerCache(pl.getUniqueId()).getClassLevel()) {
            pl.playSound(pl.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 0.5f, 1.0f);
            pl.sendMessage(ChatColor.RED + "Your level is too low to wield this!");
            return;
        }

        // create our vector to be used later
        Vector vector = pl.getEyeLocation().getDirection().normalize().multiply(SPEED_MULT);

        // play sound effect
        pl.getWorld().playSound(pl.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 0.5f, 1);

        // particle effect
        new BukkitRunnable() {

            Location location = pl.getEyeLocation();
            Location startLoc = pl.getLocation();

            @Override
            public void run() {

                // vector, particles
                location.add(vector);
                location.getWorld().spawnParticle(Particle.CRIT_MAGIC, location, 25, 0.1f, 0.1f, 0.1f, 0);
                location.getWorld().spawnParticle(Particle.REDSTONE, location,
                        5, 0.1f, 0.1f, 0.1f, new Particle.DustOptions(Color.WHITE, 1));

                // range before spell dies out naturally
                if (location.getBlock().getType().isSolid() || location.distance(startLoc) >= RANGE) {
                    this.cancel();
                }

                // get nearby entities
                for (Entity e : location.getWorld().getNearbyEntities(location, RADIUS, RADIUS, RADIUS)) {
                    if (e.getLocation().distance(location) <= BEAM_WIDTH) {
                        if (e != (pl)) {
                            if (e.getType().isAlive()) {

                                // bugfix for armor stands
                                if (e instanceof ArmorStand && e.getVehicle() != null) {
                                    e = e.getVehicle();
                                }

                                if (e instanceof ArmorStand && e.isInvulnerable()) continue;

                                LivingEntity victim = (LivingEntity) e;

                                // outlaw check
                                if (victim instanceof Player && (!OutlawManager.isOutlaw(((Player) victim)) || !OutlawManager.isOutlaw(pl))) {
                                    continue;
                                }

                                // skip party members
                                if (RunicCore.getPartyManager().getPlayerParty(pl) != null
                                        && RunicCore.getPartyManager().getPlayerParty(pl).hasMember(e.getUniqueId())) {
                                    continue;
                                }

                                // skip NPCs
                                if (victim.hasMetadata("NPC")) continue;

                                // prevent player from being hit by the same attack
                                if (hasBeenHit.containsKey(victim.getUniqueId())) {
                                    List<UUID> uuids = hasBeenHit.get(victim.getUniqueId());
                                    if (uuids.contains(pl.getUniqueId())) {
                                        break;
                                    } else {
                                        uuids.add(pl.getUniqueId());
                                        hasBeenHit.put(victim.getUniqueId(), uuids);
                                    }
                                } else {
                                    List<UUID> uuids = new ArrayList<>();
                                    uuids.add(pl.getUniqueId());
                                    hasBeenHit.put(victim.getUniqueId(), uuids);
                                }

                                // can't be hit by the same player's beam for 1 tick
                                new BukkitRunnable() {
                                    @Override
                                    public void run() {
                                        List<UUID> uuids = hasBeenHit.get(victim.getUniqueId());
                                        uuids.remove(pl.getUniqueId());
                                        hasBeenHit.put(victim.getUniqueId(), uuids);
                                    }
                                }.runTaskLater(RunicCore.getInstance(), 1L);

                                // apply attack effects, random damage amount
                                if (maxDamage != 0) {
                                    int randomNum = ThreadLocalRandom.current().nextInt(minDamage, maxDamage + 1);
                                    DamageUtil.damageEntityWeapon(randomNum, victim, pl, true, false);
                                } else {
                                    DamageUtil.damageEntityWeapon(maxDamage, victim, pl, true, false);
                                }

                                pl.playSound(pl.getLocation(), Sound.ENTITY_PLAYER_HURT, 0.5f, 1);
                                this.cancel();
                            }
                        }
                    }
                }
            }
        }.runTaskTimer(RunicCore.getInstance(), 0L, 1L);
    }
}
