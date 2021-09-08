package com.runicrealms.plugin.listeners;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.events.LeaveCombatEvent;
import com.runicrealms.plugin.events.RunicDeathEvent;
import com.runicrealms.plugin.item.hearthstone.HearthstoneLocation;
import com.runicrealms.runicitems.RunicItemsAPI;
import com.runicrealms.runicitems.item.RunicItem;
import com.runicrealms.runicitems.item.stats.RunicItemTag;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;

import java.util.Set;

public class DeathListener implements Listener {

    @EventHandler
    public void onRunicDeath(RunicDeathEvent e) {

        Player victim = e.getVictim();

        // broadcast the death message
        DamageListener.broadcastDeathMessage(victim);

        // update the scoreboard
        if (Bukkit.getScoreboardManager().getMainScoreboard().getObjective("health") != null) {
            Objective o = Bukkit.getScoreboardManager().getMainScoreboard().getObjective("health");
            Score score = o.getScore(victim);
            score.setScore((int) victim.getHealth());
        }

        // reset health, food, mana
        victim.setHealth(victim.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
        victim.setFoodLevel(20);
        int maxMana = RunicCore.getCacheManager().getPlayerCaches().get(victim).getMaxMana();
        RunicCore.getRegenManager().getCurrentManaList().put(victim.getUniqueId(), maxMana);

        // particles, sounds
        victim.getWorld().playSound(victim.getLocation(), Sound.ENTITY_PLAYER_DEATH, 1.0f, 1);
        victim.getWorld().playSound(victim.getLocation(), Sound.ENTITY_WITHER_DEATH, 0.25f, 1);
        victim.getWorld().spawnParticle(Particle.REDSTONE, victim.getEyeLocation(), 25, 0.5f, 0.5f, 0.5f,
                new Particle.DustOptions(Color.RED, 3));

        // teleport them to their hearthstone location, or the front of the dungeon
        tryDropItems(victim);
        String isDungeon = checkForDungeon(victim);
        if (isDungeon.equals("")) { // no dungeon
            victim.teleport(HearthstoneLocation.getLocationFromItemStack(victim.getInventory().getItem(8)));
            victim.sendMessage(ChatColor.RED + "You have died! Your armor and hotbar have been returned.");
        }
        victim.playSound(victim.getLocation(), Sound.ENTITY_PLAYER_DEATH, 1.0f, 1);
        victim.playSound(victim.getLocation(), Sound.ENTITY_WITHER_DEATH, 0.25f, 1);
        victim.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 80, 0));

        // flag leave combat for PvP and other things
        LeaveCombatEvent leaveCombatEvent = new LeaveCombatEvent(victim);
        Bukkit.getPluginManager().callEvent(leaveCombatEvent);
    }

    /**
     * This...
     *
     * @param player
     * @return
     */
    // todo API method for region checking
    private static String checkForDungeon(Player player) {

        // grab all regions the player is standing in
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionQuery query = container.createQuery();
        ApplicableRegionSet set = query.getApplicableRegions(BukkitAdapter.adapt(player.getLocation()));
        Set<ProtectedRegion> regions = set.getRegions();

        if (regions == null) return "";

        // check the region for the keyword 'mine'
        // ignore the rest of this event if the player cannot mine
        for (ProtectedRegion region : regions) {
            if (region.getId().contains("sebathscave")) {
                Location caveEntrance = new Location(Bukkit.getWorld("dungeons"), -1874.5, 177, -522.5, 90, 0);
                player.teleport(caveEntrance);
                return "sebathscave";
            } else if (region.getId().contains("crystalcavern")) {
                Location cavernEntrance = new Location(Bukkit.getWorld("dungeons"), 1208.5, 74, -66.5, 180, 0);
                player.teleport(cavernEntrance);
                return "crystalcavern";
            } else if (region.getId().contains("jorundrskeep")) {
                Location keepEntrance = new Location(Bukkit.getWorld("dungeons"), -534.5, 120, -177.5, 180, 0);
                player.teleport(keepEntrance);
                return "jorundrskeep";
            } else if (region.getId().contains("library")) {
                Location libraryEntrance = new Location(Bukkit.getWorld("dungeons"), -23.5, 31, 11.5, 270, 0);
                player.teleport(libraryEntrance);
                return "library";
            } else if (region.getId().contains("crypts")) {
                Location cryptsEntrance = new Location(Bukkit.getWorld("dungeons"), 298.5, 87, 6.5, 0, 0);
                player.teleport(cryptsEntrance);
                return "crypts";
            } else if (region.getId().contains("fortress")) {
                Location fortressEntrace = new Location(Bukkit.getWorld("dungeons"), 32.5, 73, 87.5, 0, 0);
                if (region.getId().contains("d3_parkour")) {
                    fortressEntrace = new Location(Bukkit.getWorld("dungeons"), 32.5, 67, 379.5, 0, 0);
                } else if (region.getId().contains("d3_alkyr")) {
                    fortressEntrace = new Location(Bukkit.getWorld("dungeons"), -9.5, 67, 503.5, 0, 0);
                } else if (region.getId().contains("eldrid")) {
                    fortressEntrace = new Location(Bukkit.getWorld("dungeons"), -9.5, 67, 623.5, 0, 0);
                }
                player.teleport(fortressEntrace);
                return "fortress";
            }
        }

        return "";
    }

    /**
     * This method controls the dropping of items.
     * It rolls a die for each item in the player's inventory, and it skips certain items by flag
     *
     * @param player player whose items may drop
     */
    private static void tryDropItems(Player player) {
        // don't drop items in dungeon world
        if (player.getWorld().getName().equalsIgnoreCase("dungeons")) return;
        for (int i = 9; i < 36; i++) { // ignore hotbar
            ItemStack itemStack = player.getInventory().getItem(i);
            if (itemStack == null) continue;
            RunicItem runicItem = RunicItemsAPI.getRunicItemFromItemStack(itemStack);
            if (runicItem == null) continue;
            if (runicItem.getTags().contains(RunicItemTag.QUEST_ITEM)) continue;
            if (runicItem.getTags().contains(RunicItemTag.SOULBOUND)) continue;
            if (runicItem.getTags().contains(RunicItemTag.UNTRADEABLE)) continue;
            player.getInventory().remove(itemStack);
            player.getWorld().dropItem(player.getLocation(), itemStack);
        }
    }
}
