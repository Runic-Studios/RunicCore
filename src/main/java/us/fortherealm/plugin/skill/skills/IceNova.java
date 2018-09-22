package us.fortherealm.plugin.skill.skills;

import us.fortherealm.plugin.skill.skilltypes.Skill;
import us.fortherealm.plugin.skill.skilltypes.SkillItemType;
import us.fortherealm.plugin.skill.skilltypes.skillutil.KnockbackUtil;
import org.bukkit.*;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.material.MaterialData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;

// TODO: party damage check
public class IceNova extends Skill {

    private HashMap<UUID, List<UUID>> hasBeenHit;

    public IceNova() {
        super("Ice Nova", "you fire a projectile that damages and freezes all enemies it hits", ChatColor.WHITE, ClickType.RIGHT_CLICK_ONLY, 6);
        this.hasBeenHit = new HashMap<>();
    }

    @Override
    public void onRightClick(Player player, SkillItemType type) {
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDERDRAGON_FLAP, 0.5f, 2.0f);
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_GLASS_BREAK, 0.25f, 1.0f);
        Vector middle = player.getEyeLocation().getDirection().normalize();
        Vector left = rotateVectorAroundY(middle, -22.5);
        Vector right = rotateVectorAroundY(middle, 22.5);

        startTask(player, new Vector[]{middle, left, right});
    }

    private void startTask(Player player, Vector[] vectors) {
        for(Vector vector : vectors) {
            Location location = player.getEyeLocation();
            Location startLoc = player.getLocation();
            for (double t = 0; t < 16; t += 1) {
                location.add(vector);
                player.getWorld().spawnParticle(Particle.BLOCK_CRACK, location, 5, 0, 0, 0, 0, new MaterialData(Material.PACKED_ICE));
                entityCheck(location, player);
                if (location.getBlock().getType().isSolid()) {
                    break;
                }
            }
        }
    }

    private Vector rotateVectorAroundY(Vector vector, double degrees) {
        Vector newVector = vector.clone();
        double rad = Math.toRadians(degrees);
        double cos = Math.cos(rad);
        double sine = Math.sin(rad);
        double x = vector.getX();
        double z = vector.getZ();
        newVector.setX(cos * x - sine * z);
        newVector.setZ(sine * x + cos * z);
        return newVector;
    }

    private void entityCheck(Location location, Player player) {
        for (Entity e : location.getChunk().getEntities()) {
            if (e.getLocation().distance(location) <= 1.5) {
                if (e != (player)) {
                    if (e.getType().isAlive()) {
                        Damageable victim = (Damageable) e;
                        if (hasBeenHit.containsKey(victim.getUniqueId())) {
                            List<UUID> uuids = hasBeenHit.get(victim.getUniqueId());
                            if (uuids.contains(player.getUniqueId())) {
                                break;
                            } else {
                                uuids.add(player.getUniqueId());
                                hasBeenHit.put(victim.getUniqueId(), uuids);
                            }
                        } else {
                            List<UUID> uuids = new ArrayList<>();
                            uuids.add(player.getUniqueId());
                            hasBeenHit.put(victim.getUniqueId(), uuids);
                        }
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                List<UUID> uuids = hasBeenHit.get(victim.getUniqueId());
                                uuids.remove(player.getUniqueId());
                                hasBeenHit.put(victim.getUniqueId(), uuids);
                            }
                        }.runTaskLater(plugin, 100L); // can't be hit by the same player's beam for 5 secs

                        victim.damage(25, player);
                        KnockbackUtil.knockback(player, victim);
                        ((LivingEntity) victim).addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 100, 2)); // 100 ticks = 5s (Slowness III)
                        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1.0f);
                        ((LivingEntity) victim).addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 100, 2)); // 100 ticks = 5s (Slowness III)
                        victim.sendMessage(ChatColor.RED + "You are slowed by " + ChatColor.WHITE + player.getName() + "§c's ice nova!");
                        break;
                    }
                }
            }
        }
    }
}

