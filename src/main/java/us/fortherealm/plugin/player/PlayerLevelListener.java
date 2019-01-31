package us.fortherealm.plugin.player;

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
import us.fortherealm.plugin.Main;
import us.fortherealm.plugin.attributes.AttributeUtil;
import us.fortherealm.plugin.classes.ClassGUI;
import us.fortherealm.plugin.classes.utilities.ClassUtil;
import us.fortherealm.plugin.item.LoreGenerator;
import us.fortherealm.plugin.player.utilities.HealthUtils;
import us.fortherealm.plugin.utilities.ChatUtils;

/**
 * Handles all the logic for when a player levels-up their primary class (archer, mage, etc.)
 * "Scales" the player's artifact, unlocks spell slots, and more.
 * @author Skyfallin_
 */
public class PlayerLevelListener implements Listener {

    private static final int hpPerLevel = 2;
    private static final int maxLevel = 50;

    @EventHandler
    public void onLevelUp(PlayerLevelChangeEvent e) {

        Player pl = e.getPlayer();

        if (pl.getLevel() > 50) return;

        // update player's level
        Main.getInstance().getConfig().set(pl.getUniqueId() + ".info.class.level", pl.getLevel());

        // grab the player's new info
        String className = Main.getInstance().getConfig().getString(pl.getUniqueId() + ".info.class.name");
        int classLevel = Main.getInstance().getConfig().getInt(pl.getUniqueId() + ".info.class.level");

        // save player hp, restore hp.food
        HealthUtils.setPlayerHealth(pl, 50+(hpPerLevel*classLevel));
        HealthUtils.setHeartDisplay(pl);
        pl.setHealth(pl.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
        Main.getInstance().getConfig().set(pl.getUniqueId() + ".info.currentHP", (int) pl.getHealth());
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
            sendLevelMessage(pl);
        }

        switch (pl.getLevel()) {
            case 1:
                break;
            case 10:
                sendUnlockMessage(pl, 10, className, classLevel);
                unlockSpell(rune, "primarySpell", pl, 1);
                break;
            case 20:
                sendUnlockMessage(pl, 20, className, classLevel);
                unlockSpell(rune, "secondarySpell", pl, 1);
                break;
            case 30:
                sendUnlockMessage(pl, 30, className, classLevel);
                unlockSpell(artifact, "secondarySpell", pl, 0);
                break;
            case 40:
                pl.sendMessage("\n");
                ChatUtils.sendCenteredMessage(pl, ChatColor.GREEN + "" + ChatColor.BOLD + "LEVEL UP!");
                ChatUtils.sendCenteredMessage(pl,
                        ChatColor.RED + "" + ChatColor.BOLD + "+" + hpPerLevel + "❤ "
                                + ChatColor.DARK_AQUA + "+" + Main.getManaManager().getManaPerLevel() + "✸");
                ChatUtils.sendCenteredMessage(pl, ChatColor.GREEN + "        You've unlocked a new artifact skin!");
                pl.sendMessage("\n");
                break;
            case 50:
                String storedName = Main.getInstance().getConfig().getString(pl.getUniqueId() + ".info.name");
                Bukkit.broadcastMessage(ChatColor.WHITE + "" + ChatColor.BOLD + storedName
                         + ChatColor.GOLD + ChatColor.BOLD + " has reached level " + pl.getLevel() + " " + className + "!");
                pl.sendMessage("\n");
                ChatUtils.sendCenteredMessage(pl, ChatColor.GOLD + "" + ChatColor.BOLD + "MAX LEVEL REACHED!");
                ChatUtils.sendCenteredMessage(pl, ChatColor.GRAY + "You've reached level " + pl.getLevel() + "!");
                ChatUtils.sendCenteredMessage(pl, ChatColor.GREEN + "You can now access RAIDS!");
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

        switch (className) {
            case "Archer":
                artifact = AttributeUtil.addCustomStat(artifact, "custom.bowSpeed",
                        ClassGUI.getArcherBaseBowSpeed()+(.015*pl.getLevel())); // 1.5 at lvl 50
                break;
            case "Cleric":
                artifact = AttributeUtil.overrideGenericDouble(artifact, "generic.attackSpeed",
                        ClassGUI.getClericBaseSpeed()+(.012*pl.getLevel())); // 1.2 at lvl 50
                break;
            case "Mage":
                artifact = AttributeUtil.overrideGenericDouble(artifact, "generic.attackSpeed",
                        ClassGUI.getMageBaseSpeed()+(.012*pl.getLevel())); // 1.2 at lvl 50
                break;
            case "Rogue":
                artifact = AttributeUtil.overrideGenericDouble(artifact, "generic.attackSpeed",
                        ClassGUI.getRogueBaseSpeed()+(.018*pl.getLevel())); // 1.8 at lvl 50
                break;
            case "Warrior":
                artifact = AttributeUtil.overrideGenericDouble(artifact, "generic.attackSpeed",
                        ClassGUI.getWarriorBaseSpeed()+(.015*pl.getLevel())); // 1.5 at lvl 50
                break;
        }

        //artifact = AttributeUtil.addCustomStat(artifact, "custom.minDamage", 9);
        //artifact = AttributeUtil.addCustomStat(artifact, "custom.maxDamage", 9);

        // update the lore
        LoreGenerator.generateArtifactLore(artifact, artifact.getItemMeta().getDisplayName(), className, durab);

        // set the player's artifact
        pl.getInventory().setItem(0, artifact);
    }

    private void unlockSpell(ItemStack item, String slot, Player pl, int itemSlot) {
        item = AttributeUtil.addSpell(item, slot, ChatColor.GREEN + "UNLOCKED");
        LoreGenerator.generateRuneLore(item);
        pl.getInventory().setItem(itemSlot, item);
    }

    private void sendLevelMessage(Player pl) {
        pl.sendMessage("\n");
        ChatUtils.sendCenteredMessage(pl, ChatColor.GREEN + "" + ChatColor.BOLD + "LEVEL UP!");
        ChatUtils.sendCenteredMessage(pl,
                ChatColor.RED + "" + ChatColor.BOLD + "+" + hpPerLevel + "❤ "
                        + ChatColor.DARK_AQUA + "+" + Main.getManaManager().getManaPerLevel() + "✸");
        ChatUtils.sendCenteredMessage(pl, ChatColor.YELLOW + "        Your artifact feels a little stronger!");
        pl.sendMessage("\n");
    }

    private void sendUnlockMessage(Player pl, int lvl, String className, int classLevel) {
        pl.sendTitle(
                ChatColor.GREEN + "Level Up!",
                ChatColor.GREEN + className + " Level " + ChatColor.WHITE + classLevel, 10, 40, 10);
        pl.sendMessage("\n");
        ChatUtils.sendCenteredMessage(pl, ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "NEW SPELL SLOT UNLOCKED!");
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

    private void saveConfig(Player pl) {
        Main.getInstance().saveConfig();
        Main.getInstance().reloadConfig();
        Main.getScoreboardHandler().updateSideInfo(pl);
    }
}
