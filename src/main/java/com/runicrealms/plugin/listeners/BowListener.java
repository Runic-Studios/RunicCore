package com.runicrealms.plugin.listeners;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.WeaponType;
import com.runicrealms.plugin.api.event.BasicAttackEvent;
import com.runicrealms.plugin.api.event.RunicBowEvent;
import com.runicrealms.plugin.events.MobDamageEvent;
import com.runicrealms.plugin.utilities.DamageUtil;
import com.runicrealms.runicitems.RunicItemsAPI;
import com.runicrealms.runicitems.item.RunicItemWeapon;
import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.mobs.ActiveMob;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupArrowEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

public class BowListener implements Listener {

    private static final int ARROW_SPEED_MULTIPLIER = 3;
    private static final int BOW_GLOBAL_COOLDOWN = 15; // ticks

    /**
     * Removes any arrows stuck in bodies
     */
    @EventHandler(priority = EventPriority.HIGH) // late
    public void onArrow(ProjectileHitEvent event) {
        if (event.getEntity() instanceof Arrow)
            event.getEntity().remove();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onArrowPickup(PlayerPickupArrowEvent event) {
        event.setCancelled(true);
    }

    /**
     * Method to handle custom damage for bows
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onDamage(EntityDamageByEntityEvent event) {
        // only listen for arrows
        if (!(event.getDamager() instanceof Arrow)) return;

        Arrow arrow = (Arrow) event.getDamager();
        if (!(arrow.getShooter() instanceof LivingEntity)) return;
        if (!(event.getEntity() instanceof LivingEntity)) return;
        Entity shooter = (Entity) arrow.getShooter();

        // only listen for arrows NOT shot by a player
        if (!(arrow.getShooter() instanceof Player)) {
            // mobs
            event.setCancelled(true);
            double dmgAmt = event.getDamage();
            if (MythicMobs.inst().getMobManager().isActiveMob(Objects.requireNonNull(shooter).getUniqueId())) {
                if (MythicMobs.inst().getMobManager().isActiveMob(event.getEntity().getUniqueId()))
                    return; // don't let mobs shoot each other
                ActiveMob mm = MythicMobs.inst().getAPIHelper().getMythicMobInstance(shooter);
                dmgAmt = mm.getDamage();
            }
            MobDamageEvent mobDamageEvent = new MobDamageEvent((int) Math.ceil(dmgAmt), event.getDamager(), event.getEntity(), false);
            Bukkit.getPluginManager().callEvent(mobDamageEvent);
            if (!mobDamageEvent.isCancelled())
                DamageUtil.damageEntityMob(Math.ceil(mobDamageEvent.getAmount()),
                        (LivingEntity) mobDamageEvent.getVictim(), event.getDamager(), mobDamageEvent.shouldApplyMechanics());
        } else {

            // bugfix for armor stands
            Entity victim = event.getEntity();
            if (event.getEntity() instanceof ArmorStand && event.getEntity().getVehicle() != null) {
                victim = event.getEntity().getVehicle();
            }

            // get our entity
            if (!(victim.getType().isAlive())) return;

            // skip NPCs
            if (victim.hasMetadata("NPC")) return;

            Player damager = (Player) arrow.getShooter();

            // skip party members
            if (RunicCore.getPartyAPI().getParty(damager.getUniqueId()) != null) {
                if (victim instanceof Player) {
                    if (RunicCore.getPartyAPI().getParty(damager.getUniqueId()).hasMember((Player) victim)) {
                        return;
                    }
                }
            }

            // player can't damage themselves
            if (victim == damager) {
                event.setCancelled(true);
                return;
            }

            ItemStack artifact = damager.getInventory().getItemInMainHand();

            // retrieve the weapon damage, cooldown
            int minDamage;
            int maxDamage;
            try {
                RunicItemWeapon runicItemWeapon = (RunicItemWeapon) RunicItemsAPI.getRunicItemFromItemStack(artifact);
                minDamage = runicItemWeapon.getWeaponDamage().getMin();
                maxDamage = runicItemWeapon.getWeaponDamage().getMax();
            } catch (Exception ex) {
                minDamage = 1;
                maxDamage = 1;
            }

            int randomNum = ThreadLocalRandom.current().nextInt(minDamage, maxDamage + 1);

            // spawn the damage indicator if the arrow is a basic attack
            if (arrow.getCustomName() == null) return;

            event.setCancelled(true);

            DamageUtil.damageEntityRanged(randomNum, (LivingEntity) victim, damager, true, arrow);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onDraw(PlayerInteractEvent event) {
        if (event.getItem() == null) return;
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (event.getPlayer().getCooldown(Material.BOW) != 0) {
            event.setCancelled(true);
            return;
        }
        // Retrieve the weapon type
        ItemStack artifact = event.getItem();
        if (event.getPlayer().getInventory().getItemInOffHand().equals(artifact))
            return; // don't let them fire from offhand
        WeaponType artifactType = WeaponType.matchType(artifact);
        double cooldown = event.getPlayer().getCooldown(artifact.getType());

        // only listen for items that can be artifact weapons
        if (artifactType == null) return;

        // only listen for bows
        if (!(artifactType.equals(WeaponType.BOW))) return;

        Player player = event.getPlayer();

        // only listen for right clicks
        if (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK)
            return;
        if (event.getAction() == Action.PHYSICAL) return;

        // only apply cooldown if it's not already active
        if (cooldown != 0) return;

        String className = RunicCore.getCharacterAPI().getPlayerClass(player);
        if (className == null) return;
        if (!className.equals("Archer")) return;

        int reqLv;
        try {
            RunicItemWeapon runicItemWeapon = (RunicItemWeapon) RunicItemsAPI.getRunicItemFromItemStack(artifact);
            reqLv = runicItemWeapon.getLevel();
        } catch (Exception ex) {
            reqLv = 0;
        }

        event.setCancelled(true);

        if (reqLv > player.getLevel()) {
            player.playSound(player.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 0.5f, 1.0f);
            player.sendMessage(ChatColor.RED + "Your level is too low to wield this!");
            return;
        }

        if (RunicCore.getSpellAPI().isCasting(player)) return;

        player.playSound(player.getLocation(), Sound.ENTITY_ARROW_SHOOT, 0.25f, 1);

        // Fire a custom arrow
        final Vector direction = player.getLocation().getDirection().multiply(ARROW_SPEED_MULTIPLIER);
        Arrow myArrow = player.launchProjectile(Arrow.class);

        myArrow.setVelocity(direction);
        myArrow.setShooter(player);
        myArrow.setCustomNameVisible(false);
        myArrow.setCustomName("autoAttack");
        myArrow.setBounce(false);

        // Remove the arrow
        new BukkitRunnable() {
            @Override
            public void run() {
                Location arrowLoc = myArrow.getLocation();
                player.getWorld().spawnParticle(Particle.CRIT, arrowLoc, 5, 0, 0, 0, 0);
                if (myArrow.isDead() || myArrow.isOnGround()) {
                    this.cancel();
                }
            }
        }.runTaskTimer(RunicCore.getInstance(), 0, 1L);

        // Set the cooldown
        Bukkit.getPluginManager().callEvent(new BasicAttackEvent(player, Material.BOW, BasicAttackEvent.BASE_BOW_COOLDOWN, BasicAttackEvent.BASE_BOW_COOLDOWN));

        // Call custom event
        Bukkit.getPluginManager().callEvent(new RunicBowEvent(player, myArrow));
    }

    /**
     * Stop mobs from targeting each other.
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onMobTargetMob(EntityTargetEvent event) {
        if (event.getTarget() == null) return; // has a target
        if (!MythicMobs.inst().getMobManager().getActiveMob(event.getTarget().getUniqueId()).isPresent())
            return; // target is a mythic mob
        event.setCancelled(true);
    }
}
