package com.runicrealms.plugin.listeners;

import com.runicrealms.plugin.api.RunicCoreAPI;
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
import org.bukkit.util.Vector;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Handles the mage basic attack
 *
 * @author Skyfallin
 */
public class StaffListener implements Listener {

    private static final int STAFF_COOLDOWN = 15; // ticks (0.75s)
    private static final int MAX_DIST = 8;
    private static final double BEAM_SIZE = 1.5;
    private static final double PARTICLE_SPACE = 1.75;

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
        String className = RunicCoreAPI.getPlayerClass(player);
        if (className == null) return;
        if (!className.equals("Mage")) return;
        if (RunicCoreAPI.isCasting(player)) return;

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
        Location first = player.getEyeLocation();
        Location second = player.getTargetBlock(null, MAX_DIST).getLocation();

        double distance = first.distance(second);
        Vector v1 = first.toVector();
        Vector v2 = second.toVector();
        Vector vector = v2.clone().subtract(v1).normalize().multiply(PARTICLE_SPACE);
        Location vectorLocation;
        outerLabel:
        for (double length = 0; length < distance; v1.add(vector)) {
            player.getWorld().spawnParticle(Particle.SPELL_WITCH,
                    new Location(player.getWorld(), v1.getX(), v1.getY(), v1.getZ()), 1, 0, 0, 0, 0);
            vectorLocation = v1.toLocation(player.getWorld());
            for (Entity en : player.getWorld().getNearbyEntities(vectorLocation, BEAM_SIZE, BEAM_SIZE, BEAM_SIZE)) {
                if (en.equals(player)) continue;
                EnemyVerifyEvent event = new EnemyVerifyEvent(player, en);
                Bukkit.getServer().getPluginManager().callEvent(event);
                if (event.isCancelled()) continue;
                damageStaff(player, (LivingEntity) en, itemStack);
                break outerLabel;
            }
            length += PARTICLE_SPACE;
        }

        player.setCooldown(itemStack.getType(), STAFF_COOLDOWN);
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
}
