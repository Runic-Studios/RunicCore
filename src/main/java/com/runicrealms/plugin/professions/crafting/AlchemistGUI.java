package com.runicrealms.plugin.professions.crafting;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.attributes.AttributeUtil;
import com.runicrealms.plugin.item.ItemGUI;
import com.runicrealms.plugin.professions.Workstation;
import com.runicrealms.plugin.utilities.ColorUtil;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

public class AlchemistGUI extends Workstation {

    public AlchemistGUI() {
    }

    public ItemGUI openMenu(Player pl) {

        // name the menu
        ItemGUI tailorMenu = super.openWorkstation(pl);
        tailorMenu.setName("&f&l" + pl.getName() + "'s &e&lCauldron");

        //set the visual items
        ItemStack menuPotion = new ItemStack(Material.POTION);
        PotionMeta pMeta = (PotionMeta) menuPotion.getItemMeta();
        if (pMeta != null) {
            pMeta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);
            pMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            pMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            pMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
            menuPotion.setItemMeta(pMeta);
        }

        tailorMenu.setOption(3, menuPotion,
                "&fBrew Potions", "&7Brew useful potions for your journey!", 0);

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

        // health potion
        LinkedHashMap<Material, Integer> healthPotReqs = new LinkedHashMap<>();
        healthPotReqs.put(Material.GLASS_BOTTLE, 1);
        healthPotReqs.put(Material.REDSTONE_ORE, 1);
        healthPotReqs.put(Material.SALMON, 1);

        // mana potion
        LinkedHashMap<Material, Integer> manaPotReqs = new LinkedHashMap<>();
        manaPotReqs.put(Material.GLASS_BOTTLE, 1);
        manaPotReqs.put(Material.LAPIS_ORE, 1);
        manaPotReqs.put(Material.COD, 1);

        // slaying potion
        LinkedHashMap<Material, Integer> slayPotReqs = new LinkedHashMap<>();
        slayPotReqs.put(Material.GLASS_BOTTLE, 1);
        slayPotReqs.put(Material.NETHER_QUARTZ_ORE, 1);
        slayPotReqs.put(Material.DIAMOND_ORE, 1);
        slayPotReqs.put(Material.TROPICAL_FISH, 1);

        // looting potion
        LinkedHashMap<Material, Integer> lootPotReqs = new LinkedHashMap<>();
        lootPotReqs.put(Material.GLASS_BOTTLE, 1);
        lootPotReqs.put(Material.GOLDEN_CARROT, 1);
        lootPotReqs.put(Material.PUFFERFISH, 1);

        ItemGUI wheelMenu = super.craftingMenu(pl, 18);

        wheelMenu.setOption(4, new ItemStack(Material.CAULDRON), "&eCauldron",
                "&fClick &7an item to start crafting!"
                        + "\n&fClick &7here to return to the station", 0);

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
                int dummyVar = 0;
                int reqLevel = 0;
                int exp = 0;
                LinkedHashMap<Material, Integer> reqHashMap = new LinkedHashMap<>();

                // health potion
                if (slot == 9) {
                    reqHashMap = healthPotReqs;
                    exp = 5;
                    if (currentLvl < 30) {
                        dummyVar = 15;
                    } else if (currentLvl < 50) {
                        dummyVar = 25;
                    } else {
                        dummyVar = 45;
                    }
                    // mana potion
                } else if (slot == 10) {
                    reqLevel = 10;
                    reqHashMap = manaPotReqs;
                    exp = 10;
                    if (currentLvl < 30) {
                        dummyVar = 15;
                    } else if (currentLvl < 50) {
                        dummyVar = 25;
                    } else {
                        dummyVar = 45;
                    }
                    // slaying potion
                } else if (slot == 11) {
                    reqLevel = 20;
                    reqHashMap = slayPotReqs;
                    exp = 25;
                    if (currentLvl < 30) {
                        dummyVar = 5;
                    } else if (currentLvl < 50) {
                        dummyVar = 10;
                    } else {
                        dummyVar = 15;
                    }
                    // looting potion
                } else if (slot == 12) {
                    reqLevel = 40;
                    reqHashMap = lootPotReqs;
                    exp = 35;
                    if (currentLvl < 30) {
                        dummyVar = 5;
                    } else if (currentLvl < 50) {
                        dummyVar = 10;
                    } else {
                        dummyVar = 15;
                    }
                }

