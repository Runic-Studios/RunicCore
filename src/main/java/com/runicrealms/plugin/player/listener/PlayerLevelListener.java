package com.runicrealms.plugin.player.listener;

import com.runicrealms.plugin.api.RunicCoreAPI;
import com.runicrealms.plugin.model.CharacterField;
import com.runicrealms.plugin.player.utilities.HealthUtils;
import com.runicrealms.plugin.player.utilities.PlayerLevelUtil;
import com.runicrealms.plugin.utilities.NametagUtil;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLevelChangeEvent;
import org.bukkit.inventory.meta.FireworkMeta;
import redis.clients.jedis.Jedis;

/**
 * Handles all the logic for when a player levels-up their primary class (archer, mage, etc.)
 *
 * @author Skyfallin_
 */
public class PlayerLevelListener implements Listener {

    @EventHandler
    public void onLevelUp(PlayerLevelChangeEvent event) {

        if (!RunicCoreAPI.getLoadedCharacters().contains(event.getPlayer().getUniqueId()))
            return; // ignore the change from PlayerJoinEvent

        Player player = event.getPlayer();
        if (player.getLevel() > PlayerLevelUtil.getMaxLevel()) return; // insurance

        // update player's level in redis
        try (Jedis jedis = RunicCoreAPI.getNewJedisResource()) { // try-with-resources to close the resource for us
            RunicCoreAPI.setRedisValue(player, CharacterField.CLASS_LEVEL.getField(), String.valueOf(player.getLevel()), jedis);
        }

        // grab the player's new info
        String className = RunicCoreAPI.getPlayerClass(player);
        if (className.equals("")) return;
        int classLevel = player.getLevel();

        HealthUtils.setPlayerMaxHealth(player);
        player.setHealthScale(HealthUtils.getHeartAmount());
        int playerHealth = (int) player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
        player.setHealth(playerHealth);
        player.setFoodLevel(20);

        if (player.getLevel() == 0) return;

        NametagUtil.updateNametag(player);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.0f);

        // title screen message
        if (player.getLevel() >= PlayerLevelUtil.getMaxLevel()) {
            player.sendTitle(
                    ChatColor.GOLD + "Max Level!",
                    ChatColor.GOLD + className + " Level " + ChatColor.WHITE + classLevel, 10, 40, 10);
        } else {
            player.sendTitle(
                    ChatColor.GREEN + "Level Up!",
                    ChatColor.GREEN + className + " Level " + ChatColor.WHITE + classLevel, 10, 40, 10);
        }

        shootFirework(player.getWorld(), player.getEyeLocation());
    }

    private void shootFirework(World world, Location location) {
        Firework firework = world.spawn(location, Firework.class);
        FireworkMeta meta = firework.getFireworkMeta();
        meta.setPower(0);
        meta.addEffect(FireworkEffect.builder().with(FireworkEffect.Type.BALL).withColor(Color.GREEN).build());
        firework.setFireworkMeta(meta);
    }

    /**
     * This method is used to calculate how much HP the wearer has from items. So it subtracts the base hp of their
     * level. Everything uses GENERIC_MAX_HEALTH, so this is the simplest way I've done it for now.
     *
     * @author Skyfallin
     */
    public static int getHpAtLevel(Player player) {

        // grab the player's new info
        String className;
        try {
            className = RunicCoreAPI.getPlayerClass(player);
        } catch (Exception e) {
            return HealthUtils.getBaseHealth();
        }

        switch (className.toLowerCase()) {
            case "archer":
                return (PlayerLevelUtil.getArcherHpLv() * player.getLevel()) + HealthUtils.getBaseHealth();
            case "cleric":
                return (PlayerLevelUtil.getClericHpLv() * player.getLevel()) + HealthUtils.getBaseHealth();
            case "mage":
                return (PlayerLevelUtil.getMageHpLv() * player.getLevel()) + HealthUtils.getBaseHealth();
            case "rogue":
                return (PlayerLevelUtil.getRogueHpLv() * player.getLevel()) + HealthUtils.getBaseHealth();
            case "warrior":
                return (PlayerLevelUtil.getWarriorHpLv() * player.getLevel()) + HealthUtils.getBaseHealth();
        }

        return HealthUtils.getBaseHealth();
    }
}
