package com.runicrealms.plugin.listeners;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.SafeZoneLocation;
import com.runicrealms.plugin.rdb.event.CharacterLoadedEvent;
import com.runicrealms.plugin.runicitems.RunicItemsAPI;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.UUID;

/**
 * Manages the server hearthstone
 */
public class HearthstoneListener implements Listener {

    private static final double MOVE_CONSTANT = 0.6;
    private static final int TELEPORT_TIME = 5;
    private final static HashMap<UUID, BukkitTask> currentlyUsing = new HashMap<>();

    public static BukkitTask beginTeleportation(Player player, Location location) {

        double timer_initX = Math.round(player.getLocation().getX() * MOVE_CONSTANT);
        double timer_initY = Math.round(player.getLocation().getY() * MOVE_CONSTANT);
        double timer_initZ = Math.round(player.getLocation().getZ() * MOVE_CONSTANT);

        return new BukkitRunnable() {
            int count = 0;

            @Override
            public void run() {

                final Location currLocation = player.getLocation();
                if ((Math.round(currLocation.getX() * MOVE_CONSTANT) != timer_initX
                        || Math.round(currLocation.getY() * MOVE_CONSTANT) != timer_initY
                        || Math.round(currLocation.getZ() * MOVE_CONSTANT) != timer_initZ)) {
                    this.cancel();
                    currentlyUsing.remove(player.getUniqueId());
                    player.sendMessage(ChatColor.RED + "Teleportation cancelled due to movement!");
                    return;
                }

                if (RunicCore.getCombatAPI().isInCombat(player.getUniqueId())) {
                    this.cancel();
                    currentlyUsing.remove(player.getUniqueId());
                    player.sendMessage(ChatColor.RED + "Teleportation cancelled due to combat!");
                    return;
                }

                if (count >= TELEPORT_TIME) {
                    this.cancel();
                    currentlyUsing.remove(player.getUniqueId());
                    player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 60, 2));
                    player.teleport(location);
                    player.getWorld().playSound(player.getLocation(), Sound.BLOCK_PORTAL_TRIGGER, 0.5f, 1.0f);
                    player.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "You arrive at your location.");
                    return;
                }

                player.getWorld().spawnParticle(Particle.REDSTONE, player.getLocation().add(0, 1, 0),
                        10, 0.5f, 0.5f, 0.5f, new Particle.DustOptions(Color.AQUA, 3));

                player.sendMessage(ChatColor.AQUA + "Teleporting... "
                        + ChatColor.WHITE + ChatColor.BOLD + (TELEPORT_TIME - count) + "s");
                count = count + 1;

            }
        }.runTaskTimer(RunicCore.getInstance(), 0, 20);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onHearthstoneUse(PlayerInteractEvent event) {
        if (event.useInteractedBlock() == Event.Result.DENY && event.useItemInHand() == Event.Result.DENY)
            return;
        Player player = event.getPlayer();

        if (player.getInventory().getItemInMainHand().getType() == Material.AIR) return;
        if (player.getGameMode() == GameMode.CREATIVE) return;

        int slot = player.getInventory().getHeldItemSlot();
        if (slot != 8) return;
        if (player.getInventory().getItem(8) == null) return;
        ItemStack hearthstone = player.getInventory().getItem(8);
        if (hearthstone == null) return;

        // annoying 1.9 feature which makes the event run twice, once for each hand
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;

        // cancel the event if the hearthstone has no location
        String location = RunicItemsAPI.getRunicItemFromItemStack(hearthstone).getData().get("location");
        if (location == null || location.equals("")) {
            return;
        }

        // prevent player's from teleporting in combat
        if (RunicCore.getCombatAPI().isInCombat(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "You can't use that in combat!");
            return;
        }

        if (currentlyUsing.containsKey(player.getUniqueId())) return;

        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_PORTAL_TRIGGER, 0.5f, 1.0f);
        currentlyUsing.put(player.getUniqueId(), beginTeleportation(player, SafeZoneLocation.getLocationFromIdentifier(location)));
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        int itemSlot = e.getSlot();
        // if it's the 8th slot in a player's inventory, run the stuff
        if (itemSlot != 8) return;
        if (player.getGameMode() == GameMode.CREATIVE) return;
        if (e.getClickedInventory() == null) return;
        if (e.getClickedInventory().getType() != InventoryType.PLAYER) return;
        e.setCancelled(true);
    }

    /**
     * Give players the hearthstone
     */
    @EventHandler
    public void onJoin(CharacterLoadedEvent event) {
        Player player = event.getPlayer();
        new BukkitRunnable() {
            @Override
            public void run() {
                if (player.getInventory().getItem(8) == null
                        || (player.getInventory().getItem(8) != null
                        && player.getInventory().getItem(8).getType() != Material.CLAY_BALL)) {
                    player.getInventory().setItem(8, SafeZoneLocation.TUTORIAL.getItemStack());
                }
            }
        }.runTaskLater(RunicCore.getInstance(), 2L);
    }
}

