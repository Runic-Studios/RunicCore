package us.fortherealm.plugin.listeners;

import org.bukkit.*;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import us.fortherealm.plugin.FTRCore;
import us.fortherealm.plugin.attributes.AttributeUtil;
import us.fortherealm.plugin.item.GearScanner;
import us.fortherealm.plugin.utilities.DamageUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class StaffListener implements Listener {

    // globals
    private static double BEAM_WIDTH = 2.0;
    private static int RADIUS = 5;
    private static double RANGE = 8.0;
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
        if (artifactType == null) return;

        // IGNORE NON-STAFF ITEMS
        if (artifactType != Material.WOODEN_HOE) return;

        Player pl = e.getPlayer();

        // only listen for left clicks
        if (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) return;

        // only apply cooldown if its not already active
        if (cooldown != 0) return;

         // cancel the event, run custom mechanics
         e.setCancelled(true);

        // if they're sneaking, they're casting a spell. so we don't fire the attack.
        if (pl.isSneaking()) return;

        staffAttack(artifact, pl);
    }

    private void staffAttack(ItemStack artifact, Player pl) {

        // grab player's armor, offhand (check for gem bonuses)
        ArrayList<ItemStack> armorAndOffhand = GearScanner.armorAndOffHand(pl);

        // calculate the player's total damage boost
        int damageBoost = 0;
        for (ItemStack item : armorAndOffhand) {
            damageBoost += (int) AttributeUtil.getCustomDouble(item, "custom.attackDamage");
        }

        // retrieve the weapon damage, cooldown
        int minDamage = (int) AttributeUtil.getCustomDouble(artifact, "custom.minDamage");
        int maxDamage = (int) AttributeUtil.getCustomDouble(artifact, "custom.maxDamage") + damageBoost;

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

                // range before skill dies out naturally
                if (location.getBlock().getType().isSolid() || location.distance(startLoc) >= RANGE) {
                    this.cancel();
                }

                // get nearby entities
                for (Entity e : location.getWorld().getNearbyEntities(location, RADIUS, RADIUS, RADIUS)) {
                    if (e.getLocation().distance(location) <= BEAM_WIDTH) {
                        if (e != (pl)) {
                            if (e.getType().isAlive()) {

                                LivingEntity victim = (LivingEntity) e;

                                // skip party members
                                if (FTRCore.getPartyManager().getPlayerParty(pl) != null
                                        && FTRCore.getPartyManager().getPlayerParty(pl).hasMember(e.getUniqueId())) {
                                    continue;
                                }

                                // skip armor stands, npcs
                                if (victim instanceof ArmorStand) continue;
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
                                }.runTaskLater(FTRCore.getInstance(), 1L);

                                // apply attack effects, random damage amount
                                if (maxDamage != 0) {
                                    int randomNum = ThreadLocalRandom.current().nextInt(minDamage, maxDamage + 1);
                                    DamageUtil.damageEntityWeapon(randomNum, victim, pl);
                                } else {
                                    DamageUtil.damageEntityWeapon(minDamage, victim, pl);
                                }

                                pl.playSound(pl.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1);
                                this.cancel();
                            }
                        }
                    }
                }
            }
        }.runTaskTimer(FTRCore.getInstance(), 0L, 1L);
    }
}
