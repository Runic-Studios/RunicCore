package com.runicrealms.plugin.professions;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.attributes.AttributeUtil;
import com.runicrealms.plugin.enums.ArmorSlotEnum;
import com.runicrealms.plugin.item.ItemGUI;
import com.runicrealms.plugin.item.LoreGenerator;
import com.runicrealms.plugin.professions.utilities.FloatingItemUtil;
import com.runicrealms.plugin.professions.utilities.ProfExpUtil;
import com.runicrealms.plugin.utilities.ColorUtil;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.LinkedHashMap;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Basic workstation class with some handy methods
 */
public abstract class Workstation {

    public ItemGUI openWorkstation(Player pl) {

        return new ItemGUI("&f&l" + pl.getName() + "'s &e&lWorkstation", 9, event -> {
        },
                RunicCore.getInstance()).setOption(5, new ItemStack(Material.BARRIER),
                "&cClose", "&7Close the menu", 0);
    }

    public ItemGUI craftingMenu(Player pl, int size) {

        return new ItemGUI("&f&l" + pl.getName() + "'s Crafting Menu", size, event -> {
        },
                RunicCore.getInstance());
    }

    public void createCraftableItem(ItemGUI gui, Player pl, int slot, Material itemType, String name,
                                    String armorType, LinkedHashMap<Material, Integer> itemReqs,
                                    String reqsToString, int itemAmt, int exp, int reqLevel, int durability,
                                    String itemStats) {

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

        // build the menu display
        String desc = "";
        if (currentLvl < reqLevel) {
            desc += "&cUnlock by reaching lv. " + reqLevel + "!\n";
        }

        desc += "\n&7Item Stats:\n" + itemStats;
        desc += "\n\n&7Material(s) Required:\n";

        String[] reqsAsList = reqsToString.split("\n");
        if (pl.isOp() || currentLvl >= reqLevel) {

            // add every item in the reagents keyset with its associated amount.
            // if there is only one reagent in the keyset, it uses the 'itemAmt' field instead.
            int i = 0;

                for (Material reagent : itemReqs.keySet()) {
                    int amt = itemReqs.get(reagent);
                    if (reqsAsList.length <= 1) {
                        amt = itemAmt;
                    }
                    if (pl.getInventory().contains(reagent, amt)) {
                        desc += "&a" + reqsAsList[i] + "&7, &f" + amt;
                    } else {
                        desc += ("&c" + reqsAsList[i] + "&7, &f" + amt);
                    }
                    i += 1;
                }


            desc += "\n\n&7Success Rate:\n" + rateToStr + "%\n\n"
                    + ChatColor.WHITE + "Left Click " + ChatColor.DARK_GRAY + "to craft\n"
                    + ChatColor.WHITE + "Right Click " + ChatColor.DARK_GRAY + "to craft 5\n\n"
                    + "&7&oRewards &f&o" + exp + " &7&oExperience";
        }

        desc = ColorUtil.format(desc);

        gui.setOption(slot, new ItemStack(itemType),
                name, desc, durability);
    }

