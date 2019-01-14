package us.fortherealm.plugin.professions.utilities;

import net.minecraft.server.v1_13_R2.PacketPlayOutEntityDestroy;
import org.bukkit.*;
import org.bukkit.craftbukkit.v1_13_R2.entity.CraftPlayer;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.lang.reflect.Field;

public class FloatingItemUtil {

    public static void spawnFloatingItem(Player pl, Location loc, Material material, int duration) {
        Item item = loc.getWorld().dropItem(loc, new ItemStack(material, 1));
        Vector vec = loc.toVector().multiply(0);
        item.setVelocity(vec);
        item.setPickupDelay(Integer.MAX_VALUE);

        // send packets to make item invisible for all other players
        for (Player p : Bukkit.getServer().getOnlinePlayers()) {
            if (p == pl) continue;
            PacketPlayOutEntityDestroy packet = new PacketPlayOutEntityDestroy(item.getEntityId());
            ((CraftPlayer) p).getHandle().playerConnection.sendPacket(packet);
        }

        // tell the item when to despawn, based on duration (in seconds)
        setAge(duration, item);
    }

    public static void spawnFloatingItem(Player pl, Location loc, Material material, int duration, Vector vec) {
        Item item = loc.getWorld().dropItem(loc, new ItemStack(material, 1));
        item.setVelocity(vec);
        item.setPickupDelay(Integer.MAX_VALUE);

        // send packets to make item invisible for all other players
        for (Player p : Bukkit.getServer().getOnlinePlayers()) {
            if (p == pl) continue;
            PacketPlayOutEntityDestroy packet = new PacketPlayOutEntityDestroy(item.getEntityId());
            ((CraftPlayer) p).getHandle().playerConnection.sendPacket(packet);
        }

        // tell the item when to despawn, based on duration (in seconds)
        setAge(duration, item);
    }

    private static void setAge(int duration, Item item) {
        try
        {
            Field itemField = item.getClass().getDeclaredField("item");
            Field ageField;
            Object entityItem;

            itemField.setAccessible(true);
            entityItem = itemField.get(item);

            ageField = entityItem.getClass().getDeclaredField("age");
            ageField.setAccessible(true);
            ageField.set(entityItem, 6000 - (20 * duration));
        }
        catch (NoSuchFieldException | IllegalAccessException e)
        {
            e.printStackTrace();
        }
    }
}
