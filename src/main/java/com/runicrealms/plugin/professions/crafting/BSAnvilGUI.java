package com.runicrealms.plugin.professions.crafting;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.attributes.AttributeUtil;
import com.runicrealms.plugin.item.GUIMenu.ItemGUI;
import com.runicrealms.plugin.item.LegendaryManager;
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
                "&fCraft Armor", "&7Forge mail, gilded or plate armor!", 0, false);

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

        // legendaries
        LinkedHashMap<Material, Integer> arrowHeadReqs = new LinkedHashMap<>();
        arrowHeadReqs.put(Material.NETHER_STAR, 1);
        arrowHeadReqs.put(Material.IRON_INGOT, 3);

        LinkedHashMap<Material, Integer> powderReqs = new LinkedHashMap<>();
        powderReqs.put(Material.NETHER_STAR, 1);
        powderReqs.put(Material.GOLDEN_CARROT, 1);

        LinkedHashMap<Material, Integer> shieldReqs = new LinkedHashMap<>();
        shieldReqs.put(Material.NETHER_STAR, 1);
        shieldReqs.put(Material.IRON_INGOT, 4);
        shieldReqs.put(Material.OAK_LOG, 4);

        ItemGUI forgeMenu = super.craftingMenu(pl, 36);

        forgeMenu.setOption(4, new ItemStack(Material.ANVIL), "&eAnvil",
                "&fClick &7an item to start crafting!"
                        + "\n&fClick &7here to return to the station", 0, false);

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
                int reqLevel = 0;
                int reagentAmt = 0;
                int exp = 0;
                LinkedHashMap<Material, Integer> reqHashMap;

                if (event.getSlot() < 13) {
                    reqHashMap = chainLinkReqs;
                } else if (event.getSlot() == 13) {
                    reqHashMap = arrowHeadReqs;
                    reqLevel = 50;
                } else if (event.getSlot() == 22) {
                    reqHashMap = powderReqs;
                    reqLevel = 50;
                } else if (event.getSlot() == 31) {
                    reqHashMap = shieldReqs;
                    reqLevel = 50;
                } else if (event.getSlot() < 27) {
                    reqHashMap = goldBarReqs;
                } else {
                    reqHashMap = ironBarReqs;
                }

                // helmets
                if (slot == 9 || slot == 18 || slot == 27) {
                    reagentAmt = 5;
                    exp = 60;
                    // chestplates
                } else if (slot == 10 || slot == 19 || slot == 28) {
                    reagentAmt = 8;
                    exp = 96;
                    // leggings
                } else if (slot == 11 || slot == 20 || slot == 29) {
                    reagentAmt = 7;
                    exp = 84;
                    // boots
                } else if (slot == 12 || slot == 21 || slot == 30) {
                    reagentAmt = 4;
                    exp = 48;
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
                super.startCrafting(pl, reqHashMap, reagentAmt, reqLevel, event.getCurrentItem().getType(),
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
                "Chain Link", 5, 60, 0, 15,
                "&c+ " + mailStr + "❤\n&3+ " + mailStr + "✸",
                false, true, false);
        super.createMenuItem(forgeMenu, pl, 10, Material.CHAINMAIL_CHESTPLATE, "&fForged Mail Body", chainLinkReqs,
                "Chain Link", 8, 96, 0, 0,
                "&c+ " + mailStr + "❤\n&3+ " + mailStr + "✸",
                false, true, false);
        super.createMenuItem(forgeMenu, pl, 11, Material.CHAINMAIL_LEGGINGS, "&fForged Mail Legs", chainLinkReqs,
                "Chain Link", 7, 84, 0, 0,
                "&c+ " + mailStr + "❤\n&3+ " + mailStr + "✸",
                false, true, false);
        super.createMenuItem(forgeMenu, pl, 12, Material.CHAINMAIL_BOOTS, "&fForged Mail Boots", chainLinkReqs,
                "Chain Link", 4, 48, 0, 0,
                "&c+ " + mailStr + "❤\n&3+ " + mailStr + "✸",
                false, true, false);
        // legendary
        LinkedHashMap<Material, Integer> arrowHeadReqs = new LinkedHashMap<>();
        arrowHeadReqs.put(Material.NETHER_STAR, 1);
        arrowHeadReqs.put(Material.IRON_INGOT, 3);
        super.createMenuItem(forgeMenu, pl, 13, Material.FLINT, "&6Frostforged Arrowhead", arrowHeadReqs,
                "Token of Valor\nIron Bar", 999, 0, 50, 0,
                "&c+ "
                        + (int) AttributeUtil.getCustomDouble(LegendaryManager.frostforgedArrowhead(), "custom.attackDamage")
                        + "⚔\n&3+ "
                        + (int) AttributeUtil.getCustomDouble(LegendaryManager.frostforgedArrowhead(), "custom.magicDamage")
                        + "ʔ",
                true, false, false);

        // gilded
        LinkedHashMap<Material, Integer> goldBarReqs = new LinkedHashMap<>();
        goldBarReqs.put(Material.GOLD_INGOT, 999);
        super.createMenuItem(forgeMenu, pl, 18, Material.SHEARS, "&fForged Gilded Helmet", goldBarReqs,
                "Gold Bar", 5, 50, 0, 20,
                "&c+ " + gildedStr + "❤\n&3+ " + gildedStr + "✸",
                false, true, false);
        super.createMenuItem(forgeMenu, pl, 19, Material.GOLDEN_CHESTPLATE, "&fForged Gilded Body", goldBarReqs,
                "Gold Bar", 8, 80, 0, 0,
                "&c+ " + gildedStr + "❤\n&3+ " + gildedStr + "✸",
                false, true, false);
        super.createMenuItem(forgeMenu, pl, 20, Material.GOLDEN_LEGGINGS, "&fForged Gilded Legs", goldBarReqs,
                "Gold Bar", 7, 70, 0, 0,
                "&c+ " + gildedStr + "❤\n&3+ " + gildedStr + "✸",
                false, true, false);
        super.createMenuItem(forgeMenu, pl, 21, Material.GOLDEN_BOOTS, "&fForged Gilded Boots", goldBarReqs,
                "Gold Bar", 4, 40, 0, 0,
                "&c+ " + gildedStr + "❤\n&3+ " + gildedStr + "✸",
                false, true, false);
        // legendary
        LinkedHashMap<Material, Integer> powderReqs = new LinkedHashMap<>();
        powderReqs.put(Material.NETHER_STAR, 1);
        powderReqs.put(Material.GOLDEN_CARROT, 1);
        super.createMenuItem(forgeMenu, pl, 22, Material.RABBIT_FOOT, "&6Ambrosian Powder", powderReqs,
                "Token of Valor\nAmbrosia Root", 999, 0, 50, 0,
                "&3+ "
                        + (int) AttributeUtil.getCustomDouble(LegendaryManager.ambrosianPowder(), "custom.manaBoost")
                        + "✸\n&a+ "
                        + (int) AttributeUtil.getCustomDouble(LegendaryManager.ambrosianPowder(), "custom.healingBoost")
                        + "✦",
                true, false, false);

        // plate
        LinkedHashMap<Material, Integer> ironBarReqs = new LinkedHashMap<>();
        ironBarReqs.put(Material.IRON_INGOT, 999);
        super.createMenuItem(forgeMenu, pl, 27, Material.SHEARS, "&fForged Iron Helmet", ironBarReqs,
                "Iron Bar", 5, 50, 0, 25,
                "&c+ " + plateStr + "❤\n&3+ " + plateStr + "✸",
                false, true, false);
        super.createMenuItem(forgeMenu, pl, 28, Material.IRON_CHESTPLATE, "&fForged Iron Body", ironBarReqs,
                "Iron Bar", 8, 80, 0, 0,
                "&c+ " + plateStr + "❤\n&3+ " + plateStr + "✸",
                false, true, false);
        super.createMenuItem(forgeMenu, pl, 29, Material.IRON_LEGGINGS, "&fForged Iron Legs", ironBarReqs,
                "Iron Bar", 7, 70, 0, 0,
                "&c+ " + plateStr + "❤\n&3+ " + plateStr + "✸",
                false, true, false);
        super.createMenuItem(forgeMenu, pl, 30, Material.IRON_BOOTS, "&fForged Iron Boots", ironBarReqs,
                "Iron Bar", 4, 40, 0, 0,
                "&c+ " + plateStr + "❤\n&3+ " + plateStr + "✸",
                false, true, false);
        // legendary
        LinkedHashMap<Material, Integer> shieldReqs = new LinkedHashMap<>();
        shieldReqs.put(Material.NETHER_STAR, 1);
        shieldReqs.put(Material.IRON_INGOT, 4);
        shieldReqs.put(Material.OAK_LOG, 4);
        super.createMenuItem(forgeMenu, pl, 31, Material.SHIELD, "&6Frostforged Bulwark", shieldReqs,
                "Token of Valor\nIron Bar\nOak Log", 999, 0, 50, 0,
                "&c+ "
                        + (int) AttributeUtil.getGenericDouble(LegendaryManager.frostforgedBulwark(), "generic.maxHealth")
                        + "❤\n&f+ "
                        + (int) AttributeUtil.getCustomDouble(LegendaryManager.frostforgedBulwark(), "custom.shield")
                        + "■",
                true, false, false);
    }

    @Override
    public void produceResult(Player pl, Material material, String dispName,
                              int currentLvl, int amt, int rate, int durability, int someVar) {

        // we're only gonna mess w/ the mechanics for processed leather
        if (material != Material.FLINT && material != Material.RABBIT_FOOT && material != Material.SHIELD) {
            super.produceResult(pl, material, dispName, currentLvl, amt, rate, durability, someVar);
            return;
        }

        for (int i = 0; i < amt; i++) {
            ItemStack craftedItem = new ItemStack(material);

            if (material == Material.FLINT) {
                craftedItem = LegendaryManager.frostforgedArrowhead();
            } else if (material == Material.RABBIT_FOOT) {
                craftedItem = LegendaryManager.ambrosianPowder();
            } else if (material == Material.SHIELD) {
                craftedItem = LegendaryManager.frostforgedBulwark();
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
