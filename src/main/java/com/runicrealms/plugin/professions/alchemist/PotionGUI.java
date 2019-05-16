package com.runicrealms.plugin.professions.alchemist;

import com.runicrealms.plugin.professions.WorkstationListener;
import com.runicrealms.plugin.professions.utilities.ProfExpUtil;
import com.runicrealms.plugin.utilities.GUIItem;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.professions.utilities.FloatingItemUtil;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.concurrent.ThreadLocalRandom;

public class PotionGUI implements InventoryProvider {

    public static final SmartInventory BREW_POTIONS = SmartInventory.builder()
            .id("potionGUI")
            .provider(new PotionGUI())
            .size(2, 9)
            .title(ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "Select an Item")
            .build();

    @Override
    public void init(Player player, InventoryContents contents) {

        // return to workstation
        contents.set(0, 4, ClickableItem.of
                (GUIItem.dispItem(Material.CAULDRON, ChatColor.YELLOW, "Cauldron",
                        ChatColor.GRAY + "Click an item to brew it!\n"
                                + ChatColor.DARK_GRAY + "Click here to return to the cauldron"),
                        e -> {
                            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1);
                            CauldronGUI.CAULDRON_GUI.open(player);
                        }));

        // create reagent hashmap
        LinkedHashMap<Material, Integer> ironLinkReqs = new LinkedHashMap<>();
        ironLinkReqs.put(Material.IRON_ORE, 1);
        ironLinkReqs.put(Material.SPRUCE_LOG, 2);
        setSmeltItem(player, contents, 1, 0, Material.IRON_BARS,
                "Chain Link", ironLinkReqs, "Iron Ore\nSpruce Log", 3, 1);

