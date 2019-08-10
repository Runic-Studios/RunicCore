package com.runicrealms.plugin.player.utilities;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.attributes.AttributeUtil;
import com.runicrealms.plugin.classes.utilities.ClassUtil;
import com.runicrealms.plugin.item.LoreGenerator;
import com.runicrealms.plugin.utilities.ChatUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;

import java.util.Objects;

public class PlayerLevelUtil {

    private static final int maxLevel = 60;

    public static void giveExperience(Player pl, int expGained) {

        String className = RunicCore.getInstance().getConfig().getString(pl.getUniqueId() + ".info.class.name");
        int currentLv = RunicCore.getInstance().getConfig().getInt(pl.getUniqueId() + ".info.class.level");
        int currentExp = RunicCore.getInstance().getConfig().getInt(pl.getUniqueId() + ".info.class.exp");

        if (currentLv >= maxLevel) return;

        currentExp = currentExp + expGained;
        RunicCore.getInstance().getConfig().set(pl.getUniqueId() + ".info.class.exp", currentExp);
        RunicCore.getInstance().saveConfig();
        RunicCore.getInstance().reloadConfig();
        int newTotalExp = RunicCore.getInstance().getConfig().getInt(pl.getUniqueId() + ".info.class.exp");

        if (calculateExpectedLv(newTotalExp) != currentLv) {

            // apply milestones for 10, 20, 30, etc.
            boolean needsMilestone = applyMileStone(pl, currentLv, className, calculateExpectedLv(newTotalExp));

            // send a basic leveling message for all the levels that aren't milestones.
            // (10, 20, etc.)
            if (!needsMilestone) {
                sendLevelMessage(pl);
            }

            pl.setLevel(calculateExpectedLv(newTotalExp));
            RunicCore.getInstance().getConfig().set(pl.getUniqueId() + ".info.class.level", calculateExpectedLv(newTotalExp));
            RunicCore.getInstance().saveConfig();
            RunicCore.getInstance().reloadConfig();
            currentLv = RunicCore.getInstance().getConfig().getInt(pl.getUniqueId() + ".info.class.level");

            RunicCore.getScoreboardHandler().updateSideInfo(pl);
        }

        int totalExpAtLevel = calculateTotalExp(currentLv);
        int totalExpToLevel = calculateTotalExp(currentLv+1);
        double proportion = (double) (currentExp - totalExpAtLevel) / (totalExpToLevel - totalExpAtLevel);
        if (currentLv == maxLevel) {
            pl.setExp(0);
        }
        if (proportion < 0) {
            proportion = 0.0f;
        }
        pl.setExp((float) proportion);
    }

    public static int calculateExpectedLv(int experience) {
        return (int) Math.cbrt((((5 * experience)+375) / 3)) - 5;
    }

    // 99750 at 50
    public static int calculateTotalExp(int currentLv) {
        int cubed = (int) Math.pow((currentLv+5), 3);
        return ((3*cubed)/5)-75;
    }

