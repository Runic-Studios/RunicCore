package com.runicrealms.plugin.spellapi.skilltrees.cmd;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import com.runicrealms.api.event.ChatChannelMessageEvent;
import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.api.RunicCoreAPI;
import com.runicrealms.plugin.item.util.ItemRemover;
import com.runicrealms.plugin.player.utilities.PlayerLevelUtil;
import com.runicrealms.plugin.spellapi.skilltrees.SkillTree;
import com.runicrealms.plugin.utilities.CurrencyUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@CommandAlias("resettree")
public class ResetTreeCMD extends BaseCommand implements Listener {

    private final Set<UUID> chatters = new HashSet<>();

    public ResetTreeCMD() {
        Bukkit.getPluginManager().registerEvents(this, RunicCore.getInstance());
    }

    @Default
    @CatchUnknown
    @Conditions("is-console")
    @Syntax("<player>")
    @CommandCompletion("@players")
    public void onCommand(CommandSender commandSender, String[] args) {
        if (args.length == 0 && commandSender instanceof Player) {
            SkillTree.resetTree((Player) commandSender);
            return;
        }
        try {
            Player toReset = Bukkit.getPlayer(args[0]);
            if (toReset != null && !toReset.isOp()) {
                toReset.sendMessage
                        (
                                ChatColor.LIGHT_PURPLE + "You are about to reset your skill points! Based on your level, the cost will be " +
                                getCostStringFromLevel(toReset) + ChatColor.LIGHT_PURPLE + ". To confirm, type " +
                                ChatColor.GREEN + ChatColor.BOLD + "YES" + ChatColor.LIGHT_PURPLE + " or " + ChatColor.RED + ChatColor.BOLD + "NO"
                        );
                chatters.add(toReset.getUniqueId());
            } else {
                SkillTree.resetTree(toReset);
            }
        } catch (Exception e) {
            Bukkit.getLogger().info(ChatColor.RED + "Player not found!");
        }
    }

    @EventHandler(priority = EventPriority.LOWEST) // executes FIRST
    public void onChat(ChatChannelMessageEvent e) {
        if (!chatters.contains(e.getMessageSender().getUniqueId())) return;
        e.setCancelled(true);
        Player player = e.getMessageSender();
        if (e.getChatMessage().toLowerCase().contains("yes") && RunicCoreAPI.hasItems(player, CurrencyUtil.goldCoin(), getCostFromLevel(player))) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(RunicCore.getInstance(), () -> {
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.0f);
                ItemRemover.takeItem(player, CurrencyUtil.goldCoin(), getCostFromLevel(player));
                SkillTree.resetTree(player);
            });
        } else if (e.getChatMessage().toLowerCase().contains("yes") && !RunicCoreAPI.hasItems(player, CurrencyUtil.goldCoin(), getCostFromLevel(player))) {
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.5f, 1.0f);
            player.sendMessage(ChatColor.RED + "You don't have enough gold!");
        } else {
            player.sendMessage(ChatColor.GRAY + "You ended the conversation.");
        }
        chatters.remove(player.getUniqueId());
    }

    public static int getCostFromLevel(Player player) {
        if (player.getLevel() == PlayerLevelUtil.getMaxLevel()) {
            return 1000;
        } else if (player.getLevel() < PlayerLevelUtil.getMaxLevel() && player.getLevel() > 29) {
            return 250;
        } else {
            return 0;
        }
    }

    public static String getCostStringFromLevel(Player player) {
        if (player.getLevel() >= 30) {
            return ChatColor.GOLD + "" + ChatColor.BOLD + getCostFromLevel(player) + "c";
        } else {
            return ChatColor.GREEN + "" + ChatColor.BOLD + "FREE";
        }
    }
}
