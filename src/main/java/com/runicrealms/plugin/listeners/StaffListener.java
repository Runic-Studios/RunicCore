package com.runicrealms.plugin.listeners;

import co.aikar.taskchain.TaskChain;
import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.api.Pair;
import com.runicrealms.plugin.api.event.BasicAttackEvent;
import com.runicrealms.plugin.api.event.StaffAttackEvent;
import com.runicrealms.plugin.events.EnemyVerifyEvent;
import com.runicrealms.plugin.spellapi.spellutil.VectorUtil;
import com.runicrealms.plugin.utilities.DamageUtil;
import com.runicrealms.runicitems.RunicItemsAPI;
import com.runicrealms.runicitems.item.RunicItem;
import com.runicrealms.runicitems.item.RunicItemWeapon;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
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
    public static final int STAFF_COOLDOWN = 15; // ticks (0.75s)
    private static final int MAX_DIST = 9;
    private static final double RAY_SIZE = 0.35D;

    /**
     * Verifies that the player's held item is a runic weapon and a staff.
     * Returns true if the item is a staff, the player can wield it, high enough level, etc.
     *
     * @param player who is holding the staff
     * @return true if all checks pass
     */
    public static Pair<Boolean, RunicItemWeapon> verifyStaff(Player player, ItemStack itemStack) {
        RunicItem runicItem = RunicItemsAPI.getRunicItemFromItemStack(itemStack);
        if (runicItem == null) return Pair.pair(false, null);
        if (!(runicItem instanceof RunicItemWeapon runicItemWeapon)) return Pair.pair(false, null);
        Material runicItemType = runicItemWeapon.getDisplayableItem().getMaterial();
        double cooldown = player.getCooldown(runicItemType);
        if (cooldown != 0) return Pair.pair(false, null);

        // Check for mage
        String className = RunicCore.getCharacterAPI().getPlayerClass(player);
        if (className == null) return Pair.pair(false, null);
        if (!className.equals("Mage")) return Pair.pair(false, null);
        if (RunicCore.getSpellAPI().isCasting(player)) return Pair.pair(false, null);
        int reqLv = runicItemWeapon.getLevel();
        if (reqLv > player.getLevel()) {
            player.playSound(player.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 0.5f, 1.0f);
            player.sendMessage(ChatColor.RED + "Your level is too low to wield this!");
            return Pair.pair(false, null);
        }

        return Pair.pair(true, runicItemWeapon);
    }

    /**
     * Create a PhysicalDamageEvent for our staff attack, verify that the player can use the weapon
     *
     * @param player          who summoned staff attack
     * @param victim          to be damaged
     * @param runicItemWeapon runic item to read damage values from
     */
    private void damageStaff(Player player, LivingEntity victim, RunicItemWeapon runicItemWeapon) {
        int minDamage = runicItemWeapon.getWeaponDamage().getMin();
        int maxDamage = runicItemWeapon.getWeaponDamage().getMax();

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

    @EventHandler(priority = EventPriority.LOWEST)
    public void onStaffAttack(PlayerInteractEvent event) {
        // Only listen for left clicks
        if (event.getAction() == Action.RIGHT_CLICK_AIR
                || event.getAction() == Action.RIGHT_CLICK_BLOCK)
            return;
        if (event.getItem() == null) return;
        if (event.getItem().getType() == Material.AIR) return;
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (event.getItem().getType() != Material.WOODEN_HOE) return;
        // Retrieve the weapon type and try to fire staff attack
        TaskChain<?> chain = RunicCore.newChain();
        chain
                .asyncFirst(() -> verifyStaff(event.getPlayer(), event.getItem()))
                .syncLast(result -> {
                    if (result.first) {
                        event.setCancelled(true);
                        Bukkit.getPluginManager().callEvent(new StaffAttackEvent(event.getPlayer(), result.second));
                    }
                })
                .execute();
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onStaffAttack(StaffAttackEvent event) {
        if (event.isCancelled()) return;
        staffAttack(event.getPlayer(), event.getRunicItemWeapon());
    }

    /**
     * Function to begin staff attack
     *
     * @param player          who initiated attack
     * @param runicItemWeapon to be passed down to damage function
     */
    private void staffAttack(Player player, RunicItemWeapon runicItemWeapon) {
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
            VectorUtil.drawLine(player, Particle.SPELL_WITCH, Color.FUCHSIA, player.getEyeLocation(), location, 0.85D, 1, 0.15f);
        } else if (rayTraceResult.getHitEntity() != null) {
            LivingEntity livingEntity = (LivingEntity) rayTraceResult.getHitEntity();
            VectorUtil.drawLine(player, Particle.SPELL_WITCH, Color.FUCHSIA, player.getEyeLocation(), livingEntity.getEyeLocation(), 0.85D, 1, 0.15f);
            damageStaff(player, livingEntity, runicItemWeapon);
        }

        Bukkit.getPluginManager().callEvent(new BasicAttackEvent
                (
                        player,
                        runicItemWeapon.getDisplayableItem().getMaterial(),
                        BasicAttackEvent.BASE_STAFF_COOLDOWN,
                        BasicAttackEvent.BASE_STAFF_COOLDOWN
                ));
    }
}
