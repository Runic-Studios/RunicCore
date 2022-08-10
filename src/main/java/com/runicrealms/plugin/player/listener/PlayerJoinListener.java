package com.runicrealms.plugin.player.listener;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.character.api.CharacterSelectEvent;
import com.runicrealms.plugin.model.CharacterData;
import com.runicrealms.plugin.model.PlayerData;
import com.runicrealms.plugin.player.utilities.HealthUtils;
import com.runicrealms.plugin.player.utilities.PlayerLevelUtil;
import com.runicrealms.plugin.resourcepack.ResourcePackManager;
import com.runicrealms.runicnpcs.api.RunicNpcsAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;

@SuppressWarnings("deprecation")
public class PlayerJoinListener implements Listener {

    /**
     * Reset the player's displayed values when they join the server, before selecting a character
     */
    @EventHandler(priority = EventPriority.LOWEST) // first
    public void onJoin(PlayerJoinEvent e) {
        e.setJoinMessage("");
        Player player = e.getPlayer();
        player.getInventory().clear();
        player.setInvulnerable(true);
        player.setMaxHealth(20);
        player.setHealth(player.getMaxHealth());
        player.setHealthScale(20);
        player.setLevel(0);
        player.setExp(0);
        player.setFoodLevel(20);
        player.teleport(new Location(Bukkit.getWorld("Alterra"), -2318.5, 2, 1720.5));
        // build database file sync (if it doesn't exist)
        Bukkit.getScheduler().runTaskLater(RunicCore.getInstance(), () -> {
            PlayerData playerData = RunicCore.getDatabaseManager().loadPlayerData(player);
            Bukkit.broadcastMessage("building player data object");
            RunicCore.getDatabaseManager().getPlayerDataMap().put(player.getUniqueId(), playerData);
            ResourcePackManager.openPackForPlayer(player); // prompt resource pack (triggers character select screen)
        }, 1L);
    }

    /**
     * Loads values on login from the CharacterData object once they select a character from select screen
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onJoin(CharacterSelectEvent e) {
        Bukkit.getScheduler().runTaskLater(RunicCore.getInstance(),
                () -> loadCharacterData(e.getPlayer(), e.getCharacterData()), 1L);
    }

    /**
     * Setup for new players
     */
    @EventHandler(priority = EventPriority.LOWEST) // first
    public void onFirstLoad(CharacterSelectEvent event) {
        if (event.getPlayer().hasPlayedBefore()) return;
        Player player = event.getPlayer();
        // broadcast new player welcome message
        Bukkit.getServer().broadcastMessage(ChatColor.WHITE + player.getName()
                + ChatColor.LIGHT_PURPLE + " joined the realm for the first time!");
        // heal player
        HealthUtils.setPlayerMaxHealth(player);
        player.setHealthScale(HealthUtils.getHeartAmount());
        int playerHealth = (int) player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
        player.setHealth(playerHealth);
        player.setFoodLevel(20);
    }

    /**
     * Handles loading in Runic NPCs on player login
     * Loads with delay, allowing for data loading in NPCs plugin
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onLoadHandleNPCs(CharacterSelectEvent event) {
        Bukkit.getScheduler().runTaskLater(RunicCore.getInstance(), () -> RunicNpcsAPI.updateNpcsForPlayer(event.getPlayer()), 5L);
    }

    /**
     * Allows donator ranks to enter a full server
     */
    @EventHandler
    public void onJoinFullServer(PlayerLoginEvent e) {
        if (e.getResult() == PlayerLoginEvent.Result.KICK_FULL) {
            if (e.getPlayer().hasPermission("core.full.join")) {
                e.allow();
            }
        }
    }

    /**
     * Sets up some basic player values, such as max health, level, location, etc.
     *
     * @param player        to set values for
     * @param characterData the stored object with values (to be deleted after use)
     */
    private void loadCharacterData(Player player, CharacterData characterData) {
        player.setInvulnerable(false);
        HealthUtils.setPlayerMaxHealth(player);
        player.setHealthScale(HealthUtils.getHeartAmount());
        player.setLevel(characterData.getClassInfo().getLevel());
        int totalExpAtLevel = PlayerLevelUtil.calculateTotalExp(characterData.getClassInfo().getLevel());
        int totalExpToLevel = PlayerLevelUtil.calculateTotalExp(characterData.getClassInfo().getLevel() + 1);
        double proportion = (double) (characterData.getClassInfo().getExp() - totalExpAtLevel) / (totalExpToLevel - totalExpAtLevel);
        if (characterData.getClassInfo().getLevel() >= PlayerLevelUtil.getMaxLevel()) player.setExp(0);
        if (proportion < 0) proportion = 0.0f;
        if (proportion >= 1) proportion = 0.99f;
        player.setExp((float) proportion);
        player.teleport(characterData.getBaseCharacterInfo().getLocation()); // set their location
        // restore their health and hunger (delayed by 1 tick because otherwise they get healed first)
        Bukkit.getScheduler().runTaskLater(RunicCore.getInstance(), () -> loadCurrentPlayerHealthAndHunger(player, characterData), 1L);
    }

    /**
     * Loads the current player values associated w/ combat, like current health, hunger, etc.
     *
     * @param player        to set values for
     * @param characterData the stored object with values (to be deleted after use)
     */
    private void loadCurrentPlayerHealthAndHunger(Player player, CharacterData characterData) {
        // set their hp to stored value from last logout
        int storedHealth = characterData.getBaseCharacterInfo().getCurrentHp();
        Bukkit.broadcastMessage("stored health is: " + storedHealth);
        // update their health
        if (storedHealth == 0) {
            storedHealth = HealthUtils.getBaseHealth(); // new players or corrupted data
        }
        if (storedHealth <= player.getMaxHealth()) {
            player.setHealth(storedHealth);
        } else {
            player.setHealth(player.getMaxHealth());
        }
        // set their last stored hunger
        player.setFoodLevel(characterData.getBaseCharacterInfo().getStoredHunger());
    }
}
