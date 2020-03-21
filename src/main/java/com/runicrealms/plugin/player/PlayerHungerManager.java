package com.runicrealms.plugin.player;

import com.runicrealms.plugin.RunicCore;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Set;

/**
 * Created by KissOfFate
 * Date: 6/26/2019
 * Time: 2:19 PM
 */
@SuppressWarnings("FieldCanBeLocal")
public class PlayerHungerManager implements Listener {

    // tick time in seconds
    private final int PLAYER_HUNGER_TIME = 90;

    public PlayerHungerManager() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for(Player player : RunicCore.getCacheManager().getLoadedPlayers()) {
//                    if (!player.hasPermission("runic.hunger.exempt")
//                            || (player.getGameMode() != GameMode.CREATIVE
//                            && player.getGameMode() != GameMode.SPECTATOR)) {

                        if (player.getFoodLevel() <= 1) return;
                        if (isSafezone(player.getLocation())) return;

                        player.setFoodLevel(player.getFoodLevel() - 1);
                   // }
                }
            }
        }.runTaskTimer(RunicCore.getInstance(), 30 * 20, PLAYER_HUNGER_TIME * 20L);
    }

    /**
     * Prevents normal decay of hunger
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        if(event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            if(event.getFoodLevel() < player.getFoodLevel()) {
                //if (!player.hasPermission("runic.hunger.exempt")) {
                    event.setCancelled(true);
                //}
            }
        }
    }

    /**
     * Prevents hunger loss in capital cities
     */
    private boolean isSafezone(Location loc) {
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionQuery query = container.createQuery();
        ApplicableRegionSet set = query.getApplicableRegions(BukkitAdapter.adapt(loc));
        Set<ProtectedRegion> regions = set.getRegions();
        if (regions == null) return false;
        for (ProtectedRegion region : regions) {
            if (region.getId().contains("safezone")) {
                return true;
            }
        }
        return false;
    }
}
