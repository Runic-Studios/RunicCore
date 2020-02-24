package com.runicrealms.plugin.item.commands;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.command.subcommands.SubCommand;
import com.runicrealms.plugin.command.util.TabCompleteUtil;
import com.runicrealms.plugin.item.hearthstone.HearthstoneListener;
import com.runicrealms.plugin.player.commands.SetSC;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.List;

public class HearthstoneCMD implements SubCommand {

    private SetSC set;
    private Plugin plugin = RunicCore.getInstance();

    public HearthstoneCMD(SetSC set) {
        this.set = set;
    }

    @Override
    public void onConsoleCommand(CommandSender sender, String[] args) {

        // if the sender does not specify a player
        if (args.length != 3 && args.length != 4) {
            sender.sendMessage(ChatColor.RED + "Correct usage: /set hearthstone [player] [location] (cost)");
        } else {

            Player pl = Bukkit.getPlayer(args[1]);
            if (pl == null) return;
            String location = args[2];

            if (args.length == 3) {
                //HearthstoneListener.setupHearthstone(pl, location);
                pl.getInventory().setItem(8, HearthstoneListener.newHearthstone(location));
            }
//            } else {
//                ItemGUI hearthstoneGUI = hearthstoneMenu(pl, args[2], Integer.parseInt(args[3]));
//                hearthstoneGUI.open(pl);
//            }
        }
    }

    @Override
    public void onOPCommand(Player sender, String[] args) {
        // if the sender does not specify a player
        if (args.length != 3 && args.length != 4) {
            sender.sendMessage(ChatColor.RED + "Correct usage: /set hearthstone [player] [location]");
        } else {
            this.onConsoleCommand(sender, args);
        }
    }

    @Override
    public void onUserCommand(Player sender, String[] args) {

        // if the sender does not specify a player
        if (args.length != 3 && args.length != 4) {
            sender.sendMessage(ChatColor.RED + "Correct usage: /set hearthstone [player] [location]");
        } else {
            this.onConsoleCommand(sender, args);
        }
    }

    @Override
    public String permissionLabel() {
        return "set.hearthstone";
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {

        return TabCompleteUtil.getPlayers(commandSender, strings, plugin);
    }

//    private ItemGUI hearthstoneMenu(Player pl, String location, int cost) {
//
//        ItemGUI hearthMenu = new ItemGUI("&f" + pl.getName() + "'s &eInnkeeper Menu", 9, event -> {
//        }, RunicCore.getInstance());
//
//        String locSpaced = location.replace("_", " ");
//
//        // hearthstone button
//        hearthMenu.setOption(0, new ItemStack(Material.CLAY_BALL),
//                "&b&lChange Hearthstone",
//                "\n&7Change hearthstone to: &a" + locSpaced +
//                        "\n\n&6Price: &f" + cost + " &6Coins", 0, false);
//
//        // close button
//        hearthMenu.setOption(8, new ItemStack(Material.BARRIER),
//                "&cClose", "&7Close the menu", 0, false);
//
//        // set the handler
//        hearthMenu.setHandler(event -> {
//
//            if (event.getSlot() == 0) {
//
//                event.setWillClose(true);
//                event.setWillDestroy(true);
//
//                // check that the player has the reagents
//                if (!pl.getInventory().contains(Material.GOLD_NUGGET, cost)) {
//                    pl.playSound(pl.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5f, 1);
//                    pl.sendMessage(ChatColor.RED + "You don't have enough gold!");
//                    return;
//                }
//
//                // take items from player
//                ItemStack[] inv = pl.getInventory().getContents();
//                for (int i = 0; i < inv.length; i++) {
//                    if (pl.getInventory().getItem(i) == null) continue;
//                    if (Objects.requireNonNull(pl.getInventory().getItem(i)).getType() == Material.GOLD_NUGGET) {
//                        Objects.requireNonNull(pl.getInventory().getItem(i)).setAmount
//                                (Objects.requireNonNull(pl.getInventory().getItem(i)).getAmount()-(cost));
//                        break;
//                    }
//                }
//
//                // dispatch command
//                Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "set hearthstone " + pl.getName() + " " + location);
//                pl.playSound(pl.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 2.0f);
//                pl.sendMessage(ChatColor.AQUA + "You've changed your hearthstone location!");
//
//            } else if (event.getSlot() == 8) {
//
//                // close editor
//                pl.playSound(pl.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1);
//                event.setWillClose(true);
//                event.setWillDestroy(true);
//            }
//        });
//        return hearthMenu;
//    }
}
