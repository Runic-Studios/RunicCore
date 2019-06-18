package com.runicrealms.plugin.professions.gathering;

import com.runicrealms.plugin.attributes.AttributeUtil;
import com.runicrealms.plugin.utilities.CurrencyUtil;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import com.runicrealms.plugin.utilities.HologramUtil;
import com.runicrealms.plugin.enums.WeaponEnum;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Listener for Mining (Gathering Profesion)
 * adds blocks material type and coordinates to yml file
 * for the ProfManager to respawn at intervals, checks name of
 * WG region for "mine" to perform tasks
 */
public class MiningListener implements Listener {

    private double nuggetRate = 5.0;

    @EventHandler
    public void onResourceBreak(BlockBreakEvent e) {

        // grab the player, location
        Player pl = e.getPlayer();
        Location plLoc = pl.getLocation();

        // grab all regions the player is standing in
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionQuery query = container.createQuery();
        ApplicableRegionSet set = query.getApplicableRegions(BukkitAdapter.adapt(plLoc));
        Set<ProtectedRegion> regions = set.getRegions();

        if (regions == null) return;

        boolean canMine = false;

        // check the region for the keyword 'mine'
        // ignore the rest of this event if the player cannot mine
        for (ProtectedRegion region : regions) {
            if (region.getId().contains("mine")) {
                canMine = true;
            }
        }

        if (!canMine) {
            //pl.sendMessage(ChatColor.RED + "You can't mine this here.");
            e.setCancelled(true);
            return;
        }

        if (e.getBlock().getType() == null) return;
        if (pl.getGameMode() == GameMode.CREATIVE) return;

        double chance = ThreadLocalRandom.current().nextDouble(0, 100);

        Block block = e.getBlock();
        Location loc = block.getLocation().add(0.5, 0, 0.5);
        Material oldType = block.getType();

        File regenBlocks = new File(Bukkit.getServer().getPluginManager().getPlugin("RunicCore").getDataFolder(),
                "regen_blocks.yml");
        FileConfiguration blockLocations = YamlConfiguration.loadConfiguration(regenBlocks);

        Material placeHolderType;
        Material itemType;
        String holoString;
        String itemName;
        String desc = "Raw Material";
        String subPath;

        switch (block.getType()) {
            case IRON_ORE:
                placeHolderType = Material.COBBLESTONE;
                itemType = Material.IRON_ORE;
                holoString = "+ Iron";
                itemName = "Iron Ore";
                subPath = "ORES";
                break;
            case GOLD_ORE:
                placeHolderType = Material.COBBLESTONE;
                itemType = Material.GOLD_ORE;
                holoString = "+ Gold";
                itemName = "Gold Ore";
                subPath = "ORES";
                break;
            case REDSTONE_ORE:
                placeHolderType = Material.COBBLESTONE;
                itemType = Material.REDSTONE_ORE;
                holoString = "+ Ruby";
                itemName = "Uncut Ruby";
                subPath = "ORES";
                break;
            case LAPIS_ORE:
                placeHolderType = Material.COBBLESTONE;
                itemType = Material.LAPIS_ORE;
                holoString = "+ Sapphire";
                itemName = "Uncut Sapphire";
                subPath = "ORES";
                break;
            case NETHER_QUARTZ_ORE:
                placeHolderType = Material.COBBLESTONE;
                itemType = Material.NETHER_QUARTZ_ORE;
                holoString = "+ Opal";
                itemName = "Uncut Opal";
                subPath = "ORES";
                break;
            case EMERALD_ORE:
                placeHolderType = Material.COBBLESTONE;
                itemType = Material.EMERALD_ORE;
                holoString = "+ Emerald";
                itemName = "Uncut Emerald";
                subPath = "ORES";
                break;
            case DIAMOND_ORE:
                placeHolderType = Material.COBBLESTONE;
                itemType = Material.DIAMOND_ORE;
                holoString = "+ Diamond";
                itemName = "Uncut Diamond";
                subPath = "ORES";
                break;
            default:
                return;
        }

        e.setCancelled(true);

        if (pl.getInventory().getItemInMainHand().getType() == Material.AIR) {
            pl.sendMessage(ChatColor.RED + "You need a mining pick to do that!");
            return;
        }

        // make sure player has harvesting tool, corresponding to the tier.
        // with durability magic, a durability 1 iron axe will display as wood, 5 as diamond, etc.
        ItemStack heldItem = pl.getInventory().getItemInMainHand();
        int slot = pl.getInventory().getHeldItemSlot();
        ItemMeta meta = heldItem.getItemMeta();
        int durability = ((Damageable) Objects.requireNonNull(meta)).getDamage();

        if (heldItem.getType() != Material.IRON_PICKAXE
                && durability != 1
                && durability != 2
                && durability != 3
                && durability != 4
                && durability != 5) {
            pl.sendMessage(ChatColor.RED + "You need a mining pick to do that!");
            return;
        }

        // reduce items durability
        double itemDurab = AttributeUtil.getCustomDouble(heldItem, "durability");
        heldItem = AttributeUtil.addCustomStat(heldItem, "durability", itemDurab-1);
        GatheringUtil.generateToolLore(heldItem, durability);
        if (itemDurab - 1 <= 0) {

            pl.playSound(pl.getLocation(), Sound.ENTITY_ITEM_BREAK, 0.5f, 1.0f);
            pl.sendMessage(ChatColor.RED + "Your tool broke!");
            pl.getInventory().setItem(slot, null);
        } else {
            pl.getInventory().setItem(slot, heldItem);
        }

        // gather material
        gatherMaterial(pl, loc, block, placeHolderType, itemType, holoString,
                itemName, desc, "You fail to gather any resources.", chance, durability);
        saveBlockLocation(regenBlocks, blockLocations, subPath, block, oldType);
    }


