package com.runicrealms.plugin.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.utilities.ColorUtil;
import org.bukkit.*;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

@CommandAlias("travel")
public class TravelCMD extends BaseCommand {

    // travel fast [player] [travelType] [x] [y] [z] [yaw] [pitch] {optional: price} {optional: locationString}

    @Default
    @CatchUnknown
    @Subcommand("fast")
    @Conditions("is-console-or-op")
    public void onCommand(CommandSender commandSender, String[] args) {
        if (args.length != 7) {
            commandSender.sendMessage(ChatColor.YELLOW + "Error, incorrect arguments. Usage: travel fast [player] [travelType] [x] [y] [z] [yaw] [pitch] {price} {locationString}");
            return;
        }
        Player player = Bukkit.getPlayer(args[0]);
        if (player == null) return;
        TravelType travelType = TravelType.valueOf(args[1].toUpperCase());
        Location location = new Location(player.getWorld(), Double.parseDouble(args[2]), Double.parseDouble(args[3]), Double.parseDouble(args[4]));
        location.setYaw(Float.parseFloat(args[5]));
        location.setPitch(Float.parseFloat(args[6]));
        fastTravelTask(player, travelType, TravelLocation.getFromLocation(location));
    }

    public enum TravelType {
        BOAT("boat", "Captain", Material.OAK_BOAT, Sound.ENTITY_PLAYER_SPLASH_HIGH_SPEED),
        WAGON("wagon", "Wagonmaster", Material.SADDLE, Sound.ENTITY_HORSE_GALLOP);

        private final String identifier;
        private final String npcName;
        private final Material material;
        private final Sound sound;

        TravelType(String identifier, String npcName, Material material, Sound sound) {
            this.identifier = identifier;
            this.npcName = npcName;
            this.material = material;
            this.sound = sound;
        }

        public String getIdentifier() {
            return identifier;
        }

        public String getNpcName() {
            return npcName;
        }

        public Material getMaterial() {
            return material;
        }

        public Sound getSound() {
            return sound;
        }

        public static TravelType getFromIdentifier(String identifier) {
            for (TravelType travelType : TravelType.values()) {
                if (travelType.getIdentifier().equalsIgnoreCase(identifier))
                    return travelType;
            }
            return null;
        }
    }

    public enum TravelLocation {
        AZANA("azana", "Azana", new Location(Bukkit.getWorld("Alterra"), -998.5, 34, 170.5,  270, 0));
//        KOLDORE(),
//        WHALETOWN(),
//        HILSTEAD(),
//        WINTERVALE(),
//        DEAD_MANS_REST(),
//        ISFODAR(),
//        TIRNEAS(),
//        ZENYTH(),
//        NAHEEN(),
//        NAZMORA();

        private final String identifier;
        private final String display;
        private final Location location;

        TravelLocation(String identifier, String display, Location location) {
            this.identifier = identifier;
            this.display = display;
            this.location = location;
        }

        public String getIdentifier() {
            return identifier;
        }

        public String getDisplay() {
            return display;
        }

        public Location getLocation() {
            return location;
        }

        public static TravelLocation getFromLocation(Location location) {
            for (TravelLocation travelLocation : TravelLocation.values()) {
                if (travelLocation.getLocation().equals(location))
                    return travelLocation;
            }
            return AZANA; // default if something went wrong
        }
    }

    public static void fastTravelTask(Player player, TravelType travelType, TravelLocation travelLocation) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 5 * 20, 2));
        player.teleport(travelLocation.getLocation());

        new BukkitRunnable() {
            int count = 1;
            @Override
            public void run() {

                if (count > 5) {
                    this.cancel();
                    player.sendMessage(ColorUtil.format("&aYou arrive at your destination!"));
                } else {
                    count += 1;
                    player.playSound(player.getLocation(), travelType.getSound(), 0.5f, 1.0f);
                }
            }
        }.runTaskTimerAsynchronously(RunicCore.getInstance(), 0, 20L);
    }
}