package com.runicrealms.plugin.command.subcommands;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.command.supercommands.TravelSC;
import com.runicrealms.plugin.item.GUIMenu.ItemGUI;
import com.runicrealms.plugin.utilities.ColorUtil;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.Objects;

public class FastTravel implements SubCommand {

    private TravelSC travelSC;
    private static final int DURATION = 5;

    public FastTravel(TravelSC travelSC) {
        this.travelSC = travelSC;
    }

    @Override
    public void onConsoleCommand(CommandSender sender, String[] args)  {

        if (args.length != 8 && args.length != 10) {
            sender.sendMessage(ChatColor.YELLOW + "Command usage: /travel fast [player] [type] [x] [y] [z] [yaw] [pitch] (needsMoney?) (location?)");
            return;
        }

        // travel fast [player] [type] [x] [y] [z] [yaw] [pitch] (needsMoney?)
        Player pl = Bukkit.getPlayer(args[1]);
        if (pl == null) return;
        String type = args[2];
        Location loc = new Location(pl.getWorld(), Double.parseDouble(args[3]), Double.parseDouble(args[4]), Double.parseDouble(args[5]));
        loc.setYaw(Float.parseFloat(args[6]));
        loc.setPitch(Float.parseFloat(args[7]));

        if (args.length == 10) {
            ItemGUI travelGUI = travelMenu
                    (pl, type,
                            Integer.parseInt(args[3]), Integer.parseInt(args[4]), Integer.parseInt(args[5]),
                            Integer.parseInt(args[6]), Integer.parseInt(args[8]), args[9]);
            //Player pl, String type, int x, int y, int z, int yaw, int cost, String location) {
            travelGUI.open(pl);
            return;
        }

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

        if (args.length == 8 || args.length == 10) {
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

    private ItemGUI travelMenu(Player pl, String type, int x, int y, int z, int yaw, int cost, String location) {

        ItemGUI travelMenu = new ItemGUI("&f" + pl.getName() + "'s &eFast Travel Menu", 9, event -> {
        }, RunicCore.getInstance());

        String locSpaced = location.replace("_", " ");

        Material material;
        if (type.toLowerCase().equals("wagon")) {
            material = Material.SADDLE;
        } else {
            material = Material.OAK_BOAT;
        }

        // hearthstone button
        travelMenu.setOption(0, new ItemStack(material),
                "&a&lFast Travel",
                "\n&7Fast travel to: &a" + locSpaced +
                        "\n\n&6Price: &f" + cost + " &6Coins", 0);

        // close button
        travelMenu.setOption(8, new ItemStack(Material.BARRIER),
                "&cClose", "&7Close the menu", 0);

        // set the handler
        travelMenu.setHandler(event -> {

            if (event.getSlot() == 0) {

                event.setWillClose(true);
                event.setWillDestroy(true);

                // check that the player has the reagents
                if (!pl.getInventory().contains(Material.GOLD_NUGGET, cost)) {
                    pl.playSound(pl.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5f, 1);
                    pl.sendMessage(ChatColor.RED + "You don't have enough gold!");
                    return;
                }

                // take items from player
                ItemStack[] inv = pl.getInventory().getContents();
                for (int i = 0; i < inv.length; i++) {
                    if (pl.getInventory().getItem(i) == null) continue;
                    if (Objects.requireNonNull(pl.getInventory().getItem(i)).getType() == Material.GOLD_NUGGET) {
                        Objects.requireNonNull(pl.getInventory().getItem(i)).setAmount
                                (Objects.requireNonNull(pl.getInventory().getItem(i)).getAmount()-(cost));
                        break;
                    }
                }

                // dispatch command
                Bukkit.getServer().dispatchCommand
                        (Bukkit.getConsoleSender(), "travel fast " + pl.getName() +
                                " " + type + " " + x + " " + y + " " + z + " " + yaw + " " + 0);

            } else if (event.getSlot() == 8) {

                // close editor
                pl.playSound(pl.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1);
                event.setWillClose(true);
                event.setWillDestroy(true);
            }
        });
        return travelMenu;
    }
}
