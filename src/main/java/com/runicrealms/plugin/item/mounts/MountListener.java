package com.runicrealms.plugin.item.mounts;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.attributes.AttributeUtil;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.HorseInventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;

@SuppressWarnings("deprecation")
public class MountListener implements Listener {

    private static final int cooldownTime = 5;
    private HashMap<UUID, Long> onCooldown = new HashMap<>();
    public static HashMap<UUID, Entity> mounted = new HashMap<>();

    @EventHandler
    public void onSaddleInteract(PlayerInteractEvent e) {

        Player pl = e.getPlayer();
        if (pl.getGameMode() != GameMode.SURVIVAL) return;
        if (e.getHand() != EquipmentSlot.HAND) return;
        if (e.getAction() != Action.RIGHT_CLICK_AIR && e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (!e.hasItem()) return;

        ItemStack pouch = e.getItem();
        if (pouch == null) return;
        if (pouch.getType() != Material.SADDLE) return;
        if (RunicCore.getCombatManager().getPlayersInCombat().containsKey(pl.getUniqueId())) {
            pl.sendMessage(ChatColor.RED + "You can't do that in combat!");
            return;
        }
        if (mounted.containsKey(pl.getUniqueId())) return;

        if (pl.getWorld().equals(Bukkit.getWorld("dungeons"))) {
            pl.sendMessage(ChatColor.RED + "You can't use that in the instance world!");
            return;
        }

        if (!isOnCooldown(pl)) {

            // remove old horse, if applicable
            if (mounted.containsKey(pl.getUniqueId())) {
                mounted.get(pl.getUniqueId()).remove();
                mounted.remove(pl.getUniqueId());
            }

            pl.playSound(pl.getLocation(), Sound.ENTITY_HORSE_BREATHE, 1.0f, 1.0f);
            pl.getWorld().spawnParticle(Particle.REDSTONE, pl.getLocation(),
                    25, 0.5f, 0.5f, 0.5f,
                    new Particle.DustOptions(Color.fromRGB(210, 180, 140), 20));

            Horse mount = spawnHorse(pl, pl.getInventory().getItemInMainHand());

            Bukkit.getScheduler().scheduleSyncDelayedTask(RunicCore.getInstance(), () -> {
                mount.setPassenger(pl);
                mounted.put(pl.getUniqueId(), mount);
            }, 1L);
        }
    }

    /**
     * Remove all horses on dismount
     */
    @EventHandler
    public void onVehicleDismount(VehicleExitEvent e) {
        if (e.getExited() instanceof Player && e.getVehicle() instanceof Horse) {
            mounted.remove(e.getExited().getUniqueId());
            Player pl = (Player) e.getExited();
            pl.playSound(e.getExited().getLocation(), Sound.ENTITY_HORSE_HURT, 0.5f, 1.0f);
            pl.getWorld().spawnParticle(Particle.REDSTONE, e.getExited().getLocation(),
                    25, 0.5f, 0.5f, 0.5f,
                    new Particle.DustOptions(Color.fromRGB(210, 180, 140), 20));
            e.getVehicle().remove();
        }
    }

    @EventHandler
    public void onHorseInvClick(InventoryClickEvent e) {
        if (e.getInventory() instanceof HorseInventory) {
            e.setCancelled(true);
            e.setResult(Event.Result.DENY);
        }
    }

    private boolean isOnCooldown(Player pl) {
        if (onCooldown.containsKey(pl.getUniqueId())) {
            pl.sendMessage(ChatColor.RED + "You must wait " + getUserCooldown(pl) + " second(s).");
            return true;
        } else {
            onCooldown.put(pl.getUniqueId(), System.currentTimeMillis());
            RunicCore.getInstance().getServer().getScheduler().runTaskLaterAsynchronously
                    (RunicCore.getInstance(), () -> onCooldown.remove(pl.getUniqueId()), (long) cooldownTime * 20);
        }
        return false;
    }

    private int getUserCooldown(Player pl) {
        double cooldownRemaining = 0;
        if (this.onCooldown.containsKey(pl.getUniqueId())) {
            long cd = this.onCooldown.get(pl.getUniqueId());
            cooldownRemaining = (cd + ((cooldownTime + 1) * 1000)) - System.currentTimeMillis();
        }
        return ((int) cooldownRemaining / 1000);
    }

    private Horse spawnHorse(Player pl, ItemStack saddle) {
        HorseTypeEnum horseType = HorseTypeEnum.valueOf(AttributeUtil.getCustomString(saddle, "horseType"));
        EntityType variant;
        if (horseType.getVariant() == Horse.Variant.HORSE) {
            variant = EntityType.HORSE;
        } else if (horseType.getVariant() == Horse.Variant.SKELETON_HORSE) {
            variant = EntityType.SKELETON_HORSE;
        } else {
            variant = EntityType.ZOMBIE_HORSE;
        }
        Horse horseMount = (Horse) Objects.requireNonNull(pl.getLocation().getWorld()).spawnEntity(pl.getLocation(), variant);
        horseMount.setColor(horseType.getColor());
        horseMount.setStyle(Horse.Style.NONE);
        horseMount.setMaxHealth(20);
        horseMount.setHealth(20);
        horseMount.setTamed(true);
        horseMount.setOwner(pl);
        horseMount.setInvulnerable(true);
        horseMount.getInventory().setSaddle(new ItemStack(Material.SADDLE, 1));
        horseMount.setCustomName(ChatColor.YELLOW + pl.getName() + "'s Horse");
        horseMount.setAdult();
        horseMount.setJumpStrength(horseType.getTier().getJumpSpeed());
        Objects.requireNonNull(horseMount.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED)).setBaseValue(horseType.getTier().getSpeed());
        return horseMount;
    }
}
