package com.runicrealms.plugin.item.hearthstone;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.attributes.AttributeUtil;
import com.runicrealms.plugin.character.api.CharacterLoadEvent;
import com.runicrealms.plugin.item.LoreGenerator;
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
 * Manages the server hearthstone. Cooldown resets on server restart, intended.
 */
public class HearthstoneListener implements Listener {

    private static final int cooldownTime = 0; // 900
    private static final double MOVE_CONSTANT = 0.6;
    private static final int TEL_TIME = 5;
    private final HashMap<UUID, Long> hsCooldowns = new HashMap<>();
    private final HashMap<UUID, BukkitTask> currentlyUsing = new HashMap<>();

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
                    pl.getInventory().setItem(8, getDefaultHearthstone());
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

        Player pl = e.getPlayer();
        UUID uuid = pl.getUniqueId();

        if (pl.getInventory().getItemInMainHand().getType() == Material.AIR) return;
        if (pl.getGameMode() == GameMode.CREATIVE) return;

        int slot = pl.getInventory().getHeldItemSlot();
        if (slot != 8) return;

        // annoying 1.9 feature which makes the event run twice, once for each hand
        if (e.getHand() != EquipmentSlot.HAND) return;
        if (e.getAction() != Action.RIGHT_CLICK_AIR && e.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        // cancel the event if the hearthstone has no location
        String itemLoc = AttributeUtil.getCustomString(pl.getInventory().getItem(8), "location");
        if (itemLoc == null || itemLoc.equals("")) {
            return;
        }

        // prevent player's from teleporting in combat
        if (RunicCore.getCombatManager().getPlayersInCombat().containsKey(uuid)) {
            pl.sendMessage(ChatColor.RED + "You can't use that in combat!");
            return;
        }

        if (currentlyUsing.containsKey(pl.getUniqueId())) return;

        if (hsCooldowns.containsKey(uuid)) {

            if ((System.currentTimeMillis()-hsCooldowns.get(uuid))/1000 >= cooldownTime) {
                hsCooldowns.remove(uuid);
                pl.getWorld().playSound(pl.getLocation(), Sound.BLOCK_PORTAL_TRIGGER, 0.5f, 1.0f);
                currentlyUsing.put(pl.getUniqueId(), activateHearthstone(pl));

            } else {

                double rawTime = (double) (-(System.currentTimeMillis() - hsCooldowns.get(uuid))) / 1000;
                pl.playSound(pl.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5f, 1.0f);
                pl.sendMessage(ChatColor.RED + "You must wait "
                        + ChatColor.YELLOW + (int) rawTime + "s"
                        + ChatColor.RED + " before using your hearthstone.");
            }
        } else {
            pl.getWorld().playSound(pl.getLocation(), Sound.BLOCK_PORTAL_TRIGGER, 0.5f, 1.0f);
            currentlyUsing.put(pl.getUniqueId(), activateHearthstone(pl));
        }
    }

    private BukkitTask activateHearthstone(Player pl) {

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

                pl.sendMessage(ChatColor.AQUA + "Teleporting... "
                        + ChatColor.WHITE + ChatColor.BOLD + + (TEL_TIME -count) + "s");
                count = count+1;

            }
        }.runTaskTimer(RunicCore.getInstance(), 0, 20);
    }

    private ItemStack getDefaultHearthstone() {
        ItemStack hearthstone = new ItemStack(Material.CLAY_BALL);
        hearthstone = AttributeUtil.addCustomStat(hearthstone, "location", "Tutorial Fortress");
        hearthstone = AttributeUtil.addCustomStat(hearthstone, "soulbound", "true");
        LoreGenerator.generateHearthstoneLore(hearthstone);
        return hearthstone;
    }

    public static ItemStack newHearthstone(String location) {
        location = location.replace("_", " ");
        ItemStack hearthstone = new ItemStack(Material.CLAY_BALL);
        hearthstone = AttributeUtil.addCustomStat(hearthstone, "location", location);
        hearthstone = AttributeUtil.addCustomStat(hearthstone, "soulbound", "true");
        LoreGenerator.generateHearthstoneLore(hearthstone);
        return hearthstone;
    }

    public static void teleportToLocation(Player pl) {
        try {
            // get location for hearthstone
            String itemLoc = AttributeUtil.getCustomString(pl.getInventory().getItem(8), "location");

            // attempt to match the player's hearthstone to a location
            Location loc;
            World world = Bukkit.getWorld("Alterra");
            double x;
            double y;
            double z;
            int yaw;
            switch (itemLoc.toLowerCase()) {

                case "naz'mora": // 12
                    x = 2587.5;
                    y = 33;
                    z = 979.5;
                    yaw = 270;
                    break;
                case "naheen": // 11
                    x = 1962.5;
                    y = 42;
                    z = 349.5;
                    yaw = 270;
                    break;
                case "zenyth": // 10
                    x = 1564.5;
                    y = 38;
                    z = -158.5;
                    yaw = 180;
                    break;
                case "tireneas": // 9
                    x = 887.5;
                    y = 43;
                    z = 547.5;
                    yaw = 270;
                    break;
                case "isfodar": // 8
                    x = 744.5;
                    y = 94;
                    z = -137.5;
                    yaw = 270;
                    break;
                case "dead man's rest": // 7
                    x = -24.5;
                    y = 32;
                    z = -475.5;
                    yaw = 90;
                    break;
                case "dawnshire inn": // 6
                    x = -306.5;
                    y = 57;
                    z = -408.5;
                    yaw = 90;
                    break;
                case "wintervale": // 5
                    x = -1672.5;
                    y = 37;
                    z = -2639.5;
                    yaw = 90;
                    break;
                case "hilstead": // 4
                    x = -1649.5;
                    y = 44;
                    z = -2053.5;
                    yaw = 270;
                    break;
                case "whaletown": // 3
                    x = -1834.5;
                    y = 32;
                    z = -654.5;
                    yaw = 0;
                    break;
                case "koldore": // 2
                    x = -1661.5;
                    y = 35;
                    z = 206.5;
                    yaw = 270;
                    break;
                case "azana": // 1
                    x = -825.5;
                    y = 38;
                    z = 167.5;
                    yaw = 180;
                    break;
                case "tutorial village": // tutorial 2
                    x = -1971.5;
                    y = 40;
                    z = 1940.5;
                    yaw = 270;
                    break;
                default:
                    x = -2317.5; // tutorial 1 (tutorial fortress)
                    y = 38;
                    z = 1719.5;
                    yaw = 0;
                    break;
            }

            loc = new Location(world, x, y, z, yaw, 0);
            pl.teleport(loc);
        } catch (Exception e) {
            Location loc = new Location(Bukkit.getWorld("Alterra"), -2317.5, 38, 1719.5, 0, 0);
            pl.teleport(loc);
        }
    }
}

