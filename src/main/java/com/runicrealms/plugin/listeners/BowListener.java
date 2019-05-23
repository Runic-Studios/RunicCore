package com.runicrealms.plugin.listeners;

import com.runicrealms.plugin.enums.WeaponEnum;
import com.runicrealms.plugin.item.GearScanner;
import com.runicrealms.plugin.utilities.HologramUtil;
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

import java.util.ArrayList;
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

        // retrieve the weapon type
        ItemStack artifact = e.getItem();
        WeaponEnum artifactType = WeaponEnum.matchType(artifact);
        double cooldown = e.getPlayer().getCooldown(artifact.getType());
        double speed = AttributeUtil.getCustomDouble(artifact, "custom.bowSpeed");

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

        pl.playSound(pl.getLocation(), Sound.ENTITY_ARROW_SHOOT, 0.5f, 1);

        // fire a custom arrow
        final Vector direction = pl.getEyeLocation().getDirection().multiply(ARROW_VELOCITY_MULT);
        Arrow myArrow = pl.launchProjectile(Arrow.class);

        myArrow.setVelocity(direction);
        myArrow.setShooter(pl);
        myArrow.setCustomNameVisible(false);
        myArrow.setCustomName("autoAttack");

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
        if (speed != 0) {
            pl.setCooldown(artifact.getType(), (int) (20 / (24 + speed)));
        }
    }

    @EventHandler
    public void onArrowPickup(PlayerPickupArrowEvent e) {
        e.setCancelled(true);
    }

    // method to handle custom damage for bows
    @EventHandler(priority = EventPriority.HIGH)
    public void onDamage(final EntityDamageByEntityEvent e) {

        // only listen for arrows
        if (!(e.getDamager() instanceof Arrow)) return;

        Arrow arrow = (Arrow) e.getDamager();

        // only listen for arrows shot by a player
        if (!(arrow.getShooter() instanceof Player)) return;

        // get our entity
        if (!(e.getEntity().getType().isAlive())) return;
        LivingEntity le = (LivingEntity) e.getEntity();

        // skip NPCs
        if (le.hasMetadata("NPC")) return;

        Player damager = (Player) arrow.getShooter();

        // grab our variables
        Damageable victim = (Damageable) e.getEntity();

        // skip party members
        if (RunicCore.getPartyManager().getPlayerParty(damager) != null
                && RunicCore.getPartyManager().getPlayerParty(damager).hasMember(le.getUniqueId())) { return; }

        // player can't damage themselves
        if (victim == damager) {
            e.setCancelled(true);
            return;
        }

        ItemStack artifact = damager.getInventory().getItem(0);

        // retrieve the weapon damage, cooldown
        int minDamage = (int) AttributeUtil.getCustomDouble(artifact, "custom.minDamage");
        int maxDamage = (int) AttributeUtil.getCustomDouble(artifact, "custom.maxDamage");

        // grab player's armor, offhand (check for gem bonuses)
        ArrayList<ItemStack> armorAndOffhand = GearScanner.armorAndOffHand(damager);

        // calculate the player's total damage boost
        for (ItemStack item : armorAndOffhand) {
            int damageBoost = (int) AttributeUtil.getCustomDouble(item, "custom.attackDamage");
            maxDamage = maxDamage + damageBoost;
        }

        // remove the arrow with nms magic
        new BukkitRunnable() {
            public void run() {
                ((CraftPlayer) damager).getHandle().getDataWatcher().set(new DataWatcherObject(10, DataWatcherRegistry.b), (Object) 0);
            }
        }.runTaskLater(RunicCore.getInstance(), 3);

        // apply attack effects, random damage amount
        if (maxDamage != 0) {
            int randomNum = ThreadLocalRandom.current().nextInt(minDamage, maxDamage + 1);
            e.setDamage(randomNum);
        } else {
            e.setDamage(minDamage);
        }

        // spawn the damage indicator if the arrow is an autoattack
        if (arrow.getCustomName() == null) return;
        HologramUtil.createDamageHologram(damager, victim.getLocation().add(0,1.5,0), e.getDamage());
    }

    // removes arrows stuck in bodies
    @EventHandler
    public void onArrow(ProjectileHitEvent e) {
        if (e.getEntity() instanceof Arrow) {
            e.getEntity().remove();
        }
    }
}
