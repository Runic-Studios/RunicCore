package com.runicrealms.plugin.listeners;

import com.runicrealms.plugin.CityLocation;
import com.runicrealms.plugin.DungeonLocation;
import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.events.LeaveCombatEvent;
import com.runicrealms.plugin.events.RunicDeathEvent;
import com.runicrealms.plugin.item.util.ItemRemover;
import com.runicrealms.plugin.player.death.Gravestone;
import com.runicrealms.plugin.player.listener.ManaListener;
import com.runicrealms.runicitems.RunicItemsAPI;
import com.runicrealms.runicitems.item.RunicItem;
import com.runicrealms.runicitems.item.stats.RunicItemTag;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;

public class DeathListener implements Listener {

    /**
     * This method controls the dropping of items.
     * It rolls a die for each item in the player's inventory, and it skips certain items by flag
     *
     * @param player player whose items may drop
     */
    private static Inventory droppedItemsInventory(Player player, World world) {
        // Don't create Gravestone in dungeon world
        if (world.getName().equalsIgnoreCase("dungeons")) return null;
        List<ItemStack> itemsToDrop = new ArrayList<>();
        for (int i = 9; i < 36; i++) { // Ignore hotbar and armor
            ItemStack itemStack = player.getInventory().getItem(i);
            if (itemStack == null) continue;
            RunicItem runicItem = RunicItemsAPI.getRunicItemFromItemStack(itemStack);
            if (runicItem == null) continue;
            if (runicItem.getTags().contains(RunicItemTag.QUEST_ITEM)) continue;
            if (runicItem.getTags().contains(RunicItemTag.SOULBOUND)) continue;
            if (runicItem.getTags().contains(RunicItemTag.UNTRADEABLE)) continue;
            itemsToDrop.add(itemStack);
            ItemRemover.takeItem(player, itemStack, itemStack.getAmount());
        }

        // Create a new inventory and add the items to drop to this inventory
        Inventory droppedItemsInventory = Bukkit.createInventory(null, 36);
        for (ItemStack item : itemsToDrop) {
            droppedItemsInventory.addItem(item);
        }

        return droppedItemsInventory;
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
            Inventory droppedItemsInventory = droppedItemsInventory(victim, world);
            // If the player should drop items, create their Gravestone
            if (droppedItemsInventory != null && !droppedItemsInventory.isEmpty()) {
                boolean victimHasPriority = event.getKiller().length <= 0 || !(event.getKiller()[0] instanceof Player);
                new Gravestone(victim, droppedItemsInventory, victimHasPriority);
            }
        }

        // Ignore following events if player is offline (PvP logging)
        if (!victim.isOnline()) return;

        // Update the scoreboard
        RunicCore.getScoreboardAPI().updatePlayerScoreboard(victim.getPlayer());

        // Reset health, food, mana
        victim.setHealth(victim.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
        victim.setFoodLevel(20);
        ManaListener.calculateMaxMana(victim);

        // Check for dungeon
        DungeonLocation dungeonLocation = RunicCore.getRegionAPI().getDungeonFromLocation(victim.getLocation());
        if (dungeonLocation == null) {
            victim.teleport(CityLocation.getLocationFromItemStack(victim.getInventory().getItem(8)));

            victim.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    "&cYou have died! Your armor and hotbar have been returned. " +
                            "Any soulbound, quest, and untradeable items have been returned also. " +
                            "Your &4&lGRAVESTONE &chas the remainder of your items and will last for " +
                            Gravestone.PRIORITY_TIME + "s until it can be looted by others."));
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
