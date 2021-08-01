package com.runicrealms.plugin.utilities;

import com.runicrealms.plugin.RunicCore;
import net.minecraft.server.v1_16_R3.EntityArmorStand;
import net.minecraft.server.v1_16_R3.PacketPlayOutEntityDestroy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftArmorStand;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Consumer;

import java.util.HashMap;
import java.util.concurrent.ThreadLocalRandom;

public class HologramUtil {

    private static final HashMap<Player, HashMap<ArmorStand, BukkitTask>> HOLOGRAMS = new HashMap<>();

    // builds the damage hologram
    public static void createDamageHologram(Player createFor, Location createAround, double hp) {
        createDamageHologram(createFor, createAround, ChatColor.RED + "-" + (int) hp + " ❤⚔");
    }

    public static void createGenericDamageHologram(Player createFor, Location createAround, double hp) {
        createDamageHologram(createFor, createAround, ChatColor.RED + "-" + (int) hp + " ❤");
    }

    public static void createHealHologram(Player createFor, Location createAround, double hp) {
        createDamageHologram(createFor, createAround, ChatColor.GREEN + "+" + (int) hp + " ❤✦");
    }

    public static void createShieldHologram(Player createFor, Location createAround, double hp) {
        createDamageHologram(createFor, createAround, ChatColor.WHITE + "+" + (int) hp + " ■");
    }

    public static void createShieldDamageHologram(Player createFor, Location createAround, double hp) {
        createDamageHologram(createFor, createAround, ChatColor.WHITE + "-" + (int) hp + " ■");
    }

    public static void createSpellDamageHologram(Player createFor, Location createAround, double hp) {
        createDamageHologram(createFor, createAround, ChatColor.DARK_AQUA + "-" + (int) hp + " ❤ʔ");
    }

    /**
     * Create a hologram that floats up and deletes itself
     */
    @SuppressWarnings("unchecked")
    public static void createDamageHologram(Player createFor, Location createAround, String display) {

        // variation of the tag
        //Random rand = new ThreadLocalRandom();
        double xDif = -0.5 + (int) (Math.random() * ((0.5 - (-0.5)) + 1));//(randInt(0, 20) - 10) / 10D //1
        if (xDif == 0) {
            xDif = 0.5;
        }
        double yDif = (randInt(0, 20) - 10) / 10D;//Math.random()
        double zDif = -0.5 + (int) (Math.random() * ((0.5 - (-0.5)) + 1));
        if (zDif == 0) {
            zDif = 0.5;
        }

        // use our consumer to prevent the armorstand from spawning into the world before it's invisible
        Consumer consumer = new InvisStandSpawner();
        ArmorStand stand = createAround.getWorld().spawn(createAround.add(xDif, yDif, zDif).subtract(0, 1, 0), ArmorStand.class, (Consumer<ArmorStand>) (Consumer<?>) consumer);
        stand.setCustomName(display);

        // nms
        EntityArmorStand nmsStand = ((CraftArmorStand) stand).getHandle();
        nmsStand.setInvulnerable(true);
        nmsStand.noclip = true;
        nmsStand.setSmall(true);
        HashMap<ArmorStand, BukkitTask> holograms = HOLOGRAMS.computeIfAbsent(createFor, k -> new HashMap<>());

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
                nmsStand.lastY = nmsStand.lastY + .1D;
                ticks++;
            }
        }.runTaskTimer(RunicCore.getInstance(), 0, 1);

        holograms.put(stand, runnable);
        if (holograms.keySet().size() > 4)
            removeDamageHologram(createFor, holograms.keySet().toArray(new ArmorStand[1])[0]);
    }

    @SuppressWarnings("unchecked")
    public static void createStaticHologram(Player createFor, Location createAround, String display, double x, double y, double z, boolean isClient) {

        // build the custom armorstand
        Consumer consumer = new InvisStandSpawner();
        ArmorStand stand = createAround.getWorld().spawn(createAround.add(x, y, z).subtract(0, 1, 0), ArmorStand.class, (Consumer<ArmorStand>) (Consumer<?>) consumer);
        stand.setCustomName(display);

        // nms
        EntityArmorStand nmsStand = ((CraftArmorStand) stand).getHandle();
        nmsStand.noclip = true;
        nmsStand.setSmall(true);
        nmsStand.setInvulnerable(true);
        HashMap<ArmorStand, BukkitTask> holograms = HOLOGRAMS.computeIfAbsent(createFor, k -> new HashMap<>());

        // send packets to make item invisible for all other players
        for (Player p : Bukkit.getServer().getOnlinePlayers()) {
            if (p == createFor) continue;
            PacketPlayOutEntityDestroy packet = new PacketPlayOutEntityDestroy(stand.getEntityId());
            ((CraftPlayer) p).getHandle().playerConnection.sendPacket(packet);
        }

        // create our runnable
        BukkitTask runnable = new BukkitRunnable() {

            int ticks = 0;

            @Override
            public void run() {
                if (ticks >= 30 || !stand.isValid() || createFor != null && !createFor.isOnline()) {
                    cancel();
                    removeDamageHologram(createFor, stand);
                    return;
                }
                ticks++;
            }
        }.runTaskTimer(RunicCore.getInstance(), 0, 1);

        holograms.put(stand, runnable);
        if (holograms.keySet().size() > 4)
            removeDamageHologram(createFor, holograms.keySet().toArray(new ArmorStand[1])[0]);
    }

    @SuppressWarnings("unchecked")
    public static void createStaticHologram(Player createFor, Location createAround, String display, double x, double y, double z) {

        // build the custom armorstand
        Consumer consumer = new InvisStandSpawner();
        ArmorStand stand = createAround.getWorld().spawn(createAround.add(x, y, z).subtract(0, 1, 0), ArmorStand.class, (Consumer<ArmorStand>) (Consumer<?>) consumer);
        stand.setCustomName(display);

        // nms
        EntityArmorStand nmsStand = ((CraftArmorStand) stand).getHandle();
        nmsStand.noclip = true;
        nmsStand.setSmall(true);
        nmsStand.setInvulnerable(true);
        HashMap<ArmorStand, BukkitTask> holograms = HOLOGRAMS.computeIfAbsent(createFor, k -> new HashMap<>());

        // create our runnable
        BukkitTask runnable = new BukkitRunnable() {

            int ticks = 0;

            @Override
            public void run() {
                if (ticks >= 30 || !stand.isValid() || createFor != null && !createFor.isOnline()) {
                    cancel();
                    removeDamageHologram(createFor, stand);
                    return;
                }
                ticks++;
            }
        }.runTaskTimer(RunicCore.getInstance(), 0, 1);

        holograms.put(stand, runnable);
        if (holograms.keySet().size() > 4)
            removeDamageHologram(createFor, holograms.keySet().toArray(new ArmorStand[1])[0]);
    }

    private static void removeDamageHologram(Player player, ArmorStand armorStand) {
        if (armorStand.isDead())
            return;

        HashMap<ArmorStand, BukkitTask> map = HOLOGRAMS.get(player);
        BukkitTask task = map.remove(armorStand);
        armorStand.remove();
        if (task != null)
            task.cancel();

        if (map.isEmpty())
            HOLOGRAMS.remove(player);
    }

    private static int randInt(int min, int max) {
        int bound = max - min + 1;
        if (bound <= 0) return 0;
        return ThreadLocalRandom.current().nextInt(bound) + min;
    }
}
