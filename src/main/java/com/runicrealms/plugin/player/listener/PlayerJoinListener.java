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

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@SuppressWarnings("deprecation")
public class PlayerJoinListener implements Listener {

    public static final Set<UUID> LOADING_PLAYERS = new HashSet<>();
    private static final String WORLD_NAME = "Alterra";
    private static final Location SPAWN_BOX = new Location(Bukkit.getWorld(WORLD_NAME), -2271.5, 2, 2289.5);

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
        LOADING_PLAYERS.add(player.getUniqueId());
        player.setLevel(characterData.getClassInfo().getLevel());
        int totalExpAtLevel = PlayerLevelUtil.calculateTotalExp(characterData.getClassInfo().getLevel());
        int totalExpToLevel = PlayerLevelUtil.calculateTotalExp(characterData.getClassInfo().getLevel() + 1);
        double proportion = (double) (characterData.getClassInfo().getExp() - totalExpAtLevel) / (totalExpToLevel - totalExpAtLevel);
        if (characterData.getClassInfo().getLevel() >= PlayerLevelUtil.getMaxLevel()) player.setExp(0);
        if (proportion < 0) proportion = 0.0f;
        if (proportion >= 1) proportion = 0.99f;
        player.setExp((float) proportion);
        // restore their hunger (delayed by 1 tick because otherwise they get healed / full hunger first)
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
        player.setHealth(player.getMaxHealth());
        player.setFoodLevel(characterData.getBaseCharacterInfo().getStoredHunger());
    }

    @EventHandler
    public void onCharacterLoaded(CharacterLoadedEvent event) {
        event.getPlayer().setInvulnerable(false);
        Bukkit.getScheduler().runTaskLater(RunicCore.getInstance(),
                () -> {
                    event.getPlayer().removePotionEffect(PotionEffectType.BLINDNESS);
                    LOADING_PLAYERS.remove(event.getPlayer().getUniqueId());
                }, 7L);
    }

    /**
     * Loads values on login from the CharacterData object once they select a character from select screen
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onJoin(CharacterSelectEvent event) {
        if (!event.getPlayer().hasPlayedBefore()) {
            setupNewPlayer(event.getPlayer());
        }
        Location location = event.getCharacterData().getBaseCharacterInfo().getLocation();
        // Teleport player and setup data sync
        Bukkit.getScheduler().runTask(RunicCore.getInstance(), () -> {
            event.getPlayer().teleport(location);
            Bukkit.getScheduler().runTaskLater(RunicCore.getInstance(),
                    () -> buildCharacterFromEvent(event.getPlayer(), event), 1L); // run 1 tick late so that player stats load
        });
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
        player.teleport(SPAWN_BOX);
        player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, Integer.MAX_VALUE, 2));
        // build database file synchronously (if it doesn't exist)
        Bukkit.getScheduler().runTaskLater(RunicCore.getInstance(), () -> {
            try (Jedis jedis = RunicCoreAPI.getNewJedisResource()) {
                PlayerData playerData = RunicCore.getDatabaseManager().loadPlayerData(player, jedis);
                RunicCore.getDatabaseManager().getPlayerDataMap().put(player.getUniqueId(), playerData);
                ResourcePackManager.openPackForPlayer(player); // prompt resource pack (triggers character select screen)
            }
        }, 1L);
    }

    /**
     * Allows donator ranks to enter a full server
     */
    @EventHandler
    public void onJoinFullServer(PlayerLoginEvent event) {
        if (event.getResult() == PlayerLoginEvent.Result.KICK_FULL) {
            if (event.getPlayer().hasPermission("core.full.join")) {
                event.allow();
            }
        }
    }

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

    private void setupNewPlayer(Player player) {
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
}
