package com.runicrealms.plugin.player.listener;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.api.Pair;
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

import java.util.UUID;

/**
 * Handles all the logic for when a player levels-up their primary class (archer, mage, etc.)
 *
 * @author Skyfallin_
 */
public class PlayerLevelListener implements Listener {

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
            className = RunicCore.getCharacterAPI().getPlayerClass(player);
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

    /**
     * Returns of pair containing title and subtitle to display to player on level up and join
     *
     * @param player     who joined or gained a level (levels are restored from data on join)
     * @param className  of the character's class
     * @param classLevel of the character
     * @return a pair that contains the title and subtitle
     */
    private Pair<String, String> getLevelTitle(Player player, String className, int classLevel) {
        if (PlayerJoinListener.LOADING_PLAYERS.contains(player.getUniqueId())) {
            return Pair.pair
                    (

                            ChatColor.DARK_GREEN + "Data Loaded!",

                            ChatColor.GREEN + "Welcome " + player.getName()
                    );
        }
        if (player.getLevel() >= PlayerLevelUtil.getMaxLevel()) {
            return Pair.pair
                    (

                            ChatColor.GOLD + "Max Level!",

                            ChatColor.GOLD + className + " Level " + ChatColor.WHITE + classLevel
                    );
        } else {
            return Pair.pair
                    (

                            ChatColor.GREEN + "Level Up!",

                            ChatColor.GREEN + className + " Level " + ChatColor.WHITE + classLevel
                    );
        }
    }

    @EventHandler
    public void onLevelUp(PlayerLevelChangeEvent event) {

        if (!RunicCore.getCharacterAPI().getLoadedCharacters().contains(event.getPlayer().getUniqueId()))
            return; // ignore the change from PlayerJoinEvent

        Player player = event.getPlayer();
        if (player.getLevel() > PlayerLevelUtil.getMaxLevel()) return; // insurance
        UUID uuid = player.getUniqueId();

        // update player's level in redis
        int slot = RunicCore.getCharacterAPI().getCharacterSlot(uuid);
        try (Jedis jedis = RunicCore.getRedisAPI().getNewJedisResource()) { // try-with-resources to close the resource for us
            jedis.set(RunicCore.getRedisAPI().getCharacterKey(uuid, slot) + ":" + CharacterField.CLASS_LEVEL.getField(), String.valueOf(player.getLevel()));
        }

        // grab the player's new info
        String className = RunicCore.getCharacterAPI().getPlayerClass(player);
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
        Pair<String, String> levelTitleMessage = getLevelTitle(player, className, classLevel);
        player.sendTitle(levelTitleMessage.first, levelTitleMessage.second, 10, 50, 10);

        if (!PlayerJoinListener.LOADING_PLAYERS.contains(player.getUniqueId()))
            shootFirework(player.getWorld(), player.getEyeLocation());
    }

    private void shootFirework(World world, Location location) {
        Firework firework = world.spawn(location, Firework.class);
        FireworkMeta meta = firework.getFireworkMeta();
        meta.setPower(0);
        meta.addEffect(FireworkEffect.builder().with(FireworkEffect.Type.BALL).withColor(Color.GREEN).build());
        firework.setFireworkMeta(meta);
    }
}
