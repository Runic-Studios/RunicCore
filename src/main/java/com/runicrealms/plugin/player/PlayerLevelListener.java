package com.runicrealms.plugin.player;

import com.runicrealms.plugin.classes.ClassGUI;
import com.runicrealms.plugin.classes.utilities.ClassUtil;
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

        // save player hp, restore hp.food
        int hpPerLevel = 0;
        switch (className.toLowerCase()) {
            case "archer":
                hpPerLevel = 1;
                break;
            case "cleric":
                hpPerLevel = 2;
                break;
            case "mage":
                hpPerLevel = 1;
                break;
            case "rogue":
                hpPerLevel = 1;
                break;
            case "warrior":
                hpPerLevel = 2;
                break;
        }
        HealthUtils.setPlayerHealth(pl, 50+(hpPerLevel*classLevel));
        HealthUtils.setHeartDisplay(pl);
        pl.setHealth(pl.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
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
        ItemStack rune = pl.getInventory().getItem(1);
        scaleArtifact(artifact, pl, className);

        // send a basic leveling message for all the levels that aren't milestones.
        // (10, 20, etc.)
        if (pl.getLevel() % 10 != 0) {
            sendLevelMessage(pl, hpPerLevel + "");
        }

        switch (pl.getLevel()) {
            case 1:
                break;
            case 10:
                sendUnlockMessage(pl, 10, className, classLevel);
                giveSpellpoint(pl);
                unlockSpell(rune, "primarySpell", pl, 1, className);
                Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(),
                        "lp user " + pl.getName() + " permission set core.skins." + className + ".21" + " true");
                break;
            case 20:
                sendUnlockMessage(pl, 20, className, classLevel);
                giveSpellpoint(pl);
                unlockSpell(rune, "secondarySpell", pl, 1, className);
                Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(),
                        "lp user " + pl.getName() + " permission set core.skins." + className + ".22" + " true");
                break;
            case 30:
                sendUnlockMessage(pl, 30, className, classLevel);
                giveSpellpoint(pl);
                unlockSpell(artifact, "secondarySpell", pl, 0, className);
                Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(),
                        "lp user " + pl.getName() + " permission set core.skins." + className + ".23" + " true");
                break;
            case 40:
                giveSpellpoint(pl);
                pl.sendMessage("\n");
                ChatUtils.sendCenteredMessage(pl, ChatColor.GREEN + "" + ChatColor.BOLD + "LEVEL UP!");
                ChatUtils.sendCenteredMessage(pl, ChatColor.WHITE + "" + ChatColor.BOLD + "+1 Spell Point");
                ChatUtils.sendCenteredMessage(pl, ChatColor.GRAY + "        You've unlocked a new artifact skin!");
                pl.sendMessage("\n");
                Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(),
                        "lp user " + pl.getName() + " permission set core.skins." + className + ".24" + " true");
                break;
            case 50:
                giveSpellpoint(pl);
                Bukkit.broadcastMessage(ChatColor.WHITE + "" + ChatColor.BOLD + pl.getName()
                         + ChatColor.GOLD + ChatColor.BOLD + " has reached level " + pl.getLevel() + " " + className + "!");
                pl.sendMessage("\n");
                ChatUtils.sendCenteredMessage(pl, ChatColor.GOLD + "" + ChatColor.BOLD + "MAX LEVEL REACHED!");
                ChatUtils.sendCenteredMessage(pl, ChatColor.WHITE + "" + ChatColor.BOLD + "+1 Spell Point");
                ChatUtils.sendCenteredMessage(pl, ChatColor.GRAY + " You've reached level " + pl.getLevel() + "!");
                ChatUtils.sendCenteredMessage(pl, ChatColor.GREEN + "  You can now access RAIDS!");
                pl.sendMessage("\n");
                ClassUtil.launchFirework(pl, className);
                break;
        }
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

    private void giveSpellpoint(Player pl) {
        int spellpoints = RunicCore.getInstance().getConfig().getInt(pl.getUniqueId() + ".info.spellpoints");
        RunicCore.getInstance().getConfig().set(pl.getUniqueId() + ".info.spellpoints", spellpoints+1);
        saveConfig(pl);
    }
    private void unlockSpell(ItemStack item, String slot, Player pl, int itemSlot, String className) {
        int durab = ((Damageable) item.getItemMeta()).getDamage();
        item = AttributeUtil.addSpell(item, slot, ChatColor.GREEN + "UNLOCKED");
        if (itemSlot == 0) {
            LoreGenerator.generateArtifactLore(item, item.getItemMeta().getDisplayName(), className, durab);
        } else {
            LoreGenerator.generateRuneLore(item);
        }
        pl.getInventory().setItem(itemSlot, item);
    }

    private void sendLevelMessage(Player pl, String hpPerLevel) {
        pl.sendMessage("\n");
        ChatUtils.sendCenteredMessage(pl, ChatColor.GREEN + "" + ChatColor.BOLD + "LEVEL UP!");
        ChatUtils.sendCenteredMessage(pl,
                ChatColor.RED + "" + ChatColor.BOLD + "+" + hpPerLevel + "❤ "
                        + ChatColor.DARK_AQUA + "+" + RunicCore.getManaManager().getManaPerLevel() + "✸");
        ChatUtils.sendCenteredMessage(pl, ChatColor.YELLOW + "        Your artifact speed increases!");
        pl.sendMessage("\n");
    }

    private void sendUnlockMessage(Player pl, int lvl, String className, int classLevel) {
        pl.sendTitle(
                ChatColor.GREEN + "Level Up!",
                ChatColor.GREEN + className + " Level " + ChatColor.WHITE + classLevel, 10, 40, 10);
        pl.sendMessage("\n");
        ChatUtils.sendCenteredMessage(pl, ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "NEW SPELL SLOT UNLOCKED!");
        ChatUtils.sendCenteredMessage(pl, ChatColor.WHITE + "" + ChatColor.BOLD + "+1 Spell Point");
        ChatUtils.sendCenteredMessage(pl, ChatColor.GRAY + "        You've unlocked a new artifact skin!");
        String item = "";
        if (lvl == 10 || lvl == 20) {
            item = "Rune";
        } else if (lvl == 30) {
            item = "Artifact";
        }
        ChatUtils.sendCenteredMessage(pl, ChatColor.WHITE + "      Click " + ChatColor.GREEN + "your " + item + " to add a spell!");
        pl.sendMessage("\n");
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
