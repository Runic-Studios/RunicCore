package com.runicrealms.plugin.listeners;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.api.RunicCoreAPI;
import com.runicrealms.plugin.character.api.CharacterLoadedEvent;
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
                if (!playerHasUnspentSkillPoints(uuid, slot)) continue;
                sendSkillPointsReminderMessage(player, slot);
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
                            ChatColor.RED + "[!] " + ChatColor.LIGHT_PURPLE + "You have unlocked " +
                                    ChatColor.GREEN + ChatColor.BOLD + "SKILL TREES" + ChatColor.LIGHT_PURPLE +
                                    "! Right click your Ancient Rune to unlock new perks with your skill points!"
                    );
        } else if (e.getOldLevel() != 0 || (e.getOldLevel() == 0 && e.getNewLevel() == 1)) { // ignores login level-up
            int levelsRemainingUntilSkillTrees = SKILL_TREE_UNLOCK_LEVEL - e.getNewLevel();
            player.sendMessage
                    (
                            ChatColor.RED + "[!] " + ChatColor.LIGHT_PURPLE + "You have " + ChatColor.WHITE +
                                    levelsRemainingUntilSkillTrees + ChatColor.LIGHT_PURPLE + " level(s) remaining until you unlock " +
                                    ChatColor.GREEN + ChatColor.BOLD + "SKILL TREES" + ChatColor.LIGHT_PURPLE + "!"
                    );
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onLogin(CharacterLoadedEvent e) {
        Bukkit.getScheduler().runTaskTimerAsynchronously(RunicCore.getInstance(), () -> {
            if (playerHasUnspentSkillPoints(e.getPlayer().getUniqueId(), e.getCharacterData().getBaseCharacterInfo().getSlot()))
                sendSkillPointsReminderMessage(e.getPlayer(), e.getCharacterData().getBaseCharacterInfo().getSlot());
        }, 90 * 20L, 180 * 20L);
    }

    /**
     * Check if a player has any available skill points to spend
     *
     * @param uuid of player to check
     * @return true if there are points to spend
     */
    private boolean playerHasUnspentSkillPoints(UUID uuid, int slot) {
        return RunicCoreAPI.getAvailableSkillPoints(uuid, slot) > 0;
    }

    /**
     * Message to remind players to spend skill points
     */
    private void sendSkillPointsReminderMessage(Player player, int slot) {
        player.sendMessage
                (
                        ChatColor.RED + "[!] " + ChatColor.LIGHT_PURPLE + "You have " + ChatColor.WHITE + RunicCoreAPI.getAvailableSkillPoints(player.getUniqueId(), slot) +
                                ChatColor.LIGHT_PURPLE + " skill points to spend! Visit your " + ChatColor.GREEN + ChatColor.BOLD +
                                "SKILL TREE" + ChatColor.LIGHT_PURPLE + " to purchase new perks!"
                );
    }
}
