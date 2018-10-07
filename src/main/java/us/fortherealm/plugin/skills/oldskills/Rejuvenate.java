package us.fortherealm.plugin.skills.oldskills;

import org.bukkit.event.inventory.ClickType;
import us.fortherealm.plugin.Main;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import us.fortherealm.plugin.skills.Skill;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

// TODO: party check for heal
public class Rejuvenate {
    /*

    public Rejuvenate() {
        super("Rejuvenate", "you fire a thing that heals", ChatColor.WHITE, ClickType.RIGHT_CLICK_ONLY, 10);
        this.hasBeenHit = new HashMap<>();
    }


    public void onRightClick(Player player, SkillItemType type) {
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 0.5f, 1.0f);
        Vector middle = player.getEyeLocation().getDirection().normalize();

        startTask(player, new Vector[]{middle});
    }

    private void startTask(Player player, Vector[] vectors) {
        for (Vector vector : vectors) {
            new BukkitRunnable() {
                Location location = player.getEyeLocation();
                Location startLoc = player.getLocation();

                @Override
                public void run() {
                    location.add(vector);
                    if (location.getBlock().getType().isSolid() || location.distance(startLoc) >= 10.0D) { // 10 block range
                        this.cancel();
                    }
                    player.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, location, 5, 0.1f, 0.1f, 0.1f, 0.01);
                    allyCheck(location, player);
                    // entityCheck.checkNearby(location, player, plugin);

                }
            }.runTaskTimer(Main.getInstance(), 0L, 1L); // 1 tick, the speed
        }
    }

    private void allyCheck(Location location, Player player) {
        for (Entity e : location.getChunk().getEntities()) {
            if (e.getLocation().distance(location) <= 1.5) {
                if (e != (player)) {
                    if (e instanceof Player) {
                        Player ally = (Player) e;
                        if (hasBeenHit.containsKey(ally.getUniqueId())) {
                            List<UUID> uuids = hasBeenHit.get(ally.getUniqueId());
                            if (uuids.contains(player.getUniqueId())) {
                                break;
                            } else {
                                uuids.add(player.getUniqueId());
                                hasBeenHit.put(ally.getUniqueId(), uuids);
                            }
                        } else {
                            List<UUID> uuids = new ArrayList<>();
                            uuids.add(player.getUniqueId());
                            hasBeenHit.put(ally.getUniqueId(), uuids);
                        }
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                List<UUID> uuids = hasBeenHit.get(ally.getUniqueId());
                                uuids.remove(player.getUniqueId());
                                hasBeenHit.put(ally.getUniqueId(), uuids);
                            }
                        }.runTaskLater(plugin, 100L); // can't be hit by the same player's beam for 5 secs

                        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1);
                        if (ally.getHealth() == ally.getMaxHealth()) {
                            ally.sendMessage(ChatColor.GRAY + "You are currently at full health.");
                            ally.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5f, 1);
                        } else {
                            HealUtil.healPlayer(25, ally, " from " + ChatColor.WHITE + player.getName());
                            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                                @Override
                                public void run() {
                                    boardUtil.updateSideScoreboard(ally);//does what you think it does
                                    boardUtil.updateHealthBar(ally);//updates enemy health bars below their name
                                }
                            }, 1);//1 tick(s)
                            break;
                        }
                    }
                }
            }
        }

    }
    */
}
