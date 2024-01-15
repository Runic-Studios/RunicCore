package com.runicrealms.plugin.listeners;

import com.runicrealms.plugin.WeaponType;
import com.runicrealms.plugin.api.event.BasicAttackEvent;
import com.runicrealms.plugin.events.MobDamageEvent;
import com.runicrealms.plugin.events.RunicDeathEvent;
import com.runicrealms.plugin.rdb.RunicDatabase;
import com.runicrealms.plugin.runicitems.RunicItemsAPI;
import com.runicrealms.plugin.runicitems.item.RunicItemWeapon;
import com.runicrealms.plugin.utilities.DamageUtil;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.mobs.ActiveMob;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Firework;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.SmallFireball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Currently, this class manages all melee damage calculators
 *
 * @author Skyfallin_
 */
public class DamageListener implements Listener {

    public static boolean matchClass(Player player, boolean sendMessage) {
        ItemStack mainHand = player.getInventory().getItemInMainHand();
        String className = RunicDatabase.getAPI().getCharacterAPI().getPlayerClass(player);
        if (className == null) return false;
        switch (mainHand.getType()) {
            case BOW:
                if (!className.equals("Archer")) {
                    if (sendMessage) {
                        player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5f, 1);
                        player.sendMessage(weaponMessage(className));
                    }
                    return false;
                } else {
                    return true;
                }
            case WOODEN_SHOVEL:
                if (!className.equals("Cleric")) {
                    if (sendMessage) {
                        player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5f, 1);
                        player.sendMessage(weaponMessage(className));
                    }
                    return false;
                } else {
                    return true;
                }
            case WOODEN_HOE:
                if (!className.equals("Mage")) {
                    if (sendMessage) {
                        player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5f, 1);
                        player.sendMessage(weaponMessage(className));
                    }
                    return false;
                } else {
                    return true;
                }
            case WOODEN_SWORD:
                if (!className.equals("Rogue")) {
                    if (sendMessage) {
                        player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5f, 1);
                        player.sendMessage(weaponMessage(className));
                    }
                    return false;
                } else {
                    return true;
                }
            case WOODEN_AXE:
                if (!className.equals("Warrior")) {
                    if (sendMessage) {
                        player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5f, 1);
                        player.sendMessage(weaponMessage(className));
                    }
                    return false;
                } else {
                    return true;
                }
            default:
                return true;
        }
    }

    private static String weaponMessage(String className) {
        return switch (className) {
            case "Archer" -> (ChatColor.RED + "Archers can only wield bows.");
            case "Cleric" -> (ChatColor.RED + "Clerics can only wield maces.");
            case "Mage" -> (ChatColor.RED + "Mages can only wield staves.");
            case "Rogue" -> (ChatColor.RED + "Rogues can only wield swords.");
            case "Warrior" -> (ChatColor.RED + "Warriors can only wield axes.");
            default -> "";
        };
    }

    public static void applySlainMechanics(Entity damager, Player victim) {
        // if the player was killed by an arrow, set damager to its shooter
        if (damager instanceof Arrow arrow) {
            if (arrow.getShooter() instanceof Player) {
                damager = (Player) arrow.getShooter();
            }
        }

        // Call custom death event
        RunicDeathEvent event = new RunicDeathEvent(victim, victim.getLocation(), damager);
        Bukkit.getPluginManager().callEvent(event);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onDamage(EntityDamageByEntityEvent e) {
        if (e.getCause() == EntityDamageByEntityEvent.DamageCause.CUSTOM) return;
        if (e.getDamager() instanceof SmallFireball) return;
        if (e.getDamager() instanceof Arrow) return;
        if (e.getDamage() <= 0) return;

        Entity damager = e.getDamager();
        if (damager instanceof Arrow && damager.getCustomName() == null) return;
        if (damager instanceof Arrow && ((Arrow) damager).getShooter() != null) {
            damager = (Entity) ((Arrow) damager).getShooter();
        }

        Entity entity = e.getEntity();

        // bugfix for armor stands
        if (e.getEntity() instanceof ArmorStand && e.getEntity().getVehicle() != null) {
            entity = e.getEntity().getVehicle();
        }

        // only listen for damageable entities
        if (!(entity instanceof LivingEntity victim)) return;

        // Fix for fireworks
        if (damager instanceof Firework) {
            e.setCancelled(true);
            return;
        }

        // mobs
        if (!(damager instanceof Player)) {
            if (damager instanceof Arrow) {
                damager = (Entity) ((Arrow) damager).getShooter();
            }
            e.setCancelled(true);
            double dmgAmt = e.getDamage();
            if (MythicBukkit.inst().getMobManager().isActiveMob(Objects.requireNonNull(damager).getUniqueId())) {
                ActiveMob mm = MythicBukkit.inst().getAPIHelper().getMythicMobInstance(damager);
                dmgAmt = mm.getDamage();
            }
            MobDamageEvent event = new MobDamageEvent((int) Math.ceil(dmgAmt), e.getDamager(), victim, false);
            Bukkit.getPluginManager().callEvent(event);
            if (!event.isCancelled())
                DamageUtil.damageEntityMob(Math.ceil(event.getAmount()), event.getVictim(), e.getDamager(), event.shouldApplyMechanics());
        }

        // only listen for when a player swings or fires an arrow
        if (damager instanceof Player player) {

            ItemStack artifact = ((Player) damager).getInventory().getItemInMainHand();
            WeaponType artifactType = WeaponType.matchType(artifact);
            int damage;
            int maxDamage;
            int reqLv;
            try {
                RunicItemWeapon runicItemWeapon = (RunicItemWeapon) RunicItemsAPI.getRunicItemFromItemStack(artifact);
                damage = runicItemWeapon.getWeaponDamage().getMin();
                maxDamage = runicItemWeapon.getWeaponDamage().getMax();
                reqLv = runicItemWeapon.getLevel();
            } catch (Exception ex) {
                damage = 1;
                maxDamage = 1;
                reqLv = 0;
            }

            // --------------------
            // for punching 'n stuff
            if (damage == 0)
                damage = 1;
            if (maxDamage == 0)
                maxDamage = 1;
            // -------------------

            if (reqLv > player.getLevel()) {
                player.playSound(player.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 0.5f, 1.0f);
                player.sendMessage(ChatColor.RED + "Your level is too low to wield this!");
                e.setCancelled(true);
                return;
            }

            // check for cooldown
            if (artifactType.equals(WeaponType.NONE)
                    //|| artifactType.equals(WeaponEnum.STAFF)
                    || artifactType.equals(WeaponType.BOW)) {
                damage = 1;
                maxDamage = 1;
            }

            if (((Player) damager).getCooldown(artifact.getType()) <= 0) {
                e.setCancelled(true);
                int randomNum = ThreadLocalRandom.current().nextInt(damage, maxDamage + 1);

                // outlaw check
                if (victim.hasMetadata("NPC"))
                    return;

                // ensure correct class/weapon combo (archers and bows, etc)
                if (!matchClass(player, true))
                    return;

                // ---------------------------
                // successful damage
                if (((Player) damager).getCooldown(artifact.getType()) != 0)
                    return;
                BasicAttackEvent basicAttackEvent = new BasicAttackEvent(
                        player,
                        artifact.getType(),
                        BasicAttackEvent.BASE_MELEE_COOLDOWN,
                        BasicAttackEvent.BASE_MELEE_COOLDOWN,
                        damage,
                        maxDamage);
                Bukkit.getPluginManager().callEvent(basicAttackEvent);
                if (!basicAttackEvent.isCancelled()) {
                    DamageUtil.damageEntityPhysical(randomNum, victim, (Player) damager, true, false);

                }
                // ---------------------------

            } else {
                e.setCancelled(true);
                return;
            }
        }

        // only listen if a player is the entity receiving damage, to check for death mechanics
        if (!(victim instanceof Player)) return;

        // only listen for if the player were to "die"
        if (!((victim.getHealth() - e.getFinalDamage() <= 0))) return;

        applySlainMechanics(e.getDamager(), ((Player) victim));
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onDamage(EntityDamageEvent event) {
        if (event.getCause() == EntityDamageEvent.DamageCause.CUSTOM) return;
        if (event.getDamage() <= 0) return;

        // This event likes to get confused with the event above, so let's just fix that.
        if (event instanceof EntityDamageByEntityEvent) return;

        // Only listen if a player is the entity receiving damage AND that player "dies" (hp < 0)
        if (!(event.getEntity() instanceof Player victim)) return;
        if (!(victim.getHealth() - event.getDamage() <= 0)) return;

        // Cancel the event
        event.setCancelled(true);

        // Call custom death event
        Bukkit.getPluginManager().callEvent(new RunicDeathEvent(victim, victim.getLocation()));
    }
}
