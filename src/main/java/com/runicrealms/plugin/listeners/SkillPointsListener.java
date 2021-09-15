package com.runicrealms.plugin.listeners;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.api.RunicCoreAPI;
import com.runicrealms.plugin.character.api.CharacterLoadEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLevelChangeEvent;

/**
 * Class for informing the player of how many levels until they unlock skill points, or whether they have points unspent
 */
public class SkillPointsListener implements Listener {

    private static final int ANNOUNCEMENT_TIME = 300; // seconds
    private static final int SKILL_TREE_UNLOCK_LEVEL = 10;

    public SkillPointsListener() {
        Bukkit.getScheduler().scheduleAsyncRepeatingTask(RunicCore.getInstance(), () -> {
            for (Player loadedPlayer : RunicCoreAPI.getLoadedPlayers()) {
                if (!playerHasUnspentSkillPoints(loadedPlayer)) continue;
                sendSkillPointsReminderMessage(loadedPlayer);
            }
        }, 0, ANNOUNCEMENT_TIME * 20L);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onLevel(PlayerLevelChangeEvent e) {
        if (e.getNewLevel() > SKILL_TREE_UNLOCK_LEVEL) return;
        Player player = e.getPlayer();
        if (e.getNewLevel() == 10 && e.getOldLevel() != 0) { // ignores login level-up
            player.sendMessage
                    (
                            ChatColor.RED + "[!] " + ChatColor.GRAY + "You have unlocked " +
                                    ChatColor.GREEN + ChatColor.BOLD + "SKILL TREES" + ChatColor.GRAY +
                                    "! Open your inventory, then click your " + ChatColor.LIGHT_PURPLE +
                                    "Ancient Rune " + ChatColor.GRAY + "to unlock new perks with your skill points!"
                    );
        } else if (e.getOldLevel() != 0 || (e.getOldLevel() == 0 && e.getNewLevel() == 1)) { // ignores login level-up
            int levelsRemainingUntilSkillTrees = SKILL_TREE_UNLOCK_LEVEL - e.getNewLevel();
            player.sendMessage
                    (
                            ChatColor.RED + "[!] " + ChatColor.GRAY + "You have " + ChatColor.WHITE +
                                    levelsRemainingUntilSkillTrees + ChatColor.GRAY + " level(s) remaining until you unlock " +
                                    ChatColor.GREEN + ChatColor.BOLD + "SKILL TREES" + ChatColor.GRAY + "!"
                    );
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onLogin(CharacterLoadEvent e) {
        Bukkit.getScheduler().runTaskTimerAsynchronously(RunicCore.getInstance(), () -> {
            if (playerHasUnspentSkillPoints(e.getPlayer()))
                sendSkillPointsReminderMessage(e.getPlayer());
        }, 90L, 180L);
    }

    /**
     * Check if a player has any available skill points to spend
     *
     * @param player to check
     * @return true if there are points to spend
     */
    private boolean playerHasUnspentSkillPoints(Player player) {
        return RunicCoreAPI.getAvailableSkillPoints(player) > 0;
    }

    /**
     * Message to remind players to spend skill points
     */
    private void sendSkillPointsReminderMessage(Player player) {
        player.sendMessage
                (
                        ChatColor.RED + "[!] " + ChatColor.GRAY + "You have " + ChatColor.WHITE + RunicCoreAPI.getAvailableSkillPoints(player) +
                                ChatColor.GRAY + " skill points to spend! Visit your " + ChatColor.GREEN + ChatColor.BOLD +
                                "SKILL TREE" + ChatColor.GRAY + " to purchase new perks!"
                );
    }
}
