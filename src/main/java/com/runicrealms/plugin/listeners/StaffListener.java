package com.runicrealms.plugin.listeners;

import co.aikar.taskchain.TaskChain;
import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.api.event.BasicAttackEvent;
import com.runicrealms.plugin.api.event.StaffAttackEvent;
import com.runicrealms.plugin.common.util.Pair;
import com.runicrealms.plugin.events.EnemyVerifyEvent;
import com.runicrealms.plugin.rdb.RunicDatabase;
import com.runicrealms.plugin.rdb.event.CharacterQuitEvent;
import com.runicrealms.plugin.runicitems.RunicItemsAPI;
import com.runicrealms.plugin.runicitems.item.RunicItem;
import com.runicrealms.plugin.runicitems.item.RunicItemWeapon;
import com.runicrealms.plugin.spellapi.spellutil.VectorUtil;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.RayTraceResult;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Handles the mage basic attack
 *
 * @author Skyfallin
 */
public class StaffListener implements Listener {
    public static final int STAFF_COOLDOWN = 15; // ticks (0.75s)
    private static final int DEFAULT_MAX_DIST = 9;
    private static final double RAY_SIZE = 0.8; //0.5

    private final Map<UUID, Long> droppedItem;

    public StaffListener() {
        this.droppedItem = new HashMap<>();
    }

    /**
     * Verifies that the player's held item is a runic weapon and a staff.
     * Returns a pair whose first arg is true if the item is a staff and the second arg is the item weapon, the player can wield it, high enough level, etc.
     *
     * @param player who is holding the staff
     * @return the pair whose first arg is true if all checks pass
     */
    public static Pair<Boolean, RunicItemWeapon> verifyStaff(Player player, ItemStack itemStack) {
        RunicItem runicItem = RunicItemsAPI.getRunicItemFromItemStack(itemStack);
        if (runicItem == null) return Pair.pair(false, null);
        if (!(runicItem instanceof RunicItemWeapon runicItemWeapon)) return Pair.pair(false, null);
        Material runicItemType = runicItemWeapon.getDisplayableItem().getMaterial();
        double cooldown = player.getCooldown(runicItemType);
        if (cooldown != 0) return Pair.pair(false, null);

        // Check for mage
        String className = RunicDatabase.getAPI().getCharacterAPI().getPlayerClass(player);
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

        if (event.getPlayer().getOpenInventory().getTopInventory().getType() != InventoryType.CRAFTING) {
            return; //make sure player does not have inventory open while doing this (CRAFTING is default)
        }

        Long time = this.droppedItem.get(event.getPlayer().getUniqueId());

        if (time != null && time + STAFF_COOLDOWN * 50 >= System.currentTimeMillis()) { //convert staff cooldown from ticks to miliseconds
            return;
        }

        // Retrieve the weapon type and try to fire staff attack
        TaskChain<?> chain = RunicCore.newChain();
        chain
                .asyncFirst(() -> verifyStaff(event.getPlayer(), event.getItem()))
                .syncLast(result -> {
                    if (result.first) {
                        event.setCancelled(true);
                        Bukkit.getPluginManager().callEvent(new StaffAttackEvent(event.getPlayer(), result.second, DEFAULT_MAX_DIST));
                    }
                })
                .execute();
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onStaffAttack(StaffAttackEvent event) {
        staffAttack(event.getPlayer(), event.getRunicItemWeapon(), event.getRange());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private void onPlayerDropItem(PlayerDropItemEvent event) {
        //does not matter if event was cancelled, matters if packet was sent
        this.droppedItem.put(event.getPlayer().getUniqueId(), System.currentTimeMillis());
    }

    @EventHandler
    private void onCharacterQuit(CharacterQuitEvent event) {
        this.droppedItem.remove(event.getPlayer().getUniqueId());
    }

    /**
     * Function to begin staff attack
     *
     * @param player          who initiated attack
     * @param runicItemWeapon to be passed down to damage function
     * @param range           the range of the attack
     */
    private void staffAttack(Player player, RunicItemWeapon runicItemWeapon, int range) {
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_GHAST_SHOOT, 0.4f, 2.0F);

        RayTraceResult rayTraceResult = player.getWorld().rayTraceEntities
                (
                        player.getLocation(),
                        player.getLocation().getDirection(),
                        range,
                        RAY_SIZE,
                        entity -> isValidEnemy(player, entity)
                );

        if (rayTraceResult == null) {
            Location location = player.getTargetBlock(null, range).getLocation();
            VectorUtil.drawLine(player, Particle.SPELL_WITCH, Color.FUCHSIA, player.getEyeLocation(), location, 0.85D, 1, 0.15f);
        } else if (rayTraceResult.getHitEntity() != null) {
            LivingEntity livingEntity = (LivingEntity) rayTraceResult.getHitEntity();
            VectorUtil.drawLine(player, Particle.SPELL_WITCH, Color.FUCHSIA, player.getEyeLocation(), livingEntity.getEyeLocation(), 0.85D, 1, 0.15f);
            damageStaff(player, livingEntity, runicItemWeapon);
        }

        int minDamage = runicItemWeapon.getWeaponDamage().getMin();
        int maxDamage = runicItemWeapon.getWeaponDamage().getMax();

        Bukkit.getPluginManager().callEvent(new BasicAttackEvent
                (
                        player,
                        runicItemWeapon.getDisplayableItem().getMaterial(),
                        BasicAttackEvent.BASE_STAFF_COOLDOWN,
                        BasicAttackEvent.BASE_STAFF_COOLDOWN,
                        minDamage,
                        maxDamage
                ));
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
}
