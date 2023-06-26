package com.runicrealms.plugin.listeners;

import com.runicrealms.plugin.CityLocation;
import com.runicrealms.plugin.DungeonLocation;
import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.common.DonorRank;
import com.runicrealms.plugin.common.util.ColorUtil;
import com.runicrealms.plugin.events.LeaveCombatEvent;
import com.runicrealms.plugin.events.RunicDeathEvent;
import com.runicrealms.plugin.player.death.Gravestone;
import com.runicrealms.plugin.player.listener.ManaListener;
import com.runicrealms.runicitems.RunicItems;
import com.runicrealms.runicitems.RunicItemsAPI;
import com.runicrealms.runicitems.item.RunicItem;
import com.runicrealms.runicitems.item.stats.RunicItemTag;
import com.runicrealms.runicitems.util.ItemUtils;
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
    private static final String NO_LOCATION_FOUND = ChatColor.RED + "No nearby respawn point could be found, so you have been returned to your hearthstone location.";
    private final String DUNGEON_DEATH_MESSAGE = ChatColor.RED + "You died in an instance! Your inventory has been returned.";

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
            itemsToDrop.add(RunicItems.getWeaponSkinAPI().disableSkin(itemStack));
            ItemUtils.takeItem(player, itemStack, itemStack.getAmount());
        }

        // Create a new inventory and add the items to drop to this inventory
        Inventory droppedItemsInventory = Bukkit.createInventory(null, 36);
        for (ItemStack item : itemsToDrop) {
            RunicItemsAPI.addItem(droppedItemsInventory, item);
        }

        return droppedItemsInventory;
    }

    @EventHandler(priority = EventPriority.HIGHEST) // last
    public void onRunicDeath(RunicDeathEvent event) {
        if (event.isCancelled()) return;
        Player victim = event.getVictim();


        // Broadcast the death message
        String message;
        if (event.getKiller().length > 0 && event.getKiller()[0] instanceof Player killerPlayer) {
            message = ChatColor.RED + event.getVictim().getName() + " was killed by " + killerPlayer.getName() + "!";
        } else {
            message = ChatColor.RED + event.getVictim().getName() + " died!";
        }
        Bukkit.getOnlinePlayers().forEach(player -> player.sendMessage(message));

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
                boolean victimHasPriority = event.getKiller().length == 0 || !(event.getKiller()[0] instanceof Player);
                DonorRank rank = DonorRank.getDonorRank(event.getVictim());
                new Gravestone(victim, event.getLocation(), droppedItemsInventory, victimHasPriority, rank.getGravestonePriorityDuration() * 60, rank.getGravestoneDuration() * 60);
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

        // Players don't lose items in dungeons
        if (victim.getWorld().getName().equalsIgnoreCase("dungeons")) {
            // Check for dungeon
            DungeonLocation dungeonLocation = RunicCore.getRegionAPI().getDungeonFromLocation(victim.getLocation());
            Bukkit.getScheduler().runTask(RunicCore.getInstance(), () -> {
                victim.sendMessage(DUNGEON_DEATH_MESSAGE);
                if (dungeonLocation != null)
                    victim.teleport(dungeonLocation.getLocation());
                else {
                    victim.teleport(CityLocation.getLocationFromItemStack(victim.getInventory().getItem(8)));
                    victim.sendMessage(NO_LOCATION_FOUND);
                }
            });
        } else {
            victim.teleport(CityLocation.getLocationFromItemStack(victim.getInventory().getItem(8)));

            Gravestone gravestone = RunicCore.getGravestoneManager().getGravestoneMap().get(victim.getUniqueId());

            String gravestoneReminder = gravestone != null ? "Your &4&lGRAVESTONE &chas the remainder of your items and will last for " +
                    gravestone.getPriorityTime() + "s until it can be looted by others." : "";

            victim.sendMessage(ColorUtil.format("&cYou have died! Your armor and hotbar have been returned. " +
                    "Any soulbound, quest, and untradeable items have also been returned. " +
                    gravestoneReminder));
        }

        // Particles, sounds
        victim.playSound(victim.getLocation(), Sound.ENTITY_PLAYER_DEATH, 1.0f, 1);
        victim.playSound(victim.getLocation(), Sound.ENTITY_WITHER_DEATH, 0.25f, 1);
        victim.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 80, 0));

        // Flag leave combat for PvP and other things
        LeaveCombatEvent leaveCombatEvent = new LeaveCombatEvent(victim);
        Bukkit.getPluginManager().callEvent(leaveCombatEvent);
    }

}
