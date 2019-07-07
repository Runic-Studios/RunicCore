package com.runicrealms.plugin.professions.crafting;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.item.GUIMenu.ItemGUI;
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

public class LWGUI extends Workstation {

    public LWGUI() {
    }

    public ItemGUI openMenu(Player pl) {

        // name the menu
        ItemGUI leatherMenu = super.openWorkstation(pl);
        leatherMenu.setName("&f&l" + pl.getName() + "'s &e&lTanning Rack");

        //set the visual items
        leatherMenu.setOption(3, new ItemStack(Material.RABBIT_HIDE),
                "&fCraft Armor", "&7Tan hides and create leather goods!", 0);

        // set the handler
        leatherMenu.setHandler(event -> {

            if (event.getSlot() == 3) {

                // open the crafting menu
                pl.playSound(pl.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1);
                ItemGUI forge = openForgeMenu(pl);
                forge.open(pl);
                event.setWillClose(false);
                event.setWillDestroy(true);

            } else if (event.getSlot() == 5) {

                // close editor
                pl.playSound(pl.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1);
                event.setWillClose(true);
                event.setWillDestroy(true);
            }
        });

        return leatherMenu;
    }

    private ItemGUI openForgeMenu(Player pl) {

        // grab the player's current profession level, progress toward that level
        int currentLvl = RunicCore.getInstance().getConfig().getInt(pl.getUniqueId() + ".info.prof.level");

        // create three hashmaps for the reagents, set to 0 since we've only got 1 reagent
        LinkedHashMap<Material, Integer> processedReqs = new LinkedHashMap<>();
        processedReqs.put(Material.LEATHER, 1);
        processedReqs.put(Material.SPRUCE_LOG, 2);
        LinkedHashMap<Material, Integer> leatherReqs = new LinkedHashMap<>();
        leatherReqs.put(Material.RABBIT_HIDE, 999);

        ItemGUI forgeMenu = super.craftingMenu(pl, 18);

        forgeMenu.setOption(4, new ItemStack(Material.BROWN_TERRACOTTA), "&eTanning Rack",
                "&fClick &7an item to start crafting!"
                        + "\n&fClick &7here to return to the station", 0);

        setupItems(forgeMenu, pl, currentLvl);

        forgeMenu.setHandler(event -> {

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
                    reqHashMap = processedReqs;
                } else {
                    reqHashMap = leatherReqs;
                }

                // helmet
                if (slot == 10) {
                    reagentAmt = 5;
                    exp = 5;
                // chestplate
                } else if (slot == 11) {
                    reqLevel = 40;
                    reagentAmt = 8;
                    exp = 30;
                // leggings
                } else if (slot == 12) {
                    reqLevel = 20;
                    reagentAmt = 7;
                    exp = 25;
                // boots
                } else if (slot == 13) {
                    reqLevel = 10;
                    reagentAmt = 4;
                    exp = 10;
                }

                // armor
                if (slot == 10 || slot == 11 || slot == 12 || slot == 13) {
                    if (currentLvl < 30) {
                        health = 5;
                    } else if (currentLvl < 50) {
                        health = 10;
                    } else {
                        health = 15;
                    }
                }

                // destroy instance of inventory to prevent bugs
                event.setWillClose(true);
                event.setWillDestroy(true);

                // craft item based on experience and reagent amount
                super.startCrafting(pl, reqHashMap, reagentAmt, reqLevel, event.getCurrentItem().getType(),
                        meta.getDisplayName(), currentLvl, exp,
                        ((Damageable) meta).getDamage(), Particle.SMOKE_NORMAL,
                        Sound.ENTITY_GHAST_SHOOT, Sound.ITEM_ARMOR_EQUIP_LEATHER, health, mult);
            }});

        return forgeMenu;
    }

    private void setupItems(ItemGUI forgeMenu, Player pl, int currentLv) {

        String healthStr;
        if (currentLv < 30) {
            healthStr = "5";
        } else if (currentLv < 50) {
            healthStr = "10";
        } else {
            healthStr = "15";
        }

        // to make processed leather
        LinkedHashMap<Material, Integer> processedReqs = new LinkedHashMap<>();
        processedReqs.put(Material.LEATHER, 1);
        processedReqs.put(Material.SPRUCE_LOG, 2);
        super.createMenuItem(forgeMenu, pl, 9, Material.RABBIT_HIDE, "&fProcessed Leather", processedReqs,
                "Animal Hide\nSpruce Log", 5, 0, 0, 0, "",
                true);

        // to make leather goods
        LinkedHashMap<Material, Integer> leatherReqs = new LinkedHashMap<>();
        leatherReqs.put(Material.RABBIT_HIDE, 999);
        super.createMenuItem(forgeMenu, pl, 10, Material.SHEARS, "&fCrafted Leather Helmet", leatherReqs,
                "Processed Leather", 5, 5, 0, 10, "&c+ " + healthStr + "❤",
                false);
        super.createMenuItem(forgeMenu, pl, 11, Material.LEATHER_CHESTPLATE, "&fCrafted Leather Tunic", leatherReqs,
                "Processed Leather", 8, 30, 40, 0, "&c+ " + healthStr + "❤",
                false);
        super.createMenuItem(forgeMenu, pl, 12, Material.LEATHER_LEGGINGS, "&fCrafted Leather Legs", leatherReqs,
                "Processed Leather", 7, 25, 20, 0, "&c+ " + healthStr + "❤",
                false);
        super.createMenuItem(forgeMenu, pl, 13, Material.LEATHER_BOOTS, "&fCrafted Leather Boots", leatherReqs,
                "Processed Leather", 4, 10, 10, 0, "&c+ " + healthStr + "❤",
                false);
    }

    @Override
    public void produceResult(Player pl, Material material, String dispName,
                              int currentLvl, int amt, int rate, int durability, int someVar) {

        // we're only gonna mess w/ the mechanics for processed leather
        if (material != Material.RABBIT_HIDE) {
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
