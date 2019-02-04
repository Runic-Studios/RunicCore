package us.fortherealm.plugin.professions.blacksmith;

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
import us.fortherealm.plugin.Main;
import us.fortherealm.plugin.attributes.AttributeUtil;
import us.fortherealm.plugin.item.LoreGenerator;
import us.fortherealm.plugin.professions.WorkstationListener;
import us.fortherealm.plugin.professions.utilities.FloatingItemUtil;
import us.fortherealm.plugin.professions.utilities.ProfExpUtil;
import us.fortherealm.plugin.enums.ArmorSlotEnum;
import us.fortherealm.plugin.utilities.GUIItem;

import java.util.concurrent.ThreadLocalRandom;

public class ArmorGUI implements InventoryProvider {

    public static final SmartInventory CRAFT_ARMOR = SmartInventory.builder()
            .id("armorGUI")
            .provider(new ArmorGUI())
            .size(2, 9)
            .title(ChatColor.WHITE + "" + ChatColor.BOLD + "Select an Item")
            .build();

    @Override
    public void init(Player player, InventoryContents contents) {

        // return to anvil
        contents.set(0, 4, ClickableItem.of
                (GUIItem.dispItem(Material.ANVIL,
                        ChatColor.YELLOW,
                        "Anvil",
                        ChatColor.GRAY + "Click an item to craft it!\n"
                                + ChatColor.DARK_GRAY + "Click here to return to the anvil"),
                        e -> {
                            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1);
                            AnvilGUI.ANVIL_GUI.open(player);
                        }));

        // mail
        setCraftItem(player, contents, 1, 0, Material.SHEARS,
                "Forged Chain Helmet", "Iron Link",
                "Mail", Material.IRON_BARS, 5, 5, 1, 15);

        setCraftItem(player, contents, 1, 1, Material.CHAINMAIL_BOOTS,
                "Forged Chain Boots", "Iron Link",
                "Mail", Material.IRON_BARS, 4, 5, 5, 0);

        setCraftItem(player, contents, 1, 2, Material.CHAINMAIL_LEGGINGS,
                "Forged Chain Legs", "Iron Link",
                "Mail", Material.IRON_BARS, 7, 10, 10, 0);

        setCraftItem(player, contents, 1, 3, Material.CHAINMAIL_CHESTPLATE,
                "Forged Chain Body", "Iron Link",
                "Mail", Material.IRON_BARS, 8, 15, 15, 0);

        // plate
        setCraftItem(player, contents, 1, 4, Material.SHEARS,
                "Forged Iron Helmet", "Iron Bar",
                "Plate", Material.IRON_INGOT, 5, 10, 20, 20);

        setCraftItem(player, contents, 1, 5, Material.IRON_BOOTS,
                "Forged Iron Boots", "Iron Bar",
                "Plate", Material.IRON_INGOT, 4, 10, 25, 0);

        setCraftItem(player, contents, 1, 6, Material.IRON_LEGGINGS,
                "Forged Iron Platelegs", "Iron Bar",
                "Plate", Material.IRON_INGOT, 7, 20, 30, 0);

        setCraftItem(player, contents, 1, 7, Material.IRON_CHESTPLATE,
                "Forged Iron Platebody", "Iron Bar",
                "Plate", Material.IRON_INGOT, 8, 30, 35, 0);
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
        int currentLvl = Main.getInstance().getConfig().getInt(pl.getUniqueId() + ".info.prof.level");

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
                (GUIItem.dispItem(craftedItem, ChatColor.WHITE, name, description, durability),
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
        Main.getProfManager().getCurrentCrafters().add(pl);
        pl.sendMessage(ChatColor.GRAY + "Forging...");
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
                    Main.getProfManager().getCurrentCrafters().remove(pl);
                    pl.playSound(pl.getLocation(), Sound.BLOCK_ANVIL_USE, 0.5f, 1.0f);
                    pl.sendMessage(ChatColor.GREEN + "Done!");
                    ProfExpUtil.giveExperience(pl, exp);
                    craftArmor(pl, craftedItem, name, currentLvl, craftedAmt, rate, durability);
                } else {
                    pl.playSound(pl.getLocation(), Sound.BLOCK_ANVIL_PLACE, 0.5f, 2.0f);
                    pl.spawnParticle(Particle.FIREWORKS_SPARK, stationLoc, 5, 0.25, 0.25, 0.25, 0.01);
                    count = count + 1;
                }
            }
        }.runTaskTimer(Main.getInstance(), 0, 20);
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
