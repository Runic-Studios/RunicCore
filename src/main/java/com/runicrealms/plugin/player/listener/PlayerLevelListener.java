package com.runicrealms.plugin.player.listener;

import com.runicrealms.plugin.common.util.Pair;
import com.runicrealms.plugin.player.utilities.HealthUtils;
import com.runicrealms.plugin.player.utilities.PlayerLevelUtil;
import com.runicrealms.plugin.rdb.RunicDatabase;
import com.runicrealms.plugin.utilities.NametagHandler;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLevelChangeEvent;
import org.bukkit.inventory.meta.FireworkMeta;

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
            className = RunicDatabase.getAPI().getCharacterAPI().getPlayerClass(player);
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

    /**
     * Handles basic logic for when a player's level changes. Data-specific logic is handled in PlayerLevelUtil
     */
    @EventHandler
    public void onLevelUp(PlayerLevelChangeEvent event) {
        if (!RunicDatabase.getAPI().getCharacterAPI().getLoadedCharacters().contains(event.getPlayer().getUniqueId()))
            return; // ignore the change from PlayerJoinEvent

        Player player = event.getPlayer();
        if (player.getLevel() > PlayerLevelUtil.getMaxLevel()) return; // insurance

        // grab the player's new info
        String className = RunicDatabase.getAPI().getCharacterAPI().getPlayerClass(player);
        if (className.equals("")) return;
        int classLevel = player.getLevel();

        HealthUtils.setPlayerMaxHealth(player);
        player.setHealthScale(HealthUtils.getHeartAmount());
        int playerHealth = (int) player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
        player.setHealth(playerHealth);
        player.setFoodLevel(20);

        if (player.getLevel() == 0) return;

        int slot = RunicDatabase.getAPI().getCharacterAPI().getCharacterSlot(player.getUniqueId());
        NametagHandler.updateNametag(player, slot);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.0f);

        if (!PlayerJoinListener.LOADING_PLAYERS.contains(player.getUniqueId())) {
            Pair<String, String> levelTitleMessage = getLevelTitle(player, className, classLevel);
            player.sendTitle(levelTitleMessage.first, levelTitleMessage.second, 10, 50, 10);
            shootFirework(player.getWorld(), player.getEyeLocation());
        }
    }

    private void shootFirework(World world, Location location) {
        Firework firework = world.spawn(location, Firework.class);
        FireworkMeta meta = firework.getFireworkMeta();
        meta.setPower(0);
        meta.addEffect(FireworkEffect.builder().with(FireworkEffect.Type.BALL).withColor(Color.GREEN).build());
        firework.setFireworkMeta(meta);
    }
}
