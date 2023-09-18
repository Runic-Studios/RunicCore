package com.runicrealms.plugin.utilities;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

public class FloatingItemUtil {

    /**
     * @param loc
     * @param material
     * @param duration
     */
    public static Item spawnFloatingItem(Location loc, Material material, int duration) {
        Item item = loc.getWorld().dropItem(loc, new ItemStack(material, 1));
        Vector vec = loc.toVector().multiply(0);
        item.setVelocity(vec);
        item.setGravity(false);
        item.setPickupDelay(Integer.MAX_VALUE);

        // tell the item when to despawn, based on duration (in seconds)
        setAge(duration, item);
        return item;
    }

    /**
     * @param loc
     * @param material
     * @param duration
     * @param vec
     * @param durab
     * @return
     */
    public static Entity spawnFloatingItem(Location loc, Material material, int duration, Vector vec, int durab) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        ((Damageable) meta).setDamage(durab);
        item.setItemMeta(meta);
        Item droppedItem = loc.getWorld().dropItem(loc, item);
        droppedItem.setVelocity(vec);
        droppedItem.setPickupDelay(Integer.MAX_VALUE);
//        // tell the item when to despawn, based on duration (in seconds)
        setAge(duration, droppedItem);
        return droppedItem;
    }

    /**
     * @param loc
     * @param material
     * @param duration
     * @return
     */
    public static Item createFloatingItem(Location loc, Material material, int duration) {
        Item item = loc.getWorld().dropItem(loc, new ItemStack(material, 1));
        Vector vec = loc.toVector().multiply(0);
        item.setVelocity(vec);
        item.setGravity(false);
        item.setPickupDelay(Integer.MAX_VALUE);

        // tell the item when to despawn, based on duration (in seconds)
        setAge(duration, item);
        return item;
    }

    /**
     * @param pl
     * @param loc
     * @param material
     * @param duration
     */
    public static Item spawnFloatingItem(Player pl, Location loc, Material material, int duration) {
        Item item = loc.getWorld().dropItem(loc, new ItemStack(material, 1));
        Vector vec = loc.toVector().multiply(0);
        item.setVelocity(vec);
        item.setGravity(false);
        item.setPickupDelay(Integer.MAX_VALUE);

        // send packets to make item invisible for all other players
        for (Player p : Bukkit.getServer().getOnlinePlayers()) {
            if (p == pl) continue;
            PacketContainer packet = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.ENTITY_DESTROY);
            packet.getIntegerArrays().write(0, new int[]{item.getEntityId()});
            ProtocolLibrary.getProtocolManager().sendServerPacket(p, packet);
        }

        // tell the item when to despawn, based on duration (in seconds)
        setAge(duration, item);
        return item;
    }

    /**
     * @param pl
     * @param loc
     * @param material
     * @param duration
     * @param vec
     */
    public static void spawnFloatingItem(Player pl, Location loc, Material material, int duration, Vector vec) {
        Item item = loc.getWorld().dropItem(loc, new ItemStack(material, 1));
        item.setVelocity(vec);
        item.setPickupDelay(Integer.MAX_VALUE);

        // send packets to make item invisible for all other players
        for (Player p : Bukkit.getServer().getOnlinePlayers()) {
            if (p == pl) continue;
            PacketContainer packet = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.ENTITY_DESTROY);
            packet.getIntegerArrays().write(0, new int[]{item.getEntityId()});
            ProtocolLibrary.getProtocolManager().sendServerPacket(p, packet);
        }

        // tell the item when to despawn, based on duration (in seconds)
        setAge(duration, item);
    }

    /**
     * @param pl
     * @param loc
     * @param material
     * @param duration
     * @param durability
     */
    public static void spawnFloatingItem(Player pl, Location loc, Material material, int duration, int durability) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        ((Damageable) meta).setDamage(durability);
        item.setItemMeta(meta);
        Item droppedItem = loc.getWorld().dropItem(loc, item);
        Vector vec = loc.toVector().multiply(0);
        droppedItem.setVelocity(vec);
        droppedItem.setPickupDelay(Integer.MAX_VALUE);

        // send packets to make item invisible for other players
        for (Player online : Bukkit.getServer().getOnlinePlayers()) {
            if (online == pl) continue;
            PacketContainer packet = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.ENTITY_DESTROY);
            packet.getIntegerArrays().write(0, new int[]{droppedItem.getEntityId()});
            ProtocolLibrary.getProtocolManager().sendServerPacket(online, packet);
        }

        // tell the item when to despawn, based on duration (in seconds)
        setAge(duration, droppedItem);
    }

    /**
     * @param duration
     * @param item
     */
    private static void setAge(int duration, Item item) {
        item.setTicksLived(6000 - (20 * duration));
    }
}
