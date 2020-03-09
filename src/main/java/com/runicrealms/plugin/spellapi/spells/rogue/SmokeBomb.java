package com.runicrealms.plugin.spellapi.spells.rogue;

import com.runicrealms.plugin.classes.ClassEnum;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.utilities.DamageUtil;
import net.minecraft.server.v1_13_R2.PacketPlayOutEntityDestroy;
import org.bukkit.*;
import org.bukkit.craftbukkit.v1_13_R2.entity.CraftPlayer;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
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

public class SmokeBomb extends Spell {

    private static final int DAMAGE_AMT = 15;
    private static final int DURATION = 2;
    private static final int RADIUS = 5;
    private HashMap<Arrow, UUID> trails = new HashMap<>();

    public SmokeBomb() {
        super("Smoke Bomb",
                "You fire a cloud of toxic smoke" +
                        "\nthat deals " + DAMAGE_AMT + " spellʔ damage and" +
                        "\nslows enemies within " + RADIUS + " blocks" +
                        "\nfor " + DURATION + " seconds!",
                ChatColor.WHITE, ClassEnum.ROGUE, 6, 15);
    }

    @Override
    public void executeSpell(Player player, SpellItemType type) {
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 0.5f, 1.0f);
        startTask(player);
    }

    private void startTask(Player player) {

        // create our vector, arrow, add arrow to hashmap
        Vector direction = player.getEyeLocation().getDirection().normalize().multiply(1);
        Arrow arrow = player.launchProjectile(Arrow.class);
        arrow.setVelocity(direction);
        arrow.setShooter(player);
        arrow.isSilent();
        UUID uuid = player.getUniqueId();
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
                    arrow.getWorld().spawnParticle(Particle.REDSTONE, arrowLoc,
                            50, 1f, 1f, 1f, new Particle.DustOptions(Color.YELLOW, 20));

                    // sound effects
                    player.getWorld().playSound(arrowLoc, Sound.BLOCK_FIRE_AMBIENT, 0.5F, 0.5F);
                    player.getWorld().playSound(player.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 0.5f, 0.5f);

                    for (Entity entity : arrow.getNearbyEntities(RADIUS, RADIUS, RADIUS)) {
                        if (entity.getLocation().distance(arrowLoc) <= RADIUS) {
                            if (verifyEnemy(player, entity)) {
                                LivingEntity victim = (LivingEntity) entity;
                                DamageUtil.damageEntitySpell(DAMAGE_AMT, victim, player, false);
                                victim.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, DURATION * 20, 2));
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

