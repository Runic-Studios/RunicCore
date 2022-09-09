package com.runicrealms.plugin.listeners;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.api.RunicCoreAPI;
import com.runicrealms.plugin.character.api.CharacterLoadedEvent;
import com.runicrealms.plugin.model.CharacterData;
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
            for (UUID uuid : RunicCoreAPI.getLoadedCharacters()) {
                Player player = Bukkit.getPlayer(uuid);
                if (player == null) continue;
                int slot = RunicCoreAPI.getCharacterSlot(uuid);
                if (slot == 0) continue;
                int pointsToSpend = RunicCoreAPI.getAvailableSkillPoints
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
        if (!RunicCoreAPI.getLoadedCharacters().contains(event.getPlayer().getUniqueId()))
            return; // ignore the change from PlayerJoinEvent
        if (event.getNewLevel() > SKILL_TREE_UNLOCK_LEVEL) return;
        Player player = event.getPlayer();
        if (event.getNewLevel() == 10 && event.getOldLevel() != 0) { // ignores login level-up
            player.sendMessage
                    (
                            ChatColor.RED + "[!] " + ChatColor.LIGHT_PURPLE + "You have unlocked " +
                                    ChatColor.GREEN + ChatColor.BOLD + "SKILL TREES" + ChatColor.LIGHT_PURPLE +
                                    "! Right click your Ancient Rune to unlock new perks with your skill points!"
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
        CharacterData characterData = event.getCharacterSelectEvent().getCharacterData();
        int pointsToSpend = RunicCore.getSkillTreeManager().loadSpentPointsData
                (
                        event.getPlayer().getUniqueId(),
                        characterData.getBaseCharacterInfo().getSlot(),
                        event.getCharacterSelectEvent().getJedis()
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
