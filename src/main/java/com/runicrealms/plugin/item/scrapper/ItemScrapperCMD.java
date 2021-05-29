//package com.runicrealms.plugin.item.scrapper;
//
//import com.runicrealms.plugin.command.subcommands.SubCommand;
//import com.runicrealms.plugin.command.supercommands.CurrencySC;
//import com.runicrealms.plugin.item.GUIMenu.ItemGUI;
//import org.bukkit.Bukkit;
//import org.bukkit.ChatColor;
//import org.bukkit.Sound;
//import org.bukkit.command.Command;
//import org.bukkit.command.CommandSender;
//import org.bukkit.entity.Player;
//import org.bukkit.plugin.Plugin;
//import com.runicrealms.plugin.RunicCore;
//import com.runicrealms.plugin.command.util.TabCompleteUtil;
//
//import java.util.List;
//
//public class ItemScrapperCMD implements SubCommand {
//
//    private CurrencySC currencySC;
//    private Plugin plugin = RunicCore.getInstance();
//
//    public ItemScrapperCMD(CurrencySC currencySC) {
//        this.currencySC = currencySC;
//    }
//
//
//    @Override
//    public void onConsoleCommand(CommandSender sender, String[] args) {
//
//        // if the sender does not specify the correct arguments
//        if (args.length != 2) {
//            sender.sendMessage(ChatColor.RED + "Usage: /currency scrapper {player}");
//            return;
//        }
//
//        Player pl = Bukkit.getPlayer(args[1]);
//        if (pl == null) return;
//
//        pl.playSound(pl.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
//        RunicCore.getShopManager().setPlayerShop(pl, new ItemScrapper(pl));
//        ItemGUI scrapperShop = ((RunicCore.getShopManager().getPlayerShop(pl))).getItemGUI();
//        scrapperShop.open(pl);
//    }
//
//    @Override
//    public void onOPCommand(Player sender, String[] args) {
//        this.onConsoleCommand(sender, args);
//    }
//
//    @Override
//    public void onUserCommand(Player sender, String[] args) {
//        this.onConsoleCommand(sender, args);
//    }
//
//    @Override
//    public String permissionLabel() {
//        return "runic.currency.scrapper";
//    }
//
//    @Override
//    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
//        return TabCompleteUtil.getPlayers(commandSender, strings, plugin);
//    }
//}
