package com.runicrealms.plugin.commands.admin;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CatchUnknown;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.Conditions;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Syntax;
import com.runicrealms.api.event.ChatChannelMessageEvent;
import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.model.SkillTreeData;
import com.runicrealms.plugin.player.utilities.PlayerLevelUtil;
import com.runicrealms.runicitems.util.CurrencyUtil;
import com.runicrealms.runicitems.util.ItemUtils;
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

    // todo: free from level 30, then use a linear function to calculate cost.
    private static final int FREE_THRESHOLD = 20;
    private final Set<UUID> chatters = new HashSet<>();

    public ResetTreeCMD() {
        Bukkit.getPluginManager().registerEvents(this, RunicCore.getInstance());
    }

    /**
     * @param player
     * @return
     */
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
            player.sendMessage(ChatColor.GRAY + "You ended the conversation.");
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
            if (toReset != null && !toReset.isOp()) {
                toReset.sendMessage
                        (
                                ChatColor.LIGHT_PURPLE + "You are about to reset your skill points! Based on your level, the cost will be " +
                                        getCostStringFromLevel(toReset) + ChatColor.LIGHT_PURPLE + ". To confirm, type " +
                                        ChatColor.GREEN + ChatColor.BOLD + "YES" + ChatColor.LIGHT_PURPLE + " or " + ChatColor.RED + ChatColor.BOLD + "NO"
                        );
                chatters.add(toReset.getUniqueId());
            } else {
                SkillTreeData.resetSkillTrees(toReset);
            }
        } catch (Exception e) {
            Bukkit.getLogger().info(ChatColor.RED + "Player not found!");
        }
    }
}
