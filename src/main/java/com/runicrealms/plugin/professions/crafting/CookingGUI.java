package com.runicrealms.plugin.professions.crafting;

import com.runicrealms.plugin.item.GUIMenu.ItemGUI;
import com.runicrealms.plugin.professions.Workstation;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Objects;

public class CookingGUI extends Workstation {

    public CookingGUI() {
    }

    public ItemGUI openMenu(Player pl) {

        // name the menu
        ItemGUI furnaceMenu = super.openWorkstation(pl);
        furnaceMenu.setName("&f&l" + pl.getName() + "'s &e&lCooking Fire");

        //set the visual items
        furnaceMenu.setOption(3, new ItemStack(Material.BREAD),
                "&fCook Food", "&7Create food for your journey!", 0);

        // set the handler
        furnaceMenu.setHandler(event -> {

            if (event.getSlot() == 3) {

                // open the forging menu
                pl.playSound(pl.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1);
                ItemGUI forge = openCookingMenu(pl);
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

    private ItemGUI openCookingMenu(Player pl) {

        // bread
        LinkedHashMap<Material, Integer> breadReqs = new LinkedHashMap<>();
        breadReqs.put(Material.WHEAT, 3);
        breadReqs.put(Material.SPRUCE_LOG, 1);

        // cod
        LinkedHashMap<Material, Integer> codReqs = new LinkedHashMap<>();
        codReqs.put(Material.COD, 1);
        codReqs.put(Material.OAK_LOG, 1);

        // salmon
        LinkedHashMap<Material, Integer> salmonReqs = new LinkedHashMap<>();
        salmonReqs.put(Material.SALMON, 1);
        salmonReqs.put(Material.OAK_LOG, 1);

        ItemGUI cookingMenu = super.craftingMenu(pl, 18);

        cookingMenu.setOption(4, new ItemStack(Material.FLINT_AND_STEEL), "&eCooking Fire",
                "&fClick &7an item to start crafting!"
                        + "\n&fClick &7here to return to the station", 0);

        setupItems(cookingMenu, pl);

        cookingMenu.setHandler(event -> {

            if (event.getSlot() == 4) {

                // return to the first menu
                pl.playSound(pl.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1);
                ItemGUI menu = openMenu(pl);
                menu.open(pl);
                event.setWillClose(false);
                event.setWillDestroy(true);

            } else {

                LinkedHashMap<Material, Integer> reqs = new LinkedHashMap<>();

                if (event.getSlot() == 9) {
                    reqs = breadReqs;
                } else if (event.getSlot() == 10) {
                    reqs = codReqs;
                } else if (event.getSlot() == 11) {
                    reqs = salmonReqs;
                }

                int mult = 1;
                if (event.isRightClick()) mult = 5;
                ItemMeta meta = Objects.requireNonNull(event.getCurrentItem()).getItemMeta();
                if (meta == null) return;

                // destroy instance of inventory to prevent bugs
                event.setWillClose(true);
                event.setWillDestroy(true);

                // craft item based on experience and reagent amount
                super.startCrafting(pl, reqs, 0, 0, event.getCurrentItem().getType(),
                        meta.getDisplayName(), 0, 0,
                        ((Damageable) meta).getDamage(), Particle.SMOKE_NORMAL,
                        Sound.ENTITY_GHAST_SHOOT, Sound.BLOCK_LAVA_EXTINGUISH, 0, mult);
            }});

        return cookingMenu;
    }

    private void setupItems(ItemGUI forgeMenu, Player pl) {

        // bread
        LinkedHashMap<Material, Integer> breadReqs = new LinkedHashMap<>();
        breadReqs.put(Material.WHEAT, 3);
        breadReqs.put(Material.SPRUCE_LOG, 1);
        super.createMenuItem(forgeMenu, pl, 9, Material.BREAD, "&fBread", breadReqs,
                "Wheat\nSpruce Log", 999, 0, 0, 0, "",
                true, false);

        // cod
        LinkedHashMap<Material, Integer> codReqs = new LinkedHashMap<>();
        codReqs.put(Material.COD, 1);
        codReqs.put(Material.OAK_LOG, 1);
        super.createMenuItem(forgeMenu, pl, 10, Material.COOKED_COD, "&fCooked Cod", codReqs,
                "Cod\nOak Log", 999, 0, 0, 0, "",
                true, false);

        // salmon
        LinkedHashMap<Material, Integer> salmonReqs = new LinkedHashMap<>();
        salmonReqs.put(Material.SALMON, 1);
        salmonReqs.put(Material.OAK_LOG, 1);
        super.createMenuItem(forgeMenu, pl, 11, Material.COOKED_SALMON, "&fCooked Salmon", salmonReqs,
                "Salmon\nOak Log", 999, 0, 0, 0, "",
                true, false);
    }

    @Override
    public void produceResult(Player pl, Material material, String dispName,
                              int currentLvl, int amt, int rate, int durability, int someVar) {

        for (int i = 0; i < amt; i++) {
            ItemStack craftedItem = new ItemStack(material);
            ItemMeta meta = craftedItem.getItemMeta();
            ((Damageable) Objects.requireNonNull(meta)).setDamage(durability);

            ArrayList<String> lore = new ArrayList<>();

            lore.add(ChatColor.GRAY + "Consumable");
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
