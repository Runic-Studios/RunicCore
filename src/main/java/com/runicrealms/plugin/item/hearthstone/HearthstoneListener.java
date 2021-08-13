package com.runicrealms.plugin.item.hearthstone;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.character.api.CharacterLoadEvent;
import com.runicrealms.runicitems.RunicItemsAPI;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
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
    private static final int TEL_TIME = 5;
    private final static HashMap<UUID, BukkitTask> currentlyUsing = new HashMap<>();

    /**
     * Give new players the hearthstone
     */
    @EventHandler
    public void onJoin(CharacterLoadEvent e) {
        Player pl = e.getPlayer();
        new BukkitRunnable() {
            @Override
            public void run() {
                if (pl.getInventory().getItem(8) == null
                        || (pl.getInventory().getItem(8) != null
                        && pl.getInventory().getItem(8).getType() != Material.CLAY_BALL)) {
                    pl.getInventory().setItem(8, HearthstoneLocation.TUTORIAL_FORTRESS.getItemStack());
                }
            }
        }.runTaskLater(RunicCore.getInstance(), 2L);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {

        Player pl = (Player) e.getWhoClicked();
        int itemslot = e.getSlot();

        // if its the 8th slot in a player's inventory, run the stuff
        if (itemslot != 8) return;
        if (pl.getGameMode() != GameMode.SURVIVAL) return;
        if (e.getClickedInventory() == null) return;
        if (!e.getClickedInventory().getType().equals(InventoryType.PLAYER)) return;

        e.setCancelled(true);
    }

    @EventHandler
    public void onHearthstoneUse(PlayerInteractEvent e) {

        Player player = e.getPlayer();
        UUID uuid = player.getUniqueId();

        if (player.getInventory().getItemInMainHand().getType() == Material.AIR) return;
        if (player.getGameMode() == GameMode.CREATIVE) return;

        int slot = player.getInventory().getHeldItemSlot();
        if (slot != 8) return;
        if (player.getInventory().getItem(8) == null) return;
        ItemStack hearthstone = player.getInventory().getItem(8);
        if (hearthstone == null) return;

        // annoying 1.9 feature which makes the event run twice, once for each hand
        if (e.getHand() != EquipmentSlot.HAND) return;
        if (e.getAction() != Action.RIGHT_CLICK_AIR && e.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        // cancel the event if the hearthstone has no location
        String location = RunicItemsAPI.getRunicItemFromItemStack(hearthstone).getData().get("location");
        if (location == null || location.equals("")) {
            return;
        }

        // prevent player's from teleporting in combat
        if (RunicCore.getCombatManager().getPlayersInCombat().containsKey(uuid)) {
            player.sendMessage(ChatColor.RED + "You can't use that in combat!");
            return;
        }

        if (currentlyUsing.containsKey(player.getUniqueId())) return;

        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_PORTAL_TRIGGER, 0.5f, 1.0f);
        currentlyUsing.put(player.getUniqueId(), beginTeleportation(player, HearthstoneLocation.getLocationFromIdentifier(location)));
    }

    public static BukkitTask beginTeleportation(Player pl, Location location) {

        double timer_initX = Math.round(pl.getLocation().getX() * MOVE_CONSTANT);
        double timer_initY = Math.round(pl.getLocation().getY() * MOVE_CONSTANT);
        double timer_initZ = Math.round(pl.getLocation().getZ() * MOVE_CONSTANT);

        return new BukkitRunnable() {
            int count = 0;
            @Override
            public void run() {

                final Location currLocation = pl.getLocation();
                if ((Math.round(currLocation.getX() * MOVE_CONSTANT) != timer_initX
                        || Math.round(currLocation.getY() * MOVE_CONSTANT) != timer_initY
                        || Math.round(currLocation.getZ() * MOVE_CONSTANT) != timer_initZ)) {
                    this.cancel();
                    currentlyUsing.remove(pl.getUniqueId());
                    pl.sendMessage(ChatColor.RED + "Teleportation cancelled due to movement!");
                    return;
                }

                if (RunicCore.getCombatManager().getPlayersInCombat().containsKey(pl.getUniqueId())) {
                    this.cancel();
                    currentlyUsing.remove(pl.getUniqueId());
                    pl.sendMessage(ChatColor.RED + "Teleportation cancelled due to combat!");
                    return;
                }

                if (count >= TEL_TIME) {
                    this.cancel();
                    currentlyUsing.remove(pl.getUniqueId());
                    pl.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 60, 2));
                    pl.teleport(location);
                    pl.getWorld().playSound(pl.getLocation(), Sound.BLOCK_PORTAL_TRIGGER, 0.5f, 1.0f);
                    pl.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "You arrive at your location.");
                    return;
                }

                pl.getWorld().spawnParticle(Particle.REDSTONE, pl.getLocation().add(0,1,0),
                        10, 0.5f, 0.5f, 0.5f, new Particle.DustOptions(Color.AQUA, 3));

                pl.sendMessage(ChatColor.AQUA + "Teleporting... "
                        + ChatColor.WHITE + ChatColor.BOLD + (TEL_TIME -count) + "s");
                count = count+1;

            }
        }.runTaskTimer(RunicCore.getInstance(), 0, 20);
    }
}

