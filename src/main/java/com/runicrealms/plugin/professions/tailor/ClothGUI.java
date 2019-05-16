package com.runicrealms.plugin.professions.tailor;

import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.attributes.AttributeUtil;
import com.runicrealms.plugin.item.LoreGenerator;
import com.runicrealms.plugin.professions.WorkstationListener;
import com.runicrealms.plugin.professions.utilities.FloatingItemUtil;
import com.runicrealms.plugin.professions.utilities.ProfExpUtil;
import com.runicrealms.plugin.enums.ArmorSlotEnum;
import com.runicrealms.plugin.utilities.GUIItem;

import java.util.concurrent.ThreadLocalRandom;

public class ClothGUI implements InventoryProvider {

    public static final SmartInventory SPIN_CLOTH = SmartInventory.builder()
            .id("clothGUI")
            .provider(new ClothGUI())
            .size(2, 9)
            .title(ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "Select an Item")
            .build();

    @Override
    public void init(Player player, InventoryContents contents) {

        // return to anvil
        contents.set(0, 4, ClickableItem.of
                (GUIItem.dispItem(Material.ANVIL,
                        ChatColor.YELLOW,
                        "Tanning Rack",
                        ChatColor.GRAY + "Click an item to craft it!\n"
                                + ChatColor.DARK_GRAY + "Click here to return to the spinning wheel"),
                        e -> {
                            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1);
                            SpinningWheelGUI.SPINNING_WHEEL_GUI.open(player);
                        }));

        // cloth
        setCraftItem(player, contents, 1, 0, Material.PAPER,
                "Cloth", "String",
                "Mail", Material.STRING, 9, 5, 1, 0);

