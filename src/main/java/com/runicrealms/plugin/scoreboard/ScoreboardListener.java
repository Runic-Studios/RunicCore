package com.runicrealms.plugin.scoreboard;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.api.RunicCoreAPI;
import com.runicrealms.plugin.character.api.CharacterSelectEvent;
import com.runicrealms.plugin.events.ArmorEquipEvent;
import com.runicrealms.plugin.player.utilities.HealthUtils;
import com.runicrealms.plugin.professions.event.ProfessionChangeEvent;
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

    @EventHandler(priority = EventPriority.LOWEST) // first
    public void onPlayerJoin(CharacterSelectEvent e) {
        Player player = e.getPlayer();
        RunicCore.getScoreboardHandler().setupScoreboard(player);
        NametagUtil.updateNametag(player);
    }

    /**
     * Updates health and scoreboard on level change
     */
    @EventHandler
    public void onLevelUp(PlayerLevelChangeEvent event) {
        if (!RunicCoreAPI.getLoadedCharacters().contains(event.getPlayer().getUniqueId()))
            return; // ignore the change from PlayerJoinEvent
        Player player = event.getPlayer();
        RunicCore.getScoreboardHandler().updatePlayerInfo(event.getPlayer(), event.getPlayer().getScoreboard());
        Bukkit.getScheduler().runTaskLater(RunicCore.getInstance(), () -> HealthUtils.setPlayerMaxHealth(player), 1L);
    }

    @EventHandler
    public void onProfessionChange(ProfessionChangeEvent e) {
        RunicCore.getScoreboardHandler().updatePlayerInfo(e.getPlayer(), e.getPlayer().getScoreboard());
    }

    // TODO: implement this event
//    @EventHandler
//    public void onGuildChange(GuildChangeEvent e) {
//
//    }

    /**
     * Updates health and scoreboard on armor equip
     * This NEEDS to be delayed by at least several ticks, or it won't update correctly
     */
    @EventHandler
    public void onArmorEquip(ArmorEquipEvent e) {
        Player player = e.getPlayer();
        Bukkit.getScheduler().runTaskLater(RunicCore.getInstance(), () -> HealthUtils.setPlayerMaxHealth(player), 5L);
    }

    /**
     * Updates health and scoreboard on offhand equip
     */
    @EventHandler
    public void onOffhandEquip(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player)) return;
        Player player = (Player) e.getWhoClicked();
        if (e.getCurrentItem() == null) return;
        if (e.getClickedInventory() == null) return;
        if (e.getSlot() != 40) return;
        if (e.getClickedInventory().getType() == InventoryType.PLAYER) {
            Bukkit.getScheduler().runTaskLater(RunicCore.getInstance(), () -> HealthUtils.setPlayerMaxHealth(player), 1L);
        }
    }

    /**
     * Updates health and scoreboard on off-hand swap
     */
    @EventHandler
    public void onOffhandSwap(PlayerSwapHandItemsEvent e) {
        Player player = e.getPlayer();
        Bukkit.getScheduler().runTaskLater(RunicCore.getInstance(), () -> HealthUtils.setPlayerMaxHealth(player), 1L);
    }

}
