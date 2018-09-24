package us.fortherealm.plugin.oldskills.skills;

import us.fortherealm.plugin.oldskills.skilltypes.skillutil.Bubble;
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
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.UUID;

// TODO: party check for blind and poison
public class SmokeBomb extends Skill {

    private HashMap<Arrow, UUID> trails = new HashMap<>();

    public SmokeBomb() {
        super("Smoke Bomb", "you fire a thing that shoots up and blinds and poisons enemies",
                ChatColor.WHITE, ClickType.RIGHT_CLICK_ONLY, 1);
    }


    public void onRightClick(Player player, SkillItemType type) {
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 0.5f, 1.0f);
        Vector middle = player.getEyeLocation().getDirection().normalize();
        startTask(player, new Vector[]{middle});
    }

    private void startTask(Player player, Vector[] vectors) {
        for (Vector vector : vectors) {
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
                    player.getWorld().spawnParticle(Particle.SMOKE_NORMAL, arrowLoc, 5, 0, 0, 0, 0);
                    if (arrow.isDead() || arrow.isOnGround()) {
                        this.cancel();
                        Bubble.bubbleEffect
                                (arrowLoc, Particle.SMOKE_LARGE, 1, 0, 1, 1.5);
                        player.getWorld().playSound(arrowLoc, Sound.BLOCK_FIRE_AMBIENT, 0.5F, 0.5F);
                        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 0.5f, 0.5f);
                        for (Entity entity : arrowLoc.getChunk().getEntities()) {
                            if (entity.getLocation().distance(arrowLoc) <= 1.5) {
                                if (entity != (player)) {
                                    if (entity.getType().isAlive()) {
                                        Damageable victim = (Damageable) entity;
                                        victim.damage(15, player);
                                        if(victim instanceof Player){
                                            ((Player) victim).addPotionEffect
                                                    (new PotionEffect(PotionEffectType.BLINDNESS, 100, 0));
                                            ((Player) victim).sendMessage(ChatColor.RED + "You are blinded by " +
                                                    ChatColor.WHITE + player.getName() + ChatColor.RED + "'s smoke bomb!");
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }.runTaskTimer(plugin, 0, 1L);
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
