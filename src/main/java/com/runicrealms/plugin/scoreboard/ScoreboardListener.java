package com.runicrealms.plugin.scoreboard;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.api.RunicCoreAPI;
import com.runicrealms.plugin.api.event.ScoreboardUpdateEvent;
import com.runicrealms.plugin.character.api.CharacterSelectEvent;
import com.runicrealms.plugin.events.ArmorEquipEvent;
import com.runicrealms.plugin.player.utilities.HealthUtils;
import com.runicrealms.plugin.utilities.NametagUtil;
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
     * This NEEDS to be delayed by at least several ticks, or it won't update correctly
     */
    @EventHandler
    public void onArmorEquip(ArmorEquipEvent event) {
        Player player = event.getPlayer();
        Bukkit.getScheduler().runTaskLater(RunicCore.getInstance(), () -> HealthUtils.setPlayerMaxHealth(player), 5L);
    }

    /**
     * Updates health and scoreboard on level change
     */
    @EventHandler
    public void onLevelUp(PlayerLevelChangeEvent event) {
        if (!RunicCoreAPI.getLoadedCharacters().contains(event.getPlayer().getUniqueId()))
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
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
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

    @EventHandler(priority = EventPriority.LOWEST) // first
    public void onPlayerJoin(CharacterSelectEvent event) {
        Player player = event.getPlayer();
        Bukkit.getScheduler().runTask(RunicCore.getInstance(), () -> {
            RunicCore.getScoreboardAPI().setupScoreboard(player);
            NametagUtil.updateNametag(player);
        }); // setup sync since the event is run async
    }

    @EventHandler(priority = EventPriority.HIGHEST) // last thing to run
    public void onScoreboardUpdate(ScoreboardUpdateEvent event) {
        RunicCore.getScoreboardAPI().updatePlayerInfo(event.getPlayer(), event);
    }

}
