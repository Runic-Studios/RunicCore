package com.runicrealms.plugin.listeners;

import com.runicrealms.plugin.enums.WeaponEnum;
import com.runicrealms.plugin.events.MobDamageEvent;
import com.runicrealms.plugin.events.RunicDeathEvent;
import com.runicrealms.plugin.events.WeaponDamageEvent;
import com.runicrealms.plugin.item.GearScanner;
import com.runicrealms.plugin.item.hearthstone.HearthstoneListener;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.attributes.AttributeUtil;
import com.runicrealms.plugin.outlaw.OutlawManager;

import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * This class does a lot. Might be worth splitting up.
 * Currently, it manages all melee damage calculators (including gemstones).
 * It also applies all of our death mechanics, melee cooldown mechanics, what have you.
 * @author Skyfallin_
 */
@SuppressWarnings("deprecation")
public class DamageListener implements Listener {

    private Plugin plugin = RunicCore.getInstance();
    private OutlawManager outlawEvent = new OutlawManager();

    @EventHandler(priority = EventPriority.LOWEST)
    public void onDamage(EntityDamageByEntityEvent e) {

        if (e.getCause() == EntityDamageByEntityEvent.DamageCause.CUSTOM) return;
        if (e.getDamager() instanceof SmallFireball) return;
        if (e.getDamage() <= 0) return;

        Entity damager = e.getDamager();
        Entity entity = e.getEntity();

        // bugfix for armor stands
        if (e.getEntity() instanceof ArmorStand && e.getEntity().getVehicle() != null) {
            entity = e.getEntity().getVehicle();
        }

        // only listen for damageable entities
        if (!(entity instanceof LivingEntity)) return;
        LivingEntity victim = (LivingEntity) entity;

        if (damager instanceof Monster) {
            e.setCancelled(true);
            MobDamageEvent event = new MobDamageEvent((int) Math.ceil(e.getDamage()), e.getDamager(), victim);
            Bukkit.getPluginManager().callEvent(event);
            if (event.isCancelled()) {
                return;
            } else {
                DamageUtil.damageEntityMob(Math.ceil(event.getAmount()), victim, (LivingEntity) damager);
            }
        }

        // only listen for when a player swings or fires an arrow
        if (damager instanceof Player) {

            Player pl = (Player) damager;

            ItemStack artifact = ((Player) damager).getInventory().getItemInMainHand();
            WeaponEnum artifactType = WeaponEnum.matchType(artifact);
            int damage = (int) AttributeUtil.getCustomDouble(artifact, "custom.minDamage");
            int maxDamage = (int) AttributeUtil.getCustomDouble(artifact, "custom.maxDamage");
            int slot = ((Player) damager).getInventory().getHeldItemSlot();

            // --------------------
            // for punching 'n stuff
            if (damage == 0) {
                damage = 1;
            }
            if (maxDamage == 0) {
                maxDamage = 1;
            }
            // -------------------

            // don't fire attack if they're sneaking, since they're casting a spell
            if (((Player) damager).isSneaking() && slot == 0) {
                e.setCancelled(true);
                return;
            }

            // check for cooldown
            if (artifactType.equals(WeaponEnum.HAND)
                    || artifactType.equals(WeaponEnum.STAFF)
                    || artifactType.equals(WeaponEnum.BOW)) {
                damage = 1;
                maxDamage = 1;
            }

            if (((Player) damager).getCooldown(artifact.getType()) <= 0) {
                e.setCancelled(true);
                int randomNum = ThreadLocalRandom.current().nextInt(damage, maxDamage + 1);

                // call our successful hit event, ensure that the item is the artifact
                if (slot != 0) return;

                // outlaw check
                if (victim instanceof Player && (!OutlawManager.isOutlaw(((Player) victim)) || !OutlawManager.isOutlaw(pl))) {
                    return;
                }

                DamageUtil.damageEntityWeapon(randomNum, victim, (Player) damager, false);

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
        //Location respawnLocation = new Location(victim.getWorld(), -732, 34, 111);

        // cancel the event
        e.setCancelled(true);

        // apply new death mechanics
        applyDeathMechanics(victim);
    }

    public void applyDeathMechanics(Player victim) {

        // call runic death event
        RunicDeathEvent event = new RunicDeathEvent(victim);
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            return;
        }

        // broadcast the death message
        broadcastDeathMessage(victim);
        victim.sendMessage(ChatColor.RED + "You have died!");

        // update the scoreboard
        if (Bukkit.getScoreboardManager().getMainScoreboard().getObjective("health") != null) {
            Objective o = Bukkit.getScoreboardManager().getMainScoreboard().getObjective("health");
            Score score = o.getScore(victim);
            score.setScore((int) victim.getHealth());
        }

        // if player is in combat, remove them
        if (RunicCore.getCombatManager().getPlayersInCombat().containsKey(victim.getUniqueId())) {
            RunicCore.getCombatManager().getPlayersInCombat().remove(victim.getUniqueId());
            victim.sendMessage(ChatColor.GREEN + "You have left combat!");
        }

        victim.setHealth(victim.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
        victim.setFoodLevel(20);
        // set their current mana to 0
        RunicCore.getManaManager().getCurrentManaList().put(victim.getUniqueId(), 0);
        RunicCore.getScoreboardHandler().updateSideInfo(victim);
        victim.getWorld().playSound(victim.getLocation(), Sound.ENTITY_PLAYER_DEATH, 1.0f, 1);
        victim.getWorld().playSound(victim.getLocation(), Sound.ENTITY_WITHER_DEATH, 0.25f, 1);
        victim.getWorld().spawnParticle(Particle.REDSTONE, victim.getEyeLocation(), 25, 0.5f, 0.5f, 0.5f,
                new Particle.DustOptions(Color.RED, 3));
        //victim.teleport(respawnLocation);
        HearthstoneListener.teleportToLocation(victim);
        victim.playSound(victim.getLocation(), Sound.ENTITY_PLAYER_DEATH, 1.0f, 1);
        victim.playSound(victim.getLocation(), Sound.ENTITY_WITHER_DEATH, 0.25f, 1);
        victim.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 80, 0));
    }

