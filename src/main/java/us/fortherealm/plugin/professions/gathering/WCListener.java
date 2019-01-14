package us.fortherealm.plugin.professions.gathering;

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
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import us.fortherealm.plugin.utilities.HologramUtil;
import us.fortherealm.plugin.utilities.WeaponEnum;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Listener for Woodcutting (Gathering Profesion)
 * adds blocks material type and coordinates to yml file
 * for the ProfManager to respawn at intervals, checks name of
 * WG region for "lumber" to perform tasks
 */
public class WCListener implements Listener {

    private double logSuccessRate = 75.0;
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

        boolean canChop = false;

        // check the region for the keyword 'lumber'
        // ignore the rest of this event if the player cannot mine
        for (ProtectedRegion region : regions) {
            if (region.getId().contains("lumber")) {
                canChop = true;
            }
        }

        if (!canChop) {
            //pl.sendMessage(ChatColor.RED + "You can't chop this here.");
            return;
        }

        if (e.getBlock().getType() == null) return;
        if (pl.getGameMode() == GameMode.CREATIVE) return;

        double chance = ThreadLocalRandom.current().nextDouble(0, 100);

        Block block = e.getBlock();
        Location loc = block.getLocation().add(0.5, 0, 0.5);
        Material oldType = block.getType();

        File regenBlocks = new File(Bukkit.getServer().getPluginManager().getPlugin("FTRCore").getDataFolder(),
                "regen_blocks.yml");
        FileConfiguration blockLocations = YamlConfiguration.loadConfiguration(regenBlocks);

        Material placeHolderType;
        Material itemType;
        String holoString;
        String itemName;
        String desc = "Raw Material";
        String subPath;

        switch (block.getType()) {
            // woodcutting
            case OAK_LOG:
                placeHolderType = Material.OAK_PLANKS;
                itemType = Material.OAK_LOG;
                holoString = "+ Oak";
                itemName = "Oak Log";
                subPath = "LOGS";
                break;
            case SPRUCE_LOG:
                placeHolderType = Material.SPRUCE_PLANKS;
                itemType = Material.SPRUCE_LOG;
                holoString = "+ Spruce";
                itemName = "Spruce Log";
                subPath = "LOGS";
                break;
            case BIRCH_LOG:
                placeHolderType = Material.BIRCH_PLANKS;
                itemType = Material.BIRCH_LOG;
                holoString = "+ Birch";
                itemName = "Birch Log";
                subPath = "LOGS";
                break;
            case JUNGLE_LOG:
                placeHolderType = Material.JUNGLE_PLANKS;
                itemType = Material.JUNGLE_LOG;
                holoString = "+ Jungle";
                itemName = "Jungle Log";
                subPath = "LOGS";
                break;
            case ACACIA_LOG:
                placeHolderType = Material.ACACIA_PLANKS;
                itemType = Material.ACACIA_LOG;
                holoString = "+ Acacia";
                itemName = "Acacia Log";
                subPath = "LOGS";
                break;
            case DARK_OAK_LOG:
                placeHolderType = Material.DARK_OAK_PLANKS;
                itemType = Material.DARK_OAK_LOG;
                holoString = "+ Dark Oak";
                itemName = "Dark Oak Log";
                subPath = "LOGS";
                break;
            default:
                return;
        }

        e.setCancelled(true);

        // make sure player has harvesting tool
        // we also ensure it has durability 100, arbitrarily chosen.
        if (pl.getInventory().getItemInMainHand() == null) return;
        WeaponEnum heldItem = WeaponEnum.matchType(pl.getInventory().getItemInMainHand());
        ItemMeta meta = pl.getInventory().getItemInMainHand().getItemMeta();
        int durability = ((Damageable) meta).getDamage();

        if (heldItem == null) {
            pl.sendMessage(ChatColor.RED + "You need a woodcutting axe to do that!");
            return;
        }

        if (!(heldItem.equals(WeaponEnum.AXE)) || durability != 100) {
            pl.sendMessage(ChatColor.RED + "You need a woodcutting axe to do that!");
            return;
        }

        gatherMaterial(pl, loc, block, placeHolderType, itemType, holoString,
                itemName, desc, "You fail to gather any resources.", chance);
        saveBlockLocation(regenBlocks, blockLocations, subPath, block, oldType);
    }


    private void gatherMaterial(Player pl, Location loc, Block b,
                                Material placeholder, Material gathered, String name, String itemName,
                                String desc, String failMssg, double chance) {

        b.setType(placeholder);

        if (chance < (100 - this.logSuccessRate)) {
            pl.sendMessage(ChatColor.RED + failMssg);
            return;
        }

        // give the player the gathered item
        HologramUtil.createStaticHologram(pl, loc, ChatColor.GREEN + "" + ChatColor.BOLD + name, 0, 2, 0);
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
    public double getLogSuccessRate() {
        return this.logSuccessRate;
    }
    public void setLogSuccessRate(double value) {
        this.logSuccessRate = value;
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
