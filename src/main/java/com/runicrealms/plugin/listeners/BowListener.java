package com.runicrealms.plugin.listeners;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.attributes.AttributeUtil;
import com.runicrealms.plugin.enums.WeaponEnum;
import com.runicrealms.plugin.events.MobDamageEvent;
import com.runicrealms.plugin.player.outlaw.OutlawManager;
import com.runicrealms.plugin.utilities.DamageUtil;
import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.mobs.ActiveMob;
import net.minecraft.server.v1_15_R1.DataWatcherObject;
import net.minecraft.server.v1_15_R1.DataWatcherRegistry;
import org.bukkit.*;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftPlayer;
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
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

@SuppressWarnings("FieldCanBeLocal")
public class BowListener implements Listener {

    private Plugin plugin = RunicCore.getInstance();
    private static final int ARROW_VELOCITY_MULT = 3;

    @EventHandler
    public void onDraw(PlayerInteractEvent e) {

        // null check
        if (e.getItem() == null) {
            return;
        }

        if (e.getHand() != EquipmentSlot.HAND) return;

        // retrieve the weapon type
        ItemStack artifact = e.getItem();
        if (e.getPlayer().getInventory().getItemInOffHand().equals(artifact)) return; // don't let them fire from offhand
        WeaponEnum artifactType = WeaponEnum.matchType(artifact);
        double cooldown = e.getPlayer().getCooldown(artifact.getType());

        // only listen for items that can be artifact weapons
        if (artifactType == null) return;

        // only listen for bows
        if (!(artifactType.equals(WeaponEnum.BOW))) return;

        Player pl = e.getPlayer();

        // only listen for left clicks
        if (e.getAction() == Action.LEFT_CLICK_AIR || e.getAction() == Action.LEFT_CLICK_BLOCK) return;

        // only apply cooldown if its not already active
        if (cooldown != 0) return;

        String className = RunicCore.getCacheManager().getPlayerCache(pl.getUniqueId()).getClassName();
        if (className == null) return;
        if (!className.equals("Archer")) {
            return;
        }

        int reqLv = (int) AttributeUtil.getCustomDouble(artifact, "required.level");

        if (reqLv > RunicCore.getCacheManager().getPlayerCache(pl.getUniqueId()).getClassLevel()) {
            pl.playSound(pl.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 0.5f, 1.0f);
            pl.sendMessage(ChatColor.RED + "Your level is too low to wield this!");
            e.setCancelled(true);
            return;
        }

        pl.playSound(pl.getLocation(), Sound.ENTITY_ARROW_SHOOT, 0.5f, 1);

        // fire a custom arrow
        final Vector direction = pl.getEyeLocation().getDirection().multiply(ARROW_VELOCITY_MULT);
        Arrow myArrow = pl.launchProjectile(Arrow.class);

        myArrow.setVelocity(direction);
        myArrow.setShooter(pl);
        myArrow.setCustomNameVisible(false);
        myArrow.setCustomName("autoAttack");
        myArrow.setBounce(false);

        // remove the arrow
        new BukkitRunnable() {
            @Override
            public void run() {
                Location arrowLoc = myArrow.getLocation();
                pl.getWorld().spawnParticle(Particle.CRIT, arrowLoc, 5, 0, 0, 0, 0);
                if (myArrow.isDead() || myArrow.isOnGround()) {
                    this.cancel();
                }
            }
        }.runTaskTimer(plugin, 0, 1L);

        // set the cooldown
        pl.setCooldown(artifact.getType(), 15);
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
        if (RunicCore.getPartyManager().getPlayerParty(damager) != null
                && RunicCore.getPartyManager().getPlayerParty(damager).hasMember(victim.getUniqueId())) { return; }

        ItemStack artifact = damager.getInventory().getItemInMainHand();

        // retrieve the weapon damage, cooldown
        int minDamage = (int) AttributeUtil.getCustomDouble(artifact, "custom.minDamage");
        int maxDamage = (int) AttributeUtil.getCustomDouble(artifact, "custom.maxDamage");

        // remove the arrow with nms magic
        new BukkitRunnable() {
            public void run() {
                ((CraftPlayer) damager).getHandle().getDataWatcher().set(new DataWatcherObject(10, DataWatcherRegistry.b), (Object) 0);
            }
        }.runTaskLater(RunicCore.getInstance(), 3);

        int randomNum = ThreadLocalRandom.current().nextInt(minDamage, maxDamage + 1);

        // spawn the damage indicator if the arrow is an autoattack
        if (arrow.getCustomName() == null) return;

        // outlaw check
        if (victim instanceof Player && (!OutlawManager.isOutlaw(((Player) victim)) || !OutlawManager.isOutlaw(damager))) {
            return;
        }

        //DamageUtil.damageEntityWeapon(randomNum, (LivingEntity) victim, damager, true, false);
    }

