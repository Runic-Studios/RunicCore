package com.runicrealms.plugin.listeners;

import com.runicrealms.plugin.enums.WeaponEnum;
import com.runicrealms.plugin.outlaw.OutlawManager;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.event.EventPriority;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import com.runicrealms.plugin.RunicCore;
import net.minecraft.server.v1_13_R2.DataWatcherObject;
import net.minecraft.server.v1_13_R2.DataWatcherRegistry;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_13_R2.entity.CraftPlayer;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupArrowEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import com.runicrealms.plugin.attributes.AttributeUtil;

import java.util.concurrent.ThreadLocalRandom;

import static com.runicrealms.plugin.listeners.DamageListener.weaponMessage;

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

        // retrieve the weapon type
        ItemStack artifact = e.getItem();
        if (e.getPlayer().getInventory().getItemInOffHand().equals(artifact)) return; // don't let them fire from offhand
        WeaponEnum artifactType = WeaponEnum.matchType(artifact);
        double cooldown = e.getPlayer().getCooldown(artifact.getType());

        // only listen for items that can be artifact weapons
        if (artifactType == null) return;

        // only listen for staves
        if (!(artifactType.equals(WeaponEnum.BOW))) return;

        Player pl = e.getPlayer();

        // only listen for left clicks
        if (e.getAction() == Action.LEFT_CLICK_AIR || e.getAction() == Action.LEFT_CLICK_BLOCK) return;

        // only apply cooldown if its not already active
        if (cooldown != 0) return;

        // don't fire arrow if they're sneaking, since they're casting a spell
        if (pl.isSneaking()) return;

        String className = RunicCore.getInstance().getConfig().getString(pl.getUniqueId() + ".info.class.name");
        if (className == null) return;
        if (!className.equals("Archer")) {
            pl.playSound(pl.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5f, 1);
            pl.sendMessage(weaponMessage(className));
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
        pl.setCooldown(artifact.getType(), 10);
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

        DamageUtil.damageEntityWeapon(randomNum, (LivingEntity) victim, damager, true, false);
    }

    // method to handle custom damage for bows
    @EventHandler(priority = EventPriority.MONITOR)
    public void onDamage(EntityDamageByEntityEvent e) {

        // only listen for arrows
        if (!(e.getDamager() instanceof Arrow)) return;

        Arrow arrow = (Arrow) e.getDamager();

        // only listen for arrows shot by a player
        if (!(arrow.getShooter() instanceof Player)) return;

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
                && RunicCore.getPartyManager().getPlayerParty(damager).hasMember(victim.getUniqueId())) { return; }

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

    // removes arrows stuck in bodies
    @EventHandler
    public void onArrow(ProjectileHitEvent e) {
        if (e.getEntity() instanceof Arrow) {
            e.getEntity().remove();
        }
    }
}
