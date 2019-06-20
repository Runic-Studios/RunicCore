package com.runicrealms.plugin.professions.crafting;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.attributes.AttributeUtil;
import com.runicrealms.plugin.item.ItemGUI;
import com.runicrealms.plugin.item.LoreGenerator;
import com.runicrealms.plugin.professions.Workstation;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.LinkedHashMap;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

public class JewelerGUI extends Workstation {

    public JewelerGUI() {
    }

    public ItemGUI openMenu(Player pl) {

        // name the menu
        ItemGUI jewelerMenu = super.openWorkstation(pl);
        jewelerMenu.setName("&f&l" + pl.getName() + "'s &e&lGemcutting Bench");

        //set the visual items
        jewelerMenu.setOption(3, new ItemStack(Material.REDSTONE),
                "&fCut Gems", "&7Create gemstones and enhance armor!", 0);

        // set the handler
        jewelerMenu.setHandler(event -> {

            if (event.getSlot() == 3) {

                // open the crafting menu
                pl.playSound(pl.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1);
                ItemGUI wheel = openBenchMenu(pl);
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

        return jewelerMenu;
    }

    private ItemGUI openBenchMenu(Player pl) {

        // grab the player's current profession level, progress toward that level
        int currentLvl = RunicCore.getInstance().getConfig().getInt(pl.getUniqueId() + ".info.prof.level");

        // ruby
        LinkedHashMap<Material, Integer> cutRubyReqs = new LinkedHashMap<>();
        cutRubyReqs.put(Material.REDSTONE_ORE, 999);

        // sapphire
        LinkedHashMap<Material, Integer> cutSapphireReqs = new LinkedHashMap<>();
        cutSapphireReqs.put(Material.LAPIS_ORE, 999);

        // emerald
        LinkedHashMap<Material, Integer> cutEmeraldReqs = new LinkedHashMap<>();
        cutEmeraldReqs.put(Material.EMERALD_ORE, 999);

        // opal
        LinkedHashMap<Material, Integer> cutOpalReqs = new LinkedHashMap<>();
        cutOpalReqs.put(Material.NETHER_QUARTZ_ORE, 999);

        // diamond
        LinkedHashMap<Material, Integer> cutDiamondReqs = new LinkedHashMap<>();
        cutDiamondReqs.put(Material.DIAMOND_ORE, 999);

        ItemGUI benchMenu = super.craftingMenu(pl, 18);

        benchMenu.setOption(4, new ItemStack(Material.COBBLESTONE_STAIRS), "&eGemcutting Bench",
                "&fClick &7an item to start crafting!"
                        + "\n&fClick &7here to return to the station", 0);

        setupItems(benchMenu, pl, currentLvl);

        benchMenu.setHandler(event -> {

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

                // ruby
                if (slot == 9) {
                    reqHashMap = cutRubyReqs;
                    exp = 5;
                    if (currentLvl < 30) {
                        dummyVar = 5;
                    } else if (currentLvl < 50) {
                        dummyVar = 10;
                    } else {
                        dummyVar = 15;
                    }
                    // sapphire
                } else if (slot == 10) {
                    reqHashMap = cutSapphireReqs;
                    exp = 10;
                    if (currentLvl < 30) {
                        dummyVar = 10;
                    } else if (currentLvl < 50) {
                        dummyVar = 20;
                    } else {
                        dummyVar = 30;
                    }
                    // emerald
                } else if (slot == 11) {
                    reqHashMap = cutEmeraldReqs;
                    reqLevel = 20;
                    exp = 15;
                    if (currentLvl < 30) {
                        dummyVar = 5;
                    } else if (currentLvl < 50) {
                        dummyVar = 10;
                    } else {
                        dummyVar = 15;
                    }
                    // opal
                } else if (slot == 12) {
                    reqHashMap = cutOpalReqs;
                    reqLevel = 40;
                    exp = 25;
                    if (currentLvl < 30) {
                        dummyVar = 1;
                    } else if (currentLvl < 50) {
                        dummyVar = 2;
                    } else {
                        dummyVar = 3;
                    }
                    // diamond
                } else if (slot == 13) {
                    reqHashMap = cutDiamondReqs;
                    reqLevel = 40;
                    exp = 35;
                    if (currentLvl < 30) {
                        dummyVar = 3;
                    } else if (currentLvl < 50) {
                        dummyVar = 6;
                    } else {
                        dummyVar = 12;
                    }
                }

                // craft item based on experience and reagent amount
                super.startCrafting(pl, reqHashMap, 1, reqLevel, event.getCurrentItem().getType(),
                        meta.getDisplayName(), currentLvl, exp,
                        ((Damageable) meta).getDamage(), Particle.WATER_SPLASH,
                        Sound.BLOCK_BREWING_STAND_BREW, Sound.ENTITY_GENERIC_DRINK, dummyVar, mult);
            }
        });

        return benchMenu;
    }

    private void setupItems(ItemGUI forgeMenu, Player pl, int currentLv) {

        String healthStr;
        String manaStr;
        String healingStr;
        String weaponStr;
        String spellStr;
        if (currentLv < 30) {
            healthStr = "5";
            manaStr = "10";
            healingStr = "5";
            weaponStr = "1";
            spellStr = "3";
        } else if (currentLv < 50) {
            healthStr = "10";
            manaStr = "20";
            healingStr = "10";
            weaponStr = "2";
            spellStr = "6";
        } else {
            healthStr = "15";
            manaStr = "30";
            healingStr = "15";
            weaponStr = "3";
            spellStr = "12";
        }

        // ruby (+health)
        LinkedHashMap<Material, Integer> cutRubyReqs = new LinkedHashMap<>();
        cutRubyReqs.put(Material.REDSTONE_ORE, 999);
        super.createMenuItem(forgeMenu, pl, 9, Material.REDSTONE, "&fCut Ruby", cutRubyReqs,
                "Uncut Ruby", 1, 5, 0, 0, "&c+" + healthStr + "❤ (Health)",
                false);

        // sapphire (+mana)
        LinkedHashMap<Material, Integer> cutSapphireReqs = new LinkedHashMap<>();
        cutSapphireReqs.put(Material.LAPIS_ORE, 999);
        super.createMenuItem(forgeMenu, pl, 10, Material.LAPIS_LAZULI, "&fCut Sapphire", cutSapphireReqs,
                "Uncut Sapphire", 1, 10, 10, 0, "&3+" + manaStr + "✸ (Mana)",
                false);

        // emerald (+healing)
        LinkedHashMap<Material, Integer> cutEmeraldReqs = new LinkedHashMap<>();
        cutEmeraldReqs.put(Material.EMERALD_ORE, 999);
        super.createMenuItem(forgeMenu, pl, 11, Material.EMERALD, "&fCut Emerald", cutEmeraldReqs,
                "Uncut Emerald", 1, 15, 20, 0, "&a+" + healingStr + "✦ (Healing)",
                false);

        // opal (+weapon dmg)
        LinkedHashMap<Material, Integer> cutOpalReqs = new LinkedHashMap<>();
        cutOpalReqs.put(Material.NETHER_QUARTZ_ORE, 999);
        super.createMenuItem(forgeMenu, pl, 12, Material.QUARTZ, "&fCut Opal", cutOpalReqs,
                "Uncut Opal", 1, 25, 40, 0, "&c+" + weaponStr + "⚔ (Weapon Dmg)",
                false);

        // diamond (+spell dmg)
        LinkedHashMap<Material, Integer> cutDiamondReqs = new LinkedHashMap<>();
        cutDiamondReqs.put(Material.DIAMOND_ORE, 999);
        super.createMenuItem(forgeMenu, pl, 13, Material.DIAMOND, "&fCut Diamond", cutDiamondReqs,
                "Uncut Diamond", 1, 35, 40, 0, "&3+" + spellStr + "ʔ (Spell Dmg)",
                false);
    }

    @Override
    public void produceResult(Player pl, Material material, String dispName,
                              int currentLvl, int amt, int rate, int durability, int someVar) {

        // check that the player has an open inventory space
        int failCount = 0;
        for (int i = 0; i < amt; i++) {

            // build our item
            ItemStack craftedItem = new ItemStack(material);

            // roll chance for each item
            double chance = ThreadLocalRandom.current().nextDouble(0, 100);

            // tell the game that this is a gemstone
            craftedItem = AttributeUtil.addCustomStat
                    (craftedItem, "custom.isGemstone", "true");

            craftedItem = addGemStat(material, craftedItem, someVar);

            LoreGenerator.generateItemLore(craftedItem, ChatColor.WHITE, dispName,
                    "\n" + ChatColor.DARK_GRAY + "Use this on an item");

            if (chance <= rate) {
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

    private ItemStack addGemStat(Material gemType, ItemStack craftedItem, int dummyVar) {
        switch (gemType) {
            case REDSTONE:
                craftedItem = AttributeUtil.addCustomStat(craftedItem, "custom.maxHealth", dummyVar);
                break;
            case LAPIS_LAZULI:
                craftedItem = AttributeUtil.addCustomStat(craftedItem, "custom.manaBoost", dummyVar);
                break;
            case EMERALD:
                craftedItem = AttributeUtil.addCustomStat(craftedItem, "custom.healingBoost", dummyVar);
                break;
            case QUARTZ:
                craftedItem = AttributeUtil.addCustomStat(craftedItem, "custom.attackDamage", dummyVar);
                break;
            case DIAMOND:
                craftedItem = AttributeUtil.addCustomStat(craftedItem, "custom.magicDamage", dummyVar);
                break;
        }
        return craftedItem;
    }
}
