package com.runicrealms.plugin.professions.commands;

import com.runicrealms.plugin.professions.gathering.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import com.runicrealms.plugin.command.subcommands.SubCommand;

import java.util.List;

public class GathertoolGive implements SubCommand {

    private GathertoolSC toolSC;

    public GathertoolGive(GathertoolSC toolSC) {
        this.toolSC = toolSC;
    }

    @Override
    public void onConsoleCommand(CommandSender sender, String[] args) {

    }

    @Override
    public void onOPCommand(Player sender, String[] args) {

        if(args.length != 4) {
            sender.sendMessage(ChatColor.YELLOW + "Command usage: /gathertool give [player] [tool] [tier]");
            return;
        }

        Player pl = Bukkit.getPlayer(args[1]);
        if (pl == null) return;
        String toolName = args[2];
        int tier = Integer.parseInt(args[3]);

//        Material pickType;
//        switch (tier) {
//            case 5:
//                pickType = Material.DIAMOND_PICKAXE;
//                break;
//            case 4:
//                pickType = Material.GOLDEN_PICKAXE;
//                break;
//            case 3:
//                pickType = Material.IRON_PICKAXE;
//                break;
//            case 2:
//                pickType = Material.STONE_PICKAXE;
//                break;
//            case 1:
//            default:
//                pickType = Material.WOODEN_PICKAXE;
//                break;
//        }

        switch (toolName.toLowerCase()) {
            case "axe":
                pl.getInventory().addItem(GatheringUtil.getGatheringTool(Material.IRON_AXE, tier));
                break;
            case "hoe":
                pl.getInventory().addItem(GatheringUtil.getGatheringTool(Material.IRON_HOE, tier));
                break;
            case "pickaxe":
                pl.getInventory().addItem(GatheringUtil.getGatheringTool(Material.IRON_PICKAXE, tier));
                break;
            case "rod":
                pl.getInventory().addItem(GatheringUtil.getGatheringTool(Material.FISHING_ROD, tier));
                break;
            default:
                pl.sendMessage(ChatColor.DARK_RED + "Please choose a tool: axe, hoe, pickaxe, or rod");
        }
    }

    @Override
    public void onUserCommand(Player sender, String[] args) {
    }

    @Override
    public String permissionLabel() {
        return "tool.give";
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
        return null;
        //return TabCompleteUtil.getPlayers(commandSender, strings, RunicCore.getInstance());
    }
}