        LinkedHashMap<Material, Integer> ironBarReqs = new LinkedHashMap<>();
        ironBarReqs.put(Material.IRON_ORE, 1);
        ironBarReqs.put(Material.OAK_LOG, 2);
        setSmeltItem(player, contents, 1, 1, Material.IRON_INGOT,
                "Iron Bar", ironBarReqs, "Iron Ore\nOak Log", 6, 20);
    }

    // used for animated inventories
    @Override
    public void update(Player player, InventoryContents contents) {
    }

    private void setSmeltItem(Player pl, InventoryContents contents, int row, int slot, Material material, String name,
                              LinkedHashMap<Material, Integer> itemReqs, String reqsToString, int exp, int reqLevel) {

        // grab the location of the workstation
        Location stationLoc = WorkstationListener.getStationLocation().get(pl.getUniqueId());

        // grab the player's current profession level, progress toward that level
        int currentLvl = RunicCore.getInstance().getConfig().getInt(pl.getUniqueId() + ".info.prof.level");

        // determine the success rate, based on level
        int rate = (40+currentLvl);
        String rateToStr;
        if (rate < 50) {
            rateToStr = ChatColor.RED + "" + rate;
        } else if (rate < 80) {
            rateToStr = ChatColor.YELLOW + "" + rate;
        } else {
            rateToStr = ChatColor.GREEN + "" + rate;
        }

        // if the player has the item to craft unlocked, build its tooltip
        ArrayList<String> desc = new ArrayList<>();
        if (!pl.isOp() && currentLvl < reqLevel) {
            desc.add(ChatColor.RED + "Unlock by reaching lv. " + reqLevel + "!");
        } else {
            String[] reqsAsList = reqsToString.split("\n");
            desc.add("");
            desc.add(ChatColor.GRAY + "Materials required:");

            // add every item in the reagents keyset
            int i = 0;
            for (Material reagent : itemReqs.keySet()) {
                if (pl.getInventory().contains(reagent, itemReqs.get(reagent))) {
                    desc.add(ChatColor.GREEN + reqsAsList[i] + ChatColor.GRAY + ", " + ChatColor.WHITE + itemReqs.get(reagent));
                } else {
                    desc.add(ChatColor.RED + reqsAsList[i] + ChatColor.GRAY + ", " + ChatColor.WHITE + itemReqs.get(reagent));
                }
                i += 1;
            }

            desc.add("");
            desc.add(ChatColor.GRAY + "Success Rate:");
            desc.add(rateToStr + "%");
            desc.add("");
            desc.add(ChatColor.WHITE + "Left Click " + ChatColor.DARK_GRAY + "to craft");
            desc.add(ChatColor.WHITE + "Right Click " + ChatColor.DARK_GRAY + "to craft 5");
            desc.add("");
            desc.add(ChatColor.GRAY + "" + ChatColor.ITALIC + "Rewards "
                    + ChatColor.WHITE + ChatColor.ITALIC + exp
                    + ChatColor.GRAY + ChatColor.ITALIC + " experience");
        }

        contents.set(row, slot, ClickableItem.of
                (GUIItem.dispItem(material, ChatColor.WHITE, name, desc),
                        e -> {

                            // ensure player is correct level
                            if (!pl.isOp() && currentLvl < reqLevel) {
                                pl.playSound(pl.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5f, 1);
                                pl.sendMessage(ChatColor.RED + "You haven't learned to craft this yet!");
                                return;
                            }

                            // make 5 if right click
                            if (e.isRightClick()) {
                                startSmelting(pl, itemReqs, stationLoc, material, name, exp*5, 5, rate);
                            } else {
                                startSmelting(pl, itemReqs, stationLoc, material, name, exp, 1, rate);
                            }
                        }));
    }

    private void startSmelting(Player pl, LinkedHashMap<Material, Integer> itemReqs, Location loc,
                               Material craftedItem, String name, int exp, int craftedAmt, int rate) {

        // check that the player has the reagents
        for (Material req : itemReqs.keySet()) {
            if (!pl.getInventory().contains(req, itemReqs.get(req)*craftedAmt)) {
                pl.playSound(pl.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5f, 1);
                pl.sendMessage(ChatColor.RED + "You don't have the items to craft this!");
                return;
            }
        }

        // check that the player has an open inventory space
        if (pl.getInventory().firstEmpty() == -1 && craftedItem.getMaxStackSize() == 1) {
            pl.playSound(pl.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5f, 1);
            pl.sendMessage(ChatColor.RED + "You don't have any inventory space!");
            return;
        }

        // check that the player has an open inventory space (if the item is stackable)
        ItemStack[] inv = pl.getInventory().getContents();
        if (pl.getInventory().firstEmpty() == -1 && craftedItem.getMaxStackSize() != 1) {
            for (int i = 0; i < inv.length; i++) {
                if (pl.getInventory().getItem(i) == null) continue;
                if (pl.getInventory().getItem(i).getType() == craftedItem
                        && pl.getInventory().getItem(i).getAmount() + craftedAmt > craftedItem.getMaxStackSize()) {
                    pl.playSound(pl.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5f, 1);
                    pl.sendMessage(ChatColor.RED + "You don't have any inventory space!");
                    return;
                }
            }
        }

        // if player has everything, take player's items, display first reagent visually
        // add player to currently crafting ArrayList
        pl.closeInventory();
        RunicCore.getProfManager().getCurrentCrafters().add(pl);
        pl.sendMessage(ChatColor.GRAY + "Smelting...");
        int j = 0;
        Material dispItem = Material.STONE;
        for (Material req : itemReqs.keySet()) {
            for (int i = 0; i < inv.length; i++) {
                if (pl.getInventory().getItem(i) == null) continue;
                if (pl.getInventory().getItem(i).getType() == req) {
                    pl.getInventory().getItem(i).setAmount(pl.getInventory().getItem(i).getAmount()-itemReqs.get(req)*craftedAmt);
                    break;
                }
            }
            if (j == 0) {
                dispItem = req;
            }
            j += 1;
        }

        // show visual reagent
        FloatingItemUtil.spawnFloatingItem(pl, loc, dispItem, 3);

        // begin crafting process
        new BukkitRunnable() {
            int count = 0;
            @Override
            public void run() {
                if (count == 3) {
                    FloatingItemUtil.spawnFloatingItem(pl, loc, craftedItem, 1);
                }
                if (count > 3) {
                    this.cancel();
                    RunicCore.getProfManager().getCurrentCrafters().remove(pl);
                    pl.playSound(pl.getLocation(), Sound.BLOCK_LAVA_EXTINGUISH, 0.5f, 0.2f);
                    pl.sendMessage(ChatColor.GREEN + "Done!");
                    ProfExpUtil.giveExperience(pl, exp);
                    smeltItem(pl, craftedItem, name, craftedAmt, rate);
                } else {
                    pl.playSound(pl.getLocation(), Sound.BLOCK_LAVA_POP, 0.5f, 1.0f);
                    pl.playSound(pl.getLocation(), Sound.ITEM_BUCKET_FILL_LAVA, 0.5f, 1.0f);
                    pl.spawnParticle(Particle.FLAME, loc, 25, 0.25, 0.25, 0.25, 0.01);
                    count = count + 1;
                }
            }
        }.runTaskTimer(RunicCore.getInstance(), 0, 20);
    }

    // gives the player the crafted item
    private void smeltItem(Player pl, Material material, String dispName, int amt, int rate) {

        int failCount = 0;
        for (int i = 0; i < amt; i++) {

            ItemStack smeltedItem = new ItemStack(material);
            ItemMeta meta = smeltedItem.getItemMeta();
            meta.setDisplayName(ChatColor.WHITE + dispName);
            ArrayList<String> lore = new ArrayList<String>();
            lore.add(ChatColor.GRAY + "Crafting Reagent");
            meta.setLore(lore);
            smeltedItem.setItemMeta(meta);

            double chance = ThreadLocalRandom.current().nextDouble(0, 100);
            if (chance <= rate) {
                pl.getInventory().addItem(smeltedItem);
            } else {
                failCount = failCount + 1;
            }
        }

        // display fail message
        if (failCount == 0) return;
        pl.sendMessage(ChatColor.RED + "You fail to craft this item. [x" + failCount + "]");
    }
}