                // destroy instance of inventory to prevent bugs
                event.setWillClose(true);
                event.setWillDestroy(true);

                // craft item based on experience and reagent amount
                super.startCrafting(pl, reqHashMap, 999, reqLevel, event.getCurrentItem().getType(),
                        meta.getDisplayName(), currentLvl, exp,
                        ((Damageable) meta).getDamage(), Particle.WATER_SPLASH,
                        Sound.BLOCK_BREWING_STAND_BREW, Sound.ENTITY_GENERIC_DRINK, dummyVar, mult);
            }
        });

        return wheelMenu;
    }

    private void setupItems(ItemGUI forgeMenu, Player pl, int currentLv) {

        String healthStr;
        String manaStr;
        String slayingStr;
        String lootingStr;
        String tierStr;
        if (currentLv < 30) {
            healthStr = "15";
            manaStr = "15";
            slayingStr = "5";
            lootingStr = "5";
            tierStr = "Minor";
        } else if (currentLv < 50) {
            healthStr = "25";
            manaStr = "25";
            slayingStr = "10";
            lootingStr = "10";
            tierStr = "Major";
        } else {
            healthStr = "45";
            manaStr = "45";
            slayingStr = "15";
            lootingStr = "15";
            tierStr = "Greater";
        }

        // health potion
        LinkedHashMap<Material, Integer> healthPotReqs = new LinkedHashMap<>();
        healthPotReqs.put(Material.GLASS_BOTTLE, 1);
        healthPotReqs.put(Material.REDSTONE_ORE, 1);
        healthPotReqs.put(Material.SALMON, 1);
        super.createMenuItem(forgeMenu, pl, 9, Material.POTION, "&c" + tierStr + " Potion of Healing", healthPotReqs,
                "Glass Bottle\nUncut Ruby\nSalmon", 5, 5, 0, 5,
                "&eRestores &c" + healthStr + "❤ &eon use",
                false);

        // mana potion
        LinkedHashMap<Material, Integer> manaPotReqs = new LinkedHashMap<>();
        manaPotReqs.put(Material.GLASS_BOTTLE, 1);
        manaPotReqs.put(Material.LAPIS_ORE, 1);
        manaPotReqs.put(Material.COD, 1);
        super.createMenuItem(forgeMenu, pl, 10, Material.POTION, "&3" + tierStr + " Potion of Mana", manaPotReqs,
                "Glass Bottle\nUncut Sapphire\nCod", 8, 10, 10, 0,
                "&eRestores &3" + manaStr + "✸ &eon use",
                false);

        // slaying potion
        LinkedHashMap<Material, Integer> slayPotReqs = new LinkedHashMap<>();
        slayPotReqs.put(Material.GLASS_BOTTLE, 1);
        slayPotReqs.put(Material.NETHER_QUARTZ_ORE, 1);
        slayPotReqs.put(Material.DIAMOND_ORE, 1);
        slayPotReqs.put(Material.TROPICAL_FISH, 1);
        super.createMenuItem(forgeMenu, pl, 11, Material.POTION, "&f" + tierStr + " Potion of Slaying", slayPotReqs,
                "Glass Bottle\nUncut Opal\nUncut Diamond\nTropical Fish", 7, 25, 20, 0,
                "&eIncreases spellʔ and weapon⚔ damage" +
                        "\n&evs. monsters by &f20% &efor &f" + lootingStr + " &eminutes",
                false);

        // looting potion
        LinkedHashMap<Material, Integer> lootPotReqs = new LinkedHashMap<>();
        lootPotReqs.put(Material.GLASS_BOTTLE, 1);
        lootPotReqs.put(Material.GOLDEN_CARROT, 1);
        lootPotReqs.put(Material.PUFFERFISH, 1);
        super.createMenuItem(forgeMenu, pl, 12, Material.POTION, "&6" + tierStr + " Potion of Looting", lootPotReqs,
                "Glass Bottle\nAmbrosia Root\nPufferfish", 4, 35, 40, 0,
                "&eIncreases looting chance by &f20%" +
                        "\n&efor &f" + slayingStr + " &eminutes",
                false);
    }

    @Override
    public void produceResult(Player pl, Material material, String dispName,
                              int currentLvl, int amt, int rate, int durability, int someVar) {

        // grab the player's current profession level, progress toward that level
        currentLvl = RunicCore.getInstance().getConfig().getInt(pl.getUniqueId() + ".info.prof.level");

        if (currentLvl == 0 || currentLvl == 1) {
            rate = 100;
        } else {
            rate = (50 + currentLvl);
        }

        int failCount = 0;

        for (int i = 0; i < amt; i++) {
            ItemStack potion = new ItemStack(Material.POTION);
            PotionMeta pMeta = (PotionMeta) potion.getItemMeta();
            Color color;
            String desc;
            if (dispName.toLowerCase().contains("healing")) {
                color = Color.RED;
                desc = "\n&eRestores &c" + someVar + "❤ &eon use";
            } else if (dispName.toLowerCase().contains("mana")) {
                color = Color.AQUA;
                desc = "\n&eRestores &3" + someVar + "✸ &eon use";
            } else if (dispName.toLowerCase().contains("slaying")) {
                color = Color.BLACK;
                desc = "\n&eIncreases spellʔ and weapon⚔ damage" +
                        "\n&evs. monsters by &f20% &efor &f" + someVar + " &eminutes";
            } else {
                color = Color.ORANGE;
                desc = "\n&eIncreases looting chance by &f20%" +
                        "\n&efor &f" + someVar + " &eminutes";
            }
            Objects.requireNonNull(pMeta).setColor(color);

            pMeta.setDisplayName(ColorUtil.format(dispName));
            ArrayList<String> lore = new ArrayList<>();
            for (String s : desc.split("\n")) {
                lore.add(ColorUtil.format(s));
            }
            lore.add("");
            lore.add(ColorUtil.format("&7Consumable"));
            pMeta.setLore(lore);

            pMeta.addEnchant(Enchantment.DURABILITY, 1, true);
            pMeta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);
            pMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            pMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            potion.setItemMeta(pMeta);

            // ----------------------------------------------
            // must be set AFTER meta is set
            if (dispName.toLowerCase().contains("healing")) {
                potion = AttributeUtil.addCustomStat(potion, "potion.healing", someVar);
            } else if (dispName.toLowerCase().contains("mana")) {
                potion = AttributeUtil.addCustomStat(potion, "potion.mana", someVar);
            } else if (dispName.toLowerCase().contains("slaying")) {
                potion = AttributeUtil.addCustomStat(potion, "potion.slaying", someVar);
            } else {
                potion = AttributeUtil.addCustomStat(potion, "potion.looting", someVar);
            }
            // ----------------------------------------------

            double chance = ThreadLocalRandom.current().nextDouble(0, 100);
            if (chance <= rate) {
                // check that the player has an open inventory space
                // this method prevents items from stacking if the player crafts 5
                if (pl.getInventory().firstEmpty() != -1) {
                    int firstEmpty = pl.getInventory().firstEmpty();
                    pl.getInventory().setItem(firstEmpty, potion);
                } else {
                    pl.getWorld().dropItem(pl.getLocation(), potion);
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
