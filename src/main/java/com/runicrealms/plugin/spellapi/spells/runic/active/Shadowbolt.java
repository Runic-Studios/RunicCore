package com.runicrealms.plugin.spellapi.spells.runic.active;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import org.bukkit.*;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

@SuppressWarnings("FieldCanBeLocal")
public class Shadowbolt extends Spell {

    private static final int DURATION = 3;
    private static final double SHADOWBOLT_SPEED = 2;
    private EnderPearl shadowbolt;

    public Shadowbolt() {
        super ("Shadowbolt",
                "You launch a ball of shadow" +
                        "\nthat blinds enemies on impact" +
                        "\nfor " + DURATION + " seconds! This ability" +
                        "\nhas no effect versus monsters.",
                ChatColor.WHITE, 5, 10);
    }

    @Override
    public void executeSpell(Player player, SpellItemType type) {
        shadowbolt = player.launchProjectile(EnderPearl.class);
        shadowbolt.setBounce(false);
        final Vector velocity = player.getLocation().getDirection().normalize().multiply(SHADOWBOLT_SPEED);
        shadowbolt.setVelocity(velocity);
        shadowbolt.setShooter(player);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 0.5f, 0.5f);
        new BukkitRunnable() {
            @Override
            public void run() {
                Location arrowLoc = shadowbolt.getLocation();
                shadowbolt.getWorld().spawnParticle(Particle.REDSTONE, arrowLoc, 5, 0, 0, 0, 0, new Particle.DustOptions(Color.BLACK, 1));
                if (shadowbolt.isDead() || shadowbolt.isOnGround()) {
                    this.cancel();
                }
            }
        }.runTaskTimer(RunicCore.getInstance(), 0L, 1L);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEnderPearlHit(EntityDamageByEntityEvent event) {

        // only listen for our shadowbolt
        if (!(event.getDamager().equals(this.shadowbolt))) return;

        event.setCancelled(true);

        // grab our variables
        Player player = (Player) shadowbolt.getShooter();
        if (player == null) return;
        LivingEntity victim = (LivingEntity) event.getEntity();

        if (verifyEnemy(player, victim)) {
            victim.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, DURATION*20, 2));
            victim.getWorld().spawnParticle(Particle.SMOKE_LARGE, victim.getEyeLocation(), 5, 0.5F, 0.5F, 0.5F, 0);
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_HURT, 0.5f, 1);
        }
    }

    @EventHandler()
    public void enderPearlThrown(PlayerTeleportEvent e) {
        if (e.getCause() == PlayerTeleportEvent.TeleportCause.ENDER_PEARL) e.setCancelled(true);
    }
}

