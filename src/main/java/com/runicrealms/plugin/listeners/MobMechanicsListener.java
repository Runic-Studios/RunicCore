package com.runicrealms.plugin.listeners;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.events.MagicDamageEvent;
import com.runicrealms.plugin.events.PhysicalDamageEvent;
import io.lumine.xikage.mythicmobs.MythicMobs;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Horse;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;

/**
 * Handles all custom mechanics with respect to mobs
 */
public final class MobMechanicsListener implements Listener {

    /**
     * Uses the MythicMobs skills system to update the MM health bars, since disguises breaks the default method.
     *
     * @param livingEntity MythicMob to update health bar for
     */
    private static void createMythicHealthDisplay(LivingEntity livingEntity) {
        int numColorBars = calculateNumColors(livingEntity);
        MythicMobs.inst().getAPIHelper().castSkill(livingEntity, "UpdateHealthBar_" + numColorBars);
    }

    /**
     * Dispalys a health bar that looks like [|||] with colors corresponding to entity's remaining health.
     *
     * @param livingEntity to display health bar for
     * @param damage       taken from most recent event
     * @return a string to set the name to
     */
    private static String createHealthDisplay(LivingEntity livingEntity, int damage) {

        int numColorBars = calculateNumColors(livingEntity);

        // colors correspond to percentage of health remaining
        String firstHalf = ChatColor.GREEN + "" + "|||||";
        String secHalf = ChatColor.DARK_GRAY + "" + "|||||";
        ChatColor healthColor = ChatColor.WHITE;
        switch (numColorBars) {
            case 10:
                secHalf = ChatColor.GREEN + "" + "|||||";
                break;
            case 9:
                secHalf = ChatColor.GREEN + "" + "||||" + ChatColor.DARK_GRAY + "|";
                break;
            case 8:
                secHalf = ChatColor.GREEN + "" + "|||" + ChatColor.DARK_GRAY + "||";
                break;
            case 7:
                firstHalf = ChatColor.YELLOW + "" + "|||||";
                secHalf = ChatColor.YELLOW + "" + "||" + ChatColor.DARK_GRAY + "|||";
                break;
            case 6:
                firstHalf = ChatColor.YELLOW + "" + "|||||";
                secHalf = ChatColor.YELLOW + "" + "|" + ChatColor.DARK_GRAY + "||||";
                break;
            case 5:
                firstHalf = ChatColor.YELLOW + "" + "|||||";
                secHalf = ChatColor.DARK_GRAY + "" + "|||||";
                break;
            case 4:
                firstHalf = ChatColor.YELLOW + "" + "||||" + ChatColor.DARK_GRAY + "|";
                break;
            case 3:
                firstHalf = ChatColor.RED + "" + "|||" + ChatColor.DARK_GRAY + "||";
                break;
            case 2:
                firstHalf = ChatColor.RED + "" + "||" + ChatColor.DARK_GRAY + "|||";
                break;
            case 1:
                firstHalf = ChatColor.RED + "" + "|" + ChatColor.DARK_GRAY + "||||";
                break;
            default:
                firstHalf = ChatColor.DARK_GRAY + "" + "|||||";
                break;

        }

        String healthStr = healthColor + "" + (int) (livingEntity.getHealth() - damage);
        return firstHalf + healthStr + secHalf;
    }

    /**
     * Calculates the number of health bars that should be colored based on entity's remaining health.
     *
     * @param livingEntity entity that took damage
     * @return number of bars to color-in
     */
    private static int calculateNumColors(LivingEntity livingEntity) {
        double maxHealth = livingEntity.getMaxHealth();
        double currentHealth;
        currentHealth = Math.max(livingEntity.getHealth(), 0);
        int healthPercentage = (int) ((currentHealth / maxHealth) * 100.0D);
        return healthPercentage / 10;
    }

    @EventHandler
    public void onBurn(EntityCombustEvent event) {
        event.setCancelled(true);
    }

    /*
     * Updates mob health on regen
     */
    @EventHandler
    public void onMobRegainHealth(EntityRegainHealthEvent event) {
        if (!(event.getEntity() instanceof LivingEntity)) return;
        if (event.getEntity() instanceof ArmorStand) return;
        if (event.getEntity() instanceof Player && !RunicCore.getCharacterAPI().getLoadedCharacters().contains(event.getEntity().getUniqueId()))
            return;
        if (event.getEntity().getPassengers().size() == 0) return;
        if (event.getEntity() instanceof Horse) return;
        LivingEntity le = (LivingEntity) event.getEntity();
        updateDisplayName(le, 0);
    }

    /**
     * Updates the healthbar of a mob!
     *
     * @param livingEntity mob to update health for
     * @param damage       from event
     */
    private void updateDisplayName(LivingEntity livingEntity, int damage) {
        String healthBar = ChatColor.YELLOW + "" + "["
                + createHealthDisplay(livingEntity, damage)
                + ChatColor.YELLOW + "]";
        if (MythicMobs.inst().getMobManager().getActiveMob(livingEntity.getUniqueId()).isPresent()) // delay by 1 tick to display correct health
            Bukkit.getScheduler().scheduleSyncDelayedTask(RunicCore.getInstance(), () -> createMythicHealthDisplay(livingEntity));
        else
            livingEntity.setCustomName(healthBar);
    }

    @EventHandler(priority = EventPriority.HIGHEST) // runs LAST
    public void updateHealthBarOnPhysicalDamage(PhysicalDamageEvent event) {
        if (event.isCancelled()) return;
        if (event.getVictim() instanceof Player && !RunicCore.getCharacterAPI().getLoadedCharacters().contains(event.getVictim().getUniqueId()))
            return;
        updateDisplayName(event.getVictim(), event.getAmount());
    }

    @EventHandler(priority = EventPriority.HIGHEST) // runs LAST
    public void updateHealthBarSpellDamage(MagicDamageEvent event) {
        if (event.isCancelled()) return;
        if (event.getVictim() instanceof Player && !RunicCore.getCharacterAPI().getLoadedCharacters().contains(event.getVictim().getUniqueId()))
            return;
        updateDisplayName(event.getVictim(), event.getAmount());
    }
}