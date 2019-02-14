package us.fortherealm.plugin.listeners;

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
import us.fortherealm.plugin.Main;
import us.fortherealm.plugin.attributes.AttributeUtil;
import us.fortherealm.plugin.item.GearScanner;
import us.fortherealm.plugin.outlaw.OutlawManager;
import us.fortherealm.plugin.utilities.HologramUtil;
import us.fortherealm.plugin.enums.WeaponEnum;

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

    private Plugin plugin = Main.getInstance();
    private OutlawManager outlawEvent = new OutlawManager();

    @EventHandler(priority = EventPriority.NORMAL)
    public void onDamage(EntityDamageByEntityEvent e) {

        Entity damager = e.getDamager();
        Entity entity = e.getEntity();

        // only listen for damageable entities
        if (!(entity instanceof Damageable)) return;
        Damageable victim = (Damageable) entity;

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

            // grab player's armor, offhand (check for gem bonuses)
            ArrayList<ItemStack> armorAndOffhand = GearScanner.armorAndOffHand(pl);

            // calculate the player's total damage boost
            for (ItemStack item : armorAndOffhand) {
                int damageBoost = (int) AttributeUtil.getCustomDouble(item, "custom.attackDamage");
                maxDamage = maxDamage + damageBoost;
            }

            // don't fire attack if they're sneaking, since they're casting a spell
            if (((Player) damager).isSneaking() && slot == 0) {
                e.setCancelled(true);
                return;
            }

            // ignore bows, staves, null check, ignore items without the attribute, check for cooldown
            if ((!(artifactType.equals(WeaponEnum.BOW)|| artifactType.equals(WeaponEnum.STAFF)))) {

                if (artifactType.equals(WeaponEnum.HAND)) {
                    damage = 1;
                    maxDamage = 1;
                }

                if (((Player) damager).getCooldown(artifact.getType()) <= 0) {
                    int randomNum = ThreadLocalRandom.current().nextInt(damage, maxDamage + 1);
                    e.setDamage(randomNum);
                } else {
                    e.setCancelled(true);
                    return;
                }
            }

            // display damage indicator
            HologramUtil.createDamageHologram(((Player) damager),
                    victim.getLocation().add(0,1.5,0), e.getDamage());
        }

        // only listen if a player is the entity receiving damage, to check for death mechanics
        if (!(victim instanceof Player)) return;

        // only listen for if the player were to "die"
        if (!((victim.getHealth() - e.getFinalDamage() < 1))) return;

        applySlainMechanics(e, e.getDamager(), ((Player) victim));
    }

    @EventHandler
    public void onDamage(EntityDamageEvent e) {

        // this event likes to get confused with the event above, so let's just fix that.
        if (e instanceof EntityDamageByEntityEvent) {
            return;
        }

        // only listen if a player is the entity receiving damage AND that player "dies" (hp < 0)
        if (!(e.getEntity() instanceof Player && ((Player) e.getEntity()).getHealth() - e.getDamage() < 1)) {
            return;
        }

        // initialize event variables
        Player victim = (Player) e.getEntity();
        Location respawnLocation = new Location(victim.getWorld(), -732, 34, 111);

        // apply new death mechanics
        applyDeathMechanics(e, victim, respawnLocation);

        // broadcast the death message
        broadcastDeathMessage(victim);
        victim.sendMessage(ChatColor.RED + "You have been slain!");

        // update the scoreboard
        if (Bukkit.getScoreboardManager().getMainScoreboard().getObjective("health") != null) {
            Objective o = Bukkit.getScoreboardManager().getMainScoreboard().getObjective("health");
            Score score = o.getScore(victim);
            score.setScore((int) victim.getHealth());
        }
    }

    private void applyDeathMechanics(EntityDamageEvent e, Player victim, Location respawnLocation) {

        // cancel the event
        e.setCancelled(true);

        // if player is in combat, remove them
        if (!Main.getCombatManager().getPlayersInCombat().containsKey(victim.getUniqueId())) {
            Main.getCombatManager().getPlayersInCombat().remove(victim.getUniqueId());
            victim.sendMessage(ChatColor.GREEN + "You have left combat!");
        }

        victim.setHealth(victim.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
        victim.setFoodLevel(20);
        // set their current mana to 0
        Main.getManaManager().getCurrentManaList().put(victim.getUniqueId(), 0);
        Main.getScoreboardHandler().updateSideInfo(victim);
        victim.getWorld().playSound(victim.getLocation(), Sound.ENTITY_PLAYER_DEATH, 1.0f, 1);
        victim.getWorld().playSound(victim.getLocation(), Sound.ENTITY_WITHER_DEATH, 0.5f, 1);
        victim.getWorld().spawnParticle(Particle.REDSTONE, victim.getEyeLocation(), 25, 0.5f, 0.5f, 0.5f,
                new Particle.DustOptions(Color.RED, 3));
        victim.teleport(respawnLocation);
        victim.playSound(victim.getLocation(), Sound.ENTITY_PLAYER_DEATH, 1.0f, 1);
        victim.playSound(victim.getLocation(), Sound.ENTITY_WITHER_DEATH, 0.5f, 1);
        victim.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 80, 0));
    }

    private void applySlainMechanics(EntityDamageEvent e, Entity damager, Player victim) {

        // if the player was killed by an arrow, set damager to its shooter
        if (damager instanceof Arrow) {
            Arrow arrow = (Arrow) damager;
            if (arrow.getShooter() instanceof Player) {
                damager = (Player) arrow.getShooter();
            }
        }

        // todo: change this to the killed player's hearthstone location
        Location respawnLocation = new Location(victim.getWorld(), -732, 34, 111);

        // apply new death mechanics
        applyDeathMechanics(e, victim, respawnLocation);

        // update the scoreboard
        if (Bukkit.getScoreboardManager().getMainScoreboard().getObjective("health") != null) {
          Objective o = Bukkit.getScoreboardManager().getMainScoreboard().getObjective("health");
            Score score = o.getScore(victim);
            score.setScore((int) victim.getHealth());
        }

        // broadcast the death message
        broadcastSlainDeathMessage(damager, victim);
        victim.sendMessage(ChatColor.RED + "You have been slain!");

        // apply outlaw mechanics if the player is an outlaw AND the killer is an outlaw
        if (damager instanceof Player) {
            outlawEvent.onKill((Player) damager, victim);
        }
    }

    private void broadcastSlainDeathMessage(Entity damager, Player victim) {

        String nameVic = plugin.getConfig().get(victim.getUniqueId() + ".info.name").toString();

        if (damager instanceof Player) {

            String nameDam = plugin.getConfig().get(damager.getUniqueId() + ".info.name").toString();
            UUID p1 = damager.getUniqueId();
            UUID p2 = victim.getUniqueId();
            double ratingP1 = outlawEvent.getRating(p1);
            double ratingP2 = outlawEvent.getRating(p2);

            // if both players are outlaws, amend the death message to display their rating
            if (plugin.getConfig().getBoolean(p1 + ".outlaw.enabled", true)
                    && plugin.getConfig().getBoolean(p2 + ".outlaw.enabled", true)) {
                nameDam = ChatColor.RED + "[" + (int) ratingP1 + "] " + ChatColor.WHITE + nameDam;
                nameVic = ChatColor.RED + "[" + (int) ratingP2 + "] " + ChatColor.WHITE + nameVic;
            }

            // display death message
            Bukkit.getServer().broadcastMessage(ChatColor.WHITE + nameVic + " was slain by " + nameDam);
        } else {
            Bukkit.getServer().broadcastMessage(ChatColor.WHITE + nameVic + " was slain by " + damager.getName());
        }
    }

    private void broadcastDeathMessage(Player victim) {

        // initialize method variables
        String nameVic = plugin.getConfig().get(victim.getUniqueId() + ".info.name").toString();

        // display death message
        Bukkit.getServer().broadcastMessage(ChatColor.WHITE + nameVic + " died!");
    }
}
