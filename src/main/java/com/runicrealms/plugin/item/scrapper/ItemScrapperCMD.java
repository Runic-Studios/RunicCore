package com.runicrealms.plugin.item.scrapper;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.item.GUIMenu.ItemGUI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandAlias("itemscrapper")
public class ItemScrapperCMD extends BaseCommand {

    @Default
    @CatchUnknown
    @Conditions("is-console-or-op")
    @Syntax("<player>")
    @CommandCompletion("@players")
    public void onCommand(CommandSender commandSender, String[] args) {
        if (args.length != 1) {
            Bukkit.getLogger().info(ChatColor.RED + "Error: incorrect number of arguments");
            return;
        }
        try {
        Player player = Bukkit.getPlayer(args[0]);
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
        RunicCore.getRunicShopManager().setPlayerShop(player, new ItemScrapper(player));
        ItemGUI scrapperShop = ((RunicCore.getRunicShopManager().getPlayerShop(player))).getItemGUI();
        scrapperShop.open(player);
        } catch (Exception e) {
            Bukkit.getLogger().info(ChatColor.RED + "Player not found!");
        }
    }
}
