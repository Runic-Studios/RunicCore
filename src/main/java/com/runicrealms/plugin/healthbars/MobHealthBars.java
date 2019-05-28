package com.runicrealms.plugin.healthbars;

import org.bukkit.ChatColor;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Consumer;
import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.utilities.InvisStandSpawner;

@SuppressWarnings({"unchecked", "deprecation"})
public final class MobHealthBars implements Listener {

    /**
     * Creates mob healthbars.
     */
    @EventHandler
    public void onMobSpawn(EntitySpawnEvent e) {
        if (e.getEntity() instanceof Player) return;
        if (e.getEntity().hasMetadata("NPC")) return;
        if (e.getEntity() instanceof ArmorStand) return;
        if (!(e.getEntity() instanceof LivingEntity)) return;
        LivingEntity mob = (LivingEntity) e.getEntity();
        setupMob(mob);
    }

    public static void setupMob(LivingEntity mob) {

        // stack two armor stands
        Consumer consumer = new InvisStandSpawner();
        ArmorStand stand = mob.getWorld().spawn(mob.getLocation(), ArmorStand.class, (Consumer<ArmorStand>) (Consumer<?>) consumer);
        stand.setMarker(false);
        stand.setSmall(true);
        stand.setMetadata("healthbar", new FixedMetadataValue(RunicCore.getInstance(), "healthbar"));
        mob.addPassenger(stand);

        // second stand
        ArmorStand stand2 = mob.getWorld().spawn(mob.getLocation(), ArmorStand.class, (Consumer<ArmorStand>) (Consumer<?>) consumer);
        stand2.setMarker(true);
        stand2.setSmall(true);
        stand2.setMetadata("healthbar", new FixedMetadataValue(RunicCore.getInstance(), "healthbar"));
        stand.addPassenger(stand2);

        Entity top = mob.getPassengers().get(mob.getPassengers().size() - 1);
        Entity bottom = top.getPassengers().get(top.getPassengers().size() - 1);

        new BukkitRunnable() {
            @Override
            public void run() {

                String healthBar = ChatColor.GRAY + "" + "["
                        + createHealthDisplay(mob, 0)
                        + ChatColor.GRAY + "]";

                if (mob instanceof Monster || mob instanceof Wolf) {
                    top.setCustomName(ChatColor.RED + mob.getName());
                } else {
                    top.setCustomName(ChatColor.GREEN + mob.getName());
                }

                bottom.setCustomName(healthBar);
            }
        }.runTaskLater(RunicCore.getInstance(), 1);
    }

    /**
     * Updates mob health bar on damage.
     */
    @EventHandler
    public void onMobDamage(EntityDamageByEntityEvent e) {

        if (e.getEntity() instanceof ArmorStand) return;
        if (!(e.getEntity() instanceof LivingEntity)) return;
        if (e.getEntity() instanceof Player) return;
        if (e.getEntity().hasMetadata("NPC")) return;

        LivingEntity mob = (LivingEntity) e.getEntity();

        if (mob.getPassengers().size() == 0) {
            String healthBar = ChatColor.GRAY + "" + "["
                    + createHealthDisplay(mob, e.getDamage())
                    + ChatColor.GRAY + "]";
            mob.setCustomName(healthBar);
            return;
        }

        Entity top = mob.getPassengers().get(mob.getPassengers().size()-1);
        Entity bottom = top.getPassengers().get(top.getPassengers().size()-1);

        String custom = mob.getCustomName();
        if (custom != null) {
            top.setCustomName(custom);
        } else {
            if (mob instanceof Monster || mob instanceof Wolf) {
                top.setCustomName(ChatColor.RED + mob.getName());
            } else {
                top.setCustomName(ChatColor.GREEN + mob.getName());
            }
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                String healthBar = ChatColor.GRAY + "" + "["
                        + createHealthDisplay(mob, 0)
                        + ChatColor.GRAY + "]";
                bottom.setCustomName(healthBar);
            }
        }.runTaskLater(RunicCore.getInstance(), 1);
    }

    /**
     * Updates mob health on regen.
     */
    @EventHandler
    public void onMobRegainHealth(EntityRegainHealthEvent e) {

        if (!(e.getEntity() instanceof LivingEntity)) return;
        if (e.getEntity() instanceof ArmorStand) return;
        if (e.getEntity() instanceof Player) return;
        if (e.getEntity().getPassengers().size() == 0) return;

        LivingEntity le = (LivingEntity) e.getEntity();

        String healthBar = ChatColor.GRAY + "" + "["
                + createHealthDisplay(le, 0)
                + ChatColor.GRAY + "]";

        for (Entity passenger : e.getEntity().getPassengers()) {
            passenger.setCustomName(healthBar);
        }
    }

    /**
     * Remove the armor stand health bars when a mob dies.
     */
    @EventHandler
    public void onMobDeath(EntityDeathEvent e) {

        if (e.getEntity() instanceof ArmorStand) return;
        if (e.getEntity() instanceof Player) return;

        for (Entity passener : e.getEntity().getPassengers()) {
            for (Entity innerPassenger : passener.getPassengers()) {
                innerPassenger.remove();
            }
            passener.remove();
        }
    }

    /**
     * Creates the string health display.
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


}