package com.runicrealms.plugin.spellapi.spells.mage;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.classes.ClassEnum;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.ChatColor;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.SmallFireball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.UUID;

@SuppressWarnings("FieldCanBeLocal")
public class MeteorShower extends Spell {

    private static final int AMOUNT = 4;
    private static final double FIREBALL_SPEED = 2;
    private static final int DAMAGE_AMOUNT = 35;
    private SmallFireball meteor;
    private HashMap<UUID, UUID> hasBeenHit;

    public MeteorShower() {
        super ("Meteor Shower",
                "You launch four projectile meteors" +
                        "\nthat deal " + DAMAGE_AMOUNT + " spellʔ damage on" +
                        "\nimpact!",
                ChatColor.WHITE, ClassEnum.MAGE, 10, 25);
        hasBeenHit = new HashMap<>();
    }

    @Override
    public void executeSpell(Player player, SpellItemType type) {
        new BukkitRunnable() {
            int count = 0;
            @Override
            public void run() {
                if (count >= AMOUNT) {
                    this.cancel();
                } else {
                    count += 1;
                    meteor = player.launchProjectile(SmallFireball.class);
                    createMeteorParticle(meteor);
                    meteor.setIsIncendiary(false);
                    meteor.setYield(0F);
                    final Vector velocity = player.getLocation().getDirection().normalize().multiply(FIREBALL_SPEED);
                    meteor.setVelocity(velocity);
                    meteor.setShooter(player);
                    player.getWorld().playSound(player.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 0.5f, 0.5f);
                }
            }
        }.runTaskTimer(RunicCore.getInstance(), 0L, 20L);
    }

    private void createMeteorParticle(SmallFireball fireball) {
        // more particles
        new BukkitRunnable() {
            @Override
            public void run() {
                if (fireball.isDead()) {
                    this.cancel();
                }
                fireball.getWorld().spawnParticle(Particle.LAVA, fireball.getLocation(), 1, 0, 0, 0, 0);
            }
        }.runTaskTimer(plugin, 0, 1);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onMeteorHit(EntityDamageByEntityEvent event) {

        // only listen for our meteor
        if (!(event.getDamager().equals(this.meteor))) return;

        event.setDamage(0);
        event.setCancelled(true);

        // grab our variables
        Player player = (Player) meteor.getShooter();
        if (player == null) return;
        LivingEntity victim = (LivingEntity) event.getEntity();

        // prevent concussive hits
        if (hasBeenHit.get(player.getUniqueId()) == victim.getUniqueId()) return;

        if (verifyEnemy(player, victim)) {
            DamageUtil.damageEntitySpell(DAMAGE_AMOUNT, victim, player, 100);
            victim.getWorld().spawnParticle(Particle.FLAME, victim.getEyeLocation(), 5, 0.5F, 0.5F, 0.5F, 0);
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1);
            hasBeenHit.put(player.getUniqueId(), victim.getUniqueId());
            new BukkitRunnable() {
                @Override
                public void run() {
                    hasBeenHit.clear();
                }
            }.runTaskLaterAsynchronously(RunicCore.getInstance(), 10L);
        }
    }
}

