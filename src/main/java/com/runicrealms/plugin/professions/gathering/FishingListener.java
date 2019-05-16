package com.runicrealms.plugin.professions.gathering;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Fish;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;
import com.runicrealms.plugin.professions.utilities.FloatingItemUtil;
import com.runicrealms.plugin.utilities.HologramUtil;

import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Listener for Fishing (Gathering Profesion)
 * randomizes which fish they receive
 * Checks name of WG region for "pond" to perform tasks
 */
public class FishingListener implements Listener {

    private double fishSuccessRate = 85.0;
    private double nuggetRate = 5.0;

    @EventHandler
    public void onFishCatch(PlayerFishEvent e) {

        // disable exp
        e.setExpToDrop(0);

        // grab the player, location
        Player pl = e.getPlayer();

        Material itemType;
        String itemName;
        String holoString;
        String desc = "Crafting Reagent";

        PlayerFishEvent.State state = e.getState();
        if (state == PlayerFishEvent.State.CAUGHT_ENTITY || state == PlayerFishEvent.State.CAUGHT_FISH) {

            // roll to see if player succesfully fished
            // roll to see what kind of fish they will receive
            double chance = ThreadLocalRandom.current().nextDouble(0, 100);
            int fishType = ThreadLocalRandom.current().nextInt(0, 100);
            Location hookLoc = e.getHook().getLocation();
            Vector fishPath = pl.getLocation().toVector().subtract
                    (hookLoc.add(0, 1, 0).toVector()).normalize();

            e.setCancelled(true);

            if (fishType < 50) {
                itemType = Material.COD;
                itemName = "Raw Cod";
                holoString = "+ Cod";
            } else if (fishType < 75) {
                itemName = "Raw Salmon";
                itemType = Material.SALMON;
                holoString = "+ Salmon";
            } else if (fishType < 95) {
                itemName = "Tropical Fish";
                itemType = Material.TROPICAL_FISH;
                holoString = "+ Tropical";
            } else {
                itemName = "Pufferfish";
                itemType = Material.PUFFERFISH;
                holoString = "+ Pufferfish";
            }

            gatherMaterial(pl, hookLoc, hookLoc.add(0, 1.5, 0), itemType, holoString,
                    itemName, desc, "The fish got away!", chance, fishPath);
        }
    }

    @EventHandler
    public void onRodUse(PlayerInteractEvent e) {

        Player pl = e.getPlayer();
        Location plLoc = pl.getLocation();

        // grab all regions the player is standing in
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionQuery query = container.createQuery();
        ApplicableRegionSet set = query.getApplicableRegions(BukkitAdapter.adapt(plLoc));
        Set<ProtectedRegion> regions = set.getRegions();

        if (regions == null) return;

        boolean canFish = false;

        // check the region for the keyword 'pond'
        // ignore the rest of this event if the player cannot fish
        for (ProtectedRegion region : regions) {
            if (region.getId().contains("pond")) {
                canFish = true;
            }
        }

        if (e.getAction() != Action.RIGHT_CLICK_AIR && e.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Material mainHand = pl.getInventory().getItemInMainHand().getType();
        Material offHand = pl.getInventory().getItemInOffHand().getType();

        if (mainHand == null && offHand == null) return;

        if (mainHand != Material.FISHING_ROD && offHand != Material.FISHING_ROD) return;

        if (!canFish) {
            e.setCancelled(true);
            pl.sendMessage(ChatColor.RED + "You can't fish here.");
        }
    }

    /**
     * Prevent fish from spawning naturally.
     * We'll spawn them in ponds as NPCs, but not out in the ocean.
     */
    @EventHandler
    public void onFishSpawn(CreatureSpawnEvent e) {

        Entity spawned = e.getEntity();

        if (e.getSpawnReason() == CreatureSpawnEvent.SpawnReason.CUSTOM) return;

        if (spawned instanceof Fish) e.setCancelled(true);
    }


    private void gatherMaterial(Player pl, Location loc, Location fishLoc, Material gathered,
                                String name, String itemName, String desc, String failMssg,
                                double chance, Vector fishPath) {

        if (chance < (100 - this.fishSuccessRate)) {
            pl.sendMessage(ChatColor.RED + failMssg);
            return;
        }

        // spawn floating fish
        FloatingItemUtil.spawnFloatingItem(pl, fishLoc, gathered, 1, fishPath);

        // give the player the gathered item
        HologramUtil.createStaticHologram(pl, loc, ChatColor.GREEN + "" + ChatColor.BOLD + name, 0, 2, 0);
        if (pl.getInventory().firstEmpty() != -1) {
            pl.getInventory().addItem(gatheredItem(gathered, itemName, desc));
        } else {
            pl.getWorld().dropItem(pl.getLocation(), gatheredItem(gathered, itemName, desc));
        }

        // give the player a coin
        if (chance >= (100 - this.nuggetRate)) {
            pl.getWorld().playSound(loc, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 2.0f);
            HologramUtil.createStaticHologram(pl, loc, ChatColor.GOLD + "" + ChatColor.BOLD + "+ Coin", 0, 1.25, 0);
            if (pl.getInventory().firstEmpty() != -1) {
                pl.getInventory().addItem(goldNugget());
            } else {
                pl.getWorld().dropItem(pl.getLocation(), goldNugget());
            }
        }
    }

    private ItemStack gatheredItem(Material material, String itemName, String desc) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        ArrayList<String> lore = new ArrayList<>();
        meta.setDisplayName(ChatColor.WHITE + itemName);
        lore.add(ChatColor.GRAY + desc);
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }
    private ItemStack goldNugget() {
        ItemStack item = new ItemStack(Material.GOLD_NUGGET);
        ItemMeta meta = item.getItemMeta();
        ArrayList<String> lore = new ArrayList<>();
        meta.setDisplayName(ChatColor.GOLD + "Gold Coin");
        lore.add(ChatColor.GRAY + "Currency");
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }
    public double getFishSuccessRate() {
        return this.fishSuccessRate;
    }
    public void setFishSuccessRate(double value) {
        this.fishSuccessRate = value;
    }
    public double getNuggetRate() {
        return this.nuggetRate;
    }
    public void setNuggetRate(double value) {
        this.nuggetRate = value;
    }

    public static ItemStack getGatheringRod(int tier) {
        ItemStack item = new ItemStack(Material.FISHING_ROD);
        ItemMeta meta = item.getItemMeta();
        ((Damageable) meta).setDamage(tier);
        meta.setUnbreakable(true);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        item.setItemMeta(meta);
        return item;
    }
}
