package us.fortherealm.plugin.utilities;

import net.minecraft.server.v1_13_R2.EntityArmorStand;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_13_R2.entity.CraftArmorStand;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import us.fortherealm.plugin.Main;

import java.util.HashMap;
import java.util.concurrent.ThreadLocalRandom;

public class DamageIndicators {

    private static HashMap<Player, HashMap<ArmorStand, BukkitTask>> DAMAGE_HOLOGRAMS = new HashMap<>();

    // builds the damage hologram
    public static void createDamageHologram(Player createFor, Location createAround, double hp) {
        createDamageHologram(createFor, createAround, ChatColor.RED + "-" + (int) hp + " ❤");
    }

    public static int randInt(int min, int max) {
        int bound = max - min + 1;
        if (bound <= 0) return 0;
        return ThreadLocalRandom.current().nextInt(bound) + min;
    }

    /**
     * Create a hologram that floats up and deletes itself
     */
    public static void createDamageHologram(Player createFor, Location createAround, String display) {

        // variation of the tag
        //Random rand = new ThreadLocalRandom();
        double xDif = -0.5 + (int)(Math.random() * ((0.5 - (-0.5)) + 1));//(randInt(0, 20) - 10) / 10D //1
        if (xDif == 0) {
            xDif = 0.5;
        }
        double yDif = (randInt(0, 20) - 10) / 10D;//Math.random()
        double zDif = -0.5 + (int)(Math.random() * ((0.5 - (-0.5)) + 1));
        if (zDif == 0) {
            zDif = 0.5;
        }

        // build the custom armorstand
        ArmorStand stand = (ArmorStand) createAround.getWorld().spawnEntity(createAround.add(xDif, yDif, zDif).subtract(0, 1, 0), EntityType.ARMOR_STAND);
        stand.setVisible(false);
        stand.setCollidable(false);
        stand.setCustomName(display);
        stand.setCustomNameVisible(true);
        stand.setInvulnerable(true);
        stand.setGravity(false);
        stand.setMarker(true);

        // nms
        EntityArmorStand nmsStand = ((CraftArmorStand) stand).getHandle();
        nmsStand.noclip = true;
        nmsStand.setSmall(true);
        HashMap<ArmorStand, BukkitTask> holograms = DAMAGE_HOLOGRAMS.computeIfAbsent(createFor, k -> new HashMap<>());

        // create our runnable
        BukkitTask runnable = new BukkitRunnable() {

            int ticks = 0;

            @Override
            public void run() {
                if (ticks >= 20 || !stand.isValid() || createFor != null && !createFor.isOnline()) {
                    cancel();
                    removeDamageHologram(createFor, stand);
                    return;
                }
                nmsStand.locY = nmsStand.locY + .1D;
                ticks++;
            }
        }.runTaskTimer(Main.getInstance(), 0, 1);

        holograms.put(stand, runnable);
        if (holograms.keySet().size() > 4)
            removeDamageHologram(createFor, holograms.keySet().toArray(new ArmorStand[1])[0]);
    }

    private static void removeDamageHologram(Player player, ArmorStand armorStand) {
        if (armorStand.isDead())
            return;

        HashMap<ArmorStand, BukkitTask> map = DAMAGE_HOLOGRAMS.get(player);
        BukkitTask task = map.remove(armorStand);
        armorStand.remove();
        if (task != null)
            task.cancel();

        if (map.isEmpty())
            DAMAGE_HOLOGRAMS.remove(player);
    }
}
