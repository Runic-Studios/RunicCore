package com.runicrealms.plugin.scoreboard;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.api.event.ScoreboardUpdateEvent;
import com.runicrealms.plugin.player.utilities.HealthUtils;
import com.runicrealms.plugin.rdb.RunicDatabase;
import com.runicrealms.plugin.rdb.event.CharacterLoadedEvent;
import com.runicrealms.plugin.utilities.NametagHandler;
import com.runicrealms.plugin.runicitems.item.event.RunicStatUpdateEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerLevelChangeEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;

public class ScoreboardListener implements Listener {

    /**
     * Updates health and scoreboard on armor equip
     */
    @EventHandler
    public void onArmorEquip(RunicStatUpdateEvent event) {
        Player player = event.getPlayer();
        Bukkit.getScheduler().runTask(RunicCore.getInstance(), () -> HealthUtils.setPlayerMaxHealth(player));
    }

    /**
     * Updates health and scoreboard on level change
     */
    @EventHandler
    public void onLevelUp(PlayerLevelChangeEvent event) {
        if (!RunicDatabase.getAPI().getCharacterAPI().getLoadedCharacters().contains(event.getPlayer().getUniqueId()))
            return; // ignore the change from PlayerJoinEvent
        Player player = event.getPlayer();
        RunicCore.getScoreboardAPI().updatePlayerScoreboard(player);
        Bukkit.getScheduler().runTaskLater(RunicCore.getInstance(), () -> HealthUtils.setPlayerMaxHealth(player), 1L);
    }

    /**
     * Updates health and scoreboard on offhand equip
     */
    @EventHandler
    public void onOffhandEquip(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (event.getCurrentItem() == null) return;
        if (event.getClickedInventory() == null) return;
        if (event.getSlot() != 40) return;
        if (event.getClickedInventory().getType() == InventoryType.PLAYER) {
            Bukkit.getScheduler().runTaskLater(RunicCore.getInstance(), () -> HealthUtils.setPlayerMaxHealth(player), 1L);
        }
    }

    /**
     * Updates health and scoreboard on off-hand swap
     */
    @EventHandler
    public void onOffhandSwap(PlayerSwapHandItemsEvent event) {
        Player player = event.getPlayer();
        Bukkit.getScheduler().runTaskLater(RunicCore.getInstance(), () -> HealthUtils.setPlayerMaxHealth(player), 1L);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoin(CharacterLoadedEvent event) {
        Player player = event.getPlayer();
        RunicCore.getScoreboardAPI().setupScoreboard(player);
        NametagHandler.updateNametag(player, event.getCharacterSelectEvent().getSlot());
    }

    @EventHandler(priority = EventPriority.HIGHEST) // last thing to run
    public void onScoreboardUpdate(ScoreboardUpdateEvent event) {
        RunicCore.getScoreboardAPI().updatePlayerInfo(event.getPlayer(), event);
    }

}
