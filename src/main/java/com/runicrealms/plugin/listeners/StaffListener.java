package com.runicrealms.plugin.listeners;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.events.EnemyVerifyEvent;
import com.runicrealms.plugin.utilities.DamageUtil;
import com.runicrealms.runicitems.RunicItemsAPI;
import com.runicrealms.runicitems.item.RunicItemWeapon;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Handles the mage basic attack
 *
 * @author Skyfallin
 */
public class StaffListener implements Listener {

    private static final int STAFF_COOLDOWN = 15; // ticks (0.75s)
    private static final int MAX_DIST = 9;
    private static final double BEAM_SIZE = 1.5;
    private static final double BEAM_SPEED = 3;

    /**
     * Creates the runnable which manages the staff particle and nearby entity checking
     *
     * @param player    who attacked
     * @param itemStack their staff weapon
     */
    private void createStaffRunnable(Player player, ItemStack itemStack) {

        Vector vector = player.getEyeLocation().getDirection().normalize().multiply(BEAM_SPEED);

        new BukkitRunnable() {
            final Location location = player.getEyeLocation();
            final Location startLoc = player.getLocation();

            @Override
            public void run() {
                location.add(vector);
                if (location.getBlock().getType().isSolid() || location.distanceSquared(startLoc) >= MAX_DIST * MAX_DIST) {
                    this.cancel();
                    Bukkit.getScheduler().runTask(RunicCore.getInstance(), () -> tryToHitEnemy(player, location, itemStack));
                }
                player.getWorld().spawnParticle(Particle.SPELL_WITCH, location, 3, 0.1f, 0.2f, 0.1f, 0);
                if (tryToHitEnemy(player, location, itemStack))
                    this.cancel();
            }
        }.runTaskTimer(RunicCore.getInstance(), 0L, 1L);
    }

    /**
     * Create a PhysicalDamageEvent for our staff attack, verify that the player can use the weapon
     *
     * @param player    who summoned staff attack
     * @param victim    to be damaged
     * @param itemStack held item to read damage values from
     */
    private void damageStaff(Player player, LivingEntity victim, ItemStack itemStack) {

        int minDamage;
        int maxDamage;
        int reqLv;

        try {
            RunicItemWeapon runicItemWeapon = (RunicItemWeapon) RunicItemsAPI.getRunicItemFromItemStack(itemStack);
            minDamage = runicItemWeapon.getWeaponDamage().getMin();
            maxDamage = runicItemWeapon.getWeaponDamage().getMax();
            reqLv = runicItemWeapon.getLevel();
        } catch (Exception ex) {
            minDamage = 1;
            maxDamage = 1;
            reqLv = 1;
        }

        if (reqLv > player.getLevel()) {
            player.playSound(player.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 0.5f, 1.0f);
            player.sendMessage(ChatColor.RED + "Your level is too low to wield this!");
            return;
        }

        // apply attack effects, random damage amount
        if (maxDamage != 0) {
            int randomNum = ThreadLocalRandom.current().nextInt(minDamage, maxDamage + 1);
            DamageUtil.damageEntityPhysical(randomNum, victim, player, true, true);
        } else {
            DamageUtil.damageEntityPhysical(maxDamage, victim, player, true, true);
        }

        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_HURT, 0.5f, 1);
    }

    @EventHandler
    public void onStaffAttack(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_AIR
                || event.getAction() == Action.RIGHT_CLICK_BLOCK) return; // only listen for left clicks
        if (event.getItem() == null) return;

        // retrieve the weapon type
        ItemStack weapon = event.getItem();
        Material artifactType = weapon.getType();
        double cooldown = event.getPlayer().getCooldown(weapon.getType());

        if (artifactType == Material.AIR) return;
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (artifactType != Material.WOODEN_HOE) return;
        if (cooldown != 0) return;

        // cancel the event, run custom mechanics
        event.setCancelled(true);
        Player player = event.getPlayer();

        // check for mage
        String className = RunicCore.getCharacterAPI().getPlayerClass(player);
        if (className == null) return;
        if (!className.equals("Mage")) return;
        if (RunicCore.getSpellAPI().isCasting(player)) return;

        staffAttack(player, weapon);
    }

    /**
     * Function to begin staff attack
     *
     * @param player    who initiated attack
     * @param itemStack to be passed down to damage function
     */
    private void staffAttack(Player player, ItemStack itemStack) {
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_GHAST_SHOOT, 0.4f, 2.0F);
        createStaffRunnable(player, itemStack);
        player.setCooldown(itemStack.getType(), STAFF_COOLDOWN);
    }

    /**
     * @param player    who cast the beam
     * @param location  current location of the beam
     * @param itemStack the weapon used (for damage)
     * @return true if the beam has hit an enemy
     */
    private boolean tryToHitEnemy(Player player, Location location, ItemStack itemStack) {
        for (Entity en : player.getWorld().getNearbyEntities(location, BEAM_SIZE, BEAM_SIZE, BEAM_SIZE)) {
            if (en.equals(player)) continue;
            EnemyVerifyEvent event = new EnemyVerifyEvent(player, en);
            Bukkit.getServer().getPluginManager().callEvent(event);
            if (event.isCancelled()) continue;
            damageStaff(player, (LivingEntity) en, itemStack);
            return true;
        }
        return false;
    }
}
