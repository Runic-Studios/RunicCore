package us.fortherealm.plugin.player;

import de.tr7zw.itemnbtapi.NBTEntity;
import de.tr7zw.itemnbtapi.NBTList;
import de.tr7zw.itemnbtapi.NBTListCompound;
import de.tr7zw.itemnbtapi.NBTType;
import org.bukkit.ChatColor;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLevelChangeEvent;
import us.fortherealm.plugin.Main;
import us.fortherealm.plugin.player.utilities.HealthUtils;

/**
 * Handles all the logic for when a player levels-up their primary class (archer, mage, etc.)
 * @author Skyfallin_
 */
public class PlayerLevelListener implements Listener {

    private static final int hpPerLevel = 2;

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

        pl.sendTitle(
                ChatColor.GREEN + "Level Up!",
                ChatColor.GREEN + className + " Level " + ChatColor.WHITE + classLevel, 10, 40, 10);
    }

    private void saveConfig(Player pl) {
        Main.getInstance().saveConfig();
        Main.getInstance().reloadConfig();
        Main.getScoreboardHandler().updateSideInfo(pl);
    }
}