    /**
     * Stop mobs from targeting each other.
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onMobTargetMob(EntityTargetEvent e) {
        if (e.getTarget() == null) return; // has target
        if (!MythicMobs.inst().getMobManager().getActiveMob(e.getTarget().getUniqueId()).isPresent()) return; // target is a mythic mob
//        ActiveMob am = MythicMobs.inst().getMobManager().getActiveMob(e.getEntity().getUniqueId()).get();
//        if (am.getFaction() != null && am.getFaction().equalsIgnoreCase("guard")) return; // targeter is not a guard
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

        // only listen for arrows shot by a player
        if (!(arrow.getShooter() instanceof Player)) {
            // mobs
                e.setCancelled(true);
                double dmgAmt = e.getDamage();
                if (MythicMobs.inst().getMobManager().isActiveMob(Objects.requireNonNull(shooter).getUniqueId())) {
                    if (MythicMobs.inst().getMobManager().isActiveMob(e.getEntity().getUniqueId())) return; // don't let mobs shoot each other
                    ActiveMob mm = MythicMobs.inst().getAPIHelper().getMythicMobInstance(shooter);
                    dmgAmt = mm.getDamage();
                }
                MobDamageEvent event = new MobDamageEvent((int) Math.ceil(dmgAmt), e.getDamager(), e.getEntity());
                Bukkit.getPluginManager().callEvent(event);
                if (event.isCancelled()) {
                    return;
                } else {
                    DamageUtil.damageEntityMob(Math.ceil(event.getAmount()), (LivingEntity) e.getEntity(), shooter);
                }
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
            if (RunicCore.getPartyManager().getPlayerParty(damager) != null
                    && RunicCore.getPartyManager().getPlayerParty(damager).hasMember(victim.getUniqueId())) {
                return;
            }

            // player can't damage themselves
            if (victim == damager) {
                e.setCancelled(true);
                return;
            }

            ItemStack artifact = damager.getInventory().getItemInMainHand();

            // retrieve the weapon damage, cooldown
            int minDamage = (int) AttributeUtil.getCustomDouble(artifact, "custom.minDamage");
            int maxDamage = (int) AttributeUtil.getCustomDouble(artifact, "custom.maxDamage");

            // remove the arrow with nms magic
            new BukkitRunnable() {
                public void run() {
                    ((CraftPlayer) damager).getHandle().getDataWatcher().set(new DataWatcherObject(10, DataWatcherRegistry.b), (Object) 0);
                }
            }.runTaskLater(RunicCore.getInstance(), 3);

            int randomNum = ThreadLocalRandom.current().nextInt(minDamage, maxDamage + 1);

            // spawn the damage indicator if the arrow is an autoattack
            if (arrow.getCustomName() == null) return;

            e.setCancelled(true);

            // outlaw check
            if (victim instanceof Player && (!OutlawManager.isOutlaw(((Player) victim)) || !OutlawManager.isOutlaw(damager))) {
                return;
            }

            DamageUtil.damageEntityWeapon(randomNum, (LivingEntity) victim, damager, true, false);
        }
    }

    // removes arrows stuck in bodies
    @EventHandler
    public void onArrow(ProjectileHitEvent e) {
        if (e.getEntity() instanceof Arrow) {
            e.getEntity().remove();
        }
    }
}
