package com.runicrealms.plugin.professions.crafting;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.item.GUIMenu.ItemGUI;
import com.runicrealms.plugin.item.LegendaryManager;
import com.runicrealms.plugin.professions.Workstation;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Objects;

public class TailorGUI extends Workstation {

    public TailorGUI() {
    }

    public ItemGUI openMenu(Player pl) {

        // name the menu
        ItemGUI tailorMenu = super.openWorkstation(pl);
        tailorMenu.setName("&f&l" + pl.getName() + "'s &e&lSpinning Wheel");

        //set the visual items
        tailorMenu.setOption(3, new ItemStack(Material.PAPER),
                "&fWeave Cloth", "&7Weave cloth and linen goods!", 0, false);

        // set the handler
        tailorMenu.setHandler(event -> {

            if (event.getSlot() == 3) {

                // open the crafting menu
                pl.playSound(pl.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1);
                ItemGUI wheel = openWheelMenu(pl);
                wheel.open(pl);
                event.setWillClose(false);
                event.setWillDestroy(true);

            } else if (event.getSlot() == 5) {

                // close editor
                pl.playSound(pl.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1);
                event.setWillClose(true);
                event.setWillDestroy(true);
            }
        });

        return tailorMenu;
    }

    private ItemGUI openWheelMenu(Player pl) {

        // grab the player's current profession level, progress toward that level
        int currentLvl = RunicCore.getInstance().getConfig().getInt(pl.getUniqueId() + ".info.prof.level");

        // to make processed leather
        LinkedHashMap<Material, Integer> makeClothReqs = new LinkedHashMap<>();
        makeClothReqs.put(Material.STRING, 999);
        // to make leather goods
        LinkedHashMap<Material, Integer> gearReqs = new LinkedHashMap<>();
        gearReqs.put(Material.PAPER, 999);
        // legendary
        LinkedHashMap<Material, Integer> tomeReqs = new LinkedHashMap<>();
        tomeReqs.put(Material.NETHER_STAR, 1);
        tomeReqs.put(Material.PAPER, 7);
        tomeReqs.put(Material.LAPIS_ORE, 1);

        ItemGUI wheelMenu = super.craftingMenu(pl, 18);

        wheelMenu.setOption(4, new ItemStack(Material.LIGHT_GRAY_WOOL), "&eSpinning Wheel",
                "&fClick &7an item to start crafting!"
                        + "\n&fClick &7here to return to the station", 0, false);

        setupItems(wheelMenu, pl, currentLvl);

        wheelMenu.setHandler(event -> {

            if (event.getSlot() == 4) {

                // return to the first menu
                pl.playSound(pl.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1);
                ItemGUI menu = openMenu(pl);
                menu.open(pl);
                event.setWillClose(false);
                event.setWillDestroy(true);

            } else {

                int mult = 1;
                if (event.isRightClick()) mult = 5;
                ItemMeta meta = Objects.requireNonNull(event.getCurrentItem()).getItemMeta();
                if (meta == null) return;

                int slot = event.getSlot();
                int health = 0;
                int reqLevel = 0;
                int reagentAmt = 0;
                int exp = 0;
                LinkedHashMap<Material, Integer> reqHashMap;

                if (event.getSlot() < 10) {
                    reqHashMap = makeClothReqs;
                } else if (event.getSlot() == 14) {
                    reqHashMap = tomeReqs;
                    reqLevel = 50;
                } else {
                    reqHashMap = gearReqs;
                }

                // cloth
                if (slot == 9) {
                    reagentAmt = 2;
                    exp = 10;
                // helmet
                } else if (slot == 10) {
                    reagentAmt = 5;
                    exp = 100;
                // chestplate
                } else if (slot == 11) {
                    //reqLevel = 40;
                    reagentAmt = 8;
                    exp = 160;
                // leggings
                } else if (slot == 12) {
                    //reqLevel = 20;
                    reagentAmt = 7;
                    exp = 140;
                // boots
                } else if (slot == 13) {
                    //reqLevel = 10;
                    reagentAmt = 4;
                    exp = 80;
                }

                // armor
                if (slot == 10 || slot == 11 || slot == 12 || slot == 13) {
                    if (currentLvl < 30) {
                        health = 12;
                    } else if (currentLvl < 50) {
                        health = 24;
                    } else {
                        health = 30;
                    }
                }

                // destroy instance of inventory to prevent bugs
                event.setWillClose(true);
                event.setWillDestroy(true);

                // craft item based on experience and reagent amount
                super.startCrafting(pl, reqHashMap, reagentAmt, reqLevel, event.getCurrentItem().getType(),
                        meta.getDisplayName(), currentLvl, exp,
                        ((Damageable) meta).getDamage(), Particle.CRIT,
                        Sound.BLOCK_SAND_BREAK, Sound.ITEM_ARMOR_EQUIP_LEATHER, health, mult);
            }});

        return wheelMenu;
    }

