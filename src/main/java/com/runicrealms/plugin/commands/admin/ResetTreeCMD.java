package com.runicrealms.plugin.commands.admin;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CatchUnknown;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Conditions;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Syntax;
import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.chat.api.event.ChatChannelMessageEvent;
import com.runicrealms.plugin.model.SkillTreeData;
import com.runicrealms.plugin.runicitems.util.CurrencyUtil;
import com.runicrealms.plugin.runicitems.util.ItemUtils;
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
@CommandPermission("runic.op")
public class ResetTreeCMD extends BaseCommand implements Listener {

    // todo: free from level 30, then use a linear function to calculate cost.
    private static final int FREE_THRESHOLD = 20;
    private final Set<UUID> chatters = new HashSet<>();

    public ResetTreeCMD() {
        Bukkit.getPluginManager().registerEvents(this, RunicCore.getInstance());
    }

    public static int getCostFromLevel(Player player) {
        return 0;
//        if (player.getLevel() <= 24) {
//            return 0;
//        } else if (player.getLevel() <= 40) {
//            return 50;
//        } else if (player.getLevel() <= 59) {
//            return 100;
//        } else {
//            return 250;
//        }
    }

    public static String getCostStringFromLevel(int cost) {
        if (cost == 0) {
            return ChatColor.GREEN.toString() + ChatColor.BOLD + "FREE";
        } else {
            return ChatColor.GOLD.toString() + ChatColor.BOLD + cost + "c";
        }
    }

    @EventHandler(priority = EventPriority.LOWEST) // executes FIRST
    public void onChat(ChatChannelMessageEvent event) {
        if (!chatters.contains(event.getMessageSender().getUniqueId())) return;
        event.setCancelled(true);
        Player player = event.getMessageSender();
        if (event.getChatMessage().toLowerCase().contains("yes") && RunicCore.getShopAPI().hasItem(player, CurrencyUtil.goldCoin(), getCostFromLevel(player))) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(RunicCore.getInstance(), () -> {
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.0f);
                ItemUtils.takeItem(player, CurrencyUtil.goldCoin(), getCostFromLevel(player));
                SkillTreeData.resetSkillTrees(player);
            });
        } else if (event.getChatMessage().toLowerCase().contains("yes") && !RunicCore.getShopAPI().hasItem(player, CurrencyUtil.goldCoin(), getCostFromLevel(player))) {
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.5f, 1.0f);
            player.sendMessage(ChatColor.RED + "You don't have enough gold!");
        } else {
            player.sendMessage(ChatColor.GRAY + "You cancelled the reset request.");
        }
        chatters.remove(player.getUniqueId());
    }

    @Default
    @CatchUnknown
    @Conditions("is-console-or-op")
    @Syntax("<player>")
    @CommandCompletion("@players")
    public void onCommand(CommandSender commandSender, String[] args) {
        if (args.length == 0 && commandSender instanceof Player) {
            SkillTreeData.resetSkillTrees((Player) commandSender);
            return;
        }
        try {
            Player toReset = Bukkit.getPlayer(args[0]);
            if (toReset != null) {
                toReset.sendMessage
                        (
                                ChatColor.LIGHT_PURPLE + "You are about to reset your skill points! Based on your level, the cost will be " +
                                        getCostStringFromLevel(getCostFromLevel(toReset)) + ChatColor.LIGHT_PURPLE + ". To confirm, type " +
                                        ChatColor.GREEN + ChatColor.BOLD + "YES" + ChatColor.LIGHT_PURPLE + " or " + ChatColor.RED + ChatColor.BOLD + "NO"
                        );
                chatters.add(toReset.getUniqueId());
            }
        } catch (Exception e) {
            Bukkit.getLogger().info(ChatColor.RED + "Player not found!");
        }
    }
}
