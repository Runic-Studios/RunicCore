package com.runicrealms.plugin.professions.crafting;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.item.GUIMenu.ItemGUI;
import com.runicrealms.plugin.professions.Workstation;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.LinkedHashMap;
import java.util.Objects;

public class BSAnvilGUI extends Workstation {

    public BSAnvilGUI() {
    }

    public ItemGUI openMenu(Player pl) {

        // name the menu
        ItemGUI blackSmithMenu = super.openWorkstation(pl);
        blackSmithMenu.setName("&f&l" + pl.getName() + "'s &e&lAnvil");

        //set the visual items
        blackSmithMenu.setOption(3, new ItemStack(Material.IRON_CHESTPLATE),
                "&fCraft Armor", "&7Forge mail, gilded or plate armor!", 0);

        // set the handler
        blackSmithMenu.setHandler(event -> {

            if (event.getSlot() == 3) {

                // open the forging menu
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

        return blackSmithMenu;
    }

    private ItemGUI openForgeMenu(Player pl) {

        // grab the player's current profession level, progress toward that level
        int currentLvl = RunicCore.getInstance().getConfig().getInt(pl.getUniqueId() + ".info.prof.level");

        // create three hashmaps for the reagents, set to 0 since we've only got 1 reagent
        LinkedHashMap<Material, Integer> chainLinkReqs = new LinkedHashMap<>();
        chainLinkReqs.put(Material.IRON_BARS, 999);
        LinkedHashMap<Material, Integer> goldBarReqs = new LinkedHashMap<>();
        goldBarReqs.put(Material.GOLD_INGOT, 999);
        LinkedHashMap<Material, Integer> ironBarReqs = new LinkedHashMap<>();
        ironBarReqs.put(Material.IRON_INGOT, 999);

        ItemGUI forgeMenu = super.craftingMenu(pl, 36);

        forgeMenu.setOption(4, new ItemStack(Material.ANVIL), "&eAnvil",
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
                int stat = 0;
                //int reqLevel = 0;
                int reagentAmt = 0;
                int exp = 0;
                LinkedHashMap<Material, Integer> reqHashMap;

                if (event.getSlot() < 13) {
                    reqHashMap = chainLinkReqs;
                } else if (event.getSlot() < 27) {
                    reqHashMap = goldBarReqs;
                } else {
                    reqHashMap = ironBarReqs;
                }

                // helmets
                if (slot == 9 || slot == 18 || slot == 27) {
                    reagentAmt = 5;
                    exp = 50;
                    // chestplates
                } else if (slot == 10 || slot == 19 || slot == 28) {
                    //reqLevel = 40;
                    reagentAmt = 8;
                    exp = 80;
                    // leggings
                } else if (slot == 11 || slot == 20 || slot == 29) {
                    //reqLevel = 20;
                    reagentAmt = 7;
                    exp = 70;
                    // boots
                } else if (slot == 12 || slot == 21 || slot == 30) {
                    //reqLevel = 10;
                    reagentAmt = 4;
                    exp = 40;
                }

                // mail
                if (slot == 9 || slot == 10 || slot == 11 || slot == 12) {
                    if (currentLvl < 30) {
                        stat = 12;
                    } else if (currentLvl < 50) {
                        stat = 24;
                    } else {
                        stat = 30;
                    }

                // gilded
                } else if (slot == 18 || slot == 19 || slot == 20 || slot == 21) {
                    if (currentLvl < 30) {
                        stat = 25;
                    } else if (currentLvl < 50) {
                        stat = 40;
                    } else {
                        stat = 50;
                    }

                // plate
                } else if (slot == 27 || slot == 28 || slot == 29 || slot == 30) {
                    if (currentLvl < 30) {
                        stat = 25;
                    } else if (currentLvl < 50) {
                        stat = 40;
                    } else {
                        stat = 50;
                    }
                }

                // destroy instance of inventory to prevent bugs
                event.setWillClose(true);
                event.setWillDestroy(true);

                // craft item based on experience and reagent amount
                super.startCrafting(pl, reqHashMap, reagentAmt, 0, event.getCurrentItem().getType(),
                        meta.getDisplayName(), currentLvl, exp,
                        ((Damageable) meta).getDamage(), Particle.FIREWORKS_SPARK,
                        Sound.BLOCK_ANVIL_PLACE, Sound.BLOCK_ANVIL_USE, stat, mult);
            }});

        return forgeMenu;
    }

    private void setupItems(ItemGUI forgeMenu, Player pl, int currentLv) {

        String mailStr;
        String gildedStr;
        String plateStr;
        if (currentLv < 30) {
            mailStr = "12";
            gildedStr = "25";
            plateStr = "25";
        } else if (currentLv < 50) {
            mailStr = "24";
            gildedStr = "40";
            plateStr = "40";
        } else {
            mailStr = "30";
            gildedStr = "50";
            plateStr = "50";
        }

        // mail
        LinkedHashMap<Material, Integer> chainLinkReqs = new LinkedHashMap<>();
        chainLinkReqs.put(Material.IRON_BARS, 999);
        super.createMenuItem(forgeMenu, pl, 9, Material.SHEARS, "&fForged Mail Helmet", chainLinkReqs,
                "Chain Link", 5, 50, 0, 15,
                "&c+ " + mailStr + "❤\n&3+ " + mailStr + "✸",
                false, true);
        super.createMenuItem(forgeMenu, pl, 10, Material.CHAINMAIL_CHESTPLATE, "&fForged Mail Body", chainLinkReqs,
                "Chain Link", 8, 80, 0, 0,
                "&c+ " + mailStr + "❤\n&3+ " + mailStr + "✸",
                false, true);
        super.createMenuItem(forgeMenu, pl, 11, Material.CHAINMAIL_LEGGINGS, "&fForged Mail Legs", chainLinkReqs,
                "Chain Link", 7, 70, 0, 0,
                "&c+ " + mailStr + "❤\n&3+ " + mailStr + "✸",
                false, true);
        super.createMenuItem(forgeMenu, pl, 12, Material.CHAINMAIL_BOOTS, "&fForged Mail Boots", chainLinkReqs,
                "Chain Link", 4, 40, 0, 0,
                "&c+ " + mailStr + "❤\n&3+ " + mailStr + "✸",
                false, true);

        // gilded
        LinkedHashMap<Material, Integer> goldBarReqs = new LinkedHashMap<>();
        goldBarReqs.put(Material.GOLD_INGOT, 999);
        super.createMenuItem(forgeMenu, pl, 18, Material.SHEARS, "&fForged Gilded Helmet", goldBarReqs,
                "Gold Bar", 5, 50, 0, 20,
                "&c+ " + gildedStr + "❤\n&3+ " + gildedStr + "✸",
                false, true);
        super.createMenuItem(forgeMenu, pl, 19, Material.GOLDEN_CHESTPLATE, "&fForged Gilded Body", goldBarReqs,
                "Gold Bar", 8, 80, 0, 0,
                "&c+ " + gildedStr + "❤\n&3+ " + gildedStr + "✸",
                false, true);
        super.createMenuItem(forgeMenu, pl, 20, Material.GOLDEN_LEGGINGS, "&fForged Gilded Legs", goldBarReqs,
                "Gold Bar", 7, 70, 0, 0,
                "&c+ " + gildedStr + "❤\n&3+ " + gildedStr + "✸",
                false, true);
        super.createMenuItem(forgeMenu, pl, 21, Material.GOLDEN_BOOTS, "&fForged Gilded Boots", goldBarReqs,
                "Gold Bar", 4, 40, 0, 0,
                "&c+ " + gildedStr + "❤\n&3+ " + gildedStr + "✸",
                false, true);

        // plate
        LinkedHashMap<Material, Integer> ironBarReqs = new LinkedHashMap<>();
        ironBarReqs.put(Material.IRON_INGOT, 999);
        super.createMenuItem(forgeMenu, pl, 27, Material.SHEARS, "&fForged Iron Helmet", ironBarReqs,
                "Iron Bar", 5, 50, 0, 25,
                "&c+ " + plateStr + "❤\n&3+ " + plateStr + "✸",
                false, true);
        super.createMenuItem(forgeMenu, pl, 28, Material.IRON_CHESTPLATE, "&fForged Iron Body", ironBarReqs,
                "Iron Bar", 8, 80, 0, 0,
                "&c+ " + plateStr + "❤\n&3+ " + plateStr + "✸",
                false, true);
        super.createMenuItem(forgeMenu, pl, 29, Material.IRON_LEGGINGS, "&fForged Iron Legs", ironBarReqs,
                "Iron Bar", 7, 70, 0, 0,
                "&c+ " + plateStr + "❤\n&3+ " + plateStr + "✸",
                false, true);
        super.createMenuItem(forgeMenu, pl, 30, Material.IRON_BOOTS, "&fForged Iron Boots", ironBarReqs,
                "Iron Bar", 4, 40, 0, 0,
                "&c+ " + plateStr + "❤\n&3+ " + plateStr + "✸",
                false, true);
    }
}
