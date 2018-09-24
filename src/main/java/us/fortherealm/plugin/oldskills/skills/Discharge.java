package us.fortherealm.plugin.oldskills.skills;

import us.fortherealm.plugin.oldskills.skilltypes.Skill;
import us.fortherealm.plugin.oldskills.skilltypes.SkillItemType;
import net.minecraft.server.v1_12_R1.PacketPlayOutEntityDestroy;
import org.bukkit.*;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.UUID;

public class Discharge extends Skill {

    private HashMap<Arrow, UUID> trails = new HashMap<>();

    public Discharge() {
        super("Discharge", "fire a bolt of lightning that travels. if it hits a solid block, it blows up",
                ChatColor.WHITE, ClickType.RIGHT_CLICK_ONLY, 1);
    }

    @Override
    public void onRightClick(Player player, SkillItemType type) {
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_FIREWORK_LAUNCH, 0.5f, 1.0f);
        Vector middle = player.getEyeLocation().getDirection().normalize();

        startTask(player, new Vector[]{middle});
    }

    private void startTask(Player player, Vector[] vectors) {
        for(Vector vector : vectors) {
            Vector direction = player.getEyeLocation().getDirection().normalize().multiply(1);
            Arrow arrow = player.launchProjectile(Arrow.class);
            arrow.isSilent();
            UUID uuid = player.getUniqueId();
            arrow.setVelocity(direction);
            arrow.setShooter(player);
            trails.put(arrow, uuid);
            for(Player p : Bukkit.getServer().getOnlinePlayers()) {
                PacketPlayOutEntityDestroy packet = new PacketPlayOutEntityDestroy(arrow.getEntityId());
                ((CraftPlayer) p).getHandle().playerConnection.sendPacket(packet);
            }
            new BukkitRunnable() {
                @Override
                public void run() {
                    Location arrowLoc = arrow.getLocation();
                    player.getWorld().spawnParticle(Particle.CRIT_MAGIC, arrowLoc, 5, 0, 0, 0, 0);
                    if (arrow.isDead() || arrow.isOnGround()) {
                        this.cancel();
                        arrow.getWorld().spigot().strikeLightningEffect(arrowLoc, true);
                        arrowLoc.getWorld().playSound(arrowLoc, Sound.ENTITY_LIGHTNING_THUNDER, 0.5f, 1.0f);
                        arrowLoc.getWorld().playSound(arrowLoc, Sound.ENTITY_GENERIC_EXPLODE, 0.5f, 1.0f);
                        arrowLoc.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, arrowLoc, 5, 0.2f, 0.2f, 0.2f, 0);

                        for (Entity entity : arrowLoc.getChunk().getEntities()) {
                            if (entity.getLocation().distance(arrowLoc) <= 5) {
                                if (entity != (player)) {
                                    if (entity.getType().isAlive()) {
                                        Damageable victim = (Damageable) entity;
                                        victim.damage(25, player);
                                        Vector force = (arrowLoc.toVector().subtract(victim.getLocation().toVector()).multiply(-0.75).setY(0.6));
                                        victim.setVelocity(force);
                                    }
                                }
                            }
                        }
                    }
                }
            }.runTaskTimer(plugin, 0L, 1L);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onArrowDamage(EntityDamageByEntityEvent e) {
        if (e.getDamager() instanceof Arrow) {
            Arrow arrow = (Arrow) e.getDamager();
            if (trails.containsKey(arrow)) {
                e.setCancelled(true);
            }
        }
    }
}
