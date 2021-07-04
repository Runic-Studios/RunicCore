package com.runicrealms.plugin.spellapi.spells.mage;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.classes.ClassEnum;
import com.runicrealms.plugin.spellapi.spelltypes.EffectEnum;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.*;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

@SuppressWarnings("FieldCanBeLocal")
public class Shadowbolt extends Spell {

    private static final int DAMAGE_AMT = 15;
    private static final double DURATION = 1.5; // seconds
    private static final double SHADOWBOLT_SPEED = 2;
    private EnderPearl shadowbolt;

    public Shadowbolt() {
        super ("Shadowbolt",
                "You launch a ball of shadow " +
                        "that silences enemies on impact " +
                        "for " + DURATION + " seconds!",
                ChatColor.WHITE, ClassEnum.MAGE, 12, 20);
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
                shadowbolt.getWorld().spawnParticle(Particle.REDSTONE, arrowLoc, 5, 0, 0, 0, 0, new Particle.DustOptions(Color.PURPLE, 1));
                if (shadowbolt.isDead() || shadowbolt.isOnGround()) {
                    this.cancel();
                    silenceNearest(player);
                }
            }
        }.runTaskTimer(RunicCore.getInstance(), 0L, 1L);
    }

    private void silenceNearest(Player pl) {
        for (Entity en : shadowbolt.getNearbyEntities(1, 1, 1)) {
            if (!verifyEnemy(pl, en)) continue;
            LivingEntity victim = (LivingEntity) en;
            DamageUtil.damageEntitySpell(DAMAGE_AMT, victim, pl, this);
            // victim.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, (int) (DURATION * 20), 2));
            addStatusEffect(victim, EffectEnum.SILENCE, DURATION);
            victim.getWorld().spawnParticle(Particle.SMOKE_LARGE, victim.getEyeLocation(), 5, 0.5F, 0.5F, 0.5F, 0);
            pl.playSound(pl.getLocation(), Sound.ENTITY_PLAYER_HURT, 0.5f, 1);
            return;
        }
    }

    @EventHandler()
    public void enderPearlThrown(PlayerTeleportEvent e) {
        if (e.getCause() == PlayerTeleportEvent.TeleportCause.ENDER_PEARL)
            e.setCancelled(true);
    }

    public static double getDuration() {
        return DURATION;
    }
}

