package com.runicrealms.plugin.professions.crafting;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.item.ItemGUI;
import com.runicrealms.plugin.professions.Workstation;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Objects;

public class BSFurnaceGUI extends Workstation {

    public BSFurnaceGUI() {
    }

    public ItemGUI openMenu(Player pl) {

        // name the menu
        ItemGUI furnaceMenu = super.openWorkstation(pl);
        furnaceMenu.setName("&f&l" + pl.getName() + "'s &e&lFurnace");

        //set the visual items
        furnaceMenu.setOption(3, new ItemStack(Material.IRON_INGOT),
                "&fSmelt Ores", "&7Smelt raw ores into crafting materials!", 0);

        // set the handler
        furnaceMenu.setHandler(event -> {

            if (event.getSlot() == 3) {

                // open the forging menu
                pl.playSound(pl.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1);
                ItemGUI forge = openSmeltMenu(pl);
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

        return furnaceMenu;
    }

    private ItemGUI openSmeltMenu(Player pl) {

        // grab the player's current profession level, progress toward that level
        int currentLvl = RunicCore.getInstance().getConfig().getInt(pl.getUniqueId() + ".info.prof.level");

        // create three hashmaps for the reagents, set to 0 since we've only got 1 reagent
        LinkedHashMap<Material, Integer> chainLinkReqs = new LinkedHashMap<>();
        chainLinkReqs.put(Material.IRON_ORE, 1);
        chainLinkReqs.put(Material.SPRUCE_LOG, 2);
        LinkedHashMap<Material, Integer> goldBarReqs = new LinkedHashMap<>();
        goldBarReqs.put(Material.GOLD_ORE, 1);
        goldBarReqs.put(Material.OAK_LOG, 2);
        LinkedHashMap<Material, Integer> ironBarReqs = new LinkedHashMap<>();
        ironBarReqs.put(Material.IRON_ORE, 1);
        ironBarReqs.put(Material.OAK_LOG, 2);

        ItemGUI forgeMenu = super.craftingMenu(pl, 18);

        forgeMenu.setOption(4, new ItemStack(Material.FURNACE), "&eFurnace",
                "&fClick &7an item to start crafting!"
                        + "\n&fClick &7here to return to the station", 0);

        setupItems(forgeMenu, pl);

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

                int exp = 5;
                LinkedHashMap<Material, Integer> reqHashMap;
                if (event.getSlot() == 9) {
                    reqHashMap = chainLinkReqs;
                } else if (event.getSlot() == 10) {
                    reqHashMap = goldBarReqs;
                } else {
                    reqHashMap = ironBarReqs;
                }

                // destroy instance of inventory to prevent bugs
                event.setWillClose(true);
                event.setWillDestroy(true);

                // craft item based on experience and reagent amount
                super.startCrafting(pl, reqHashMap, 0, 0, event.getCurrentItem().getType(),
                        meta.getDisplayName(), currentLvl, exp,
                        ((Damageable) meta).getDamage(), Particle.FLAME,
                        Sound.ITEM_BUCKET_FILL_LAVA, Sound.BLOCK_LAVA_EXTINGUISH, 0, mult);
            }});

        return forgeMenu;
    }

    private void setupItems(ItemGUI forgeMenu, Player pl) {

        // chain link
        LinkedHashMap<Material, Integer> chainLinkReqs = new LinkedHashMap<>();
        chainLinkReqs.put(Material.IRON_ORE, 1);
        chainLinkReqs.put(Material.SPRUCE_LOG, 2);
        super.createMenuItem(forgeMenu, pl, 9, Material.IRON_BARS, "&fChain Link", chainLinkReqs,
                "Iron Ore\nSpruce Log", 999, 5, 0, 0, "",
                true);

        // gold bar
        LinkedHashMap<Material, Integer> goldBarReqs = new LinkedHashMap<>();
        goldBarReqs.put(Material.GOLD_ORE, 1);
        goldBarReqs.put(Material.OAK_LOG, 2);
        super.createMenuItem(forgeMenu, pl, 10, Material.GOLD_INGOT, "&fGold Bar", goldBarReqs,
                "Gold Ore\nOak Log", 999, 5, 0, 0, "",
                true);

        // iron bar
        LinkedHashMap<Material, Integer> ironBarReqs = new LinkedHashMap<>();
        ironBarReqs.put(Material.IRON_ORE, 1);
        ironBarReqs.put(Material.OAK_LOG, 2);
        super.createMenuItem(forgeMenu, pl, 11, Material.IRON_INGOT, "&fIron Bar",ironBarReqs,
                "Iron Ore\nOak Log", 999, 5, 0, 0, "",
                true);
    }

    @Override
    public void produceResult(Player pl, Material material, String dispName,
                              int currentLvl, int amt, int rate, int durability, int someVar) {

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
