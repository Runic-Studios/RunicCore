package com.runicrealms.plugin.item;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Random;

/**
 * This class generates four necessary pieces of information for an item, which serves as the baseline to create
 * a 'tiered' item, i.e. common, uncommon, rare, epic.
 */
public class TieredItemGenerator {

    private String itemTypeName = ""; // just a cosmetic thing, determined the name of the item
    private Material material = Material.STICK; // used for armor
    private int durability = 0; // used for shears helmets
    private String itemSlot = ""; // used for NBT attributes to determine which 'slot' the item must be in to activate boost

    public TieredItemGenerator() {
        generateItem();
    }

    private void generateItem() {

        Random rand = new Random();

        String itemType = "";
        int type = rand.nextInt(4) + 1;
        switch (type) {
            case 1:
                itemType = "helmet";
                break;
            case 2:
                itemType = "chestplate";
                break;
            case 3:
                itemType = "leggings";
                break;
            case 4:
                itemType = "boots";
                break;
        }

        int randomNum = rand.nextInt(5) + 1;
        switch (itemType.toLowerCase()) {
            case "helmet":
                material = Material.SHEARS;
                itemSlot = "head";
                switch (randomNum) {
                    case 1:
                        durability = 5;
                        itemTypeName = "Hood";
                        break;
                    case 2:
                        durability = 10;
                        itemTypeName = "Cowl";
                        break;
                    case 3:
                        durability = 15;
                        itemTypeName = "Coif";
                        break;
                    case 4:
                        durability = 20;
                        itemTypeName = "Crown";
                        break;
                    case 5:
                        durability = 25;
                        itemTypeName = "Helm";
                        break;
                }
                break;
            case "chestplate":
                itemSlot = "chest";
                switch (randomNum) {
                    case 1:
                        material = Material.DIAMOND_CHESTPLATE;
                        itemTypeName = "Robe";
                        break;
                    case 2:
                        material = Material.LEATHER_CHESTPLATE;
                        itemTypeName = "Tunic";
                        break;
                    case 3:
                        material = Material.CHAINMAIL_CHESTPLATE;
                        itemTypeName = "Chest";
                        break;
                    case 4:
                        material = Material.GOLDEN_CHESTPLATE;
                        itemTypeName = "Chestplate";
                        break;
                    case 5:
                        material = Material.IRON_CHESTPLATE;
                        itemTypeName = "Chestplate";
                        break;
                }
                break;
            case "leggings":
                itemSlot = "legs";
                switch (randomNum) {
                    case 1:
                        material = Material.DIAMOND_LEGGINGS;
                        itemTypeName = "Legs";
                        break;
                    case 2:
                        material = Material.LEATHER_LEGGINGS;
                        itemTypeName = "Chaps";
                        break;
                    case 3:
                        material = Material.CHAINMAIL_LEGGINGS;
                        itemTypeName = "Tassets";
                        break;
                    case 4:
                        material = Material.GOLDEN_LEGGINGS;
                        itemTypeName = "Platelegs";
                        break;
                    case 5:
                        material = Material.IRON_LEGGINGS;
                        itemTypeName = "Platelegs";
                        break;
                }
                break;
            case "boots":
                itemSlot = "feet";
                switch (randomNum) {
                    case 1:
                        material = Material.DIAMOND_BOOTS;
                        itemTypeName = "Boots";
                        break;
                    case 2:
                        material = Material.LEATHER_BOOTS;
                        itemTypeName = "Boots";
                        break;
                    case 3:
                        material = Material.CHAINMAIL_BOOTS;
                        itemTypeName = "Greaves";
                        break;
                    case 4:
                        material = Material.GOLDEN_BOOTS;
                        itemTypeName = "Boots";
                        break;
                    case 5:
                        material = Material.IRON_BOOTS;
                        itemTypeName = "Boots";
                        break;
                }
                break;
        }
    }


    /**
     * Determines which class we're dealing with, based on the material of the item,
     * in order to determine the correct stat range.
     */
    public static String determineClass(ItemStack item, Material material) {
        String className = "";
        if (material == Material.SHEARS) {
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                int durability = ((Damageable) meta).getDamage();
                switch (durability) {
                    case 5:
                        className = "mage";
                        break;
                    case 10:
                        className = "rogue";
                        break;
                    case 15:
                        className = "archer";
                        break;
                    case 20:
                        className = "cleric";
                        break;
                    case 25:
                        className = "warrior";
                        break;
                }
            }
        } else if (material == Material.DIAMOND_CHESTPLATE
                || material == Material.DIAMOND_LEGGINGS
                || material == Material.DIAMOND_BOOTS) {
            className = "mage";
        } else if (material == Material.LEATHER_CHESTPLATE
                || material == Material.LEATHER_LEGGINGS
                || material == Material.LEATHER_BOOTS) {
            className = "rogue";
        } else if (material == Material.CHAINMAIL_CHESTPLATE
                || material == Material.CHAINMAIL_LEGGINGS
                || material == Material.CHAINMAIL_BOOTS) {
            className = "archer";
        } else if (material == Material.GOLDEN_CHESTPLATE
                || material == Material.GOLDEN_LEGGINGS
                || material == Material.GOLDEN_BOOTS) {
            className = "cleric";
        } else if (material == Material.IRON_CHESTPLATE
                || material == Material.IRON_LEGGINGS
                || material == Material.IRON_BOOTS) {
            className = "warrior";
        }
        return className;
    }

    public String getItemTypeName() {
        return itemTypeName;
    }

    public void setItemTypeName(String itemTypeName) {
        this.itemTypeName = itemTypeName;
    }

    public Material getMaterial() {
        return material;
    }

    public void setMaterial(Material material) {
        this.material = material;
    }

    public int getDurability() {
        return durability;
    }

    public void setDurability(int durability) {
        this.durability = durability;
    }

    public String getItemSlot() {
        return itemSlot;
    }

    public void setItemSlot(String itemSlot) {
        this.itemSlot = itemSlot;
    }
}
