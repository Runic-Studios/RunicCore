package com.runicrealms.plugin.listeners;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.WeaponType;
import com.runicrealms.plugin.api.RunicCoreAPI;
import com.runicrealms.plugin.events.MobDamageEvent;
import com.runicrealms.plugin.utilities.DamageUtil;
import com.runicrealms.runicitems.RunicItemsAPI;
import com.runicrealms.runicitems.item.RunicItemWeapon;
import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.mobs.ActiveMob;
import org.bukkit.*;
import org.bukkit.entity.*;
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

@SuppressWarnings("FieldCanBeLocal")
public class BowListener implements Listener {

    private static final int ARROW_VELOCITY_MULT = 3;
    private static final int BOW_GLOBAL_COOLDOWN = 15; // ticks

    @EventHandler
    public void onDraw(PlayerInteractEvent e) {

        // null check
        if (e.getItem() == null) return;

        if (e.getHand() != EquipmentSlot.HAND) return;

        // retrieve the weapon type
        ItemStack artifact = e.getItem();
        if (e.getPlayer().getInventory().getItemInOffHand().equals(artifact))
            return; // don't let them fire from offhand
        WeaponType artifactType = WeaponType.matchType(artifact);
        double cooldown = e.getPlayer().getCooldown(artifact.getType());

        // only listen for items that can be artifact weapons
        if (artifactType == null) return;

        // only listen for bows
        if (!(artifactType.equals(WeaponType.BOW))) return;

        Player player = e.getPlayer();

        // only listen for left clicks
        if (e.getAction() == Action.LEFT_CLICK_AIR || e.getAction() == Action.LEFT_CLICK_BLOCK) return;

        // only apply cooldown if it's not already active
        if (cooldown != 0) return;

        String className = RunicCoreAPI.getPlayerClass(player);
        if (className == null) return;
        if (!className.equals("Archer")) return;

        int reqLv;
        try {
            RunicItemWeapon runicItemWeapon = (RunicItemWeapon) RunicItemsAPI.getRunicItemFromItemStack(artifact);
            reqLv = runicItemWeapon.getLevel();
        } catch (Exception ex) {
            reqLv = 0;
        }

        if (reqLv > player.getLevel()) {
            player.playSound(player.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 0.5f, 1.0f);
            player.sendMessage(ChatColor.RED + "Your level is too low to wield this!");
            e.setCancelled(true);
            return;
        }

        if (RunicCoreAPI.isCasting(player)) return;

        player.playSound(player.getLocation(), Sound.ENTITY_ARROW_SHOOT, 0.5f, 1);

        // fire a custom arrow
        final Vector direction = player.getEyeLocation().getDirection().multiply(ARROW_VELOCITY_MULT);
        Arrow myArrow = player.launchProjectile(Arrow.class);

        myArrow.setVelocity(direction);
        myArrow.setShooter(player);
        myArrow.setCustomNameVisible(false);
        myArrow.setCustomName("autoAttack");
        myArrow.setBounce(false);

        // remove the arrow
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

        // set the cooldown
        player.setCooldown(artifact.getType(), BOW_GLOBAL_COOLDOWN);
    }

    @EventHandler
    public void onArrowPickup(PlayerPickupArrowEvent e) {
        e.setCancelled(true);
    }

    /**
     * Bugfix for armor stands
     */
    @EventHandler
    public void onCollide(ProjectileHitEvent e) {

        // only listen for arrows
        if (!(e.getEntity() instanceof Arrow)) return;

        Arrow arrow = (Arrow) e.getEntity();

        // only listen for arrows shot by a player
        if (!(arrow.getShooter() instanceof Player)) return;

        Entity victim = e.getHitEntity();
        if (e.getHitEntity() instanceof ArmorStand && e.getHitEntity().getVehicle() != null) {
            victim = e.getHitEntity().getVehicle();
        }

        if (victim == null) return;
        // get our entity
        if (!(victim.getType().isAlive())) return;

        // skip NPCs
        if (victim.hasMetadata("NPC")) return;

        Player damager = (Player) arrow.getShooter();

        // skip party members
        if (RunicCore.getPartyManager().getPlayerParty(damager) != null) {
            if (victim instanceof Player) {
                if (RunicCore.getPartyManager().getPlayerParty(damager).hasMember((Player) victim)) {
                    return;
                }
            }
        }
    }

    /**
     * Stop mobs from targeting each other.
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onMobTargetMob(EntityTargetEvent e) {
        if (e.getTarget() == null) return; // has a target
        if (!MythicMobs.inst().getMobManager().getActiveMob(e.getTarget().getUniqueId()).isPresent())
            return; // target is a mythic mob
        e.setCancelled(true);
    }

    // method to handle custom damage for bows
    @EventHandler(priority = EventPriority.MONITOR)
    public void onDamage(EntityDamageByEntityEvent e) {

        // only listen for arrows
        if (!(e.getDamager() instanceof Arrow)) return;

        Arrow arrow = (Arrow) e.getDamager();
        if (!(arrow.getShooter() instanceof LivingEntity)) return;
        if (!(e.getEntity() instanceof LivingEntity)) return;
        Entity shooter = (Entity) arrow.getShooter();

        // only listen for arrows NOT shot by a player
        if (!(arrow.getShooter() instanceof Player)) {
            // mobs
            e.setCancelled(true);
            double dmgAmt = e.getDamage();
            if (MythicMobs.inst().getMobManager().isActiveMob(Objects.requireNonNull(shooter).getUniqueId())) {
                if (MythicMobs.inst().getMobManager().isActiveMob(e.getEntity().getUniqueId()))
                    return; // don't let mobs shoot each other
                ActiveMob mm = MythicMobs.inst().getAPIHelper().getMythicMobInstance(shooter);
                dmgAmt = mm.getDamage();
            }
            MobDamageEvent event = new MobDamageEvent((int) Math.ceil(dmgAmt), e.getDamager(), e.getEntity(), false);
            Bukkit.getPluginManager().callEvent(event);
            if (!event.isCancelled())
                DamageUtil.damageEntityMob(Math.ceil(event.getAmount()),
                        (LivingEntity) event.getVictim(), e.getDamager(), event.shouldApplyMechanics());
        } else {

            // bugfix for armor stands
            Entity victim = e.getEntity();
            if (e.getEntity() instanceof ArmorStand && e.getEntity().getVehicle() != null) {
                victim = e.getEntity().getVehicle();
            }

            // get our entity
            if (!(victim.getType().isAlive())) return;

            // skip NPCs
            if (victim.hasMetadata("NPC")) return;

            Player damager = (Player) arrow.getShooter();

            // skip party members
            if (RunicCore.getPartyManager().getPlayerParty(damager) != null) {
                if (victim instanceof Player) {
                    if (RunicCore.getPartyManager().getPlayerParty(damager).hasMember((Player) victim)) {
                        return;
                    }
                }
            }

            // player can't damage themselves
            if (victim == damager) {
                e.setCancelled(true);
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

            // spawn the damage indicator if the arrow is an basic attack
            if (arrow.getCustomName() == null) return;

            e.setCancelled(true);

            DamageUtil.damageEntityWeapon(randomNum, (LivingEntity) victim, damager, true, true);
        }
    }

    // removes any arrows stuck in bodies
    @EventHandler
    public void onArrow(ProjectileHitEvent e) {
        if (e.getEntity() instanceof Arrow)
            e.getEntity().remove();
    }
}