    private static boolean applyMileStone(Player pl, int oldLevel, String className, int classLevel) {
        ItemStack artifact = pl.getInventory().getItem(0);
        ItemStack rune = pl.getInventory().getItem(1);
        if (artifact == null || rune == null) return false;
        if (classLevel == 60) {
            giveSpellpoint(pl);
            Bukkit.broadcastMessage(ChatColor.WHITE + "" + ChatColor.BOLD + pl.getName()
                    + ChatColor.GOLD + ChatColor.BOLD + " has reached level " + classLevel + " " + className + "!");
            pl.sendMessage("\n");
            ChatUtils.sendCenteredMessage(pl, ChatColor.GOLD + "" + ChatColor.BOLD + "MAX LEVEL REACHED!");
            ChatUtils.sendCenteredMessage(pl, ChatColor.WHITE + "" + ChatColor.BOLD + "+1 Spell Point");
            ChatUtils.sendCenteredMessage(pl, ChatColor.GRAY + " You've reached level " + classLevel + "!");
            //ChatUtils.sendCenteredMessage(pl, ChatColor.GREEN + "  You can now access The Flaming Volanco!"); // WIP name
            pl.sendMessage("\n");
            ClassUtil.launchFirework(pl, className);
        } else if (classLevel == 50) {
            giveSpellpoint(pl);
            pl.sendMessage("\n");
            ChatUtils.sendCenteredMessage(pl, ChatColor.WHITE + "" + ChatColor.BOLD + "+1 Spell Point");
            ChatUtils.sendCenteredMessage(pl, ChatColor.GRAY + " You've reached level " + classLevel + "!");
            ChatUtils.sendCenteredMessage(pl, ChatColor.GREEN + "     You can now access " + ChatColor.DARK_RED + "The Frozen Fortress!");
            pl.sendMessage("\n");
            return true;
        } else if (classLevel >= 10  && oldLevel < 10) {
            sendUnlockMessage(pl, 10, className, classLevel);
            giveSpellpoint(pl);
            unlockSpell(rune, "primarySpell", pl, 1, className);
            Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(),
                    "lp user " + pl.getName() + " permission set core.skins." + className + ".21" + " true");
            return true;
        } else if (classLevel >= 20 && oldLevel < 20) {
            sendUnlockMessage(pl, 20, className, classLevel);
            giveSpellpoint(pl);
            unlockSpell(rune, "secondarySpell", pl, 1, className);
            Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(),
                    "lp user " + pl.getName() + " permission set core.skins." + className + ".22" + " true");
            return true;
        } else if (classLevel >= 30 && oldLevel < 30) {
            sendUnlockMessage(pl, 30, className, classLevel);
            giveSpellpoint(pl);
            unlockSpell(artifact, "secondarySpell", pl, 0, className);
            Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(),
                    "lp user " + pl.getName() + " permission set core.skins." + className + ".23" + " true");
            return true;
        } else if (classLevel >= 40 && oldLevel < 40) {
            giveSpellpoint(pl);
            pl.sendMessage("\n");
            ChatUtils.sendCenteredMessage(pl, ChatColor.GREEN + "" + ChatColor.BOLD + "LEVEL UP!");
            ChatUtils.sendCenteredMessage(pl, ChatColor.WHITE + "" + ChatColor.BOLD + "+1 Spell Point");
            ChatUtils.sendCenteredMessage(pl, ChatColor.GRAY + "        You've unlocked a new artifact skin!");
            pl.sendMessage("\n");
            Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(),
                    "lp user " + pl.getName() + " permission set core.skins." + className + ".24" + " true");
            return true;
        }
        return false;
    }

    private static void giveSpellpoint(Player pl) {
        int spellpoints = RunicCore.getInstance().getConfig().getInt(pl.getUniqueId() + ".info.spellpoints");
        RunicCore.getInstance().getConfig().set(pl.getUniqueId() + ".info.spellpoints", spellpoints+1);
//        RunicCore.getInstance().saveConfig();
//        RunicCore.getInstance().reloadConfig();
//        RunicCore.getScoreboardHandler().updateSideInfo(pl);
    }
    private static void unlockSpell(ItemStack item, String slot, Player pl, int itemSlot, String className) {
        int durab = ((Damageable) Objects.requireNonNull(item.getItemMeta())).getDamage();
        //item = AttributeUtil.addSpell(item, slot, ChatColor.GREEN + "UNLOCKED");
        if (itemSlot == 0) {
            LoreGenerator.generateArtifactLore(item, Objects.requireNonNull(item.getItemMeta()).getDisplayName(), className, durab);
        } else {
            LoreGenerator.generateRuneLore(item);
        }
        pl.getInventory().setItem(itemSlot, item);
    }

    private static void sendLevelMessage(Player pl) {

        String className = RunicCore.getInstance().getConfig().getString(pl.getUniqueId() + ".info.class.name");

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

        pl.sendMessage("\n");
        ChatUtils.sendCenteredMessage(pl, ChatColor.GREEN + "" + ChatColor.BOLD + "LEVEL UP!");
        ChatUtils.sendCenteredMessage(pl,
                ChatColor.RED + "" + ChatColor.BOLD + "+" + hpPerLevel + "❤ "
                        + ChatColor.DARK_AQUA + "+" + RunicCore.getManaManager().getManaPerLevel() + "✸");
        ChatUtils.sendCenteredMessage(pl, ChatColor.YELLOW + "        Your artifact speed increases!");
        pl.sendMessage("\n");
    }

    private static void sendUnlockMessage(Player pl, int lvl, String className, int classLevel) {
        pl.sendTitle(
                ChatColor.GREEN + "Level Up!",
                ChatColor.GREEN + className + " Level " + ChatColor.WHITE + classLevel, 10, 40, 10);
        pl.sendMessage("\n");
        ChatUtils.sendCenteredMessage(pl, ChatColor.WHITE + "" + ChatColor.BOLD + "+1 Spell Point");
        ChatUtils.sendCenteredMessage(pl, ChatColor.GRAY + "        You've unlocked a new artifact skin!");
        ChatUtils.sendCenteredMessage(pl, ChatColor.WHITE + "      Click " + ChatColor.GREEN + "your Artifact or Rune to add a spell!");
        pl.sendMessage("\n");
    }
}
