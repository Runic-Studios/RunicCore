package com.runicrealms.plugin.professions.blacksmith;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.item.ItemGUI;
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

public class BlacksmithGUI extends Workstation {

    public BlacksmithGUI() {
    }

    public ItemGUI openMenu(Player pl) {

        // name the menu
        ItemGUI blackSmithMenu = super.openWorkstation(pl);
        blackSmithMenu.setName("&f&l" + pl.getName() + "'s &e&lAnvil");

        //set the visual items
        blackSmithMenu.setOption(3, new ItemStack(Material.IRON_CHESTPLATE),
                "&fCraft Armor", "&7Forge mail, guilded or plate armor!", 0);

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
        chainLinkReqs.put(Material.IRON_BARS, 0);
        LinkedHashMap<Material, Integer> goldBarReqs = new LinkedHashMap<>();
        chainLinkReqs.put(Material.GOLD_INGOT, 0);
        LinkedHashMap<Material, Integer> ironBarReqs = new LinkedHashMap<>();
        chainLinkReqs.put(Material.IRON_INGOT, 0);

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
                int health = 0;
                int reqLevel = 0;
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
                    exp = 5;
                    // chestplates
                } else if (slot == 10 || slot == 19 || slot == 28) {
                    reqLevel = 40;
                    reagentAmt = 8;
                    exp = 30;
                    // leggings
                } else if (slot == 11 || slot == 20 || slot == 29) {
                    reqLevel = 20;
                    reagentAmt = 7;
                    exp = 25;
                    // boots
                } else if (slot == 12 || slot == 21 || slot == 30) {
                    reqLevel = 10;
                    reagentAmt = 4;
                    exp = 10;
                }

                // mail
                if (slot == 9 || slot == 10 || slot == 11 || slot == 12) {
                    if (currentLvl < 30) {
                        health = 5;
                    } else if (currentLvl < 50) {
                        health = 12;
                    } else {
                        health = 20;
                    }

                    // guilded
                } else if (slot == 18 || slot == 19 || slot == 20 || slot == 21) {
                    if (currentLvl < 30) {
                        health = 7;
                    } else if (currentLvl < 50) {
                        health = 15;
                    } else {
                        health = 30;
                    }

                    // plate
                } else if (slot == 27 || slot == 28 || slot == 29 || slot == 30) {
                    if (currentLvl < 30) {
                        health = 10;
                    } else if (currentLvl < 50) {
                        health = 30;
                    } else {
                        health = 50;
                    }
                }

                // craft item based on experience and reagent amount
                super.startCrafting(pl, reqHashMap, reagentAmt * mult, reqLevel, event.getCurrentItem().getType(),
                        meta.getDisplayName(), currentLvl, exp * mult,
                        mult, ((Damageable) meta).getDamage(), Particle.FIREWORKS_SPARK,
                        Sound.BLOCK_ANVIL_PLACE, Sound.BLOCK_ANVIL_USE, health);
            }});

        return forgeMenu;
    }

    private void setupItems(ItemGUI forgeMenu, Player pl, int currentLv) {

        String mailStr; // 5, 12, 20
        String guildedStr; // 7, 15, 30
        String plateStr; // 10, 30, 50
        if (currentLv < 30) {
            mailStr = "5";
            guildedStr = "7";
            plateStr = "10";
        } else if (currentLv < 50) {
            mailStr = "12";
            guildedStr = "15";
            plateStr = "30";
        } else {
            mailStr = "20";
            guildedStr = "30";
            plateStr = "50";
        }

        // mail
        LinkedHashMap<Material, Integer> chainLinkReqs = new LinkedHashMap<>();
        chainLinkReqs.put(Material.IRON_BARS, 0);
        super.createCraftableItem(forgeMenu, pl, 9, Material.SHEARS, "&fForged Mail Helmet",
                "Mail", chainLinkReqs, "Chain Link", 5, 5, 0, 15, "&c+ " + mailStr + "❤");
        super.createCraftableItem(forgeMenu, pl, 10, Material.CHAINMAIL_CHESTPLATE, "&fForged Mail Body",
                "Mail", chainLinkReqs, "Chain Link", 8, 5, 40, 0, "&c+ " + mailStr + "❤");
        super.createCraftableItem(forgeMenu, pl, 11, Material.CHAINMAIL_LEGGINGS, "&fForged Mail Legs",
                "Mail", chainLinkReqs, "Chain Link", 7, 5, 20, 0, "&c+ " + mailStr + "❤");
        super.createCraftableItem(forgeMenu, pl, 12, Material.CHAINMAIL_BOOTS, "&fForged Mail Boots",
                "Mail", chainLinkReqs, "Chain Link", 4, 5, 10, 0, "&c+ " + mailStr + "❤");

        // guilded
        LinkedHashMap<Material, Integer> goldBarReqs = new LinkedHashMap<>();
        goldBarReqs.put(Material.GOLD_INGOT, 0);
        super.createCraftableItem(forgeMenu, pl, 18, Material.SHEARS, "&fForged Guilded Helmet",
                "Guilded", goldBarReqs, "Gold Bar", 5, 5, 0, 20, "&c+ " + guildedStr + "❤");
        super.createCraftableItem(forgeMenu, pl, 19, Material.GOLDEN_CHESTPLATE, "&fForged Guilded Body",
                "Guilded", goldBarReqs, "Gold Bar", 8, 5, 40, 0, "&c+ " + guildedStr + "❤");
        super.createCraftableItem(forgeMenu, pl, 20, Material.GOLDEN_LEGGINGS, "&fForged Guilded Legs",
                "Guilded", goldBarReqs, "Gold Bar", 7, 5, 20, 0, "&c+ " + guildedStr + "❤");
        super.createCraftableItem(forgeMenu, pl, 21, Material.GOLDEN_BOOTS, "&fForged Guilded Boots",
                "Guilded", goldBarReqs, "Gold Bar", 4, 5, 10, 0, "&c+ " + guildedStr + "❤");

        // plate
        LinkedHashMap<Material, Integer> ironBarReqs = new LinkedHashMap<>();
        ironBarReqs.put(Material.IRON_INGOT, 0);
        super.createCraftableItem(forgeMenu, pl, 27, Material.SHEARS, "&fForged Iron Helmet",
                "Plate", ironBarReqs, "Iron Bar", 5, 5, 0, 25, "&c+ " + plateStr + "❤");
        super.createCraftableItem(forgeMenu, pl, 28, Material.IRON_CHESTPLATE, "&fForged Iron Body",
                "Plate", ironBarReqs, "Iron Bar", 8, 5, 40, 0, "&c+ " + plateStr + "❤");
        super.createCraftableItem(forgeMenu, pl, 29, Material.IRON_LEGGINGS, "&fForged Iron Legs",
                "Plate", ironBarReqs, "Iron Bar", 7, 5, 20, 0, "&c+ " + plateStr + "❤");
        super.createCraftableItem(forgeMenu, pl, 30, Material.IRON_BOOTS, "&fForged Iron Boots",
                "Plate", ironBarReqs, "Iron Bar", 4, 5, 10, 0, "&c+ " + plateStr + "❤");
    }
}
