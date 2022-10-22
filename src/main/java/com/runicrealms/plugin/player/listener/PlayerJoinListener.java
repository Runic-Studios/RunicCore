package com.runicrealms.plugin.player.listener;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.api.RunicCoreAPI;
import com.runicrealms.plugin.character.api.CharacterLoadedEvent;
import com.runicrealms.plugin.character.api.CharacterSelectEvent;
import com.runicrealms.plugin.model.CharacterData;
import com.runicrealms.plugin.model.PlayerData;
import com.runicrealms.plugin.player.utilities.HealthUtils;
import com.runicrealms.plugin.player.utilities.PlayerLevelUtil;
import com.runicrealms.plugin.resourcepack.ResourcePackManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import redis.clients.jedis.Jedis;

@SuppressWarnings("deprecation")
public class PlayerJoinListener implements Listener {

    @EventHandler
    public void onPreJoin(AsyncPlayerPreLoginEvent event) {
        try (Jedis jedis = RunicCoreAPI.getNewJedisResource()) {
            if (jedis.exists(event.getUniqueId() + ":" + PlayerQuitListener.DATA_SAVING_KEY)) {
                event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER,
                        "You recently played and your data is saving!" +
                                "\nTry again in a moment");
            }
        }
    }

    /**
     * Reset the player's displayed values when they join the server, before selecting a character
     */
    @EventHandler(priority = EventPriority.LOWEST) // first
    public void onJoin(PlayerJoinEvent event) {
        event.setJoinMessage("");
        Player player = event.getPlayer();
        player.getInventory().clear();
        player.setInvulnerable(true);
        player.setMaxHealth(20);
        player.setHealth(player.getMaxHealth());
        player.setHealthScale(20);
        player.setLevel(0);
        player.setExp(0);
        player.setFoodLevel(20);
        player.teleport(new Location(Bukkit.getWorld("Alterra"), -2318.5, 2, 1720.5));
        player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, Integer.MAX_VALUE, 2));
        // build database file synchronously (if it doesn't exist)
        Bukkit.getScheduler().runTaskLater(RunicCore.getInstance(), () -> {
            try (Jedis jedis = RunicCoreAPI.getNewJedisResource()) {
                PlayerData playerData = RunicCore.getDatabaseManager().loadPlayerData(player, jedis);
                // Bukkit.broadcastMessage("building player data object");
                RunicCore.getDatabaseManager().getPlayerDataMap().put(player.getUniqueId(), playerData);
                ResourcePackManager.openPackForPlayer(player); // prompt resource pack (triggers character select screen)
                // jedis.close();
            }
        }, 1L);
    }

    /**
     * Loads values on login from the CharacterData object once they select a character from select screen
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onJoin(CharacterSelectEvent event) {
        Location location = event.getCharacterData().getBaseCharacterInfo().getLocation();
        event.getPlayer().teleport(location);
        Bukkit.getScheduler().runTaskLater(RunicCore.getInstance(),
                () -> buildCharacterFromEvent(event.getPlayer(), event), 1L); // run 1 tick late so that player stats load
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

    @EventHandler
    public void onCharacterLoaded(CharacterLoadedEvent event) {
        event.getPlayer().setInvulnerable(false);
        Bukkit.getScheduler().runTaskLater(RunicCore.getInstance(),
                () -> event.getPlayer().removePotionEffect(PotionEffectType.BLINDNESS), 5L);
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
     * @param player               to set values for
     * @param characterSelectEvent the associated select event to finish loading
     */
    private void buildCharacterFromEvent(Player player, CharacterSelectEvent characterSelectEvent) {
        CharacterData characterData = characterSelectEvent.getCharacterData();
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
        // restore their health and hunger (delayed by 1 tick because otherwise they get healed first)
        Bukkit.getScheduler().runTaskLater(RunicCore.getInstance(), () -> {
            loadCurrentPlayerHealthAndHunger(player, characterData);
            CharacterLoadedEvent characterLoadedEvent = new CharacterLoadedEvent(player, characterSelectEvent);
            Bukkit.getPluginManager().callEvent(characterLoadedEvent); // inform plugins that character is loaded!
        }, 1L);
    }

    /**
     * Loads the current player values associated w/ combat, like current health, hunger, etc.
     *
     * @param player        to set values for
     * @param characterData the stored object with values (to be deleted after use)
     */
    private void loadCurrentPlayerHealthAndHunger(Player player, CharacterData characterData) {
        // set their hp to stored value from last logout
//        int storedHealth = characterData.getBaseCharacterInfo().getCurrentHp();
//        // Bukkit.broadcastMessage("stored health is: " + storedHealth);
//        // update their health
//        if (storedHealth == 0) {
//            storedHealth = HealthUtils.getBaseHealth(); // new players or corrupted data
//        }
        player.setHealth(player.getMaxHealth());
//        if (storedHealth <= player.getMaxHealth()) {
//            player.setHealth(storedHealth);
//        } else {
//            player.setHealth(player.getMaxHealth());
//        }
        // set their last stored hunger
        player.setFoodLevel(characterData.getBaseCharacterInfo().getStoredHunger());
    }
}
