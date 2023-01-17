package com.runicrealms.plugin.listeners;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.events.EnemyVerifyEvent;
import com.runicrealms.plugin.spellapi.spellutil.VectorUtil;
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
import org.bukkit.util.RayTraceResult;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Handles the mage basic attack
 *
 * @author Skyfallin
 */
public class StaffListener implements Listener {

    private static final int STAFF_COOLDOWN = 15; // ticks (0.75s)
    private static final int MAX_DIST = 9;
    private static final double RAY_SIZE = 1.0D;

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

    /**
     * @param player who cast the beam
     * @param victim the entity hit by the staff beam
     * @return true if the enemy can be damaged
     */
    private boolean isValidEnemy(Player player, Entity victim) {
        EnemyVerifyEvent enemyVerifyEvent = new EnemyVerifyEvent(player, victim);
        Bukkit.getServer().getPluginManager().callEvent(enemyVerifyEvent);
        return !enemyVerifyEvent.isCancelled();
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


        RayTraceResult rayTraceResult = player.getWorld().rayTraceEntities
                (
                        player.getLocation(),
                        player.getLocation().getDirection(),
                        MAX_DIST,
                        RAY_SIZE,
                        entity -> isValidEnemy(player, entity)
                );

        if (rayTraceResult == null) {
            Location location = player.getTargetBlock(null, MAX_DIST).getLocation();
            VectorUtil.drawLine(player, Particle.SPELL_WITCH, Color.FUCHSIA, player.getEyeLocation(), location, 0.5D, 1, 0.25f);
        } else if (rayTraceResult.getHitEntity() != null) {
            LivingEntity livingEntity = (LivingEntity) rayTraceResult.getHitEntity();
            VectorUtil.drawLine(player, Particle.SPELL_WITCH, Color.FUCHSIA, player.getEyeLocation(), livingEntity.getEyeLocation(), 0.5D, 1, 0.15f);
            damageStaff(player, livingEntity, itemStack);
        }

        player.setCooldown(itemStack.getType(), STAFF_COOLDOWN);
    }
}
