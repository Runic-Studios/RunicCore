package com.runicrealms.plugin.command.subcommands;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.command.supercommands.TravelSC;
import com.runicrealms.plugin.utilities.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

public class FastTravel implements SubCommand {

    private TravelSC travelSC;
    private static final int DURATION = 5;

    public FastTravel(TravelSC travelSC) {
        this.travelSC = travelSC;
    }

    @Override
    public void onConsoleCommand(CommandSender sender, String[] args)  {

        if (args.length != 8 && args.length != 9) {
            sender.sendMessage(ChatColor.YELLOW + "Command usage: /travel fast [player] [type] [x] [y] [z] [yaw] [pitch] ([needsMoney?])");
            return;
        }

        // travel {player} {type} {x} {y} {z}
        Player pl = Bukkit.getPlayer(args[1]);
        if (pl == null) return;
        String type = args[2];
        Location loc = new Location(pl.getWorld(), Double.parseDouble(args[3]), Double.parseDouble(args[4]), Double.parseDouble(args[5]));
        loc.setYaw(Float.parseFloat(args[6]));
        loc.setPitch(Float.parseFloat(args[7]));

        String npcName;
        Sound sound;
        switch (type.toLowerCase()) {
            case "boat":
                npcName = "Captain";
                sound = Sound.ENTITY_PLAYER_SPLASH_HIGH_SPEED;
                break;
            case "wagon":
                npcName = "Wagonmaster";
                sound = Sound.ENTITY_HORSE_GALLOP;
                break;
            default:
                sender.sendMessage(ChatColor.RED + "Something went wrong!");
                return;
        }

        // wagon command
        if (args.length == 9 && Boolean.parseBoolean(args[8])) {
            pl.sendMessage(ColorUtil.format("&7[1/1] &e" + npcName + ": &f"));
        }

        pl.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, DURATION*20, 2));
        pl.teleport(loc);

        new BukkitRunnable() {
            int count = 1;
            @Override
            public void run() {

                if (count > DURATION) {
                    this.cancel();
                    pl.sendMessage(ColorUtil.format("&aYou arrive at your destination!"));
                } else {
                    count += 1;
                    pl.playSound(pl.getLocation(), sound, 0.5f, 1.0f);
                }
            }
        }.runTaskTimerAsynchronously(RunicCore.getInstance(), 0, 20L);

    }

    @Override
    public void onOPCommand(Player sender, String[] args) {

        if (args.length == 8 || args.length == 9) {
            this.onConsoleCommand(sender, args);
        } else {
            sender.sendMessage(ChatColor.YELLOW + "Command usage: /travel fast [player] [type] [x] [y] [z] [yaw] [pitch] [needsMoney?]");
        }
    }

    @Override
    public void onUserCommand(Player sender, String[] args) {
    }

    @Override
    public String permissionLabel() {
        return "runic.travel.fast";
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
        return null;
        //return TabCompleteUtil.getPlayers(commandSender, strings, RunicCore.getInstance());
    }
}
