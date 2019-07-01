package com.runicrealms.plugin.item.hearthstone;

import com.runicrealms.plugin.attributes.AttributeUtil;
import com.runicrealms.plugin.item.commands.HearthstoneCMD;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerCommandSendEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import com.runicrealms.plugin.RunicCore;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/**
 * Manages the server hearthstone. Cooldown resets on server restart, intended.
 */
public class HearthstoneListener implements Listener {

    private static final int cooldownTime = 900;
    private static final int teleportTime = 5;
    private HashMap<UUID, Long> hsCooldowns = new HashMap<>();
    private List<UUID> currentlyUsing = new ArrayList<>();

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {

        Player pl = (Player) e.getWhoClicked();
        int itemslot = e.getSlot();

        // if its the 3rd slot in a player's inventory, run the stuff
        if (itemslot != 2) return;
        if (pl.getGameMode() != GameMode.SURVIVAL) return;
        if (e.getClickedInventory() == null) return;
        if (!e.getClickedInventory().getType().equals(InventoryType.PLAYER)) return;

        e.setCancelled(true);
    }

    @EventHandler
    public void onHearthstoneUse(PlayerInteractEvent e) {

        Player pl = e.getPlayer();
        UUID uuid = pl.getUniqueId();

        if (pl.getInventory().getItemInMainHand().getType() == Material.AIR) return;
        if (pl.getGameMode() == GameMode.CREATIVE) return;

        int slot = pl.getInventory().getHeldItemSlot();
        if (slot != 2) return;

        // annoying 1.9 feature which makes the event run twice, once for each hand
        if (e.getHand() != EquipmentSlot.HAND) return;
        if (e.getAction() != Action.RIGHT_CLICK_AIR && e.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        // cancel the event if the hearthstone has no location
        String itemLoc = AttributeUtil.getCustomString(pl.getInventory().getItem(2), "location");
        if (itemLoc == null || itemLoc.equals("")) {
            return;
        }

        // prevent player's from teleporting in combat
        if (RunicCore.getCombatManager().getPlayersInCombat().containsKey(uuid)) {
            pl.sendMessage(ChatColor.RED + "You can't use that in combat!");
            return;
        }

        if (currentlyUsing.contains(pl.getUniqueId())) return;

        if (hsCooldowns.containsKey(uuid)) {

            if ((System.currentTimeMillis()-hsCooldowns.get(uuid))/1000 >= cooldownTime) {
                hsCooldowns.remove(uuid);
                activateHearthstone(pl);

            } else {

                double rawTime = (double) ((cooldownTime*1000) - (System.currentTimeMillis()-hsCooldowns.get(uuid))) / 1000;
                //NumberFormat toDecimal = new DecimalFormat("#0.00");
                //String timeLeft = toDecimal.format(rawTime);
                pl.playSound(pl.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5f, 1.0f);
                pl.sendMessage(ChatColor.RED + "You must wait "
                        + ChatColor.YELLOW + (int) rawTime + "s"
                        + ChatColor.RED + " before using your hearthstone.");
            }
        } else {
            activateHearthstone(pl);
        }
    }

    private void activateHearthstone(Player pl) {

        pl.getWorld().playSound(pl.getLocation(), Sound.BLOCK_PORTAL_TRIGGER, 0.5f, 1.0f);
        currentlyUsing.add(pl.getUniqueId());

        double originalX = pl.getLocation().getX();
        double originalZ = pl.getLocation().getZ();

        new BukkitRunnable() {
            int count = 0;
            @Override
            public void run() {

                double x = pl.getLocation().getX();
                double z = pl.getLocation().getZ();

                if (x != originalX || z != originalZ) {
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

                if (count >= teleportTime) {
                    this.cancel();
                    hsCooldowns.put(pl.getUniqueId(), System.currentTimeMillis());
                    currentlyUsing.remove(pl.getUniqueId());
                    pl.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 60, 2));
                    teleportToLocation(pl);
                    pl.getWorld().playSound(pl.getLocation(), Sound.BLOCK_PORTAL_TRIGGER, 0.5f, 1.0f);
                    pl.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "You arrive at your hearthstone location.");
                    return;
                }

                pl.getWorld().spawnParticle(Particle.REDSTONE, pl.getLocation().add(0,1,0),
                        10, 0.5f, 0.5f, 0.5f, new Particle.DustOptions(Color.AQUA, 3));

                pl.sendMessage(ChatColor.AQUA + "" + ChatColor.BOLD + "Teleporting... "
                        + ChatColor.WHITE + ChatColor.BOLD + + (teleportTime-count) + "s");
                count = count+1;

            }
        }.runTaskTimer(RunicCore.getInstance(), 0, 20);
    }

    @EventHandler
    public void onItemSwap(PlayerSwapHandItemsEvent swapevent) {

        Player p = swapevent.getPlayer();
        int slot = p.getInventory().getHeldItemSlot();

        // cancel the event
        if (slot == 2) {
            swapevent.setCancelled(true);
            p.playSound(p.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 1, 1);
            p.sendMessage(ChatColor.GRAY + "You cannot perform this action in this slot.");
        }
    }

    public static void teleportToLocation(Player pl) {

        String itemLoc = AttributeUtil.getCustomString(pl.getInventory().getItem(2), "location");

        if (itemLoc.equals("")) {
            pl.sendMessage(ChatColor.DARK_RED + "Error: location not found");
            return;
        }

        // attempt to match the player's hearthstone to a location
        Location loc;
        World world = Bukkit.getWorld("Alterra");
        double x;
        double y;
        double z;
        int yaw;
        switch (itemLoc.toLowerCase()) {

            case "naz'mora": // 9
                x = 2587.5;
                y = 33;
                z = 979.5;
                yaw = 270;
                break;
            case "naheen": // 8
                x = 1962.5;
                y = 42;
                z = 349.5;
                yaw = 270;
                break;
            case "zenyth": // 7
                x = 1569.5;
                y = 38;
                z = -161.5;
                yaw = 90;
                break;
            //case "hilstead": // 6
                //break;
            case "wintervale": // 5
                x = -1672.5;
                y = 37;
                z = -2639.5;
                yaw = 90;
                break;
            case "isfodar": // 4
                x = 744.5;
                y = 94;
                z = -137.5;
                yaw = 90;
                break;
            case "dead man's rest": // 3
                x = -24.5;
                y = 32;
                z = -475.5;
                yaw = 90;
                break;
            case "koldore": // 2
                x = -1616.5;
                y = 43;
                z = 305.5;
                yaw = 0;
                break;
            case "azana": // 1
                x = -825.5;
                y = 38;
                z = 167.5;
                yaw = 180;
                break;
            case "tutorial island": // tutorial 2
                x = -1927.5;
                y = 42;
                z = 2012.5;
                yaw = 180;
                break;
            default:
                x = -2205.0; // tutorial 1
                y = 39;
                z = 1903;
                yaw = 325;
                break;
        }

        loc = new Location(world, x, y, z, yaw, 0);
        pl.teleport(loc);
    }
}

