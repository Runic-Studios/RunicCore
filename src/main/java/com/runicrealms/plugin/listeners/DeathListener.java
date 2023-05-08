package com.runicrealms.plugin.listeners;

import com.runicrealms.plugin.CityLocation;
import com.runicrealms.plugin.DungeonLocation;
import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.events.LeaveCombatEvent;
import com.runicrealms.plugin.events.RunicDeathEvent;
import com.runicrealms.plugin.player.listener.ManaListener;
import com.runicrealms.runicitems.RunicItemsAPI;
import com.runicrealms.runicitems.item.RunicItem;
import com.runicrealms.runicitems.item.stats.RunicItemTag;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;

public class DeathListener implements Listener {

    /**
     * This method controls the dropping of items.
     * It rolls a die for each item in the player's inventory, and it skips certain items by flag
     *
     * @param player player whose items may drop
     */
    private static void tryDropItems(Player player, World world) {
        // don't drop items in dungeon world
        if (world.getName().equalsIgnoreCase("dungeons")) return;
        for (int i = 9; i < 36; i++) { // ignore hotbar
            ItemStack itemStack = player.getInventory().getItem(i);
            if (itemStack == null) continue;
            RunicItem runicItem = RunicItemsAPI.getRunicItemFromItemStack(itemStack);
            if (runicItem == null) continue;
            if (runicItem.getTags().contains(RunicItemTag.QUEST_ITEM)) continue;
            if (runicItem.getTags().contains(RunicItemTag.SOULBOUND)) continue;
            if (runicItem.getTags().contains(RunicItemTag.UNTRADEABLE)) continue;
            player.getInventory().remove(itemStack);
            world.dropItem(player.getLocation(), itemStack);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST) // last
    public void onRunicDeath(RunicDeathEvent event) {

        if (event.isCancelled()) return;
        Player victim = event.getVictim();

        // Broadcast the death message
        DamageListener.broadcastDeathMessage(victim);

        // Particles, sounds
        World world = event.getLocation().getWorld();
        if (world != null) {
            world.playSound(event.getLocation(), Sound.ENTITY_PLAYER_DEATH, 1.0f, 1);
            world.playSound(event.getLocation(), Sound.ENTITY_WITHER_DEATH, 0.25f, 1);
            world.spawnParticle(Particle.REDSTONE, event.getLocation(), 25, 0.5f, 0.5f, 0.5f,
                    new Particle.DustOptions(Color.RED, 3));
            // Teleport them to their hearthstone location, or the front of the dungeon
            tryDropItems(victim, world);
        }

        // Ignore following events if player is offline (PvP logging)
        if (!victim.isOnline()) return;

        // Update the scoreboard
        if (Bukkit.getScoreboardManager().getMainScoreboard().getObjective("health") != null) {
            Objective o = Bukkit.getScoreboardManager().getMainScoreboard().getObjective("health");
            Score score = o.getScore(victim);
            score.setScore((int) victim.getHealth());
        }

        // Reset health, food, mana
        victim.setHealth(victim.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
        victim.setFoodLevel(20);
        ManaListener.calculateMaxMana(victim);

        // Check for dungeon
        DungeonLocation dungeonLocation = RunicCore.getRegionAPI().getDungeonFromLocation(victim.getLocation());
        if (dungeonLocation == null) {
            victim.teleport(CityLocation.getLocationFromItemStack(victim.getInventory().getItem(8)));
            victim.sendMessage(ChatColor.RED + "You have died! Your armor and hotbar have been returned.");
            victim.sendMessage(ChatColor.RED + "Any soulbound, quest, and untradeable items have been returned also.");
        } else {
            Bukkit.getScheduler().runTask(RunicCore.getInstance(), () -> {
                victim.teleport(dungeonLocation.getLocation());
                victim.sendMessage(ChatColor.RED + "You died in an instance! Your inventory has been returned.");
            });
        }

        // particles, sounds
        victim.playSound(victim.getLocation(), Sound.ENTITY_PLAYER_DEATH, 1.0f, 1);
        victim.playSound(victim.getLocation(), Sound.ENTITY_WITHER_DEATH, 0.25f, 1);
        victim.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 80, 0));

        // flag leave combat for PvP and other things
        LeaveCombatEvent leaveCombatEvent = new LeaveCombatEvent(victim);
        Bukkit.getPluginManager().callEvent(leaveCombatEvent);
    }

}
