package us.fortherealm.plugin.skillapi.skills.rogue;

import net.minecraft.server.v1_13_R2.PacketPlayOutEntityDestroy;
import org.bukkit.*;
import org.bukkit.craftbukkit.v1_13_R2.entity.CraftPlayer;
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
import us.fortherealm.plugin.Main;
import us.fortherealm.plugin.skillapi.skilltypes.Skill;
import us.fortherealm.plugin.skillapi.skilltypes.SkillItemType;

import java.util.HashMap;
import java.util.UUID;

public class SmokeBomb extends Skill {

    // globals
    private static final int DURATION = 5;
    private static final int RADIUS = 5;
    private HashMap<Arrow, UUID> trails = new HashMap<>();

    // constructor
    public SmokeBomb() {
        super("Smoke Bomb", "you fire a thing that shoots up and blinds and poisons enemies",
                ChatColor.WHITE, ClickType.RIGHT_CLICK_ONLY, 1);
    }

    // skill execute code
    @Override
    public void onRightClick(Player player, SkillItemType type) {
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 0.5f, 1.0f);
        startTask(player);
    }

    private void startTask(Player player) {

        // create our vector, arrow, add arrow to hashmap
        Vector direction = player.getEyeLocation().getDirection().normalize().multiply(1);
        Arrow arrow = player.launchProjectile(Arrow.class);
        arrow.isSilent();
        UUID uuid = player.getUniqueId();
        arrow.setVelocity(direction);
        arrow.setShooter(player);
        trails.put(arrow, uuid);

        // make our arrow invisible
        for (Player p : Bukkit.getServer().getOnlinePlayers()) {
            PacketPlayOutEntityDestroy packet = new PacketPlayOutEntityDestroy(arrow.getEntityId());
            ((CraftPlayer) p).getHandle().playerConnection.sendPacket(packet);
        }

        // start our running task
        new BukkitRunnable() {
            @Override
            public void run() {

                // grab our arrow's location
                Location arrowLoc = arrow.getLocation();
                player.getWorld().spawnParticle(Particle.REDSTONE, arrowLoc, 5, 0, 0, 0, 0, new Particle.DustOptions(Color.YELLOW, 1));
                if (arrow.isDead() || arrow.isOnGround()) {
                    this.cancel();

                    // particle effect
                    arrowLoc.getWorld().spawnParticle(Particle.REDSTONE, arrowLoc,
                            50, 1f, 1f, 1f, new Particle.DustOptions(Color.YELLOW, 20));

                    // sound effects
                    player.getWorld().playSound(arrowLoc, Sound.BLOCK_FIRE_AMBIENT, 0.5F, 0.5F);
                    player.getWorld().playSound(player.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 0.5f, 0.5f);

                    for (Entity entity : arrow.getNearbyEntities(RADIUS, RADIUS, RADIUS)) {
                        if (entity.getLocation().distance(arrowLoc) <= RADIUS) {

                            // ignore the caster
                            if (entity == player) {
                                continue;
                            }

                            // skip party members
                            if (Main.getPartyManager().getPlayerParty(player) != null
                                    && Main.getPartyManager().getPlayerParty(player).hasMember(entity.getUniqueId())) {
                                continue;
                            }

                            // damage the entity, blind them if they're a player
                            if (entity.getType().isAlive()) {
                                Damageable victim = (Damageable) entity;
                                victim.damage(15, player);
                                if (victim instanceof Player) {
                                    ((Player) victim).addPotionEffect
                                            (new PotionEffect(PotionEffectType.BLINDNESS, DURATION * 20, 0));
                                }
                            }
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 0, 1L);
    }

    // prevent damage from our invisible arrow
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