    public void startCrafting(Player pl, LinkedHashMap<Material, Integer> itemReqs, int itemAmt, int reqLevel,
                               /*.Location stationLoc,*/ Material craftedItemType, String itemName,
                               int currentLvl, String type, int exp, int craftedAmt, /*int rate,*/
                               int durability, Particle particle, Sound soundCraft, Sound soundDone) {

        if (RunicCore.getProfManager().getCurrentCrafters().contains(pl)) return;

        // grab the location of the anvil
        Location stationLoc = WorkstationListener.getStationLocation().get(pl.getUniqueId());

        int rate = (40+currentLvl);

        // check that the player has reached the req. lv
        if (!(pl.isOp()) && currentLvl < reqLevel) {
            pl.playSound(pl.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5f, 1);
            pl.sendMessage(ChatColor.RED + "You haven't learned to craft this yet!");
            return;
        }

        // check that the player has the reagents
        for (Material reagent : itemReqs.keySet()) {
            int amt = itemReqs.get(reagent);
            if (itemReqs.size() <= 1) {
                amt = itemAmt;
            }
            if (!pl.getInventory().contains(reagent, amt)) {
                pl.playSound(pl.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5f, 1);
                pl.sendMessage(ChatColor.RED + "You don't have the items to craft this!");
                return;
            }
        }

        // check that the player has an open inventory space
        if (pl.getInventory().firstEmpty() == -1 && craftedItemType.getMaxStackSize() == 1) {
            pl.playSound(pl.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5f, 1);
            pl.sendMessage(ChatColor.RED + "You don't have any inventory space!");
            return;
        }

        // check that the player has an open inventory space (if the item is stackable)
        ItemStack[] inv = pl.getInventory().getContents();
        if (pl.getInventory().firstEmpty() == -1 && craftedItemType.getMaxStackSize() != 1) {
            for (int i = 0; i < inv.length; i++) {
                if (pl.getInventory().getItem(i) == null) continue;
                if (pl.getInventory().getItem(i).getType() == craftedItemType
                        && pl.getInventory().getItem(i).getAmount() + craftedAmt > craftedItemType.getMaxStackSize()) {
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
        pl.sendMessage(ChatColor.GRAY + "Crafting...");
        //int j = 0;
        for (Material reagent : itemReqs.keySet()) {
            int amt = itemReqs.get(reagent);
            if (itemReqs.size() <= 1) {
                amt = itemAmt;
            }
            for (int i = 0; i < inv.length; i++) {
                if (pl.getInventory().getItem(i) == null) continue;
                if (pl.getInventory().getItem(i).getType() == reagent) {
                    pl.getInventory().getItem(i).setAmount(pl.getInventory().getItem(i).getAmount()-amt);
                    break;
                }
            }
//            if (j == 0) {
//                dispItem = req;
//            }
            //j += 1;
        }

        // spawn item on workstation for visual
        FloatingItemUtil.spawnFloatingItem(pl, stationLoc, craftedItemType, 4, durability);

        // start the crafting process
        new BukkitRunnable() {
            int count = 0;

            @Override
            public void run() {
                if (count > 3) {
                    this.cancel();
                    RunicCore.getProfManager().getCurrentCrafters().remove(pl);
                    pl.playSound(pl.getLocation(), soundDone, 0.5f, 1.0f);
                    pl.sendMessage(ChatColor.GREEN + "Done!");
                    ProfExpUtil.giveExperience(pl, exp);
                    produceResult(pl, craftedItemType, itemName, currentLvl, craftedAmt, rate, durability);
                } else {
                    pl.playSound(pl.getLocation(), soundCraft, 0.5f, 2.0f);
                    pl.spawnParticle(particle, stationLoc, 5, 0.25, 0.25, 0.25, 0.01);
                    count = count + 1;
                }
            }
        }.runTaskTimer(RunicCore.getInstance(), 0, 20);
    }

    private void produceResult(Player pl, Material material, String dispName,
                            int currentLvl, int amt, int rate, int durability) {

        // create a new item up to the amount
        int failCount = 0;
        for (int i = 0; i < amt; i++) {

            ItemStack craftedItem = new ItemStack(material);
            ItemMeta meta = craftedItem.getItemMeta();
            ((Damageable) meta).setDamage(durability);
            craftedItem.setItemMeta(meta);

            String itemSlot = "";
            ArmorSlotEnum armorType = ArmorSlotEnum.matchSlot(craftedItem);
            switch (armorType) {
                case HELMET:
                    itemSlot = "head";
                    break;
                case CHESTPLATE:
                    itemSlot = "chest";
                    break;
                case LEGGINGS:
                    itemSlot = "legs";
                    break;
                case BOOTS:
                    itemSlot = "feet";
                    break;
            }

            // item will have a random health value that increases w/ prof lv
            int health;
            if (currentLvl < 30) {
                health = 5;
            } else if (currentLvl < 50) {
                health = 15;
            } else {
                health = 25;
            }

            craftedItem = AttributeUtil.addGenericStat
                    (craftedItem, "generic.maxHealth", health, itemSlot);

            // item can be socketed once
            craftedItem = AttributeUtil.addCustomStat(craftedItem, "custom.socketCount", 1);

            LoreGenerator.generateItemLore(craftedItem, ChatColor.WHITE, dispName, "");

            double chance = ThreadLocalRandom.current().nextDouble(0, 100);
            if (chance <= rate) {
                // check that the player has an open inventory space
                // this method prevents items from stacking if the player crafts 5
                if (pl.getInventory().firstEmpty() != -1) {
                    int firstEmpty = pl.getInventory().firstEmpty();
                    pl.getInventory().setItem(firstEmpty, craftedItem);
                } else {
                    pl.getWorld().dropItem(pl.getLocation(), craftedItem);
                }
            } else {
                failCount = failCount + 1;
            }
        }

        // display fail message
        if (failCount == 0) return;
        pl.sendMessage(ChatColor.RED + "You fail to craft this item. [x" + failCount + "]");
    }
}
