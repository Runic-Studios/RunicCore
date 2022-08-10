package com.runicrealms.plugin.listeners;

import com.runicrealms.plugin.WeaponType;
import com.runicrealms.plugin.api.RunicCoreAPI;
import com.runicrealms.plugin.events.MobDamageEvent;
import com.runicrealms.plugin.events.RunicDeathEvent;
import com.runicrealms.plugin.model.CharacterField;
import com.runicrealms.plugin.utilities.DamageUtil;
import com.runicrealms.runicitems.RunicItemsAPI;
import com.runicrealms.runicitems.item.RunicItemWeapon;
import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.mobs.ActiveMob;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;

import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Currently, this class manages all melee damage calculators
 *
 * @author Skyfallin_
 */
public class DamageListener implements Listener {

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
        if (!(entity instanceof LivingEntity)) return;
        LivingEntity victim = (LivingEntity) entity;

        // mobs
        if (!(damager instanceof Player)) {
            if (damager instanceof Arrow) {
                damager = (Entity) ((Arrow) damager).getShooter();
            }
            e.setCancelled(true);
            double dmgAmt = e.getDamage();
            if (MythicMobs.inst().getMobManager().isActiveMob(Objects.requireNonNull(damager).getUniqueId())) {
                ActiveMob mm = MythicMobs.inst().getAPIHelper().getMythicMobInstance(damager);
                dmgAmt = mm.getDamage();
            }
            MobDamageEvent event = new MobDamageEvent((int) Math.ceil(dmgAmt), e.getDamager(), victim, false);
            Bukkit.getPluginManager().callEvent(event);
            if (!event.isCancelled())
                DamageUtil.damageEntityMob(Math.ceil(event.getAmount()),
                        (LivingEntity) event.getVictim(), e.getDamager(), event.shouldApplyMechanics());
        }

        // only listen for when a player swings or fires an arrow
        if (damager instanceof Player) {

            Player player = (Player) damager;

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
                DamageUtil.damageEntityWeapon(randomNum, victim, (Player) damager, true, false);
                ((Player) damager).setCooldown(artifact.getType(), 10);
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

    public static boolean matchClass(Player player, boolean sendMessage) {
        ItemStack mainHand = player.getInventory().getItemInMainHand();
        String className = RunicCoreAPI.getPlayerClass(player);
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
        String s = "";
        switch (className) {
            case "Archer":
                s = (ChatColor.RED + "Archers can only wield bows.");
                break;
            case "Cleric":
                s = (ChatColor.RED + "Clerics can only wield maces.");
                break;
            case "Mage":
                s = (ChatColor.RED + "Mages can only wield staves.");
                break;
            case "Rogue":
                s = (ChatColor.RED + "Rogues can only wield swords.");
                break;
            case "Warrior":
                s = (ChatColor.RED + "Warriors can only wield axes.");
                break;
        }
        return s;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onDamage(EntityDamageEvent e) {

        if (e.getCause() == EntityDamageEvent.DamageCause.CUSTOM) return;
        if (e.getDamage() <= 0) return;

        // this event likes to get confused with the event above, so let's just fix that.
        if (e instanceof EntityDamageByEntityEvent) return;

        // only listen if a player is the entity receiving damage AND that player "dies" (hp < 0)
        if (!(e.getEntity() instanceof Player)) return;
        Player pl = (Player) e.getEntity();
        if (!(pl.getHealth() - e.getDamage() <= 0)) return;

        // initialize event variables
        Player victim = (Player) e.getEntity();

        // cancel the event
        e.setCancelled(true);

        // call custom death event
        RunicDeathEvent event = new RunicDeathEvent(victim);
        Bukkit.getPluginManager().callEvent(event);
    }

    public static void applySlainMechanics(Entity damager, Player victim) {

        // if the player was killed by an arrow, set damager to its shooter
        if (damager instanceof Arrow) {
            Arrow arrow = (Arrow) damager;
            if (arrow.getShooter() instanceof Player) {
                damager = (Player) arrow.getShooter();
            }
        }

        // call custom death event
        RunicDeathEvent event = new RunicDeathEvent(victim, damager);
        Bukkit.getPluginManager().callEvent(event);

        // update the scoreboard
        if (Bukkit.getScoreboardManager().getMainScoreboard().getObjective("health") != null) {
            Objective o = Bukkit.getScoreboardManager().getMainScoreboard().getObjective("health");
            Score score = o.getScore(victim);
            score.setScore((int) victim.getHealth());
        }

        // broadcast the death message
        broadcastSlainDeathMessage(damager, victim);
    }

    private static void broadcastSlainDeathMessage(Entity damager, Player victim) {

        String nameVic = victim.getName();

        if (damager instanceof Player) {

            String nameDam = damager.getName();
//            double ratingP1 = RunicCore.getCacheManager().getPlayerCaches().get((Player) damager).getRating();
//            double ratingP2 = RunicCore.getCacheManager().getPlayerCaches().get(victim).getRating();

            // if both players are outlaws, amend the death message to display their rating
            int damagerSlot = RunicCoreAPI.getCharacterSlot(damager.getUniqueId());
            int victimSlot = RunicCoreAPI.getCharacterSlot(victim.getUniqueId());
            boolean damagerIsOutlaw = Boolean.parseBoolean(RunicCoreAPI.getRedisCharacterValue(damager.getUniqueId(), CharacterField.OUTLAW_ENABLED.getField(), damagerSlot));
            boolean victimIsOutlaw = Boolean.parseBoolean(RunicCoreAPI.getRedisCharacterValue(victim.getUniqueId(), CharacterField.OUTLAW_ENABLED.getField(), victimSlot));
            if (damagerIsOutlaw && victimIsOutlaw) {
                nameDam = ChatColor.WHITE + nameDam; // ChatColor.RED + "[" + (int) ratingP1 + "] " +
                nameVic = ChatColor.WHITE + nameVic; // ChatColor.RED + "[" + (int) ratingP2 + "] " +
                Bukkit.getServer().broadcastMessage(ChatColor.WHITE + nameVic + " was slain by " + nameDam);
            }
        }
    }

    /**
     * A generic death message for general purposes
     *
     * @param victim who died
     */
    public static void broadcastDeathMessage(Player victim) {
        String nameVic = victim.getName();
        Bukkit.getServer().broadcastMessage(ChatColor.RED + nameVic + " died!");
    }
}
