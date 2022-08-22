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
    private static final int RADIUS = 5;
    private static final int MAX_DIST = 8;
    private static final double BEAM_HITBOX_SIZE = 1.5;

    @EventHandler
    public void onStaffAttack(PlayerInteractEvent e) {

        // check for null
        if (e.getItem() == null) return;

        // retrieve the weapon type
        ItemStack artifact = e.getItem();
        Material artifactType = artifact.getType();
        double cooldown = e.getPlayer().getCooldown(artifact.getType());

        // only listen for items that can be artifact weapons
        if (artifactType == Material.AIR) return;
        if (e.getHand() != EquipmentSlot.HAND) return;

        // IGNORE NON-STAFF ITEMS
        if (artifactType != Material.WOODEN_HOE) return;

        Player player = e.getPlayer();

        // only listen for left clicks
        if (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) return;

        // only apply cooldown if it's not already active
        if (cooldown != 0) return;

        // cancel the event, run custom mechanics
        e.setCancelled(true);

        // check for mage
        String className = RunicCoreAPI.getPlayerClass(player);
        if (className == null) return;
        if (!className.equals("Mage")) return;
        if (RunicCoreAPI.isCasting(player)) return;

        staffAttack(player, artifact);
        // set the cooldown
        player.setCooldown(artifact.getType(), STAFF_COOLDOWN);
    }

    /**
     * Function to begin staff attack
     *
     * @param player    who initiated attack
     * @param itemStack to be passed down to damage function
     */
    private void staffAttack(Player player, ItemStack itemStack) {
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_GHAST_SHOOT, 0.4f, 2.0F);
        createStaffParticle(player, player.getEyeLocation(), player.getTargetBlock(null, MAX_DIST).getLocation(), itemStack);
    }

    /**
     * Summon a vector which acts as our staff attack
     *
     * @param player    who conjured attack
     * @param point1    eye location of player
     * @param point2    player's target block
     * @param itemStack to be passed to the damage function
     */
    private void createStaffParticle(Player player, Location point1, Location point2, ItemStack itemStack) {
        double space = 0.5;
        double distance = point1.distance(point2);
        Vector p1 = point1.toVector();
        Vector p2 = point2.toVector();
        Vector vector = p2.clone().subtract(p1).normalize().multiply(space);
        for (double length = 0; length < distance; p1.add(vector)) {
            Location vectorLocation = p1.toLocation(player.getWorld());
            for (Entity en : player.getWorld().getNearbyEntities(p1.toLocation(player.getWorld()), RADIUS, RADIUS, RADIUS)) {
                EnemyVerifyEvent e = new EnemyVerifyEvent(player, en);
                Bukkit.getServer().getPluginManager().callEvent(e);
                if (e.isCancelled()) continue;
                if (en.getLocation().distanceSquared(vectorLocation) <= BEAM_HITBOX_SIZE * BEAM_HITBOX_SIZE) {
                    damageStaff(player, (LivingEntity) en, itemStack);
                    return;
                }
            }
            player.getWorld().spawnParticle(Particle.CRIT_MAGIC,
                    new Location(player.getWorld(), p1.getX(), p1.getY(), p1.getZ()), 25, 0, 0, 0, 0);
            length += space;
        }
    }

    /**
     * Create a WeaponDamageEvent for our staff attack
     *
     * @param player   who summoned staff attack
     * @param victim   to be damaged
     * @param artifact held item to read damage values from
     */
    private void damageStaff(Player player, LivingEntity victim, ItemStack artifact) {

        int minDamage;
        int maxDamage;
        int reqLv;

        try {
            RunicItemWeapon runicItemWeapon = (RunicItemWeapon) RunicItemsAPI.getRunicItemFromItemStack(artifact);
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
            DamageUtil.damageEntityWeapon(randomNum, victim, player, true, true);
        } else {
            DamageUtil.damageEntityWeapon(maxDamage, victim, player, true, true);
        }

        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_HURT, 0.5f, 1);
    }
}
