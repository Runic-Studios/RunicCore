package com.runicrealms.plugin.item.commands;

import com.runicrealms.plugin.attributes.AttributeUtil;
import com.runicrealms.plugin.command.subcommands.SubCommand;
import com.runicrealms.plugin.command.supercommands.CurrencySC;
import com.runicrealms.plugin.item.LoreGenerator;
import com.runicrealms.plugin.utilities.CurrencyUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.command.util.TabCompleteUtil;

import java.util.List;

public class CurrencyPouch implements SubCommand {

    private CurrencySC currencySC;
    private Plugin plugin = RunicCore.getInstance();

    public CurrencyPouch(CurrencySC currencySC) {
        this.currencySC = currencySC;
    }


    @Override
    public void onConsoleCommand(CommandSender sender, String[] args) {

        // if the sender does not specify the correct arguments
        if (args.length != 3) {
            sender.sendMessage(ChatColor.RED + "Usage: /currency pouch {player} {size}");
            return;
        }

        Player pl = Bukkit.getPlayer(args[1]);
        if (pl == null) return;
        int size = Integer.parseInt(args[2]);

        if (size % 64 != 0) {
            pl.sendMessage(ChatColor.RED + "Error: size must be divisible by 64.");
            return;
        }

        ItemStack goldPouch = new ItemStack(Material.SHEARS);
        goldPouch = AttributeUtil.addCustomStat(goldPouch, "pouchSize", size);
        LoreGenerator.generateGoldPouchLore(goldPouch);

        if (pl.getInventory().firstEmpty() != -1) {
            pl.getInventory().addItem(goldPouch);
        } else {
            pl.getWorld().dropItem(pl.getLocation(), goldPouch);
        }
    }

    @Override
    public void onOPCommand(Player sender, String[] args) {
        this.onConsoleCommand(sender, args);
    }

    @Override
    public void onUserCommand(Player sender, String[] args) {
        this.onConsoleCommand(sender, args);
    }

    @Override
    public String permissionLabel() {
        return "runic.currency.pouch";
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
        return TabCompleteUtil.getPlayers(commandSender, strings, plugin);
    }
}
