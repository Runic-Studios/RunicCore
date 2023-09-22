package com.runicrealms.plugin.utilities;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

public class FloatingItemUtil {

    /**
     * @param loc        of the item
     * @param material   of the item
     * @param duration   that the item will last (in seconds)
     * @param vec        the vector to determine the item velocity
     * @param durability of the floating item (to change the texture)
     * @return a floating entity
     */
    public static Entity spawnFloatingItem(Location loc, Material material, int duration, Vector vec, int durability) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        ((Damageable) meta).setDamage(durability);
        item.setItemMeta(meta);
        Item droppedItem = loc.getWorld().dropItem(loc, item);
        droppedItem.setVelocity(vec);
        droppedItem.setPickupDelay(Integer.MAX_VALUE);
        // Tell the item when to despawn, based on duration (in seconds)
        setAge(duration, droppedItem);
        return droppedItem;
    }

    /**
     * @param duration of the item to last (in seconds)
     * @param item     to be expired
     */
    private static void setAge(int duration, Item item) {
        item.setTicksLived(6000 - (20 * duration));
    }
}
