package com.runicrealms.plugin.player;

import com.runicrealms.plugin.classes.ClassGUI;
import com.runicrealms.plugin.classes.utilities.ClassUtil;
import com.runicrealms.plugin.item.GearScanner;
import com.runicrealms.plugin.item.LoreGenerator;
import com.runicrealms.plugin.player.utilities.HealthUtils;
import com.runicrealms.plugin.utilities.ChatUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLevelChangeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.attributes.AttributeUtil;

/**
 * Handles all the logic for when a player levels-up their primary class (archer, mage, etc.)
 * "Scales" the player's artifact, unlocks spell slots, and more.
 * @author Skyfallin_
 */
public class PlayerLevelListener implements Listener {

    private static final int maxLevel = 50;

    @EventHandler
    public void onLevelUp(PlayerLevelChangeEvent e) {

        Player pl = e.getPlayer();

        if (pl.getLevel() > 50) return;

        // update player's level
        RunicCore.getInstance().getConfig().set(pl.getUniqueId() + ".info.class.level", pl.getLevel());

        // grab the player's new info
        String className = RunicCore.getInstance().getConfig().getString(pl.getUniqueId() + ".info.class.name");
        if (className == null) return;
        int classLevel = RunicCore.getInstance().getConfig().getInt(pl.getUniqueId() + ".info.class.level");

        HealthUtils.setPlayerMaxHealth(pl);
        HealthUtils.setHeartDisplay(pl);
        int playerHealth = (int) pl.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
        pl.setHealth(playerHealth);
        RunicCore.getInstance().getConfig().set(pl.getUniqueId() + ".info.currentHP", (int) pl.getHealth());
        pl.setFoodLevel(20);

        saveConfig(pl);

        if (pl.getLevel() == 0) return;

        pl.getWorld().playSound(pl.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.0f);

        // title screen message
        if (pl.getLevel() == maxLevel) {
            pl.sendTitle(
                    ChatColor.GOLD + "Max Level!",
                    ChatColor.GOLD + className + " Level " + ChatColor.WHITE + classLevel, 10, 40, 10);
        } else {
            pl.sendTitle(
                    ChatColor.GREEN + "Level Up!",
                    ChatColor.GREEN + className + " Level " + ChatColor.WHITE + classLevel, 10, 40, 10);
        }

        // scale artifact
        ItemStack artifact = pl.getInventory().getItem(0);
        if (artifact == null) return;
        scaleArtifact(artifact, pl, className);
    }

    /**
     * Artifacts auto-scale their speed and damage based on preset values.
     * Max attack speed is 24, so to get 1.0 attack speed, enter -23.
     * (1.0 is ~1 swing per second, give or take)
     */
    private void scaleArtifact(ItemStack artifact, Player pl, String className) {

        int durab = ((Damageable) artifact.getItemMeta()).getDamage();

        double newSpeed;
        //double newDamage;
        switch (className) {
            case "Archer":
                newSpeed = ClassGUI.getArcherBaseBowSpeed() + (24+ClassGUI.getArcherBaseBowSpeed()) / 50 * pl.getLevel();
                artifact = AttributeUtil.addCustomStat(artifact, "custom.bowSpeed", newSpeed);
                break;
            case "Cleric":
                newSpeed = ClassGUI.getClericBaseSpeed() + (24+ClassGUI.getClericBaseSpeed()) / 50 * pl.getLevel();
                artifact = AttributeUtil.overrideGenericDouble(artifact, "generic.attackSpeed", newSpeed);
                break;
            case "Mage":
                newSpeed = ClassGUI.getMageBaseSpeed() + (24+ClassGUI.getMageBaseSpeed()) / 50 * pl.getLevel();
                artifact = AttributeUtil.overrideGenericDouble(artifact, "generic.attackSpeed", newSpeed);
                break;
            case "Rogue":
                newSpeed = ClassGUI.getRogueBaseSpeed() + (24+ClassGUI.getRogueBaseSpeed()) / 50 * pl.getLevel();
                artifact = AttributeUtil.overrideGenericDouble(artifact, "generic.attackSpeed", newSpeed);
                break;
            case "Warrior":
                newSpeed = ClassGUI.getWarriorBaseSpeed() + (24+ClassGUI.getWarriorBaseSpeed()) / 50 * pl.getLevel();
                artifact = AttributeUtil.overrideGenericDouble(artifact, "generic.attackSpeed", newSpeed);
                break;
        }

        // update the lore
        LoreGenerator.generateArtifactLore(artifact, artifact.getItemMeta().getDisplayName(), className, durab);

        // set the player's artifact
        pl.getInventory().setItem(0, artifact);
    }

    /**
     * This method is used to calculate how much HP the wearer has from items. So it subtracts the base hp of their
     * level. Everything uses GENERIC_MAX_HEALTH, so this is the simplest way I've done it for now.
     * @author Skyfallin
     */
    public static int getHpAtLevel(Player pl) {

        // grab the player's new info
        String className = RunicCore.getInstance().getConfig().getString(pl.getUniqueId() + ".info.class.name");
        if (className == null) return 50;

        switch (className.toLowerCase()) {
            case "archer":
                return (pl.getLevel()) + 50;
            case "cleric":
                return (2*pl.getLevel()) + 50;
            case "mage":
                return (pl.getLevel()) + 50;
            case "rogue":
                return (pl.getLevel()) + 50;
            case "warrior":
                return (2*pl.getLevel()) + 50;
        }

        return 50;
    }

    private void saveConfig(Player pl) {
        RunicCore.getInstance().saveConfig();
        RunicCore.getInstance().reloadConfig();
        RunicCore.getScoreboardHandler().updateSideInfo(pl);
    }
}
