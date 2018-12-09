package us.fortherealm.plugin.listeners;

import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import us.fortherealm.plugin.Main;
import us.fortherealm.plugin.skillapi.skillutil.KnockbackUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

@SuppressWarnings("deprecation")
public class StavesListener  implements Listener {

    // globals
    private static double BEAM_WIDTH = 1.5;
    private static int DAMAGE_AMT = 10;
    private static int RADIUS = 5;
    private static double RANGE = 8.0;
    private static final int SPEED_MULT = 2;
    private HashMap<UUID, List<UUID>> hasBeenHit = new HashMap<>();

    // creates the mage ranged auto-attack
    @EventHandler
    public void onStaffAttack(PlayerInteractEvent e) {

        // check for null
        if (e.getItem() == null) { return; }

        // only listen for staves
        if (!(e.getItem().getType().equals(Material.WOODEN_HOE)
            || e.getItem().getType().equals(Material.STONE_HOE)
            || e.getItem().getType().equals(Material.IRON_HOE)
            || e.getItem().getType().equals(Material.GOLDEN_HOE)
            || e.getItem().getType().equals(Material.DIAMOND_HOE))) {
            return;
        }

        Player pl = e.getPlayer();

        if ((e.getAction() == Action.LEFT_CLICK_AIR || e.getAction() == Action.LEFT_CLICK_BLOCK) && pl.getCooldown(Material.WOODEN_HOE) <= 0) {

            // cancel the event
            e.setCancelled(true);

            // set the cooldown
            pl.setCooldown(Material.WOODEN_HOE, 30);
            // todo: in the class select, make the artifacts have different attack speeds, make the cooldwn equal to that speed
            //pl.getAttribute(Attribute.GENERIC_ATTACK_SPEED).setBaseValue(1);

            // create our vector to be used later
            Vector vector = pl.getEyeLocation().getDirection().normalize().multiply(SPEED_MULT);

            // play sound effect
            // todo: add cooldown
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
                                    Damageable victim = (Damageable) e;

                                    // skip party members
                                    if (Main.getPartyManager().getPlayerParty(pl) != null
                                            && Main.getPartyManager().getPlayerParty(pl).hasMember(e.getUniqueId())) {
                                        continue;
                                    }

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

                                    // can't be hit by the same player's beam for 0.25 secs
                                    new BukkitRunnable() {
                                        @Override
                                        public void run() {
                                            List<UUID> uuids = hasBeenHit.get(victim.getUniqueId());
                                            uuids.remove(pl.getUniqueId());
                                            hasBeenHit.put(victim.getUniqueId(), uuids);
                                        }
                                    }.runTaskLater(Main.getInstance(), 5L);

                                    // apply skill effects
                                    victim.damage(DAMAGE_AMT, pl);
                                    pl.playSound(pl.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1);
                                    KnockbackUtil.knockback(pl, victim, 1);
                                    this.cancel();
                                }
                            }
                        }
                    }
                }
            }.runTaskTimer(Main.getInstance(), 0L, 1L);
        }
    }
}
