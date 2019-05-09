package us.fortherealm.plugin.command.subcommands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import us.fortherealm.plugin.FTRCore;
import us.fortherealm.plugin.command.subcommands.SubCommand;
import us.fortherealm.plugin.command.supercommands.CurrencySC;
import us.fortherealm.plugin.command.supercommands.SkillpointSC;
import us.fortherealm.plugin.command.util.TabCompleteUtil;
import us.fortherealm.plugin.scoreboard.ScoreboardHandler;
import us.fortherealm.plugin.utilities.CurrencyUtil;

import java.util.List;

public class CurrencyGive implements SubCommand {

    private CurrencySC currencySC;
    private Plugin plugin = FTRCore.getInstance();

    public CurrencyGive(CurrencySC currencySC) {
        this.currencySC = currencySC;
    }


    @Override
    public void onConsoleCommand(CommandSender sender, String[] args) {
        this.onUserCommand((Player) sender, args);
    }

    @Override
    public void onOPCommand(Player sender, String[] args) {
        this.onUserCommand(sender, args);
    }

    @Override
    public void onUserCommand(Player sender, String[] args) {

        // if the sender does not specify the correct arguments
        if (args.length != 3) {
            sender.sendMessage(ChatColor.RED + "Usage: /currency give {player} {amount}");
            return;
        }

        Player pl = Bukkit.getPlayer(args[1]);
        if (pl == null) return;
        int amt = Integer.parseInt(args[2]);

        for (int i = 0; i < amt; i++) {
            if (pl.getInventory().firstEmpty() != -1) {
                pl.getInventory().addItem(CurrencyUtil.goldCoin());
            } else {
                pl.getWorld().dropItem(pl.getLocation(), CurrencyUtil.goldCoin());
            }
        }
    }

    @Override
    public String permissionLabel() {
        return "ftrcore.currency";
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {

        return TabCompleteUtil.getPlayers(commandSender, strings, plugin);
    }
}
