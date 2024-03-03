package com.runicrealms.plugin.player.listener;

import co.aikar.taskchain.TaskChain;
import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.common.resourcepack.ResourcePackManager;
import com.runicrealms.plugin.model.CoreCharacterData;
import com.runicrealms.plugin.model.CorePlayerData;
import com.runicrealms.plugin.player.utilities.HealthUtils;
import com.runicrealms.plugin.player.utilities.PlayerLevelUtil;
import com.runicrealms.plugin.rdb.RunicDatabase;
import com.runicrealms.plugin.rdb.event.CharacterLoadedEvent;
import com.runicrealms.plugin.rdb.event.CharacterSelectEvent;
import com.runicrealms.plugin.taskchain.TaskChainUtil;
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
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@SuppressWarnings("deprecation")
public class PlayerJoinListener implements Listener {
    public static final Set<UUID> LOADING_SCREEN_PLAYERS = new HashSet<>();
    // These are players that have begun a lo
    public static final Set<UUID> LOADS_IN_PROGRESS = new HashSet<>();
    private static final String WORLD_NAME = "Alterra";
    public static final Location SPAWN_BOX = new Location(Bukkit.getWorld(WORLD_NAME), -2271.5, 2, 2289.5);

    /**
     * Sets up some basic player values, such as max health, level, location, etc.
     *
     * @param player               to set values for
     * @param characterSelectEvent the associated select event to finish loading
     */
    private BukkitTask buildCharacterFromEvent(Player player, CharacterSelectEvent characterSelectEvent) {
        // For benchmarking
        long startTime = System.nanoTime();
        characterSelectEvent.getPluginsToLoadData().add("core");
        CorePlayerData corePlayerData = (CorePlayerData) characterSelectEvent.getSessionDataMongo();
        CoreCharacterData coreCharacterData = corePlayerData.getCharacter(characterSelectEvent.getSlot());
        LOADING_SCREEN_PLAYERS.add(player.getUniqueId());
        LOADS_IN_PROGRESS.add(player.getUniqueId());
        player.setLevel(coreCharacterData.getLevel());
        int totalExpAtLevel = PlayerLevelUtil.calculateTotalExp(coreCharacterData.getLevel());
        int totalExpToLevel = PlayerLevelUtil.calculateTotalExp(coreCharacterData.getLevel() + 1);
        double proportion = (double) (coreCharacterData.getExp() - totalExpAtLevel) / (totalExpToLevel - totalExpAtLevel);
        if (coreCharacterData.getLevel() >= PlayerLevelUtil.getMaxLevel())
            player.setExp(0);
        if (proportion < 0) proportion = 0.0f;
        if (proportion >= 1) proportion = 0.99f;
        player.setExp((float) proportion);
        // Call the CharacterLoadedEvent sync at the end of the event
        characterSelectEvent.getPluginsToLoadData().remove("core");
        // Calculate elapsed time
        long endTime = System.nanoTime();
        long elapsedTime = endTime - startTime;
        // Log elapsed time in milliseconds
        Bukkit.getLogger().info("RunicCore|character took: " + elapsedTime / 1_000_000 + "ms to load");
        return new BukkitRunnable() {
            @Override
            public void run() {
                if (characterSelectEvent.getPluginsToLoadData().size() > 0)
                    return; // Other plugins loading data
                this.cancel();
                if (characterSelectEvent.isCancelled()) {
                    LOADS_IN_PROGRESS.remove(player.getUniqueId());
//                    Bukkit.getLogger().severe("SELECT EVENT WAS CANCELLED");
                } else {
//                    Bukkit.getLogger().severe("FIRING LOADED EVENT");
                    CharacterLoadedEvent characterLoadedEvent = new CharacterLoadedEvent(player, characterSelectEvent);
                    // Inform all plugins that character is loaded!
                    Bukkit.getScheduler().runTask(RunicCore.getInstance(), () -> Bukkit.getPluginManager().callEvent(characterLoadedEvent));
                }
            }
        }.runTaskTimerAsynchronously(RunicCore.getInstance(), 5L, 20L);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onCharacterLoaded(CharacterLoadedEvent event) {
        Player player = event.getPlayer();
        int slot = event.getCharacterSelectEvent().getSlot();
        player.teleport(((CorePlayerData) event.getCharacterSelectEvent().getSessionDataMongo()).getCharacter(slot).getLocation());
        Bukkit.getScheduler().runTaskLater(RunicCore.getInstance(), () -> LOADING_SCREEN_PLAYERS.remove(event.getPlayer().getUniqueId()), 7L);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCharacterLoadedLast(CharacterLoadedEvent event) {
        Player player = event.getPlayer();
        player.setInvulnerable(false);
        HealthUtils.setPlayerMaxHealth(player);
        player.setHealthScale(HealthUtils.getHeartAmount());
        player.setHealth(player.getMaxHealth());
        LOADS_IN_PROGRESS.remove(event.getPlayer().getUniqueId());
    }

    /**
     * Loads values on login from the CorePlayerData object once they select a character from select screen
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onCharacterSelect(CharacterSelectEvent event) {
        if (!event.getPlayer().hasPlayedBefore()) {
            setupNewPlayer(event.getPlayer());
        }
        buildCharacterFromEvent(event.getPlayer(), event);
    }

    /**
     * Reset the player's displayed values when they join the server, before selecting a character
     */
    @EventHandler(priority = EventPriority.LOWEST) // first
    public void onJoin(PlayerJoinEvent event) {
        event.setJoinMessage("");
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        player.getInventory().clear();
        player.setInvulnerable(true);
        player.setMaxHealth(20);
        player.setHealth(player.getMaxHealth());
        player.setHealthScale(20);
        player.setLevel(0);
        player.setExp(0);
        player.setFoodLevel(20);
        player.teleport(SPAWN_BOX);
        // Sync task to load player's data object, or will call an async task to create it in mongo
        TaskChain<?> chain = RunicCore.newChain();
        chain
                .asyncFirst(() -> RunicCore.getPlayerDataAPI().loadCorePlayerData(uuid))
                .abortIfNull(TaskChainUtil.CONSOLE_LOG, null, "RunicCore failed to load on join!")
                .syncLast(corePlayerData -> {
                    RunicCore.getPlayerDataAPI().getCorePlayerDataMap().put(uuid, corePlayerData);
                    ResourcePackManager.openPackForPlayer(player); // Prompt resource pack (triggers character select screen)
                })
                .execute();
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPreJoin(AsyncPlayerPreLoginEvent event) {
        if (LOADS_IN_PROGRESS.contains(event.getUniqueId())) {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER,
                    ChatColor.GREEN + "You recently played and your data is saving!" +
                            "\n" + ChatColor.GREEN + "Try again in a moment");
        } else if (RunicDatabase.getAPI().getDataAPI().getLockedOutPlayers().contains(event.getUniqueId())) {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER,
                    ChatColor.GREEN + "You recently played and your data is saving!" +
                            "\n" + ChatColor.GREEN + "Try again in a moment");
        }
    }

    private void setupNewPlayer(Player player) {
        HealthUtils.setPlayerMaxHealth(player);
        player.setHealthScale(HealthUtils.getHeartAmount());
        int playerHealth = (int) player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
        player.setHealth(playerHealth);
        player.setFoodLevel(20);
    }
}
