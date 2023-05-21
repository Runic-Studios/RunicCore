package com.runicrealms.plugin.listeners;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.model.SkillTreeData;
import com.runicrealms.plugin.rdb.RunicDatabase;
import com.runicrealms.plugin.rdb.event.CharacterLoadedEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLevelChangeEvent;

import java.util.UUID;

/**
 * Class for informing the player of how many levels until they unlock skill points, or whether they have points unspent
 */
public class SkillPointsListener implements Listener {

    private static final int ANNOUNCEMENT_TIME = 300; // seconds
    private static final int SKILL_TREE_UNLOCK_LEVEL = 10;

    public SkillPointsListener() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(RunicCore.getInstance(), () -> {
            for (UUID uuid : RunicDatabase.getAPI().getCharacterAPI().getLoadedCharacters()) {
                Player player = Bukkit.getPlayer(uuid);
                if (player == null) continue;
                int slot = RunicDatabase.getAPI().getCharacterAPI().getCharacterSlot(uuid);
                if (slot == 0) continue;
                int pointsToSpend = RunicCore.getSkillTreeAPI().getAvailableSkillPoints
                        (
                                uuid,
                                slot
                        );
                if (pointsToSpend > 0)
                    sendSkillPointsReminderMessage(player, pointsToSpend);
            }
        }, 0, ANNOUNCEMENT_TIME * 20L);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onLevel(PlayerLevelChangeEvent event) {
        if (!RunicDatabase.getAPI().getCharacterAPI().getLoadedCharacters().contains(event.getPlayer().getUniqueId()))
            return; // ignore the change from PlayerJoinEvent
        if (event.getNewLevel() > SKILL_TREE_UNLOCK_LEVEL) return;
        Player player = event.getPlayer();
        if (event.getNewLevel() == 10 && event.getOldLevel() != 0) { // ignores login level-up
            player.sendMessage
                    (
                            ChatColor.RED + "[!] " + ChatColor.LIGHT_PURPLE + "You have unlocked " +
                                    ChatColor.GREEN + ChatColor.BOLD + "SKILL TREES" + ChatColor.LIGHT_PURPLE +
                                    "! Right click your Ancient Runestone to unlock new perks " +
                                    "with your skill points!"
                    );
        } else if (event.getOldLevel() != 0 || (event.getOldLevel() == 0 && event.getNewLevel() == 1)) { // ignores login level-up
            int levelsRemainingUntilSkillTrees = SKILL_TREE_UNLOCK_LEVEL - event.getNewLevel();
            player.sendMessage
                    (
                            ChatColor.RED + "[!] " + ChatColor.LIGHT_PURPLE + "You have " + ChatColor.WHITE +
                                    levelsRemainingUntilSkillTrees + ChatColor.LIGHT_PURPLE + " level(s) remaining until you unlock " +
                                    ChatColor.GREEN + ChatColor.BOLD + "SKILL TREES" + ChatColor.LIGHT_PURPLE + "!"
                    );
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onLoaded(CharacterLoadedEvent event) {
        int pointsToSpend = SkillTreeData.getAvailablePoints
                (
                        event.getPlayer().getUniqueId(),
                        event.getCharacterSelectEvent().getSlot()
                );
        if (pointsToSpend > 0)
            sendSkillPointsReminderMessage(event.getPlayer(), pointsToSpend);
    }

    /**
     * Sends the player a reminder to spend their available skill points
     *
     * @param player to receive message
     * @param points their available points
     */
    private void sendSkillPointsReminderMessage(Player player, int points) {
        player.sendMessage
                (
                        ChatColor.RED + "[!] " + ChatColor.LIGHT_PURPLE + "You have " + ChatColor.WHITE + points +
                                ChatColor.LIGHT_PURPLE + " skill points to spend! Visit your " + ChatColor.GREEN + ChatColor.BOLD +
                                "SKILL TREE" + ChatColor.LIGHT_PURPLE + " to purchase new perks!"
                );
    }
}
