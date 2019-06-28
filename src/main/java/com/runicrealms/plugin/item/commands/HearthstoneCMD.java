package com.runicrealms.plugin.item.commands;

import com.runicrealms.plugin.classes.ClassGUI;
import com.runicrealms.plugin.item.ItemGUI;
import com.runicrealms.plugin.player.PlayerLevelUtil;
import com.runicrealms.plugin.player.commands.SetSC;
import com.runicrealms.plugin.professions.crafting.BSFurnaceGUI;
import com.runicrealms.plugin.professions.utilities.ProfExpUtil;
import com.runicrealms.plugin.utilities.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandSendEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.command.subcommands.SubCommand;
import com.runicrealms.plugin.command.util.TabCompleteUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
                ClassGUI.setupHearthstone(pl, location);
            } else {
                ItemGUI hearthstoneGUI = hearthstoneMenu(pl, args[2], Integer.parseInt(args[3]));
                hearthstoneGUI.open(pl);
            }
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

    private ItemGUI hearthstoneMenu(Player pl, String location, int cost) {

        ItemGUI hearthMenu = new ItemGUI("&f&l" + pl.getName() + "'s &eInnkeeper Menu", 9, event -> {
        }, RunicCore.getInstance());

        // hearthstone button
        hearthMenu.setOption(0, new ItemStack(Material.CLAY_BALL),
                "&bSet Hearthstone to &a" + location,
                "&7Change your hearthstone location!" +
                        "\n\n&6Price: &f" + cost + " &6Coins", 0);

        // close button
        hearthMenu.setOption(8, new ItemStack(Material.BARRIER),
                "&cClose", "&7Close the menu", 0);

        // set the handler
        hearthMenu.setHandler(event -> {

            if (event.getSlot() == 0) {

                Bukkit.broadcastMessage("here");
                event.setWillClose(true);
                event.setWillDestroy(true);

            } else if (event.getSlot() == 8) {

                // close editor
                pl.playSound(pl.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1);
                event.setWillClose(true);
                event.setWillDestroy(true);
            }
        });
        return hearthMenu;
    }
}
