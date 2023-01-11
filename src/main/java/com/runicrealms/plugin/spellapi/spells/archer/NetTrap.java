package com.runicrealms.plugin.spellapi.spells.archer;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.api.ArmorStandAPI;
import com.runicrealms.plugin.classes.CharacterClass;
import com.runicrealms.plugin.spellapi.spelltypes.RunicStatusEffect;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.spellapi.spellutil.particles.Circle;
import org.bukkit.*;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;

public class NetTrap extends Spell {

    private static final int DURATION = 12;
    private static final int RADIUS = 2;
    private static final int STUN_DURATION = 3;
    private static final double WARMUP = 0.5; // seconds

    public NetTrap() {
        super("Net Trap",
                "You lay down a trap, which arms after " + WARMUP +
                        "s and lasts for " + DURATION +
                        "s. The first enemy to step over the trap triggers it, " +
                        "causing all enemies within " + RADIUS +
                        " blocks to be lifted into their air and stunned for " +
                        STUN_DURATION + "s! Mobs caught in the trap take " +
                        ChatColor.BOLD + ChatColor.GRAY + "double " + ChatColor.GRAY +
                        "damage for the duration!",
                ChatColor.WHITE, CharacterClass.ARCHER, 15, 25);
    }

    @Override
    public void executeSpell(Player player, SpellItemType type) {
        Location castLocation = player.getLocation();
        Location lower = player.getLocation().subtract(0, 1, 0);
        ArmorStand armorStand = spawnRabbitHide(lower);
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_ANVIL_PLACE, 0.5f, 2.0f);
        new BukkitRunnable() {
            int count = 1;

            @Override
            public void run() {
                if (count > DURATION) {
                    this.cancel();
                    armorStand.remove();
                } else {
                    count += 1;
                    Circle.createParticleCircle(player, castLocation, RADIUS, Particle.CRIT);
                    for (Entity entity : player.getWorld().getNearbyEntities(castLocation, RADIUS, RADIUS, RADIUS)) {
                        if (isValidEnemy(player, entity)) {
                            springTrap(entity);
                            this.cancel();
                            armorStand.remove();
                        }
                    }
                }

            }
        }.runTaskTimer(RunicCore.getInstance(), (long) (WARMUP * 20), 20L);
    }

    private ArmorStand spawnRabbitHide(Location location) {
        // todo: some bug is removing the armor stand
        ArmorStand armorStand = ArmorStandAPI.spawnArmorStand(location);
        armorStand.setArms(true);
        armorStand.setCustomName("Net Trap");
        armorStand.setCustomNameVisible(false);
        armorStand.setRightArmPose(new EulerAngle(ArmorStandAPI.degreesToRadians(235), ArmorStandAPI.degreesToRadians(315), 0));
        armorStand.getEquipment().setItemInMainHand(new ItemStack(Material.RABBIT_HIDE));
        return armorStand;
    }

    private void springTrap(Entity entity) {
        if (entity instanceof Player) {
            Player player = (Player) entity;
            Location higher = player.getLocation().add(0, 2, 0);
            player.getWorld().spawnParticle(Particle.CRIT, higher, 15, 0.25f, 0.25f, 0.25f);
            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ZOMBIE_BREAK_WOODEN_DOOR, 0.25f, 1.0f);
            player.getWorld().playSound(player.getLocation(), Sound.BLOCK_PORTAL_TRAVEL, 0.25f, 1.0f);
            player.teleport(higher);
            addStatusEffect(entity, RunicStatusEffect.STUN, STUN_DURATION);
        } else {
            // todo: if mob, just apply debuff
        }
    }
}
