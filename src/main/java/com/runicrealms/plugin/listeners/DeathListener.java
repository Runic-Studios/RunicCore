package com.runicrealms.plugin.listeners;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.api.RunicCoreAPI;
import com.runicrealms.plugin.enums.DungeonLocation;
import com.runicrealms.plugin.events.LeaveCombatEvent;
import com.runicrealms.plugin.events.RunicDeathEvent;
import com.runicrealms.plugin.enums.CityLocation;
import com.runicrealms.runicitems.RunicItemsAPI;
import com.runicrealms.runicitems.item.RunicItem;
import com.runicrealms.runicitems.item.stats.RunicItemTag;
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

        // check dungeon
        DungeonLocation dungeonLocation = RunicCoreAPI.getDungeonFromLocation(victim.getLocation());
        if (dungeonLocation == null) {
            victim.teleport(CityLocation.getLocationFromItemStack(victim.getInventory().getItem(8)));
            victim.sendMessage(ChatColor.RED + "You have died! Your armor and hotbar have been returned.");
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
