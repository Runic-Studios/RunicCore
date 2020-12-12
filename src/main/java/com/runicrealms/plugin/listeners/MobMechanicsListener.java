package com.runicrealms.plugin.listeners;

import com.runicrealms.plugin.events.SpellDamageEvent;
import com.runicrealms.plugin.events.WeaponDamageEvent;
import org.bukkit.ChatColor;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Horse;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;

public final class MobMechanicsListener implements Listener {

    @EventHandler
    public void updateHealthBarSpellDamage(SpellDamageEvent e) {
        if (e.getEntity() instanceof Player) return;
        if (!(e.getEntity() instanceof LivingEntity)) return;
        LivingEntity le = (LivingEntity) e.getEntity();
        updateDisplayName(le, e.getAmount());
    }

    @EventHandler
    public void updateHealthBarWeaponDamage(WeaponDamageEvent e) {
        if (e.getEntity() instanceof Player) return;
        if (!(e.getEntity() instanceof LivingEntity)) return;
        LivingEntity le = (LivingEntity) e.getEntity();
        updateDisplayName(le, e.getAmount());
    }
    /*
     * Updates mob health on regen
     */
    @EventHandler
    public void onMobRegainHealth(EntityRegainHealthEvent e) {
        if (!(e.getEntity() instanceof LivingEntity)) return;
        if (e.getEntity() instanceof ArmorStand) return;
        if (e.getEntity() instanceof Player) return;
        if (e.getEntity().getPassengers().size() == 0) return;
        if (e.getEntity() instanceof Horse) return;
        LivingEntity le = (LivingEntity) e.getEntity();
        updateDisplayName(le, 0);
    }

    private void updateDisplayName(LivingEntity le, int damage) {
        String healthBar = ChatColor.YELLOW + "" + "["
                + createHealthDisplay(le, damage)
                + ChatColor.YELLOW + "]";
        le.setCustomName(healthBar);
    }

    /**
     * Dispalys a health bar that looks like [|||] with colors corresponding to entity's remaining health.
     * @param entity to display health bar for
     * @param damage taken from most recent event
     * @return a string to set the name to
     */
    private static String createHealthDisplay(LivingEntity entity, double damage) {

        double maxHealth = entity.getMaxHealth();
        double currentHealth;
        if (damage == 0) {
            currentHealth = entity.getHealth();
        } else {
            currentHealth = Math.max(entity.getHealth() - damage, 0);
        }
        int healthPercentage = (int) ((currentHealth / maxHealth) * 100.0D);
        int numColorBars = healthPercentage/10;

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
            default :
                firstHalf = ChatColor.DARK_GRAY + "" + "|||||";
                break;

        }

        String healthStr = healthColor + "" + (int) currentHealth;
        return firstHalf + healthStr + secHalf;
    }

    @EventHandler
    public void onBurn(EntityCombustEvent event){
        event.setCancelled(true);
    }
}