    private void gatherMaterial(Player pl, Location loc, Block b,
                                Material placeholder, Material gathered, String name, String itemName,
                                String desc, String failMssg, double chance, int tier) {

        b.setType(placeholder);

        double successRate;
        switch (tier) {
            case 5:
                successRate = 75;
                break;
            case 4:
                successRate = 62.5;
                break;
            case 3:
                successRate = 50;
                break;
            case 2:
                successRate = 37.5;
                break;
            case 1:
            default:
                successRate = 25;
                break;
        }

        if (chance < (100 - successRate)) {
            pl.sendMessage(ChatColor.RED + failMssg);
            return;
        }

        // give the player the gathered item
        if (loc.clone().add(0, 1.5, 0).getBlock().getType() == Material.AIR) {
            HologramUtil.createStaticHologram(pl, loc, ChatColor.GREEN + "" + ChatColor.BOLD + name, 0, 2, 0);
        }
        if (pl.getInventory().firstEmpty() != -1) {
            pl.getInventory().addItem(gatheredItem(gathered, itemName, desc));
        } else {
            pl.getWorld().dropItem(pl.getLocation(), gatheredItem(gathered, itemName, desc));
        }

        // give the player a coin
        if (chance >= (100 - this.nuggetRate)) {
            b.getWorld().playSound(loc, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 2.0f);
            HologramUtil.createStaticHologram(pl, loc, ChatColor.GOLD + "" + ChatColor.BOLD + "+ Coin", 0, 1.25, 0);
            if (pl.getInventory().firstEmpty() != -1) {
                pl.getInventory().addItem(CurrencyUtil.goldCoin());
            } else {
                pl.getWorld().dropItem(pl.getLocation(), CurrencyUtil.goldCoin());
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
    public double getNuggetRate() {
        return this.nuggetRate;
    }
    public void setNuggetRate(double value) {
        this.nuggetRate = value;
    }
    private void saveBlockLocation(File file, FileConfiguration fileConfig, String subPath, Block b, Material oldType) {

        int firstAvailableID = fileConfig.getInt(b.getWorld().getName() + ".NEXT_ID_" + subPath);

        fileConfig.set(b.getWorld().getName() + "." + subPath + "." + firstAvailableID + ".type", oldType.toString());
        fileConfig.set(b.getWorld().getName() + "." + subPath + "." + firstAvailableID + ".x", b.getLocation().getBlockX());
        fileConfig.set(b.getWorld().getName() + "." + subPath + "." + firstAvailableID + ".y", b.getLocation().getBlockY());
        fileConfig.set(b.getWorld().getName() + "." + subPath + "." + firstAvailableID + ".z", b.getLocation().getBlockZ());

        fileConfig.set(b.getWorld().getName() + ".NEXT_ID_" + subPath, firstAvailableID+1);

        // save data file
        try {
            fileConfig.save(file);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
