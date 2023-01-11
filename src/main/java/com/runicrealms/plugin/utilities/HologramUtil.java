package com.runicrealms.plugin.utilities;

import com.runicrealms.plugin.RunicCore;
import net.minecraft.server.v1_16_R3.EntityArmorStand;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftArmorStand;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Consumer;

import java.util.HashMap;
import java.util.concurrent.ThreadLocalRandom;

public class HologramUtil {

    private static final HashMap<Player, HashMap<ArmorStand, BukkitTask>> HOLOGRAMS = new HashMap<>();

    /**
     * Used in place of damage holograms for when players are fighting each other
     *
     * @param createFor    the hologram is client-sided, only displays for player
     * @param createAround the location to spawn around (location is slightly random)
     */
    public static void createHealthBarHologram(Player createFor, Location createAround, int damageReceived) {
        createAround.add(0, 1, 0);
        int healthToDisplay = (int) (createFor.getHealth() - damageReceived);
        int maxHealth = (int) createFor.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
        double healthPercent = (double) healthToDisplay / maxHealth;
        ChatColor chatColor;
        if (healthPercent >= .75) {
            chatColor = ChatColor.GREEN;
        } else if (healthPercent >= .5) {
            chatColor = ChatColor.YELLOW;
        } else if (healthPercent >= .25) {
            chatColor = ChatColor.RED;
        } else {
            chatColor = ChatColor.DARK_RED;
        }
        createDamageHologram
                (
                        createFor,
                        createAround,
                        chatColor + "" + healthToDisplay + " ❤",
                        25
                ); // todo: rename this method
    }

    // builds the damage hologram
    public static void createDamageHologram(Player createFor, Location createAround, double hp, boolean... isCritical) {
        ChatColor chatColor = (isCritical.length > 0 && isCritical[0]) ? ChatColor.GOLD : ChatColor.RED;
        createDamageHologram(createFor, createAround, chatColor + "-" + (int) hp + " ❤⚔");
    }

    public static void createGenericDamageHologram(Player createFor, Location createAround, double hp) {
        createDamageHologram(createFor, createAround, ChatColor.RED + "-" + (int) hp + " ❤");
    }

    public static void createHealHologram(Player createFor, Location createAround, double hp, boolean... isCritical) {
        ChatColor chatColor = (isCritical.length > 0 && isCritical[0]) ? ChatColor.GOLD : ChatColor.GREEN;
        createDamageHologram(createFor, createAround, chatColor + "+" + (int) hp + " ❤✦");
    }

    public static void createShieldHologram(Player createFor, Location createAround, double hp) {
        createDamageHologram(createFor, createAround, ChatColor.WHITE + "+" + (int) hp + " ■");
    }

    public static void createShieldDamageHologram(Player createFor, Location createAround, double hp) {
        createDamageHologram(createFor, createAround, ChatColor.WHITE + "-" + (int) hp + " ■");
    }

    public static void createSpellDamageHologram(Player createFor, Location createAround, double hp, boolean... isCritical) {
        ChatColor chatColor = (isCritical.length > 0 && isCritical[0]) ? ChatColor.GOLD : ChatColor.DARK_AQUA;
        createDamageHologram(createFor, createAround, chatColor + "-" + (int) hp + " ❤ʔ");
    }

    /**
     * @param createFor
     * @param createAround
     * @param display
     * @param durationInTicks
     */
    @SuppressWarnings("unchecked")
    public static void createDamageHologram(Player createFor, Location createAround, String display, int... durationInTicks) {

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
        Consumer<ArmorStand> consumer = new InvisStandSpawner();
        ArmorStand stand = createAround.getWorld().spawn(createAround.add(xDif, yDif, zDif).subtract(0, 1, 0), ArmorStand.class, consumer);
        stand.setCustomName(display);

        // nms
        EntityArmorStand nmsStand = ((CraftArmorStand) stand).getHandle();
        nmsStand.setInvulnerable(true);
        nmsStand.noclip = true;
        nmsStand.setSmall(true);
        HashMap<ArmorStand, BukkitTask> holograms = HOLOGRAMS.computeIfAbsent(createFor, k -> new HashMap<>());

        // create our runnable
        int duration = durationInTicks.length > 0 ? durationInTicks[0] : 20;
        BukkitTask runnable = new BukkitRunnable() {

            int ticks = 0;

            @Override
            public void run() {
                if (ticks >= duration || !stand.isValid() || createFor != null && !createFor.isOnline()) {
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
