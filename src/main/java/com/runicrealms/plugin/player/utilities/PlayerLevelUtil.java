package com.runicrealms.plugin.player.utilities;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.model.CharacterField;
import com.runicrealms.plugin.utilities.ChatUtils;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import redis.clients.jedis.Jedis;

public class PlayerLevelUtil {

    private static final int MAX_LEVEL = 60;

    /*
    Class-specific level coefficients
     */
    private static final int ARCHER_HP_LV = 6;
    private static final int CLERIC_HP_LV = 10;
    private static final int MAGE_HP_LV = 4;
    private static final int ROGUE_HP_LV = 8;
    private static final int WARRIOR_HP_LV = 12;

    private static final double HEALTH_LEVEL_COEFFICIENT = 0.2;

    /**
     * Here is our exp curve!
     * At level 50, the player is ~ halfway to max, w/ 997,500
     * At level 60, the player needs 1,647,000 total exp
     *
     * @param currentLv the current level of the player
     * @return the experience they've earned at that level
     */
    public static int calculateTotalExp(int currentLv) {
        int cubed = (int) Math.pow((currentLv + 5), 3);
        return ((53 * cubed) / 5) - 1325;
    }

    /**
     * This method takes in an experience amount and returns the level which corresponds to that amount.
     * i.e., passing 997,500 will return level 50.
     *
     * @param experience the experience of the player
     */
    public static int calculateExpectedLv(int experience) {
        return (int) Math.cbrt((((5 * experience) + 6625.0) / 53)) - 5;
    }

    /**
     * Called when a player earns experience towards their combat class
     *
     * @param player    who earned the exp
     * @param expGained the amount of exp earned
     * @param jedis     the jedis resource
     */
    public static void giveExperience(Player player, int expGained, Jedis jedis) {
        int currentLevel = player.getLevel();
        if (currentLevel >= MAX_LEVEL) return;

        int slot = RunicCore.getCharacterAPI().getCharacterSlot(player.getUniqueId());
        String key = RunicCore.getRedisAPI().getCharacterKey(player.getUniqueId(), slot);
        int currentExp = Integer.parseInt(jedis.hmget(key, CharacterField.CLASS_EXP.getField()).get(0));
        currentExp = currentExp + expGained;
        jedis.set(key + ":" + CharacterField.CLASS_EXP.getField(), String.valueOf(currentExp));

        if (calculateExpectedLv(currentExp) != currentLevel) {
            player.sendMessage("\n");
            sendLevelMessage(player, calculateExpectedLv(currentExp));
            player.sendMessage("\n");
            player.setLevel(calculateExpectedLv(currentExp));
            currentLevel = calculateExpectedLv(currentExp);
            jedis.set(key + ":" + CharacterField.CLASS_LEVEL.getField(), String.valueOf(currentLevel));
        }

        int totalExpAtLevel = calculateTotalExp(currentLevel);
        int totalExpToLevel = calculateTotalExp(currentLevel + 1);
        double proportion = (double) (currentExp - totalExpAtLevel) / (totalExpToLevel - totalExpAtLevel);
        if (currentLevel == MAX_LEVEL) {
            player.setExp(0);
        }
        if (proportion < 0) {
            proportion = 0.0f;
        }
        player.setExp((float) proportion);
    }

    /**
     * When the player earns a level, send them a message!
     *
     * @param player  to receive message
     * @param classLv the level they reached
     */
    private static void sendLevelMessage(Player player, int classLv) {
        String className = RunicCore.getCharacterAPI().getPlayerClass(player);
        if (className == null) return;
        player.sendTitle(
                ChatColor.GREEN + "Level Up!",
                ChatColor.GREEN + className + " Level " + ChatColor.WHITE + classLv, 10, 40, 10);

        // save player hp, restore hp.food
        player.sendMessage("\n");
        if (classLv != MAX_LEVEL)
            ChatUtils.sendCenteredMessage(player, ChatColor.GREEN + "" + ChatColor.BOLD + "LEVEL UP!");
        else
            ChatUtils.sendCenteredMessage(player, ChatColor.GOLD + "" + ChatColor.BOLD + "MAX LEVEL REACHED!");
        int gainedHealth = calculateHealthAtLevel(classLv, className) - calculateHealthAtLevel(classLv - 1, className);
        ChatUtils.sendCenteredMessage(player,
                ChatColor.RED + "" + ChatColor.BOLD + "+" + gainedHealth + "❤ "
                        + ChatColor.DARK_AQUA + "+" + RunicCore.getRegenManager().getManaPerLv(player) + "✸");
        player.sendMessage("\n");
    }

    /**
     * Calculates the base health of the player based on class and current level
     *
     * @param currentLv their class level
     * @param className their class name
     * @return the HP they should have based on scaling
     */
    public static int calculateHealthAtLevel(int currentLv, String className) {
        double hpPerLevel = PlayerLevelUtil.determineHealthLvByClass(className);
        return (int) (HealthUtils.getBaseHealth() + (HEALTH_LEVEL_COEFFICIENT * Math.pow(currentLv, 2)) + (hpPerLevel * currentLv));
    }

    /**
     * May return either the scaling coefficient or linear hp-per-level of class based on boolean flag value
     *
     * @param className name of class
     * @return um can return either dis might be bad but to lazy to write two methods
     */
    public static double determineHealthLvByClass(String className) {
        switch (className.toLowerCase()) {
            case "archer":
                return PlayerLevelUtil.getArcherHpLv();
            case "cleric":
                return PlayerLevelUtil.getClericHpLv();
            case "mage":
                return PlayerLevelUtil.getMageHpLv();
            case "rogue":
                return PlayerLevelUtil.getRogueHpLv();
            case "warrior":
                return PlayerLevelUtil.getWarriorHpLv();
            default:
                throw new IllegalStateException("Unexpected value: " + className.toLowerCase());
        }
    }

    public static int getMaxLevel() {
        return MAX_LEVEL;
    }

    public static int getArcherHpLv() {
        return ARCHER_HP_LV;
    }

    public static int getClericHpLv() {
        return CLERIC_HP_LV;
    }

    public static int getMageHpLv() {
        return MAGE_HP_LV;
    }

    public static int getRogueHpLv() {
        return ROGUE_HP_LV;
    }

    public static int getWarriorHpLv() {
        return WARRIOR_HP_LV;
    }

    public static double getHealthLevelCoefficient() {
        return HEALTH_LEVEL_COEFFICIENT;
    }
}