        // cloth armor
        setCraftItem(player, contents, 1, 1, Material.SHEARS,
                "Woven Cloth Hood", "Cloth",
                "Cloth", Material.PAPER, 5, 5, 1, 5);
        setCraftItem(player, contents, 1, 4, Material.DIAMOND_BOOTS,
                "Woven Cloth Boots", "Cloth",
                "Cloth", Material.PAPER, 5, 5, 1, 0);
        setCraftItem(player, contents, 1, 3, Material.DIAMOND_LEGGINGS,
                "Woven Cloth Leggings", "Cloth",
                "Cloth", Material.PAPER, 5, 5, 1, 0);
        setCraftItem(player, contents, 1, 2, Material.DIAMOND_CHESTPLATE,
                "Woven Cloth Tunic", "Cloth",
                "Cloth", Material.PAPER, 5, 5, 1, 0);
    }

    // used for animated inventories
    @Override
    public void update(Player player, InventoryContents contents) {
    }

    private void setCraftItem(Player pl, InventoryContents contents, int row, int slot, Material craftedItem,
                              String name, String requirements, String armorType,
                              Material reagent, int itemAmt, int exp, int reqLevel, int durability) {

        // grab the location of the anvil
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

        // different color displays, depending on whether the player has the required items
        ChatColor color = ChatColor.RED;
        if (pl.getInventory().contains(reagent, itemAmt)) {
            color = ChatColor.GREEN;
        }

        String description = ChatColor.RED + "Unlock by reaching lv. " + reqLevel + "!";
        if (pl.isOp() || currentLvl >= reqLevel) {
            description = "\n"
                    + "Materials required:\n"
                    + color + requirements + ChatColor.GRAY + ", " + ChatColor.WHITE + itemAmt + "\n\n"
                    + "Success Rate:\n" + rateToStr + "%\n\n"
                    + ChatColor.WHITE + "Left Click " + ChatColor.DARK_GRAY + "to craft\n"
                    + ChatColor.WHITE + "Right Click " + ChatColor.DARK_GRAY + "to craft 5\n\n"
                    + ChatColor.ITALIC + "Rewards "
                    + ChatColor.WHITE + ChatColor.ITALIC + exp
                    + ChatColor.GRAY + ChatColor.ITALIC + " Experience";
        }

        contents.set(row, slot, ClickableItem.of
                (GUIItem.dispItem(craftedItem, ChatColor.DARK_AQUA, name, description, durability),
                        e -> {

                            // check that the player has reached the req. lv
                            if (!(pl.isOp()) && currentLvl < reqLevel) {
                                pl.playSound(pl.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5f, 1);
                                pl.sendMessage(ChatColor.RED + "You haven't learned to craft this yet!");
                                return;
                            }

                            // make 5 if right click
                            if (e.isRightClick()) {
                                startCrafting(pl, reagent, itemAmt*5, stationLoc,
                                        craftedItem, name, currentLvl, armorType, exp*5, 5, rate, durability);
                            } else {
                                startCrafting(pl, reagent, itemAmt, stationLoc,
                                        craftedItem, name, currentLvl, armorType, exp, 1, rate, durability);
                            }
                        }));
    }

    private void startCrafting(Player pl, Material reagent, int reagentAmt, Location stationLoc, Material craftedItem,
                               String name, int currentLvl, String type, int exp, int craftedAmt, int rate, int durability) {

        // check that the player has the items.
        if (!pl.getInventory().contains(reagent, reagentAmt)) {
            pl.playSound(pl.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5f, 1);
            pl.sendMessage(ChatColor.RED + "You don't have the items to craft this!");
            return;
        }

        // check that the player has an open inventory space
        if (pl.getInventory().firstEmpty() == -1 && craftedItem.getMaxStackSize() == 1) {
            pl.playSound(pl.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5f, 1);
            pl.sendMessage(ChatColor.RED + "You don't have any inventory space!");
            return;
        }

        // take player's items, add player to currently crafting ArrayList
        pl.closeInventory();
        RunicCore.getProfManager().getCurrentCrafters().add(pl);
        pl.sendMessage(ChatColor.GRAY + "Weaving...");
        ItemStack[] inv = pl.getInventory().getContents();
        for (int i = 0; i < inv.length; i++) {
            if (pl.getInventory().getItem(i) == null) continue;
            if (pl.getInventory().getItem(i).getType() == reagent) {
                pl.getInventory().getItem(i).setAmount(pl.getInventory().getItem(i).getAmount() - reagentAmt);
                break;
            }
        }

        // spawn item on anvil for visual
        FloatingItemUtil.spawnFloatingItem(pl, stationLoc, craftedItem, 4, durability);

        // start the crafting process
        new BukkitRunnable() {
            int count = 0;

            @Override
            public void run() {
                if (count > 3) {
                    this.cancel();
                    RunicCore.getProfManager().getCurrentCrafters().remove(pl);
                    pl.playSound(pl.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 2.0f, 1.0f);
                    pl.sendMessage(ChatColor.GREEN + "Done!");
                    ProfExpUtil.giveExperience(pl, exp);
                    craftArmor(pl, craftedItem, name, currentLvl, craftedAmt, rate, durability);
                } else {
                    pl.playSound(pl.getLocation(), Sound.BLOCK_SAND_BREAK, 2.0f, 0.5f);
                    pl.spawnParticle(Particle.CRIT, stationLoc, 5, 0.25, 0.25, 0.25, 0.01);
                    count = count + 1;
                }
            }
        }.runTaskTimer(RunicCore.getInstance(), 0, 20);
    }

    private void craftArmor(Player pl, Material material, String dispName,
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
            int minHP = 1;
            int maxHP;
            if (currentLvl == 0) {
                maxHP = 2;
            } else {
                maxHP = currentLvl+1;
            }
            int range = ThreadLocalRandom.current().nextInt(minHP, maxHP + 1);

            craftedItem = AttributeUtil.addGenericStat
                    (craftedItem, "generic.maxHealth", range, itemSlot);

            // item can be socketed once
            craftedItem = AttributeUtil.addCustomStat(craftedItem, "custom.socketCount", 1);

            LoreGenerator.generateItemLore(craftedItem, ChatColor.DARK_AQUA, dispName, "");

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