    public void applySlainMechanics(Entity damager, Player victim) {

        // if the player was killed by an arrow, set damager to its shooter
        if (damager instanceof Arrow) {
            Arrow arrow = (Arrow) damager;
            if (arrow.getShooter() instanceof Player) {
                damager = (Player) arrow.getShooter();
            }
        }

        // todo: change this to the killed player's hearthstone location
        //Location respawnLocation = new Location(victim.getWorld(), -732, 34, 111);

        // apply new death mechanics
        applyDeathMechanics(victim);

        // update the scoreboard
        if (Bukkit.getScoreboardManager().getMainScoreboard().getObjective("health") != null) {
          Objective o = Bukkit.getScoreboardManager().getMainScoreboard().getObjective("health");
            Score score = o.getScore(victim);
            score.setScore((int) victim.getHealth());
        }

        // broadcast the death message
        broadcastSlainDeathMessage(damager, victim);

        // apply outlaw mechanics if the player is an outlaw AND the killer is an outlaw
        if (damager instanceof Player && OutlawManager.isOutlaw((Player) damager) && OutlawManager.isOutlaw(victim)) {
            outlawEvent.onKill((Player) damager, victim);
        }
    }

    private void broadcastSlainDeathMessage(Entity damager, Player victim) {

        //String nameVic = plugin.getConfig().get(victim.getUniqueId() + ".info.name").toString();
        String nameVic = victim.getName();

        if (damager instanceof Player) {

            //String nameDam = plugin.getConfig().get(damager.getUniqueId() + ".info.name").toString();
            String nameDam = damager.getName();
            UUID p1 = damager.getUniqueId();
            UUID p2 = victim.getUniqueId();
            double ratingP1 = outlawEvent.getRating(p1);
            double ratingP2 = outlawEvent.getRating(p2);

            // if both players are outlaws, amend the death message to display their rating
            if (plugin.getConfig().getBoolean(p1 + ".outlaw.enabled", true)
                    && plugin.getConfig().getBoolean(p2 + ".outlaw.enabled", true)) {
                nameDam = ChatColor.RED + "[" + (int) ratingP1 + "] " + ChatColor.WHITE + nameDam;
                nameVic = ChatColor.RED + "[" + (int) ratingP2 + "] " + ChatColor.WHITE + nameVic;
                Bukkit.getServer().broadcastMessage(ChatColor.WHITE + nameVic + " was slain by " + nameDam);
            }
        }
    }

    private void broadcastDeathMessage(Player victim) {
        String nameVic = victim.getName();
        // display death message
        Bukkit.getServer().broadcastMessage(ChatColor.RED + nameVic + " died!");
    }
}