    private void setupItems(ItemGUI forgeMenu, Player pl, int currentLv) {

        String healthStr;
        if (currentLv < 30) {
            healthStr = "12";
        } else if (currentLv < 50) {
            healthStr = "24";
        } else {
            healthStr = "30";
        }

        // to make processed leather
        LinkedHashMap<Material, Integer> makeClothReqs = new LinkedHashMap<>();
        makeClothReqs.put(Material.STRING, 999);
        super.createMenuItem(forgeMenu, pl, 9, Material.PAPER, "&fCloth", makeClothReqs,
                "Thread", 2, 10, 0, 0, "",
                true, false, false);

        // to make leather goods
        LinkedHashMap<Material, Integer> gearReqs = new LinkedHashMap<>();
        gearReqs.put(Material.PAPER, 999);
        super.createMenuItem(forgeMenu, pl, 10, Material.SHEARS, "&fWoven Cloth Hood", gearReqs,
                "Cloth", 5, 50, 0, 5,
                "&c+ " + healthStr + "❤\n&3+ " + healthStr + "✸",
                false, true, false);
        super.createMenuItem(forgeMenu, pl, 11, Material.DIAMOND_CHESTPLATE, "&fWoven Cloth Robe", gearReqs,
                "Cloth", 8, 80, 0, 0,
                "&c+ " + healthStr + "❤\n&3+ " + healthStr + "✸",
                false, true, false);
        super.createMenuItem(forgeMenu, pl, 12, Material.DIAMOND_LEGGINGS, "&fWoven Cloth Legs", gearReqs,
                "Cloth", 7, 70, 0, 0,
                "&c+ " + healthStr + "❤\n&3+ " + healthStr + "✸",
                false, true, false);
        super.createMenuItem(forgeMenu, pl, 13, Material.DIAMOND_BOOTS, "&fWoven Cloth Boots", gearReqs,
                "Cloth", 4, 40, 0, 0,
                "&c+ " + healthStr + "❤\n&3+ " + healthStr + "✸",
                false, true, false);
        // legendary
        LinkedHashMap<Material, Integer> tomeReqs = new LinkedHashMap<>();
        tomeReqs.put(Material.NETHER_STAR, 1);
        tomeReqs.put(Material.PAPER, 7);
        tomeReqs.put(Material.LAPIS_ORE, 1);
        super.createMenuItem(forgeMenu, pl, 14, Material.BOOK, "&6Tome of the Frost Lords", tomeReqs,
                "Token of Valor\nCloth\nUncut Sapphire", 999, 0, 50, 0,
                "&3+ 100✸\n&3+ 3ʔ",
                true, false, false);
    }

    @Override
    public void produceResult(Player pl, Material material, String dispName,
                              int currentLvl, int amt, int rate, int durability, int someVar) {

        // we're only gonna mess w/ the mechanics for processed leather
        if (material != Material.PAPER && material != Material.BOOK) {
            super.produceResult(pl, material, dispName, currentLvl, amt, rate, durability, someVar);
            return;
        }

        for (int i = 0; i < amt; i++) {
            ItemStack craftedItem = new ItemStack(material);
            ItemMeta meta = craftedItem.getItemMeta();
            ((Damageable) Objects.requireNonNull(meta)).setDamage(durability);

            ArrayList<String> lore = new ArrayList<>();

            lore.add(ChatColor.GRAY + "Crafting Reagent");
            meta.setLore(lore);
            meta.setDisplayName(ChatColor.WHITE + dispName);
            craftedItem.setItemMeta(meta);

            if (material == Material.BOOK) {
                craftedItem = LegendaryManager.tomeOfTheFrostLords();
            }

            // check that the player has an open inventory space
            // this method prevents items from stacking if the player crafts 5
            if (pl.getInventory().firstEmpty() != -1) {
                int firstEmpty = pl.getInventory().firstEmpty();
                pl.getInventory().setItem(firstEmpty, craftedItem);
            } else {
                pl.getWorld().dropItem(pl.getLocation(), craftedItem);
            }
        }
    }
}
