package me.skyfallin.plugin.commands;

import net.minecraft.server.v1_12_R1.CommandExecute;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;

public class Commands extends CommandExecute implements Listener, CommandExecutor {

    //private CustomItems ci = new CustomItems();

    public String artifactcmd = "artifactinfo";
    public String runecmd = "runeinfo";
    // public String getweaponcmd = "getweapon";
    public String getrune = "getrune";


    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        Player player = (Player) sender;
        ItemStack item = new ItemStack(Material.DIAMOND_HOE, 1);
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName(ChatColor.GREEN + "Test Staff");
        ArrayList<String> lore = new ArrayList<String>();
        lore.add(ChatColor.GRAY + "Staff");
        meta.setLore(lore);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);

        if (sender instanceof Player) {
            if (cmd.getName().equalsIgnoreCase(artifactcmd)) {
                sender.sendMessage(ChatColor.RED + "*TODO* " + ChatColor.GREEN + "Artifacts may be placed in the artifact slot.");
                return true;
            } else if (cmd.getName().equalsIgnoreCase(runecmd)) {
                sender.sendMessage(ChatColor.RED + "*TODO* " + ChatColor.GREEN + "Runes may be placed in the rune slot.");
                return true;
           // } else if (cmd.getName().equalsIgnoreCase(getweaponcmd)) {
               // player.getInventory().addItem(item);
               // return true;
            } else if (cmd.getName().equalsIgnoreCase(getrune)) {
                player.getInventory().addItem(item);
            }

    }else{

            sender.sendMessage(ChatColor.RED + "This command is intended for use by a player.");
            return false;
        }
        return false;

    }
}
