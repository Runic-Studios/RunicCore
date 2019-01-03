package us.fortherealm.plugin.professions.utilities;

import net.minecraft.server.v1_13_R2.PacketPlayOutEntityDestroy;
import org.bukkit.*;
import org.bukkit.craftbukkit.v1_13_R2.entity.CraftPlayer;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.lang.reflect.Field;

public class ItemUtils {

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
//    public static BukkitTask startCrafting(Player pl, Location loc, Material material, int exp) {
//        BukkitTask crafting = new BukkitRunnable() {
//            int count = 0;
//            @Override
//            public void run() {
//                if (count == 3) {
//                    ItemUtils.spawnFloatingItem(pl, loc, material, 1);
//                } else if (count > 3) {
//                    this.cancel();
//                    pl.playSound(pl.getLocation(), Sound.BLOCK_LAVA_EXTINGUISH, 0.5f, 0.2f);
//                    //smeltItem(pl, material, name);
//                    pl.sendMessage(ChatColor.GREEN + "Done!");
//                    ProfExpUtil.giveExperience(pl, exp);
//                }
//                pl.playSound(pl.getLocation(), Sound.BLOCK_LAVA_POP, 0.5f, 1.0f);
//                pl.playSound(pl.getLocation(), Sound.ITEM_BUCKET_FILL_LAVA, 0.5f, 1.0f);
//                pl.spawnParticle(Particle.FLAME, loc, 25, 0.25, 0.25, 0.25, 0.01);
//                count = count + 1;
//            }
//        }.runTaskTimer(Main.getInstance(), 0, 20);
//        return crafting;
//    }
}
