package com.runicrealms.plugin.listeners;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.attributes.AttributeUtil;
import com.runicrealms.plugin.enums.WeaponEnum;
import com.runicrealms.plugin.events.MobDamageEvent;
import com.runicrealms.plugin.events.RunicDeathEvent;
import com.runicrealms.plugin.item.hearthstone.HearthstoneListener;
import com.runicrealms.plugin.utilities.DamageUtil;
import com.runicrealms.runicitems.RunicItemsAPI;
import com.runicrealms.runicitems.item.RunicItemWeapon;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.mobs.ActiveMob;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/**
 * This class does a lot. Might be worth splitting up.
 * Currently, it manages all melee damage calculators (including gemstones).
 * It also applies all of our death mechanics, melee cooldown mechanics, what have you.
 *
 * @author Skyfallin_
 */
@SuppressWarnings("deprecation")
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

            Player pl = (Player) damager;

            ItemStack artifact = ((Player) damager).getInventory().getItemInMainHand();
            WeaponEnum artifactType = WeaponEnum.matchType(artifact);
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

            if (reqLv > RunicCore.getCacheManager().getPlayerCaches().get(pl).getClassLevel()) {
                pl.playSound(pl.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 0.5f, 1.0f);
                pl.sendMessage(ChatColor.RED + "Your level is too low to wield this!");
                e.setCancelled(true);
                return;
            }

            // check for cooldown
            if (artifactType.equals(WeaponEnum.NONE)
                    //|| artifactType.equals(WeaponEnum.STAFF)
                    || artifactType.equals(WeaponEnum.BOW)) {
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
                if (!matchClass(pl, true))
                    return;

                // ---------------------------
                // successful damage
                if (((Player) damager).getCooldown(artifact.getType()) != 0)
                    return;
                DamageUtil.damageEntityWeapon(randomNum, victim, (Player) damager, true, false, true);
                ((Player) damager).setCooldown(artifact.getType(), 15);
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

    public static boolean matchClass(Player pl, boolean sendMessage) {
        ItemStack mainHand = pl.getInventory().getItemInMainHand();
        String className = RunicCore.getCacheManager().getPlayerCaches().get(pl).getClassName();
        if (className == null) return false;
        switch (mainHand.getType()) {
            case BOW:
                if (!className.equals("Archer")) {
                    if (sendMessage) {
                        pl.playSound(pl.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5f, 1);
                        pl.sendMessage(weaponMessage(className));
                    }
                    return false;
                } else {
                    return true;
                }
            case WOODEN_SHOVEL:
                if (!className.equals("Cleric")) {
                    if (sendMessage) {
                        pl.playSound(pl.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5f, 1);
                        pl.sendMessage(weaponMessage(className));
                    }
                    return false;
                } else {
                    return true;
                }
            case WOODEN_HOE:
                if (!className.equals("Mage")) {
                    if (sendMessage) {
                        pl.playSound(pl.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5f, 1);
                        pl.sendMessage(weaponMessage(className));
                    }
                    return false;
                } else {
                    return true;
                }
            case WOODEN_SWORD:
                if (!className.equals("Rogue")) {
                    if (sendMessage) {
                        pl.playSound(pl.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5f, 1);
                        pl.sendMessage(weaponMessage(className));
                    }
                    return false;
                } else {
                    return true;
                }
            case WOODEN_AXE:
                if (!className.equals("Warrior")) {
                    if (sendMessage) {
                        pl.playSound(pl.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5f, 1);
                        pl.sendMessage(weaponMessage(className));
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

        // apply new death mechanics
        applyDeathMechanics(null, victim);
    }

    /**
     * This method applies custom mechanics when a player would die
     *
     * @param victim who died
     * @param killer optional mob/player responsible for death
     */
    public static void applyDeathMechanics(Player victim, Entity... killer) {

        // call runic death event
        RunicDeathEvent event = new RunicDeathEvent(victim, killer);
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            return;
        }

        // broadcast the death message
        broadcastDeathMessage(victim);

        // update the scoreboard
        if (Bukkit.getScoreboardManager().getMainScoreboard().getObjective("health") != null) {
            Objective o = Bukkit.getScoreboardManager().getMainScoreboard().getObjective("health");
            Score score = o.getScore(victim);
            score.setScore((int) victim.getHealth());
        }

        // if player is in combat, remove them
        if (RunicCore.getCombatManager().getPlayersInCombat().containsKey(victim.getUniqueId())) {
            RunicCore.getCombatManager().removePlayer(victim.getUniqueId());
        }

        victim.setHealth(victim.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
        victim.setFoodLevel(20);
        // set their current mana to max
        int maxMana = RunicCore.getCacheManager().getPlayerCaches().get(victim).getMaxMana();
        RunicCore.getRegenManager().getCurrentManaList().put(victim.getUniqueId(), maxMana);
        victim.getWorld().playSound(victim.getLocation(), Sound.ENTITY_PLAYER_DEATH, 1.0f, 1);
        victim.getWorld().playSound(victim.getLocation(), Sound.ENTITY_WITHER_DEATH, 0.25f, 1);
        victim.getWorld().spawnParticle(Particle.REDSTONE, victim.getEyeLocation(), 25, 0.5f, 0.5f, 0.5f,
                new Particle.DustOptions(Color.RED, 3));
        // teleport them to their hearthstone location, or the front of the dungeon
        tryDropItems(victim);
        String isDungeon = checkForDungeon(victim);
        if (isDungeon.equals("")) { // no dungeon
            victim.teleport(HearthstoneListener.getHearthstoneLocation(victim));
            victim.sendMessage(ChatColor.RED + "You have died! Your armor and hotbar have been returned.");
        }
        victim.playSound(victim.getLocation(), Sound.ENTITY_PLAYER_DEATH, 1.0f, 1);
        victim.playSound(victim.getLocation(), Sound.ENTITY_WITHER_DEATH, 0.25f, 1);
        victim.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 80, 0));
    }

    public static void applySlainMechanics(Entity damager, Player victim) {

        // if the player was killed by an arrow, set damager to its shooter
        if (damager instanceof Arrow) {
            Arrow arrow = (Arrow) damager;
            if (arrow.getShooter() instanceof Player) {
                damager = (Player) arrow.getShooter();
            }
        }

        // apply new death mechanics
        applyDeathMechanics(victim, damager);

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
            double ratingP1 = RunicCore.getCacheManager().getPlayerCaches().get((Player) damager).getRating();
            double ratingP2 = RunicCore.getCacheManager().getPlayerCaches().get(victim).getRating();

            // if both players are outlaws, amend the death message to display their rating
            if (RunicCore.getCacheManager().getPlayerCaches().get(damager).getIsOutlaw()
                    && RunicCore.getCacheManager().getPlayerCaches().get(victim).getIsOutlaw()) {
                nameDam = ChatColor.RED + "[" + (int) ratingP1 + "] " + ChatColor.WHITE + nameDam;
                nameVic = ChatColor.RED + "[" + (int) ratingP2 + "] " + ChatColor.WHITE + nameVic;
                Bukkit.getServer().broadcastMessage(ChatColor.WHITE + nameVic + " was slain by " + nameDam);
            }
        }
    }

    private static void broadcastDeathMessage(Player victim) {
        String nameVic = victim.getName();
        // display death message
        Bukkit.getServer().broadcastMessage(ChatColor.RED + nameVic + " died!");
    }

    /**
     * This method controls the dropping of items. It rolls a dice for each item in the player's inventory, and
     * it skips soulbound items. It removes protections from protected items.
     *
     * @param pl player whose items may drop
     */
    private static void tryDropItems(Player pl) {

        // don't drop items in dungeon world.
        if (pl.getWorld().getName().toLowerCase().equals("dungeons"))
            return;

        for (int i = 9; i < 36; i++) {
            ItemStack is = pl.getInventory().getItem(i);
            if (is == null)
                continue;
            if (AttributeUtil.getCustomString(is, "soulbound").equals("true"))
                continue;
            if (is.getItemMeta() != null
                    && is.getItemMeta().getLore() != null
                    && foundQuestItem(is.getItemMeta().getLore()))
                continue;

            pl.getInventory().remove(is);
            pl.getWorld().dropItem(pl.getLocation(), is);
        }
    }

    private static boolean foundQuestItem(List<String> lore) {
        for (String s : lore) {
            if (ChatColor.stripColor(s).contains("Quest Item"))
                return true;
        }
        return false;
    }

    private static String checkForDungeon(Player pl) {

        // grab all regions the player is standing in
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionQuery query = container.createQuery();
        ApplicableRegionSet set = query.getApplicableRegions(BukkitAdapter.adapt(pl.getLocation()));
        Set<ProtectedRegion> regions = set.getRegions();

        if (regions == null) return "";

        // check the region for the keyword 'mine'
        // ignore the rest of this event if the player cannot mine
        for (ProtectedRegion region : regions) {
            if (region.getId().contains("sebathscave")) {
                Location caveEntrance = new Location(Bukkit.getWorld("dungeons"), -1874.5, 177, -522.5, 90, 0);
                pl.teleport(caveEntrance);
                return "sebathscave";
            } else if (region.getId().contains("crystalcavern")) {
                // todo: crystal cavern location
                Location cavernEntrance = new Location(Bukkit.getWorld("dungeons"), -534.5, 120, -177.5, 180, 0);
                pl.teleport(cavernEntrance);
                return "crystalcavern";
            } else if (region.getId().contains("odinskeep")) {
                Location keepEntrance = new Location(Bukkit.getWorld("dungeons"), -534.5, 120, -177.5, 180, 0);
                pl.teleport(keepEntrance);
                return "odinskeep";
            } else if (region.getId().contains("library")) {
                Location libraryEntrance = new Location(Bukkit.getWorld("dungeons"), -23.5, 31, 11.5, 270, 0);
                pl.teleport(libraryEntrance);
                return "library";
            } else if (region.getId().contains("crypts")) {
                Location cryptsEntrance = new Location(Bukkit.getWorld("dungeons"), 298.5, 87, 6.5, 0, 0);
                pl.teleport(cryptsEntrance);
                return "crypts";
            } else if (region.getId().contains("fortress")) {
                Location fortressEntrace = new Location(Bukkit.getWorld("dungeons"), 32.5, 73, 87.5, 0, 0);
                if (region.getId().contains("d3_parkour")) {
                    fortressEntrace = new Location(Bukkit.getWorld("dungeons"), 32.5, 67, 379.5, 0, 0);
                } else if (region.getId().contains("d3_alkyr")) {
                    fortressEntrace = new Location(Bukkit.getWorld("dungeons"), -9.5, 67, 503.5, 0, 0);
                } else if (region.getId().contains("eldrid")) {
                    fortressEntrace = new Location(Bukkit.getWorld("dungeons"), -9.5, 67, 623.5, 0, 0);
                }
                pl.teleport(fortressEntrace);
                return "fortress";
            }
        }

        return "";
    }
}
