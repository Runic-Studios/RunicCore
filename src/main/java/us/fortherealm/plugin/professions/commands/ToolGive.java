package us.fortherealm.plugin.professions.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import us.fortherealm.plugin.command.subcommands.SubCommand;
import us.fortherealm.plugin.professions.gathering.FarmingListener;
import us.fortherealm.plugin.professions.gathering.FishingListener;
import us.fortherealm.plugin.professions.gathering.MiningListener;
import us.fortherealm.plugin.professions.gathering.WCListener;

import java.util.List;

public class ToolGive implements SubCommand {

    private ToolSC toolSC;

    public ToolGive(ToolSC toolSC) {
        this.toolSC = toolSC;
    }

    @Override
    public void onConsoleCommand(CommandSender sender, String[] args) {

    }

    @Override
    public void onOPCommand(Player sender, String[] args) {

        if(args.length != 3) {
            sender.sendMessage(ChatColor.YELLOW + "Command usage: /gathertool give [tool] [tier]");
            return;
        }

        int tier = Integer.parseInt(args[2]);
        String toolName = args[1];

        switch (toolName.toLowerCase()) {
            case "axe":
                sender.getInventory().addItem(WCListener.getGatheringAxe(tier));
                break;
            case "hoe":
                sender.getInventory().addItem(FarmingListener.getGatheringHoe(tier));
                break;
            case "pickaxe":
                sender.getInventory().addItem(MiningListener.getGatheringPick(tier));
                break;
            case "rod":
                sender.getInventory().addItem(FishingListener.getGatheringRod(tier));
                break;
            default:
                sender.sendMessage(ChatColor.DARK_RED + "Please choose a tool: axe, hoe, pickaxe, or rod");
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
        //return TabCompleteUtil.getPlayers(commandSender, strings, FTRCore.getInstance());
    }
}
